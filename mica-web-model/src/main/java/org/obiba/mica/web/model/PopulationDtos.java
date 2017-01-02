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

import java.util.HashMap;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import com.google.common.base.Strings;
import org.obiba.mica.JSONUtils;
import org.obiba.mica.study.date.PersistableYearMonth;
import org.obiba.mica.study.domain.DataCollectionEvent;
import org.obiba.mica.study.domain.Population;
import org.springframework.stereotype.Component;

import static org.obiba.mica.web.model.Mica.StudyDto.PopulationDto;

@Component
@SuppressWarnings({ "OverlyLongMethod", "OverlyCoupledClass" })
class PopulationDtos {

  @Inject
  private LocalizedStringDtos localizedStringDtos;

  @NotNull
  PopulationDto asDto(Population population) {
    PopulationDto.Builder builder = PopulationDto.newBuilder();

    if(population.hasModel()) builder.setContent(JSONUtils.toJSON(population.getModel()));

    builder.setId(population.getId());

    if(population.getName() != null) builder.addAllName(localizedStringDtos.asDto(population.getName()));

    if(population.getDescription() != null) {
      builder.addAllDescription(localizedStringDtos.asDto(population.getDescription()));
    }

    if(population.getDataCollectionEvents() != null) {
      population.getDataCollectionEvents().forEach(dce -> builder.addDataCollectionEvents(asDto(dce)));
    }

    return builder.build();
  }

  @NotNull
  Population fromDto(Mica.StudyDto.PopulationDtoOrBuilder dto) {
    Population population = new Population();
    population.setId(dto.getId());
    if(dto.getNameCount() > 0) population.setName(localizedStringDtos.fromDto(dto.getNameList()));
    if(dto.getDescriptionCount() > 0) population.setDescription(localizedStringDtos.fromDto(dto.getDescriptionList()));
    if(dto.getDataCollectionEventsCount() > 0) {
      dto.getDataCollectionEventsList().forEach(dceDto -> population.addDataCollectionEvent(fromDto(dceDto)));
    }

    if (dto.hasContent() && !Strings.isNullOrEmpty(dto.getContent()))
      population.setModel(JSONUtils.toMap(dto.getContent()));
    else
      population.setModel(new HashMap<>());

    return population;
  }

  @NotNull
  PopulationDto.DataCollectionEventDto asDto(@NotNull DataCollectionEvent dce) {
    PopulationDto.DataCollectionEventDto.Builder builder = PopulationDto.DataCollectionEventDto.newBuilder();

    if(dce.hasModel()) builder.setContent(JSONUtils.toJSON(dce.getModel()));

    builder.setId(dce.getId());

    if(dce.getName() != null) builder.addAllName(localizedStringDtos.asDto(dce.getName()));

    if(dce.getDescription() != null) {
      builder.addAllDescription(localizedStringDtos.asDto(dce.getDescription()));
    }

    if(dce.getStart() != null) {
      PersistableYearMonth.YearMonthData startData = dce.getStart().getYearMonthData();
      builder.setStartYear(startData.getYear());
      if (startData.getMonth() != 0) {
        builder.setStartMonth(startData.getMonth());
      }
    }

    if(dce.getEnd() != null) {
      PersistableYearMonth.YearMonthData endData = dce.getEnd().getYearMonthData();
      builder.setEndYear(endData.getYear());
      if (endData.getMonth() != 0) {
        builder.setEndMonth(endData.getMonth());
      }
    }

    return builder.build();
  }

  @NotNull
  DataCollectionEvent fromDto(@NotNull PopulationDto.DataCollectionEventDto dto) {
    DataCollectionEvent dce = new DataCollectionEvent();
    dce.setId(dto.getId());
    if(dto.getNameCount() > 0) dce.setName(localizedStringDtos.fromDto(dto.getNameList()));
    if(dto.getDescriptionCount() > 0) dce.setDescription(localizedStringDtos.fromDto(dto.getDescriptionList()));
    if(dto.hasStartYear()) dce.setStart(dto.getStartYear(), dto.hasStartMonth() ? dto.getStartMonth() : null);
    if(dto.hasEndYear()) dce.setEnd(dto.getEndYear(), dto.hasEndMonth() ? dto.getEndMonth() : null);

    if(dto.hasContent() && !Strings.isNullOrEmpty(dto.getContent()))
      dce.setModel(JSONUtils.toMap(dto.getContent()));
    else
      dce.setModel(new HashMap<>());

    return dce;
  }
}
