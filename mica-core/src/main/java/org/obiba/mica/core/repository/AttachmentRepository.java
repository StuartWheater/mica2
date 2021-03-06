/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.core.repository;

import java.util.List;

import org.obiba.mica.file.Attachment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface AttachmentRepository extends MongoRepository<Attachment, String> {
  @Query("{'path': {$regex: ?0}}")
  List<Attachment> findByPath(String pathRegEx);

  List<Attachment> findByPathAndNameOrderByCreatedDateDesc(String path, String name);
}
