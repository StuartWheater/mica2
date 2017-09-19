/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.study.search.rest;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.obiba.mica.search.JoinQueryExecutor;
import org.obiba.mica.search.csvexport.GenericReportGenerator;
import org.obiba.mica.search.csvexport.SpecificStudyReportGenerator;
import org.obiba.mica.search.queries.protobuf.JoinQueryDtoWrapper;
import org.obiba.mica.search.queries.protobuf.QueryDtoHelper;
import org.obiba.mica.search.queries.rql.RQLQueryBuilder;
import org.obiba.mica.search.queries.rql.RQLQueryFactory;
import org.obiba.mica.study.search.StudyIndexer;
import org.obiba.mica.web.model.MicaSearch;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import static org.obiba.mica.web.model.MicaSearch.JoinQueryResultDto;

@Component
@Path("/studies")
@RequiresAuthentication
@Scope("request")
public class PublishedStudiesSearchResource {

  @Inject
  private JoinQueryExecutor joinQueryExecutor;

  @Inject
  private RQLQueryFactory rqlQueryFactory;

  @Inject
  private GenericReportGenerator genericReportGenerator;

  @Inject
  private SpecificStudyReportGenerator specificStudyReportGenerator;

  @GET
  @Timed
  public JoinQueryResultDto rqlList(@QueryParam("from") @DefaultValue("0") int from,
    @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("sort") @DefaultValue("name") String sort,
    @QueryParam("order") @DefaultValue("asc") String order, @QueryParam("locale") @DefaultValue("en") String locale)
    throws IOException {

    String rql = RQLQueryBuilder.newInstance().target(
      RQLQueryBuilder.TargetQueryBuilder.studyInstance().exists("id").limit(from, limit).sort(sort, order).build())
      .locale(locale).buildArgsAsString();

    return joinQueryExecutor.query(JoinQueryExecutor.QueryType.STUDY, rqlQueryFactory.makeJoinQuery(rql));
  }

  @GET
  @Path("/_search")
  @Timed
  public JoinQueryResultDto list(@QueryParam("from") @DefaultValue("0") int from,
    @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("sort") String sort,
    @QueryParam("order") @DefaultValue("asc") String order, @QueryParam("query") String query,
    @QueryParam("locale") @DefaultValue("en") String locale) throws IOException {

    JoinQueryResultDto.Builder builder = joinQueryExecutor.listQuery(JoinQueryExecutor.QueryType.STUDY, QueryDtoHelper
      .createQueryDto(from, limit, Strings.isNullOrEmpty(sort) ? StudyIndexer.DEFAULT_SORT_FIELD + "." + locale : sort,
        order, query, locale, Stream.of(StudyIndexer.LOCALIZED_ANALYZED_FIELDS)), locale).toBuilder();
    builder.clearDatasetResultDto().clearNetworkResultDto().clearVariableResultDto();
    builder.setStudyResultDto(builder.getStudyResultDto().toBuilder().clearAggs());
    return builder.build();
  }

  @POST
  @Path("/_search")
  @Timed
  public JoinQueryResultDto query(MicaSearch.JoinQueryDto joinQueryDto) throws IOException {
    return joinQueryExecutor.query(JoinQueryExecutor.QueryType.STUDY, new JoinQueryDtoWrapper(joinQueryDto));
  }

  @GET
  @Path("/_rql")
  @Timed
  public JoinQueryResultDto rqlQuery(@QueryParam("query") String query) throws IOException {
    String queryStr = query;
    if (Strings.isNullOrEmpty(queryStr)) queryStr = "study(exists(Mica_study.id))";
    return joinQueryExecutor.query(JoinQueryExecutor.QueryType.STUDY, rqlQueryFactory.makeJoinQuery(queryStr));
  }

  @GET
  @Path("/_rql_csv")
  @Timed
  public Response rqlQueryAsCsv(@QueryParam("query") String query, @QueryParam("columnsToHide") List<String> columnsToHide) throws IOException {
    StreamingOutput stream = os -> genericReportGenerator.generateCsv(JoinQueryExecutor.QueryType.STUDY, query, columnsToHide, os);
    return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"SearchStudies.csv\"").build();
  }

  @GET
  @Path("/_report")
  @Timed
  public Response report(@QueryParam("query") String query) throws IOException {
    StreamingOutput stream = os -> specificStudyReportGenerator.report(query, os);
    return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"Studies.csv\"").build();
  }

  @GET
  @Path("/_report_by_network")
  @Timed
  public Response report(@QueryParam("networkId") String networkId, @QueryParam("locale") @DefaultValue("en") String locale) throws IOException {
    StreamingOutput stream = os -> specificStudyReportGenerator.report(networkId, locale, os);
    return Response.ok(stream).header("Content-Disposition", "attachment; filename=\"Studies.csv\"").build();
  }
}
