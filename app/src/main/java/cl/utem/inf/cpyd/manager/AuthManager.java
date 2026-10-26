package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.domain.repository.CredentialRepository;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthManager {

    private final String sign;
    private final CredentialRepository credentialRepository;

    public AuthManager(@Value("${cpyd.sign}") String cpydSign, final CredentialRepository credentialRepository) {
        this.sign = StringUtils.trimToEmpty(cpydSign);
        this.credentialRepository = credentialRepository;
    }

    public String getSign() {
        return sign;
    }

    public Optional<Credential> getCredential(final String email) {
        return credentialRepository.findByEmailIgnoreCase(email);
    }

    public boolean isValid(final Credential credential, final String apiKey) {
        if (credential == null) {
            return false;
        }

        if (StringUtils.isBlank(apiKey)) {
            return false;
        }

        return Strings.CS.equals(credential.getApiKey(), apiKey);
    }
}
