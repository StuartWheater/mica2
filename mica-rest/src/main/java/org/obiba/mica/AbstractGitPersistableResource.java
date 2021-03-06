/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica;

import org.obiba.git.CommitInfo;
import org.obiba.mica.comment.rest.CommentResource;
import org.obiba.mica.comment.rest.CommentsResource;
import org.obiba.mica.core.domain.EntityState;
import org.obiba.mica.core.domain.GitPersistable;
import org.obiba.mica.core.service.AbstractGitPersistableService;
import org.obiba.mica.micaConfig.service.MicaConfigService;
import org.obiba.mica.security.service.SubjectAclService;
import org.obiba.mica.web.model.Dtos;
import org.obiba.mica.web.model.Mica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class AbstractGitPersistableResource<T extends EntityState, T1 extends GitPersistable> {

  @Inject
  private MicaConfigService micaConfigService;

  @Inject
  private SubjectAclService subjectAclService;

  protected abstract String getId();

  protected abstract AbstractGitPersistableService<T, T1> getService();

  @Inject
  Dtos dtos;

  @Inject
  private CommentsResource commentsResource;

  @Inject
  private CommentResource commentResource;

  @Value("${portal.draftResource.urlPattern}")
  private String portalUrlPattern;

  private static final Logger log = LoggerFactory.getLogger(AbstractGitPersistableResource.class);

  @GET
  @Path("/commits")
  public List<Mica.GitCommitInfoDto> getCommitsInfo() {
    checkPermission("/draft/" + getService().getTypeName(), "VIEW");
    return dtos.asDto(getService().getCommitInfos(getService().findDraft(getId())));
  }

  @PUT
  @Path("/commit/{commitId}/restore")
  public Response restoreCommit(@NotNull @PathParam("commitId") String commitId) throws IOException {
    checkPermission("/draft/" + getService().getTypeName(), "EDIT");
    T1 gitPersistable = getService().getFromCommit(getService().findDraft(getId()), commitId);

    if (gitPersistable != null){
      getService().save(gitPersistable, createRestoreComment(getService().getCommitInfo(gitPersistable, commitId)));
    }

    return Response.noContent().build();
  }

  @GET
  @Path("/commit/{commitId}")
  public Mica.GitCommitInfoDto getCommitInfo(@NotNull @PathParam("commitId") String commitId) throws IOException {
    checkPermission("/draft/" + getService().getTypeName(), "VIEW");
    return dtos.asDto(
      getCommitInfoInternal(getService().getCommitInfo(getService().findDraft(getId()), commitId), commitId, null));
  }

  @Path("/comments")
  public CommentsResource comments() {
    commentsResource.setService(getService());
    return commentsResource;
  }

  @Path("/comment/{commentId}")
  public CommentResource getCommentResource() {
    commentResource.setService(getService());
    return commentResource;
  }

  @PUT
  @Path("/_share_key")
  public Response getShareKey(@QueryParam("expire") String expire) {
    checkPermission("/draft/" + getService().getTypeName(), "EDIT");
    return Response.ok().entity(createShareKey(expire)).build();
  }

  @PUT
  @Path("/_share")
  public Response getShareURL(@QueryParam("expire") String expire) {
    checkPermission("/draft/" + getService().getTypeName(), "EDIT");
    log.debug("Get share url for {}", getService().getTypeName());
    return Response.ok().entity(generatePortalLinkForDraftResource(createShareKey(expire))).build();
  }

  /**
   * Check the permission (action on a resource). If a key is provided and is valid, the permission check is by-passed.
   * If the provided key is not valid, permission check is applied.
   * @param resource
   * @param action
   * @param shareKey
   */
  protected void checkPermission(@NotNull String resource, @NotNull String action, @Nullable String shareKey) {
    subjectAclService.checkPermission(resource, action, getId(), shareKey);
  }

  protected void checkPermission(@NotNull String resource, @NotNull String action) {
    checkPermission(resource, action, null);
  }

  //
  // Private methods
  //

  private String createShareKey(String expire) {
    return subjectAclService.createShareKey(String.format("/draft/%s/%s", getService().getTypeName(), getId()), expire);
  }

  private CommitInfo getCommitInfoInternal(@NotNull CommitInfo commitInfo, @NotNull String commitId,
    @Nullable String prevCommitId) {
    Iterable<String> diffEntries = getService().getDiffEntries(getService().findDraft(getId()), commitId, prevCommitId);
    return CommitInfo.Builder.createFromObject(commitInfo).diffEntries((List<String>) diffEntries).build();
  }

  private String createRestoreComment(CommitInfo commitInfo) {
    LocalDateTime date = LocalDateTime
      .parse(commitInfo.getDate().toString(), DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss zzz yyyy"));
    String formatted =  date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy h:mm a"));

    return String.format("Restored revision from '%s' (%s...)", formatted, commitInfo.getCommitId().substring(0,9));
  }

  private String generatePortalLinkForDraftResource(String shareKey) {
    return portalUrlPattern
      .replace("{portalUrl}", micaConfigService.getPortalUrl())
      .replace("{resourceType}", getService().getTypeName())
      .replace("{resourceId}", getId())
      .replace("{shareKey}", shareKey);
  }
}
