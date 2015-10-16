package org.obiba.mica.study.domain;

import javax.validation.constraints.NotNull;

import org.obiba.mica.core.domain.EntityState;
import org.obiba.mica.core.domain.LocalizedString;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class StudyState extends EntityState {

  private static final long serialVersionUID = -4271967393906681773L;

  @Override
  public String pathPrefix() {
    return "studies";
  }

  @NotNull
  private LocalizedString name;

  public LocalizedString getName() {
    return name;
  }

  public void setName(LocalizedString name) {
    this.name = name;
  }
}
