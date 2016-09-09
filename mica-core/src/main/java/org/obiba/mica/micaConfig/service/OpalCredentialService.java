/*
 * Copyright (c) 2016 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.mica.micaConfig.service;

import java.io.IOException;
import java.security.KeyStoreException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;

import org.obiba.mica.dataset.service.KeyStoreService;
import org.obiba.mica.micaConfig.AuthType;
import org.obiba.mica.micaConfig.NoSuchOpalCredential;
import org.obiba.mica.micaConfig.domain.OpalCredential;
import org.obiba.mica.micaConfig.repository.OpalCredentialRepository;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static java.util.stream.Collectors.toList;

@Service
@Validated
public class OpalCredentialService {

  @Inject
  private OpalCredentialRepository repository;

  @Inject
  private KeyStoreService keyStoreService;

  @Inject
  private MicaConfigService micaConfigService;

  public boolean hasOpalCredential(String id) {
    return repository.findOne(id) != null;
  }

  @NotNull
  public OpalCredential getOpalCredential(@NotNull String id) throws NoSuchOpalCredential {
    OpalCredential opalCredential = Optional.ofNullable(repository.findOne(id)).orElseThrow(NoSuchOpalCredential::new);

    if(opalCredential.getAuthType() == AuthType.USERNAME)
      opalCredential.setPassword(micaConfigService.decrypt(opalCredential.getPassword()));

    return opalCredential;
  }

  public Optional<OpalCredential> findOpalCredentialById(String id) {
    OpalCredential opalCredential = repository.findOne(id);

    if(opalCredential != null &&  opalCredential.getAuthType() == AuthType.USERNAME)
      opalCredential.setPassword(micaConfigService.decrypt(opalCredential.getPassword()));

    return Optional.ofNullable(opalCredential);
  }

  public List<OpalCredential> findAllOpalCredentials() {
    return repository.findAll().stream().map(c -> {
      if (c.getAuthType() == AuthType.USERNAME)
        c.setPassword(micaConfigService.decrypt(c.getPassword()));

      return c;
    }).collect(toList());
  }

  public void createOrUpdateOpalCredential(String opalUrl, String username, String password) {
    OpalCredential credential = Optional.ofNullable(repository.findOne(opalUrl))
      .map(c -> {
        if(c.getAuthType() == AuthType.CERTIFICATE)
          keyStoreService.deleteKeyPair(OpalService.OPAL_KEYSTORE, opalUrl);
        c.setAuthType(AuthType.USERNAME);
        c.setUsername(username);
        c.setPassword(micaConfigService.encrypt(password));

        return c;
      })
      .orElse(new OpalCredential(opalUrl, AuthType.USERNAME, username, micaConfigService.encrypt(password)));

    repository.save(credential);
  }

  public void deleteOpalCredential(String opalUrl) {
    OpalCredential credential = repository.findOne(opalUrl);

    if(credential == null) return;

    repository.delete(credential);

    if(credential.getAuthType() == AuthType.CERTIFICATE) {
      keyStoreService.deleteKeyPair(OpalService.OPAL_KEYSTORE, opalUrl);
    }
  }

  public void saveOrUpdateOpalCertificateCredential(String opalUrl) {
    OpalCredential credential = Optional.ofNullable(repository.findOne(opalUrl))
      .map(c -> {
        c.setAuthType(AuthType.CERTIFICATE);
        c.setUsername(null);
        c.setPassword(null);

        return c;
      })
      .orElse(new OpalCredential(opalUrl, AuthType.CERTIFICATE));

    repository.save(credential);
  }

  public String getCertificate(String opalUrl) {
    try {
      return keyStoreService.getPEMCertificate(OpalService.OPAL_KEYSTORE, opalUrl);
    } catch(KeyStoreException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
