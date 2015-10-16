package org.obiba.mica.core.domain;

import java.io.Serializable;
import java.util.Map;

import org.springframework.data.mongodb.core.index.Indexed;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class EntityState extends AbstractGitPersistable {

  @Indexed
  private RevisionStatus revisionStatus = RevisionStatus.DRAFT;

  @Indexed
  private String publishedTag;

  private int revisionsAhead = 0;

  public String getPublishedTag() {
    return publishedTag;
  }

  public void setPublishedTag(String publishedTag) {
    this.publishedTag = publishedTag;
  }

  public boolean isPublished() {
    return !Strings.isNullOrEmpty(publishedTag);
  }

  public int getRevisionsAhead() {
    return revisionsAhead;
  }

  public void resetRevisionsAhead() {
    revisionsAhead = 0;
  }

  public void incrementRevisionsAhead() {
    revisionsAhead++;
  }

  public boolean hasRevisionsAhead() {
    return revisionsAhead > 0;
  }

  public RevisionStatus getRevisionStatus() {
    return revisionStatus;
  }

  public void setRevisionStatus(RevisionStatus status) {
    revisionStatus = status;
  }

  @Override
  protected MoreObjects.ToStringHelper toStringHelper() {
    return super.toStringHelper().add("id", getId()) //
      .add("revisionStatus", revisionStatus) //
      .add("publishedTag", publishedTag);
  }

  @Override
  public Map<String, Serializable> parts() {
    throw new NotImplementedException();
  }
}
