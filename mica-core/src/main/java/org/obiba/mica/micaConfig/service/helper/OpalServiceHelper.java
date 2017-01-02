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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.obiba.mica.core.domain.AttributeKey;
import org.obiba.mica.micaConfig.event.TaxonomiesUpdatedEvent;
import org.obiba.opal.core.domain.taxonomy.Taxonomy;
import org.obiba.opal.rest.client.magma.OpalJavaClient;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.taxonomy.Dtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.eventbus.EventBus;

@Component
public class OpalServiceHelper {

  private static final Logger log = LoggerFactory.getLogger(OpalServiceHelper.class);

  @Inject
  private EventBus eventBus;

  @Cacheable(value = "opal-taxonomies", key = "#opalJavaClient.newUri().build()") //opal root url as key
  public Map<String, Taxonomy> getTaxonomies(OpalJavaClient opalJavaClient) {
    log.info("Fetching opal taxonomies");
    URI uri = opalJavaClient.newUri().segment("system", "conf", "taxonomies").build();
    List<Opal.TaxonomyDto> taxonomies = opalJavaClient
      .getResources(Opal.TaxonomyDto.class, uri, Opal.TaxonomyDto.newBuilder());

    eventBus.post(new TaxonomiesUpdatedEvent());

    return taxonomies.stream().collect(Collectors.toConcurrentMap(Opal.TaxonomyDto::getName, this::fromDto));
  }

  /**
   * Decorate the variable taxonomies with some Mica specific attributes.
   *
   * @param dto
   * @return
   */
  private Taxonomy fromDto(Opal.TaxonomyDto dto) {
    Taxonomy taxonomy = Dtos.fromDto(dto);
    taxonomy.getVocabularies().forEach(vocabulary -> {
      String field = vocabulary.getAttributeValue("field");
      if(Strings.isNullOrEmpty(field)) {
        vocabulary.addAttribute("field",
          "attributes." + AttributeKey.getMapKey(vocabulary.getName(), taxonomy.getName()) + ".und");
      }
      String alias = vocabulary.getAttributeValue("alias");
      if(Strings.isNullOrEmpty(alias)) {
        vocabulary.addAttribute("alias",
          "attributes-" + AttributeKey.getMapKey(vocabulary.getName(), taxonomy.getName()) + "-und");
      }
    });
    return taxonomy;
  }
}
