package kz.school.security;
import java.time.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import kz.school.security.security.JwtService;
import kz.school.security.exception.ApiException;
class JwtServiceTest {
    private static final String KEY="test-only-key-never-used-in-production-0123456789abcdef0123456789abcdef";
    private static final Clock NOW=Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"),ZoneOffset.UTC);
    @Test void validTokenRetainsUuid() { JwtService s=new JwtService(KEY,"HS256",60,NOW);UUID id=UUID.randomUUID();assertThat(s.validate(s.create(id,"PARENT"))).isEqualTo(id); }
    @Test void rejectsExpiredToken() { String t=new JwtService(KEY,"HS256",1,NOW).create(UUID.randomUUID(),"ADMIN");assertThatThrownBy(()->new JwtService(KEY,"HS256",1,Clock.offset(NOW,Duration.ofMinutes(1))).validate(t)).isInstanceOf(ApiException.class); }
    @Test void rejectsWrongSecretAndAlgorithm() {
        String t=new JwtService(KEY,"HS256",60,NOW).create(UUID.randomUUID(),"ADMIN");
        assertThatThrownBy(()->new JwtService(KEY+"x","HS256",60,NOW).validate(t)).isInstanceOf(ApiException.class);
        assertThatThrownBy(()->new JwtService(KEY,"HS512",60,NOW).validate(t)).isInstanceOf(ApiException.class);
    }
    @Test void rejectsMalformedTokensAndWeakConfiguration() {
        assertThatThrownBy(()->new JwtService(KEY,"HS256",60,NOW).validate("invalid")).isInstanceOf(ApiException.class);
        assertThatThrownBy(()->new JwtService("short","HS256",60,NOW)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()->new JwtService(KEY,"none",60,NOW)).isInstanceOf(IllegalArgumentException.class);
    }
}
