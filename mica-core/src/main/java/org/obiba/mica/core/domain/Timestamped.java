/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.core.domain;

import javax.validation.constraints.NotNull;

import org.joda.time.DateTime;

public interface Timestamped {

  @NotNull
  DateTime getCreatedDate();

  void setCreatedDate(DateTime createdDate);

  DateTime getLastModifiedDate();

  void setLastModifiedDate(DateTime lastModifiedDate);
}
