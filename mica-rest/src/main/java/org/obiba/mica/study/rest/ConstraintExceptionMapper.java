/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.study.rest;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.obiba.mica.study.ConstraintException;

@Provider
public class ConstraintExceptionMapper implements ExceptionMapper<ConstraintException> {
  @Override
  public Response toResponse(ConstraintException e) {
    return Response.status(Response.Status.CONFLICT).type(MediaType.APPLICATION_JSON_TYPE)
      .entity(e.getConflicts()).build();
  }
}
