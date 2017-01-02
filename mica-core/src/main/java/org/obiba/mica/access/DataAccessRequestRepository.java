/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.access;

import java.util.List;

import org.obiba.mica.access.domain.DataAccessRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Spring Data MongoDB repository for the {@link org.obiba.mica.access.domain.DataAccessRequest} entity.
 */

public interface DataAccessRequestRepository extends MongoRepository<DataAccessRequest, String>,
  DataAccessRequestRepositoryCustom {

  List<DataAccessRequest> findByApplicant(String applicant);

}
