/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.core.event;

import org.springframework.data.domain.Persistable;

public class PersistablePublishedEvent<TPersistable extends Persistable<?>>
    extends PersistableUpdatedEvent<TPersistable> {

  public PersistablePublishedEvent(TPersistable persistable) {
    super(persistable);
  }
}
