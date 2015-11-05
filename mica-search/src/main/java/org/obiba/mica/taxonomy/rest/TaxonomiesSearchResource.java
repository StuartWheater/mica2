/*
 * Copyright (c) 2015 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.taxonomy.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.obiba.mica.taxonomy.TaxonomyResolver;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Component
@Scope("request")
@Path("/taxonomies")
@RequiresAuthentication
public class TaxonomiesSearchResource extends AbstractTaxonomySearchResource {

  @GET
  @Path("/_filter")
  @Timed
  public List<Opal.TaxonomyDto> filter(@QueryParam("query") String query) {
    if(Strings.isNullOrEmpty(query)) return opalService.getTaxonomyDtos();

    Map<String, Map<String, List<String>>> taxoNamesMap = TaxonomyResolver.asMap(filterVocabularies(query), filterTerms(query));

    List<Opal.TaxonomyDto> results = Lists.newArrayList();
    opalService.getTaxonomies().stream().filter(t -> taxoNamesMap.containsKey(t.getName())).forEach(taxo -> {
      Opal.TaxonomyDto.Builder tBuilder = Dtos.asDto(taxo, false).toBuilder();
      populate(tBuilder, taxo, taxoNamesMap);
      results.add(tBuilder.build());
    });

    return results;
  }

}
