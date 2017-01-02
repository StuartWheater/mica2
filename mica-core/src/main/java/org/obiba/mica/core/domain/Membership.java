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

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.DBRef;

public class Membership implements Serializable {

  public final static String INVESTIGATOR = "investigator";

  public final static String CONTACT = "contact";

  @DBRef
  private Person person;

  private String role;

  public Membership() {

  }

  public Membership(Person person, String role) {
    this.person = person;
    this.role = role;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }
}
