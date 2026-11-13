package cl.utem.inf.cpyd.manager;

import cl.utem.inf.cpyd.domain.model.Credential;
import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.manager.helper.CacheHelper;
import cl.utem.inf.cpyd.utils.JwtUtils;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthManagerTest {

    private static final String SIGN = "01234567890123456789012345678901";

    private CacheHelper cache;
    private AuthManager auth;
    private Credential cred;

    @BeforeEach
    void setUp() {
        cache = mock(CacheHelper.class);
        auth = new AuthManager(SIGN, cache);
        cred = new Credential();
        cred.setEmail("a@utem.cl");
        cred.setToken(UUID.randomUUID());
        cred.setApiKey("key");
        cred.setActive(true);
    }

    private String bearer(String email, UUID token) {
        return "Bearer " + JwtUtils.makeJwt(SIGN, email, token);
    }

    @Test
    void isValidChecksActiveEmails() {
        when(cache.getActiveEmails()).thenReturn(Set.of("a@utem.cl"));
        assertTrue(auth.isValid("a@utem.cl"));
        assertFalse(auth.isValid("b@utem.cl"));
        assertFalse(auth.isValid(""));
        assertFalse(auth.isValid(null));
    }

    @Test
    void isValidFalseWhenNoActiveEmails() {
        when(cache.getActiveEmails()).thenReturn(Set.of());
        assertFalse(auth.isValid("a@utem.cl"));
    }

    @Test
    void apiKeyComparison() {
        assertTrue(auth.isValidApiKey(cred, "key"));
        assertFalse(auth.isValidApiKey(cred, "KEY"));
        assertFalse(auth.isValidApiKey(cred, "other"));
        assertFalse(auth.isValidApiKey(cred, " "));
        assertFalse(auth.isValidApiKey(null, "key"));
    }

    @Test
    void validateAcceptsValidBearer() {
        when(cache.getCredentialByToken(cred.getToken())).thenReturn(Optional.of(cred));
        assertDoesNotThrow(() -> auth.validate(bearer("A@utem.cl", cred.getToken())));
    }

    @Test
    void validateRejectsEmptyHeader() {
        assertThrows(ForbiddenException.class, () -> auth.validate(null));
        assertThrows(ForbiddenException.class, () -> auth.validate(""));
    }

    @Test
    void validateRejectsUnknownToken() {
        when(cache.getCredentialByToken(cred.getToken())).thenReturn(Optional.empty());
        final String h = bearer("a@utem.cl", cred.getToken());
        assertThrows(ForbiddenException.class, () -> auth.validate(h));
    }

    @Test
    void validateRejectsInactiveCredential() {
        cred.setActive(false);
        when(cache.getCredentialByToken(cred.getToken())).thenReturn(Optional.of(cred));
        final String h = bearer("a@utem.cl", cred.getToken());
        assertThrows(ForbiddenException.class, () -> auth.validate(h));
    }

    @Test
    void validateRejectsEmailMismatch() {
        when(cache.getCredentialByToken(cred.getToken())).thenReturn(Optional.of(cred));
        final String h = bearer("other@utem.cl", cred.getToken());
        assertThrows(ForbiddenException.class, () -> auth.validate(h));
    }

    @Test
    void validateRejectsGarbage() {
        assertThrows(ForbiddenException.class, () -> auth.validate("Bearer garbage"));
    }
}
