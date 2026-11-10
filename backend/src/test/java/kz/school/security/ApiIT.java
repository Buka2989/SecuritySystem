package kz.school.security;

import com.fasterxml.jackson.databind.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import org.springframework.test.web.servlet.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.testcontainers.containers.PostgreSQLContainer;
import kz.school.security.entity.User;
import kz.school.security.repository.UserRepository;

@SpringBootTest @AutoConfigureMockMvc
class ApiIT {
    static PostgreSQLContainer<?> pg;
    @DynamicPropertySource static void database(DynamicPropertyRegistry r) {
        String url=System.getenv("TEST_DATABASE_URL");
        if (url==null) {
            pg=new PostgreSQLContainer<>("postgres:18-alpine").withDatabaseName("security_test"); pg.start();
            r.add("spring.datasource.url",pg::getJdbcUrl); r.add("spring.datasource.username",pg::getUsername); r.add("spring.datasource.password",pg::getPassword);
        } else {
            if (!url.endsWith("/security_test")) throw new IllegalArgumentException("Tests require a disposable database named security_test");
            r.add("spring.datasource.url",()->url); r.add("spring.datasource.username",()->System.getenv("TEST_DATABASE_USER"));
            r.add("spring.datasource.password",()->System.getenv("TEST_DATABASE_PASSWORD"));
        }
        r.add("spring.sql.init.mode",()->"always"); r.add("spring.sql.init.schema-locations",()->"classpath:schema.sql");
        r.add("app.jwt-secret",()->"integration-test-only-key-at-least-32-bytes");
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    String admin,parent,guard,other;
    UUID parentId;
    @BeforeEach void seed() throws Exception {
        jdbc.execute("TRUNCATE access_logs, qr_codes, verification_requests, guardian_student, students, users");
        parentId=seedUser("parent","PARENT");seedUser("admin","ADMIN");seedUser("guard","SECURITY");seedUser("other","TRUSTED_PERSON");
        admin=login("admin");parent=login("parent");guard=login("guard");other=login("other");
    }
    UUID seedUser(String username,String role) {
        User u=new User();u.username=username;u.fullName=username;u.role=role;u.status="ACTIVE";u.passwordHash=passwords.encode("Test-password-123");
        u.createdAt=LocalDateTime.now();u.updatedAt=u.createdAt;return users.save(u).id;
    }
    String login(String name) throws Exception { return call("POST","/auth/login",null,Map.of("username",name,"password","Test-password-123"),200).get("access_token").asText(); }
    JsonNode call(String method,String url,String token,Object body,int status) throws Exception {
        var req=request(org.springframework.http.HttpMethod.valueOf(method),url).contentType("application/json");
        if(token!=null)req.header("Authorization","Bearer "+token);if(body!=null)req.content(json.writeValueAsBytes(body));
        var response=mvc.perform(req).andReturn().getResponse();
        assertThat(response.getStatus()).as(method+" "+url+" "+response.getContentAsString()).isEqualTo(status);
        return response.getContentAsString().isBlank()?json.nullNode():json.readTree(response.getContentAsString());
    }
    JsonNode student() throws Exception { return call("POST","/students/",admin,Map.of("full_name","Test Child","class_name","5A"),201); }
    JsonNode link() throws Exception { return call("POST","/guardian-students/",parent,Map.of("student_id",student().get("id").asText(),"relationship_type","FATHER"),201); }
    String requestId(String link) throws Exception { for(JsonNode r:call("GET","/verification-requests/",admin,null,200))if(r.get("guardian_student_id").asText().equals(link)&&r.get("status").asText().equals("PENDING"))return r.get("id").asText();throw new AssertionError("Missing request"); }
    JsonNode approvedQr() throws Exception { String id=link().get("id").asText();call("PATCH","/verification-requests/"+requestId(id)+"/approve",admin,null,200);return call("POST","/qr-codes/",parent,Map.of("guardian_student_id",id),201); }
    JsonNode scan(JsonNode q,String actor) throws Exception { return call("POST","/qr-codes/verify",actor,Map.of("token",q.get("token").asText()),200); }
    @Test void authenticationRolesAndValidation() throws Exception {
        assertThat(call("GET","/auth/me",parent,null,200).get("username").asText()).isEqualTo("parent");
        assertThat(call("GET","/auth/me",parent,null,200).has("password_hash")).isFalse();
        call("POST","/auth/login",null,Map.of("username","parent","password","wrong"),401);
        call("GET","/auth/me","malformed",null,401);call("GET","/students/",null,null,401);
        call("GET","/users/",parent,null,403); call("GET","/users/",admin,null,200);
        call("POST","/students/",parent,Map.of("full_name","x","class_name","1A"),403);
        call("POST","/users/",null,Map.of("full_name","bad","username","bad","password","x","role","ADMIN"),403);
        call("POST","/users/",null,Map.of("full_name","new","username","new","password","x","role","PARENT"),201);
        call("POST","/users/",admin,Map.of("full_name","new guard","username","newguard","password","x","role","SECURITY"),201);
        call("GET","/students/search?full_name=x&class_name=1",parent,null,422);
        call("POST","/guardian-students/",parent,Map.of("student_id","not-a-uuid","relationship_type","FATHER"),422);
        call("PATCH","/users/me",parent,Map.of("full_name","Changed Name"),200);
        call("PATCH","/users/"+parentId+"/status",admin,Map.of("status","BLOCKED"),200);
        call("GET","/auth/me",parent,null,403);
    }
    @Test void searchAndVerificationLifecycle() throws Exception {
        JsonNode g=link();String id=g.get("id").asText();String sid=g.get("student_id").asText();
        assertThat(call("GET","/students/search?full_name=test&class_name=5a",parent,null,200)).hasSize(1);
        call("GET","/students/"+sid,parent,null,200); call("GET","/students/",parent,null,200);
        call("POST","/guardian-students/",parent,Map.of("student_id",sid,"relationship_type","MOTHER"),400);
        call("POST","/qr-codes/",parent,Map.of("guardian_student_id",id),403);
        call("POST","/verification-requests/",other,Map.of("guardian_student_id",id),404);
        call("POST","/verification-requests/",parent,Map.of("guardian_student_id",id),400);
        String rid=requestId(id);call("PATCH","/verification-requests/"+rid+"/reject",admin,Map.of("comment","Check documents"),200);
        call("PATCH","/verification-requests/"+rid+"/approve",admin,null,400);
        call("POST","/verification-requests/",parent,Map.of("guardian_student_id",id),201);
        call("PATCH","/verification-requests/"+requestId(id)+"/approve",admin,null,200);
        assertThat(call("GET","/guardian-students/",parent,null,200).get(0).get("verification_status").asText()).isEqualTo("APPROVED");
        call("POST","/verification-requests/",parent,Map.of("guardian_student_id",id),400);
    }
    @Test void qrExpiryRevocationOwnershipAndLogs() throws Exception {
        JsonNode q=approvedQr();
        call("POST","/qr-codes/verify",parent,Map.of("token",q.get("token").asText()),403);
        call("PATCH","/qr-codes/"+q.get("id").asText()+"/revoke",other,null,404);
        assertThat(scan(q,guard).get("reason").asText()).isEqualTo("VALID_QR");
        assertThat(scan(q,guard).get("reason").asText()).isEqualTo("QR_ALREADY_USED");
        assertThat(call("GET","/access-logs/",guard,null,200)).hasSize(2);
        call("GET","/access-logs/",parent,null,403);call("GET","/qr-codes/",parent,null,200);
        JsonNode expired=approvedQr();jdbc.update("update qr_codes set expires_at=? where id=?",LocalDateTime.of(2000,1,1,0,0),UUID.fromString(expired.get("id").asText()));
        assertThat(scan(expired,guard).get("reason").asText()).isEqualTo("QR_EXPIRED");
        JsonNode revoked=approvedQr();call("PATCH","/qr-codes/"+revoked.get("id").asText()+"/revoke",parent,null,200);
        assertThat(scan(revoked,guard).get("reason").asText()).isEqualTo("QR_REVOKED");
        assertThat(call("POST","/qr-codes/verify",guard,Map.of("token","unknown"),200).get("reason").asText()).isEqualTo("INVALID_QR");
    }
    @Test void deletingAndRestoringPreservesHistoryAndRevokesQr() throws Exception {
        JsonNode q=approvedQr();String id=q.get("guardian_student_id").asText();
        String sid=call("GET","/guardian-students/",parent,null,200).get(0).get("student_id").asText();
        call("DELETE","/guardian-students/"+id,other,null,404);call("DELETE","/guardian-students/"+id,parent,null,204);
        assertThat(scan(q,guard).get("reason").asText()).isEqualTo("QR_REVOKED");
        call("POST","/qr-codes/",parent,Map.of("guardian_student_id",id),403);
        JsonNode restored=call("POST","/guardian-students/",parent,Map.of("student_id",sid,"relationship_type","MOTHER"),201);
        assertThat(restored.get("id").asText()).isEqualTo(id);assertThat(restored.get("verification_status").asText()).isEqualTo("PENDING");
        String pending=requestId(id);call("DELETE","/guardian-students/"+id,parent,null,204);
        assertThat(jdbc.queryForObject("select status from verification_requests where id=?",String.class,UUID.fromString(pending))).isEqualTo("CANCELLED");
    }
    @Test void concurrentScansAllowExactlyOneAndLogEveryAttempt() throws Exception {
        JsonNode q=approvedQr();int count=8;CountDownLatch start=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(count)) {
            List<Future<String>> results=new ArrayList<>();
            for(int i=0;i<count;i++) results.add(executor.submit(()->{start.await();return scan(q,guard).get("result").asText();}));
            start.countDown();int allowed=0;for(var result:results)if(result.get(30,TimeUnit.SECONDS).equals("ALLOWED"))allowed++;
            assertThat(allowed).isEqualTo(1);
        }
        assertThat(jdbc.queryForObject("select count(*) from access_logs where qr_code_id=?",Integer.class,UUID.fromString(q.get("id").asText()))).isEqualTo(count);
    }
    @Test void concurrentResubmissionCreatesOnlyOnePendingRequest() throws Exception {
        String id=link().get("id").asText();
        call("PATCH","/verification-requests/"+requestId(id)+"/reject",admin,Map.of("comment","retry"),200);
        CountDownLatch start=new CountDownLatch(1);
        try(var executor=Executors.newFixedThreadPool(4)) {
            List<Future<Integer>> results=new ArrayList<>();
            for(int i=0;i<4;i++)results.add(executor.submit(()->{
                start.await();return mvc.perform(post("/verification-requests/").header("Authorization","Bearer "+parent)
                    .contentType("application/json").content(json.writeValueAsBytes(Map.of("guardian_student_id",id)))).andReturn().getResponse().getStatus();
            }));
            start.countDown();List<Integer> statuses=new ArrayList<>();for(var f:results)statuses.add(f.get(30,TimeUnit.SECONDS));
            assertThat(statuses).containsExactlyInAnyOrder(201,400,400,400);
        }
        assertThat(jdbc.queryForObject("select count(*) from verification_requests where guardian_student_id=? and status='PENDING'",Integer.class,UUID.fromString(id))).isEqualTo(1);
    }
    @Test void logWriteFailureRollsBackQrConsumption() throws Exception {
        JsonNode q=approvedQr();
        // Disposable test database only. Force the final audit insert to fail after setting USED.
        jdbc.execute("ALTER TABLE access_logs ADD CONSTRAINT test_reject_allowed CHECK (result <> 'ALLOWED')");
        try {
            call("POST","/qr-codes/verify",guard,Map.of("token",q.get("token").asText()),409);
            assertThat(jdbc.queryForObject("select status from qr_codes where id=?",String.class,UUID.fromString(q.get("id").asText()))).isEqualTo("ACTIVE");
        } finally { jdbc.execute("ALTER TABLE access_logs DROP CONSTRAINT test_reject_allowed"); }
        assertThat(scan(q,guard).get("reason").asText()).isEqualTo("VALID_QR");
    }
}
