/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.study.rest;

import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.obiba.mica.core.service.PublishedDocumentService;
import org.obiba.mica.study.domain.Study;
import org.obiba.mica.study.service.PublishedStudyService;
import org.obiba.mica.web.model.Dtos;
import org.obiba.mica.web.model.Mica;
import org.springframework.context.ApplicationContext;

import com.codahale.metrics.annotation.Timed;

@Path("/")
@RequiresAuthentication
public class PublishedStudiesResource {

  @Inject
  private PublishedStudyService publishedStudyService;

  @Inject
  private Dtos dtos;

  @Inject
  private ApplicationContext applicationContext;

  @GET
  @Path("/studies")
  @Timed
  public Mica.StudySummariesDto list(@QueryParam("from") @DefaultValue("0") int from,
      @QueryParam("limit") @DefaultValue("10") int limit, @QueryParam("sort") String sort,
      @QueryParam("order") String order, @QueryParam("query") String query) {

    PublishedDocumentService.Documents<Study> studies = publishedStudyService.find(from, limit, sort, order, null, query);

    Mica.StudySummariesDto.Builder builder = Mica.StudySummariesDto.newBuilder();

    builder.setFrom(studies.getFrom()).setLimit(studies.getLimit()).setTotal(studies.getTotal());
    builder.addAllStudySummaries(studies.getList().stream().map(dtos::asSummaryDto).collect(Collectors.toList()));

    return builder.build();
  }

  @Path("/study/{id}")
  public PublishedStudyResource study(@PathParam("id") String id) {
    PublishedStudyResource studyResource = applicationContext.getBean(PublishedStudyResource.class);
    studyResource.setId(id);
    return studyResource;
  }

}
