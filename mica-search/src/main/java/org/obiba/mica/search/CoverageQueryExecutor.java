/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.search;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.mica.core.domain.AttributeKey;
import org.obiba.mica.micaConfig.service.OpalService;
import org.obiba.mica.search.queries.JoinQueryWrapper;
import org.obiba.mica.search.queries.QueryWrapper;
import org.obiba.mica.search.queries.protobuf.JoinQueryDtoWrapper;
import org.obiba.mica.web.model.Dtos;
import org.obiba.mica.web.model.MicaSearch;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import sun.util.locale.LanguageTag;

@Component
@Scope("request")
public class CoverageQueryExecutor {

  @Inject
  private OpalService opalService;

  @Inject
  private JoinQueryExecutor joinQueryExecutor;

  @Inject
  private Dtos dtos;

  private JoinQueryWrapper joinQueryWrapper;

  private Map<String, Map<String, List<String>>> restrictedTermsMap;

  public MicaSearch.TaxonomiesCoverageDto coverageQuery(MicaSearch.JoinQueryDto joinQuery, boolean strict)
    throws IOException {
    return coverageQuery(new JoinQueryDtoWrapper(joinQuery == null ? getDefaultJoinQueryDto() : joinQuery), strict);
  }

  public MicaSearch.TaxonomiesCoverageDto coverageQuery(JoinQueryWrapper joinQuery) throws IOException {
    // always strict
    return coverageQuery(joinQuery, true);
  }

  public MicaSearch.TaxonomiesCoverageDto coverageQuery(JoinQueryWrapper joinQuery, boolean strict) throws IOException {
    joinQueryWrapper = joinQuery;

    // Strict coverage means that coverage result is restricted to the terms specified in the variable query.
    // If no variable query is specified, nothing is returned if strictness is applied, otherwise coverage of all terms is returned.
    if(strict) {
      restrictedTermsMap = joinQueryWrapper.getVariableQueryWrapper().getTaxonomyTermsMap();

      // If do not need all the facets then we can restrict the variable aggregations to the ones matching these names.
      if(!joinQueryWrapper.isWithFacets()) {
        restrictedTermsMap.forEach((taxo, vocMap) -> vocMap.keySet().forEach(
          voc -> joinQueryWrapper.getVariableQueryWrapper().getAggregations()
            .add("attributes." + AttributeKey.getMapKey(voc, taxo) + "." +
              LanguageTag.UNDETERMINED)));
      }
    }

    // We need the aggregations internally for building the coverage result,
    // but we may not need them in the final result
    JoinQueryWrapper joinQueryDtoWrapperWithFacets = new JoinQueryWrapperWithFacets(joinQueryWrapper);

    MicaSearch.JoinQueryResultDto result = joinQueryExecutor.queryCoverage(joinQueryDtoWrapperWithFacets);

    List<MicaSearch.AggregationResultDto> aggregations = ungroupAggregations(
      result.getVariableResultDto().getAggsList());

    MicaSearch.TaxonomiesCoverageDto.Builder builder = MicaSearch.TaxonomiesCoverageDto.newBuilder()//
      .setTotalCount(result.getVariableResultDto().getTotalCount()) //
      .setTotalHits(result.getVariableResultDto().getTotalHits()) //
      .addAllTaxonomies(getCoverages(aggregations));

    // Do not append the aggregations if no facets is requested
    if(joinQueryWrapper.isWithFacets()) builder.setQueryResult(result);

    return builder.build();
  }

  private MicaSearch.JoinQueryDto getDefaultJoinQueryDto() {
    MicaSearch.JoinQueryDto.Builder builder = MicaSearch.JoinQueryDto.newBuilder() //
      .setWithFacets(false);
    return builder.build();
  }

  /**
   * Extract hits from aggregations and merge them into the taxonomies descriptions.
   *
   * @param taxonomyNames
   * @param aggregations
   * @return
   */
  private Iterable<MicaSearch.TaxonomyCoverageDto> getCoverages(List<MicaSearch.AggregationResultDto> aggregations) {
    Map<String, Map<String, MicaSearch.TermsAggregationResultDto>> aggTermsTitlesMap = aggregations.stream().collect(
      Collectors.toMap(MicaSearch.AggregationResultDto::getAggregation,
        a -> a.getExtension(MicaSearch.TermsAggregationResultDto.terms).stream()
          .collect(Collectors.toMap(MicaSearch.TermsAggregationResultDto::getKey, t -> t))));

    Map<String, List<BucketResult>> bucketResultsByTaxonomy = extractBucketResults(aggregations).stream()
      .collect(Collectors.groupingBy(BucketResult::getTaxonomy));

    Map<String, Map<String, Integer>> aggsMap = Maps.newHashMap();
    aggregations.forEach(agg -> {
      String name = agg.getAggregation();
      List<MicaSearch.TermsAggregationResultDto> results = agg.getExtension(MicaSearch.TermsAggregationResultDto.terms);
      if(results != null && !results.isEmpty() && isAttributeField(name)) {
        String key = name.replaceAll("^attributes-", "").replaceAll("-und$", "");
        if(!aggsMap.containsKey(key)) aggsMap.put(key, Maps.newHashMap());
        results.forEach(res -> aggsMap.get(key).put(res.getKey(), res.getCount()));
      }
    });

    List<MicaSearch.TaxonomyCoverageDto> coverages = Lists.newArrayList();
    getTaxonomies().stream().filter(taxonomy -> applyFilter(taxonomy)).forEach(
      taxonomy -> addTaxonomyCoverage(coverages, taxonomy, aggsMap, bucketResultsByTaxonomy.get(taxonomy.getName()),
        aggTermsTitlesMap));

    return coverages;
  }

  private boolean applyFilter(Taxonomy taxonomy) {
    return restrictedTermsMap == null || restrictedTermsMap.containsKey(taxonomy.getName());
  }

  private boolean isAttributeField(String name) {
    return name.startsWith("attributes-") && name.endsWith("-und");
  }

  private List<MicaSearch.AggregationResultDto> ungroupAggregations(List<MicaSearch.AggregationResultDto> aggsList) {
    List<MicaSearch.AggregationResultDto> newList = Lists.newArrayList();
    aggsList.stream().forEach(agg -> {
      if(agg.getChildrenCount() > 0) {
        newList.addAll(agg.getChildrenList());
      } else {
        newList.add(agg);
      }
    });

    return newList;
  }

  /**
   * Extract the hits per taxonomy term and bucket.
   *
   * @param aggregations
   * @return
   */
  @NotNull
  private Collection<BucketResult> extractBucketResults(List<MicaSearch.AggregationResultDto> aggregations) {
    if(joinQueryWrapper == null || joinQueryWrapper.getVariableQueryWrapper() == null ||
      joinQueryWrapper.getVariableQueryWrapper().getAggregationBuckets().isEmpty()) return Collections.emptyList();

    List<String> aggsBy = joinQueryWrapper.getVariableQueryWrapper().getAggregationBuckets();

    List<BucketResult> termResults = Lists.newArrayList();

    aggregations.stream().filter(agg -> aggsBy.contains(agg.getAggregation())).forEach(bucket -> {
      String bucketField = bucket.getAggregation(); // studyIds for instance
      bucket.getExtension(MicaSearch.TermsAggregationResultDto.terms).stream() //
        .filter(agg -> agg.getAggsCount() > 0) //
        .forEach(agg -> agg.getAggsList().stream() //
          .filter(t -> !t.getExtension(MicaSearch.TermsAggregationResultDto.terms).isEmpty()) //
          .forEach(t -> termResults.addAll(BucketResult.list(bucketField, agg.getKey(), t))));
    });

    Collections.sort(termResults);

    return termResults;
  }

  /**
   * For a {@link Taxonomy}, report the number of hits and optionally the
   * number of hits for each bucket.
   *
   * @param coverages
   * @param taxonomy
   * @param aggsMap
   * @param bucketResults
   */
  private void addTaxonomyCoverage(List<MicaSearch.TaxonomyCoverageDto> coverages, Taxonomy taxonomy,
    Map<String, Map<String, Integer>> aggsMap, @Nullable List<BucketResult> bucketResults,
    Map<String, Map<String, MicaSearch.TermsAggregationResultDto>> aggTermsTitlesMap) {
    if(taxonomy.hasVocabularies()) {
      MicaSearch.TaxonomyCoverageDto.Builder taxoBuilder = MicaSearch.TaxonomyCoverageDto.newBuilder();
      taxoBuilder.setTaxonomy(dtos.asDto(taxonomy, getLocale()));
      List<Integer> hits = Lists.newArrayList();
      String namespace = taxonomy.getName().equals("Default") ? null : taxonomy.getName();
      Map<String, List<BucketResult>> bucketResultsByVocabulary = bucketResults == null
        ? Maps.newHashMap()
        : bucketResults.stream().collect(Collectors.groupingBy(BucketResult::getVocabulary));

      taxonomy.getVocabularies().stream().filter(vocabulary -> applyFilter(taxonomy, vocabulary)).forEach(
        vocabulary -> hits.add(addVocabularyCoverage(taxoBuilder, taxonomy, vocabulary,
          aggsMap.get(AttributeKey.getMapKey(vocabulary.getName(), namespace)),
          bucketResults == null ? null : bucketResultsByVocabulary.get(vocabulary.getName()), aggTermsTitlesMap)));

      taxoBuilder.setHits(hits.isEmpty() ? 0 : hits.stream().mapToInt(x -> x).sum());
      // compute the sum of the hits for all vocabularies per bucket
      if(bucketResults != null) {
        Map<String, List<BucketResult>> bucketResultsByBucketField = bucketResults.stream()
          .collect(Collectors.groupingBy(BucketResult::getBucketField));

        bucketResultsByBucketField.keySet().forEach(field -> {
          Map<String, List<BucketResult>> bucketResultsByBucketValue = bucketResultsByBucketField.get(field).stream()
            .collect(Collectors.groupingBy(BucketResult::getBucketValue));

          bucketResultsByBucketValue.keySet().stream().sorted().forEach(value -> {
            List<BucketResult> buckets = bucketResultsByBucketValue.get(value);
            int sumOfHits = buckets.stream().mapToInt(BucketResult::getHits).sum();
            if(sumOfHits > 0) {
              taxoBuilder.addBuckets(
                getBucketCoverageDtoBuilder(field, value, sumOfHits, aggTermsTitlesMap.get(field).get(value)));
            }
          });
        });
      }

      if(!taxoBuilder.getVocabulariesList().isEmpty()) {
        coverages.add(taxoBuilder.build());
      }
    }
  }

  private boolean applyFilter(Taxonomy taxonomy, Vocabulary vocabulary) {
    return restrictedTermsMap == null || restrictedTermsMap.get(taxonomy.getName()).containsKey(vocabulary.getName());
  }

  /**
   * For a taxonomy {@link Vocabulary}, report the number of hits and optionally the
   * number of hits for each bucket.
   *
   * @param taxoBuilder
   * @param vocabulary
   * @param hits
   * @param bucketResults
   * @return
   */
  private int addVocabularyCoverage(MicaSearch.TaxonomyCoverageDto.Builder taxoBuilder, Taxonomy taxonomy,
    Vocabulary vocabulary, Map<String, Integer> hits, @Nullable List<BucketResult> bucketResults,
    Map<String, Map<String, MicaSearch.TermsAggregationResultDto>> aggTermsTitlesMap) {
    int sumOfHits = 0;
    if(vocabulary.hasTerms()) {
      Map<String, List<BucketResult>> bucketResultsByTerm = bucketResults == null
        ? Maps.newHashMap()
        : bucketResults.stream().collect(Collectors.groupingBy(BucketResult::getTerm));

      MicaSearch.VocabularyCoverageDto.Builder vocBuilder = MicaSearch.VocabularyCoverageDto.newBuilder();
      vocBuilder.setVocabulary(dtos.asDto(vocabulary, getLocale()));
      vocabulary.getTerms().stream().filter(term -> applyFilter(taxonomy, vocabulary, term)).forEach(
        term -> addTermCoverage(vocBuilder, term, hits, bucketResultsByTerm.get(term.getName()), aggTermsTitlesMap));
      // only one term can be applied at a time, then the sum of the term hits is the number of variables
      // that cover this vocabulary
      sumOfHits = hits == null ? 0 : hits.values().stream().mapToInt(x -> x).sum();
      vocBuilder.setHits(sumOfHits);

      if(!vocabulary.isRepeatable()) {
        vocBuilder.setCount(sumOfHits);
      }

      // compute the sum of the hits for all terms per bucket
      if(bucketResults != null) {
        Map<String, List<BucketResult>> bucketResultsByBucketField = bucketResults.stream()
          .collect(Collectors.groupingBy(BucketResult::getBucketField));

        bucketResultsByBucketField.keySet().forEach(field -> {
          Map<String, List<BucketResult>> bucketResultsByBucketValue = bucketResultsByBucketField.get(field).stream()
            .collect(Collectors.groupingBy(BucketResult::getBucketValue));

          bucketResultsByBucketValue.keySet().stream().sorted().forEach(value -> {
            List<BucketResult> buckets = bucketResultsByBucketValue.get(value);
            int sumOfBucketHits = buckets.stream().mapToInt(BucketResult::getHits).sum();
            if(sumOfBucketHits > 0) {
              MicaSearch.BucketCoverageDto.Builder builder = getBucketCoverageDtoBuilder(field, value, sumOfBucketHits,
                aggTermsTitlesMap.get(field).get(value));

              if(!vocabulary.isRepeatable()) builder.setCount(builder.getHits());

              vocBuilder.addBuckets(builder);
            }
          });
        });
      }
      if(!vocBuilder.getTermsList().isEmpty()) {
        taxoBuilder.addVocabularies(vocBuilder);
      } else {
        sumOfHits = 0;
      }
    }

    return sumOfHits;
  }

  private boolean applyFilter(Taxonomy taxonomy, Vocabulary vocabulary, Term term) {
    return restrictedTermsMap == null ||
      restrictedTermsMap.get(taxonomy.getName()).get(vocabulary.getName()).contains(term.getName());
  }

  /**
   * For a taxonomy {@link Term}, report the number of hits and optionally
   * the number of hits for each bucket.
   *
   * @param vocBuilder
   * @param term
   * @param hits
   * @param bucketResults
   */
  private void addTermCoverage(MicaSearch.VocabularyCoverageDto.Builder vocBuilder, Term term,
    Map<String, Integer> hits, @Nullable List<BucketResult> bucketResults,
    Map<String, Map<String, MicaSearch.TermsAggregationResultDto>> aggTermsTitlesMap) {
    MicaSearch.TermCoverageDto.Builder termBuilder = MicaSearch.TermCoverageDto.newBuilder();
    termBuilder.setTerm(dtos.asDto(term, getLocale()));
    termBuilder.setHits(0);
    // add the hits per buckets
    if(bucketResults != null) {
      termBuilder.addAllBuckets(bucketResults.stream().filter(b -> b.getHits() > 0).map(
        b -> getBucketCoverageDtoBuilder(b.getBucketField(), b.getBucketValue(), b.getHits(),
          aggTermsTitlesMap.get(b.getBucketField()).get(b.getBucketValue())).build()).collect(Collectors.toList()));
    }

    if(hits != null && hits.containsKey(term.getName())) termBuilder.setHits(hits.get(term.getName()));

    vocBuilder.addTerms(termBuilder);
  }

  @NotNull
  private MicaSearch.BucketCoverageDto.Builder getBucketCoverageDtoBuilder(String field, String value, int hits,
    @NotNull MicaSearch.TermsAggregationResultDto term) {
    MicaSearch.BucketCoverageDto.Builder builder = MicaSearch.BucketCoverageDto.newBuilder().setField(field)
      .setValue(value).setHits(hits);

    if(term.hasTitle()) builder.setTitle(term.getTitle());
    if(term.hasDescription()) builder.setDescription(term.getDescription());
    if(term.hasStart()) builder.setStart(term.getStart());
    if(term.hasEnd()) builder.setEnd(term.getEnd());

    return builder;
  }

  @NotNull
  private List<Taxonomy> getTaxonomies() {
    List<Taxonomy> taxonomies = null;
    try {
      taxonomies = opalService.getTaxonomies();
    } catch(Exception e) {
      // ignore
    }
    return taxonomies == null ? Collections.emptyList() : taxonomies;
  }

  @Nullable
  private String getLocale() {
    return joinQueryWrapper.getLocale();
  }

  /**
   * The number of variable hits per bucket and taxonomy term.
   */
  private static class BucketResult implements Comparable<BucketResult> {

    private final String bucketField;

    private final String bucketValue;

    private final String taxonomy;

    private final String vocabulary;

    private final String term;

    private final int hits;

    private BucketResult(@Nullable String bucketField, @Nullable String bucketValue, String taxonomy, String vocabulary,
      String term, int hits) {
      this.bucketField = bucketField;
      this.bucketValue = bucketValue;
      this.taxonomy = taxonomy;
      this.vocabulary = vocabulary;
      this.term = term;
      this.hits = hits;
    }

    private static Collection<BucketResult> list(@Nullable String bucketField, @Nullable String bucketValue,
      MicaSearch.AggregationResultDto agg) {
      String key = agg.getAggregation().replaceAll("^attributes-", "").replaceAll("-und$", "");
      AttributeKey attrKey = AttributeKey.from(key);
      String taxonomy = attrKey.hasNamespace(null) ? "Default" : attrKey.getNamespace();
      String vocabulary = attrKey.getName();
      return agg.getExtension(MicaSearch.TermsAggregationResultDto.terms).stream()
        .map(t -> new BucketResult(bucketField, bucketValue, taxonomy, vocabulary, t.getKey(), t.getCount()))
        .collect(Collectors.toList());
    }

    public String getBucketField() {
      return bucketField;
    }

    public String getBucketValue() {
      return bucketValue;
    }

    public String getTaxonomy() {
      return taxonomy;
    }

    public String getVocabulary() {
      return vocabulary;
    }

    public String getTerm() {
      return term;
    }

    public int getHits() {
      return hits;
    }

    public String toString() {
      return "[" + bucketField + "," + bucketValue + "," + taxonomy + "," + vocabulary + "," + term + "]=" + hits;
    }

    @Override
    public int hashCode() {
      return getBucketField().hashCode() + getBucketValue().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof BucketResult && obj.toString().equals(toString());
    }

    @Override
    public int compareTo(BucketResult o) {
      return getBucketValue().compareTo(o.getBucketValue());
    }
  }

  private static class JoinQueryWrapperWithFacets implements JoinQueryWrapper {

    private final JoinQueryWrapper joinQueryWrapper;

    JoinQueryWrapperWithFacets(JoinQueryWrapper joinQueryWrapper) {
      this.joinQueryWrapper = joinQueryWrapper;
    }

    @Override
    public boolean isWithFacets() {
      return true;
    }

    @Override
    public String getLocale() {
      return joinQueryWrapper.getLocale();
    }

    @Override
    public QueryWrapper getVariableQueryWrapper() {
      return joinQueryWrapper.getVariableQueryWrapper();
    }

    @Override
    public QueryWrapper getDatasetQueryWrapper() {
      return joinQueryWrapper.getDatasetQueryWrapper();
    }

    @Override
    public QueryWrapper getStudyQueryWrapper() {
      return joinQueryWrapper.getStudyQueryWrapper();
    }

    @Override
    public QueryWrapper getNetworkQueryWrapper() {
      return joinQueryWrapper.getNetworkQueryWrapper();
    }
  }
}
