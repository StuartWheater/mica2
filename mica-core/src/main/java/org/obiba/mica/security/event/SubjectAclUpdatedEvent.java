/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.security.event;

import javax.annotation.Nullable;

import com.google.common.eventbus.Subscribe;
import org.obiba.mica.security.domain.SubjectAcl;

public class SubjectAclUpdatedEvent {

  private final SubjectAcl.Subject subject;

  public SubjectAclUpdatedEvent() {
    // any subject
    subject = null;
  }

  public SubjectAclUpdatedEvent(SubjectAcl.Subject subject) {
    this.subject = subject;
  }

  public boolean hasSubject() {
    return subject != null;
  }

  @Nullable
  public SubjectAcl.Subject getSubject() {
    return subject;
  }
}
