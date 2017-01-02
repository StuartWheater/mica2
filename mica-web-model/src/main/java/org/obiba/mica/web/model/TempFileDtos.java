/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.web.model;

import javax.validation.constraints.NotNull;

import org.obiba.mica.file.TempFile;
import org.springframework.stereotype.Component;

@Component
class TempFileDtos {

  @NotNull
  Mica.TempFileDto asDto(@NotNull TempFile tempFile) {
    return Mica.TempFileDto.newBuilder().setId(tempFile.getId()).setName(tempFile.getName()).setSize(tempFile.getSize())
        .setMd5(tempFile.getMd5()).build();
  }

}
