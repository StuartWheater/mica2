/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.study.domain;

import java.io.Serializable;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Maps;
import org.obiba.mica.core.domain.AbstractAttributeModelAware;
import org.obiba.mica.core.domain.LocalizedString;
import org.obiba.mica.file.Attachment;
import org.obiba.mica.study.date.PersistableYearMonth;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;

public class DataCollectionEvent extends AbstractAttributeModelAware
    implements Serializable, Persistable<String>, Comparable<DataCollectionEvent> {

  private static final long serialVersionUID = 6559914069652243954L;

  private String id;

  @NotNull
  private LocalizedString name;

  private LocalizedString description;

  private PersistableYearMonth start;

  private PersistableYearMonth end;

  private List<String> dataSources;

  private List<String> administrativeDatabases;

  private LocalizedString otherDataSources;

  private List<String> bioSamples;

  private LocalizedString tissueTypes;

  private LocalizedString otherBioSamples;

  @Transient
  private List<Attachment> attachments = Lists.newArrayList();

  @Override
  public String getId() {
    return id;
  }

  @JsonIgnore
  @Override
  public boolean isNew() {
    return Strings.isNullOrEmpty(id);
  }

  public void setId(String id) {
    if(!Strings.isNullOrEmpty(id)) this.id = id;
  }

  public LocalizedString getName() {
    return name;
  }

  public void setName(LocalizedString name) {
    this.name = name;
  }

  public LocalizedString getDescription() {
    return description;
  }

  public void setDescription(LocalizedString description) {
    this.description = description;
  }

  public boolean hasStart() {
    return start != null;
  }

  public PersistableYearMonth getStart() {
    return start;
  }

  public void setStart(PersistableYearMonth start)
  {
    this.start = start;
  }

  public void setStart(int year, @Nullable Integer month) {
    start = (month == null || month == 0) ? PersistableYearMonth.of(year) : PersistableYearMonth.of(year, month);
  }

  public boolean hasEnd() {
    return end != null;
  }

  public PersistableYearMonth getEnd() {
    return end;
  }

  public void setEnd(PersistableYearMonth end) {
    this.end = end;
  }

  public void setEnd(int year, @Nullable Integer month) {
    end = month == null || month == 0 ? PersistableYearMonth.of(year) : PersistableYearMonth.of(year, month);
  }

  public List<String> getDataSources() {
    return dataSources;
  }

  public void addDataSource(@NotNull String datasource) {
    if(dataSources == null) dataSources = new ArrayList<>();
    dataSources.add(datasource);
  }

  public void setDataSources(List<String> dataSources) {
    this.dataSources = dataSources;
  }

  public List<String> getAdministrativeDatabases() {
    return administrativeDatabases;
  }

  public void addAdministrativeDatabases(@NotNull String database) {
    if(administrativeDatabases == null) administrativeDatabases = new ArrayList<>();
    administrativeDatabases.add(database);
  }

  public void setAdministrativeDatabases(List<String> administrativeDatabases) {
    this.administrativeDatabases = administrativeDatabases;
  }

  public LocalizedString getOtherDataSources() {
    return otherDataSources;
  }

  public void setOtherDataSources(LocalizedString otherDataSources) {
    this.otherDataSources = otherDataSources;
  }

  public List<String> getBioSamples() {
    return bioSamples;
  }

  public void addBioSample(@NotNull String bioSample) {
    if(bioSamples == null) bioSamples = new ArrayList<>();
    bioSamples.add(bioSample);
  }

  public void setBioSamples(List<String> bioSamples) {
    this.bioSamples = bioSamples;
  }

  public LocalizedString getTissueTypes() {
    return tissueTypes;
  }

  public void setTissueTypes(LocalizedString tissueTypes) {
    this.tissueTypes = tissueTypes;
  }

  public LocalizedString getOtherBioSamples() {
    return otherBioSamples;
  }

  public void setOtherBioSamples(LocalizedString otherBioSamples) {
    this.otherBioSamples = otherBioSamples;
  }

  /**
   * @deprecated kept for backward compatibility.
   * @return
     */
  @Deprecated
  @JsonIgnore
  public List<Attachment> getAttachments() {
    return attachments;
  }

  /**
   * @deprecated kept for backward compatibility.
   * @param attachments
     */
  @Deprecated
  @JsonProperty
  public void setAttachments(@NotNull List<Attachment> attachments) {
    this.attachments = attachments == null ? Lists.newArrayList() : attachments;
  }

  @Override
  public int hashCode() {return Objects.hash(id);}

  @Override
  @SuppressWarnings("SimplifiableIfStatement")
  public boolean equals(Object obj) {
    if(this == obj) return true;
    if(obj == null || getClass() != obj.getClass()) return false;
    return Objects.equals(id, ((DataCollectionEvent) obj).id);
  }

  @Override
  public int compareTo(@NotNull DataCollectionEvent dce) {
    ComparisonChain chain = ComparisonChain.start();

    if(start != null && dce.start != null) {
      chain = chain.compare(start, dce.start);
    } else if(start != dce.start) {
      return start == null ? 1 : -1;
    }

    if(end != null && dce.end != null) {
      chain = chain.compare(end, dce.end);
    } else if(end != dce.end) {
      return end == null ? 1 : -1;
    }

    return chain.compare(id, dce.id).result();
  }

  @Override
  public Map<String, Object> getModel() {
    if (!this.hasModel()) {
      Map<String, Object> map = Maps.newHashMap();

      if (this.getDescription() != null) map.put("description", this.getDescription());
      if (this.getDataSources() != null) map.put("dataSources", this.getDataSources());
      if (this.getAdministrativeDatabases() != null) map.put("administrativeDatabases", this.getAdministrativeDatabases());
      if (this.getOtherDataSources() != null) map.put("otherDataSources", this.getOtherDataSources());
      if (this.getBioSamples() != null) map.put("bioSamples", this.getBioSamples());
      if (this.getTissueTypes() != null) map.put("tissueTypes", this.getTissueTypes());
      if (this.getOtherBioSamples() != null) map.put("otherBioSamples", this.getOtherBioSamples());

      this.setModel(map);
    }

    return super.getModel();
  }
}
