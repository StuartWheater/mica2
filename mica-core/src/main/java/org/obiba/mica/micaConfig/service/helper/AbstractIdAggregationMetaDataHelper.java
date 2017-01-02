/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.micaConfig.service.helper;

import java.util.Map;
import java.util.Optional;

import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.core.domain.taxonomy.Term;
import org.obiba.opal.core.domain.taxonomy.Vocabulary;

/**
 * Helper class used to create Terms for ID vocabularies. These vocabularies correspond to an indexed entity ID values.
 */
public abstract class AbstractIdAggregationMetaDataHelper {

  public void applyIdTerms(Taxonomy taxonomy, String idVocabularyName) {
    Optional<Vocabulary> idVocabulary = taxonomy.getVocabularies().stream()
      .filter(v -> v.getName().equals(idVocabularyName) && !v.hasTerms()).findFirst();

    if (idVocabulary.isPresent()) {
      applyTerms(idVocabulary.get());
    }
  }

  protected abstract Map<String, AggregationMetaDataProvider.LocalizedMetaData> getIdAggregationMap();

  private void applyTerms(Vocabulary idVocabulary) {
    Map<String, AggregationMetaDataProvider.LocalizedMetaData> map = getIdAggregationMap();
    if(idVocabulary.getTerms() != null) idVocabulary.getTerms().clear();
    map.keySet().stream().sorted().forEach(key ->
      idVocabulary.addTerm(createTermFromMetaData(key, map.get(key)))
    );
  }

  private Term createTermFromMetaData(String id, AggregationMetaDataProvider.LocalizedMetaData metaData) {
    Term term = new Term(id);
    term.setTitle(metaData.getTitle());
    term.setDescription(metaData.getDescription());
    if(metaData.getStart() != null) term.addAttribute("start", metaData.getStart());
    if(metaData.getEnd() != null) term.addAttribute("end", metaData.getEnd());

    return term;
  }

}
