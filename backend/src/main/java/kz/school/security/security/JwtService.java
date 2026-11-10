package kz.school.security.security;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import kz.school.security.exception.ApiException;

@Service
public class JwtService {
    private final byte[] secret;
    private final JWSAlgorithm algorithm;
    private final long minutes;
    private final Clock clock;

    public JwtService(@Value("${app.jwt-secret}") String secret, @Value("${app.jwt-algorithm}") String algorithm,
                      @Value("${app.jwt-expire-minutes}") long minutes, Clock clock) {
        if (!Set.of("HS256", "HS384", "HS512").contains(algorithm))
            throw new IllegalArgumentException("JWT_ALGORITHM must be HS256, HS384 or HS512");
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        int requiredBytes = Integer.parseInt(algorithm.substring(2)) / 8;
        if (this.secret.length < requiredBytes || minutes < 1)
            throw new IllegalArgumentException("JWT secret is too short for the algorithm or expiry is invalid");
        this.algorithm = JWSAlgorithm.parse(algorithm);
        this.minutes = minutes;
        this.clock = clock;
    }

    public String create(UUID userId, String role) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader(algorithm), new JWTClaimsSet.Builder()
                .subject(userId.toString()).claim("role", role)
                .expirationTime(Date.from(clock.instant().plusSeconds(minutes * 60))).build());
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException e) { throw new IllegalStateException("Cannot sign token", e); }
    }

    public UUID validate(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!algorithm.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret)))
                throw new IllegalArgumentException();
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (claims.getExpirationTime() == null || !claims.getExpirationTime().toInstant().isAfter(clock.instant())
                || (claims.getNotBeforeTime() != null && claims.getNotBeforeTime().toInstant().isAfter(clock.instant())))
                throw new IllegalArgumentException();
            return UUID.fromString(claims.getSubject());
        } catch (Exception e) { throw new ApiException(401, "Invalid or expired token"); }
    }
}
