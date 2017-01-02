/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.dataset.domain;

import org.obiba.mica.core.domain.EntityState;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class HarmonizationDatasetState extends EntityState {
  @Override
  public String pathPrefix() {
    return "harmonizationDatasets";
  }
}
