package kz.school.security;

import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import static org.assertj.core.api.Assertions.*;
import kz.school.security.security.JwtService;

class PythonCompatibilityTest {
    @Test void readsPythonArgon2idWithPwdlibDefaultParameters() {
        // Generated independently by Python cryptography Argon2id: m=65536, t=3, p=4.
        String hash="$argon2id$v=19$m=65536,t=3,p=4$MDEyMzQ1Njc4OWFiY2RlZg$Cwvw/Kgwel1p1gX6FOI9O8H/jTkqQG/X/totRY1nPbs";
        var encoder=new Argon2PasswordEncoder(16,32,1,65536,3);
        assertThat(encoder.matches("Python-to-Java-test-password",hash)).isTrue();
        assertThat(encoder.matches("incorrect",hash)).isFalse();
    }
    @Test void readsIndependentlySignedPythonJwt() {
        // Python json + base64.urlsafe_b64encode + hmac/sha256, with original sub/role/exp payload.
        String token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIwMDAwMDAwMC0wMDAwLTAwMDAtMDAwMC0wMDAwMDAwMDAxMjMiLCJyb2xlIjoiUEFSRU5UIiwiZXhwIjoxNzY3MjI5MjAwfQ.SMdkVbC1nWywW3ITvYdpSwVFQ8T-o_Plexg2wvb7xSg";
        var jwt=new JwtService("test-only-key-never-used-in-production-0123456789abcdef0123456789abcdef","HS256",60,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),ZoneOffset.UTC));
        assertThat(jwt.validate(token)).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000123"));
    }
}
