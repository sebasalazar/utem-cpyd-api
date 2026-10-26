package cl.utem.inf.cpyd.utils;

import cl.utem.inf.cpyd.exceptions.UtemException;
import cl.utem.inf.cpyd.exceptions.ValidationException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;

public final class JwtUtils {

    private static final String ISSUER = "sebastian.cl";
    private static final String CLAIM_TOKEN = "ct";
    private static final Duration JWT_DURATION = Duration.ofMinutes(67);
    private static final int MIN_SIGN_LENGHT = 32;

    private JwtUtils() {
        throw new UnsupportedOperationException("Clase utilitaria no instanciable");
    }

    public static String makeJwt(final String sign, final String subject, final UUID token) {
        if (StringUtils.length(sign) < MIN_SIGN_LENGHT) {
            throw new UtemException("Se han encontrado problemas con la firma del JWT");
        }

        if (StringUtils.isBlank(subject)) {
            throw new UtemException("Se requiere un asunto previo a la firma");
        }

        if (token == null) {
            throw new ValidationException("Se requiere un token válido");
        }

        final Instant issuedAt = Instant.now();
        final Instant expiresAt = issuedAt.plus(JWT_DURATION);

        try {
            final byte[] secretBytes = sign.getBytes(StandardCharsets.UTF_8);
            final JWSSigner signer = new MACSigner(secretBytes);

            final JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder();
            claimBuilder
                    .jwtID(UUID.randomUUID().toString())
                    .issuer(ISSUER)
                    .subject(subject)
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .claim(CLAIM_TOKEN, token.toString());

            final SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimBuilder.build());
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception e) {
            throw new UtemException("No fue posible generar el JWT", e);
        }
    }
}
