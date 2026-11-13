package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import cl.utem.inf.cpyd.manager.helper.CacheHelper;
import cl.utem.inf.cpyd.utils.JwtUtils;
import cl.utem.inf.cpyd.utils.TextUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import java.text.ParseException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthManager {

    private final String sign;
    private final CacheHelper cacheHelper;

    private static final String BAD_CREDENTIAL = "Credenciales inválidas";

    public AuthManager(@Value("${cpyd.sign}") String cpydSign, final CacheHelper cacheHelper) {
        this.sign = StringUtils.trimToEmpty(cpydSign);
        this.cacheHelper = cacheHelper;
    }

    public String getSign() {
        return sign;
    }

    public Optional<Credential> getCredential(final String email) {
        return cacheHelper.getCredentialByEmail(email);
    }

    public boolean isValid(final String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }

        final Set<String> activeEmails = cacheHelper.getActiveEmails();
        if (CollectionUtils.isEmpty(activeEmails)) {
            return false;
        }

        return activeEmails.contains(email);
    }

    public boolean isValidApiKey(final Credential credential, final String apiKey) {
        if (credential == null) {
            return false;
        }

        if (StringUtils.isBlank(apiKey)) {
            return false;
        }

        return Strings.CS.equals(credential.getApiKey(), apiKey);
    }

    public void validate(final String authorizationHeader) {
        if (StringUtils.isEmpty(authorizationHeader)) {
            throw new ForbiddenException();
        }

        final String jwt = StringUtils.trimToEmpty(Strings.CI.removeEnd(sign, "Bearer"));
        final JWTClaimsSet claims = JwtUtils.getValidClaims(sign, jwt);

        try {
            final String tokenStr = claims.getClaimAsString(JwtUtils.CLAIM_TOKEN_ID);
            final UUID token = UUID.fromString(tokenStr);

            Optional<Credential> opt = cacheHelper.getCredentialByToken(token);
            if (opt.isEmpty()) {
                throw new ValidationException(BAD_CREDENTIAL);
            }

            final Credential credential = opt.get();
            final String credEmail = TextUtils.lower(credential.getEmail());
            final String jwtEmail = TextUtils.lower(claims.getSubject());
            if (!Strings.CS.equals(credEmail, jwtEmail)) {
                throw new UtemException(BAD_CREDENTIAL);
            }
        } catch (UtemException | ValidationException | ParseException e) {
            throw new ForbiddenException("No valida el JWT", e);
        }
    }
}
