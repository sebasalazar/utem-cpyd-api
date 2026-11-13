package cl.utem.inf.cpyd.utils;

import cl.utem.inf.cpyd.exceptions.ForbiddenException;
import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class JwtUtilsTest {

    private static final String SIGN = "01234567890123456789012345678901";

    @Test
    void roundTripKeepsSubjectAndTokenId() {
        final UUID token = UUID.randomUUID();
        final String jwt = JwtUtils.makeJwt(SIGN, "a@utem.cl", token);
        final JWTClaimsSet claims = JwtUtils.getValidClaims(SIGN, jwt);

        assertEquals("a@utem.cl", claims.getSubject());
        assertEquals(token.toString(), claims.getClaim(JwtUtils.CLAIM_TOKEN_ID));
    }

    @Test
    void makeJwtRejectsBadInput() {
        final UUID t = UUID.randomUUID();
        assertThrows(UtemException.class, () -> JwtUtils.makeJwt("", "a@utem.cl", t));
        assertThrows(UtemException.class, () -> JwtUtils.makeJwt("short", "a@utem.cl", t));
        assertThrows(UtemException.class, () -> JwtUtils.makeJwt(SIGN, " ", t));
        assertThrows(ValidationException.class, () -> JwtUtils.makeJwt(SIGN, "a@utem.cl", null));
    }

    @Test
    void wrongSecretIsForbidden() {
        final String jwt = JwtUtils.makeJwt(SIGN, "a@utem.cl", UUID.randomUUID());
        assertThrows(ForbiddenException.class, () -> JwtUtils.getValidClaims("99999999999999999999999999999999", jwt));
    }

    @Test
    void blankOrMalformedIsForbidden() {
        assertThrows(ForbiddenException.class, () -> JwtUtils.getValidClaims("", "x"));
        assertThrows(ForbiddenException.class, () -> JwtUtils.getValidClaims(SIGN, " "));
        assertThrows(ForbiddenException.class, () -> JwtUtils.getValidClaims(SIGN, "not-a-jwt"));
    }

    @Test
    void expiredIsForbidden() throws Exception {
        final JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("a@utem.cl")
                .expirationTime(new Date(System.currentTimeMillis() - 60_000))
                .build();
        final SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(SIGN.getBytes()));

        assertThrows(ForbiddenException.class, () -> JwtUtils.getValidClaims(SIGN, jwt.serialize()));
    }

    @Test
    void eachJwtHasUniqueId() {
        final UUID t = UUID.randomUUID();
        final String a = JwtUtils.makeJwt(SIGN, "a@utem.cl", t);
        final String b = JwtUtils.makeJwt(SIGN, "a@utem.cl", t);
        assertNotEquals(a, b);
    }
}
