package kz.school.security.config;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import kz.school.security.entity.User;
import kz.school.security.repository.*;
import kz.school.security.service.*;
import static kz.school.security.dto.ApiDtos.*;

/** Explicit demonstration profile; never changes a populated database. */
@Component @Profile("demo") @Order(0)
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository users;private final StudentRepository students;private final VerificationRequestRepository requests;
    private final UserService accounts;private final StudentService school;private final GuardianService guardians;
    private final VerificationService reviews;private final QrService qr;private final String password;
    public DemoDataInitializer(UserRepository users,StudentRepository students,VerificationRequestRepository requests,UserService accounts,
        StudentService school,GuardianService guardians,VerificationService reviews,QrService qr,@Value("${DEMO_PASSWORD:}") String password){
        this.users=users;this.students=students;this.requests=requests;this.accounts=accounts;this.school=school;
        this.guardians=guardians;this.reviews=reviews;this.qr=qr;this.password=password;
    }
    @Override @Transactional public void run(ApplicationArguments args){
        if(users.count()!=0 || students.count()!=0)return;
        if(password.length()<12)throw new IllegalArgumentException("Demo mode requires DEMO_PASSWORD of at least 12 characters");
        User actor=new User();actor.role="ADMIN";
        for(String[] entry:new String[][]{{"demo_admin","ADMIN"},{"demo_parent","PARENT"},{"demo_guard","SECURITY"},{"demo_trusted","TRUSTED_PERSON"}})
            accounts.create(new UserCreate(entry[0],entry[0],password,entry[1],"ACTIVE"),actor);
        var parent=users.findByUsername("demo_parent").orElseThrow();var guard=users.findByUsername("demo_guard").orElseThrow();
        var child=school.create(new StudentCreate("Demo Student One","5A","ACTIVE"));
        var second=school.create(new StudentCreate("Demo Student Two","5A","ACTIVE"));
        var link=guardians.create(parent,new LinkCreate(child.id(),"FATHER"));
        reviews.review(requests.findByGuardianStudentIdAndStatus(link.id(),"PENDING").getFirst().id,true,null);
        guardians.create(parent,new LinkCreate(second.id(),"FATHER"));
        guardians.create(users.findByUsername("demo_trusted").orElseThrow(),new LinkCreate(child.id(),"GUARDIAN"));
        var code=qr.create(parent,new QrCreate(link.id(),15));
        qr.verify(guard,new QrVerify(code.token()));qr.verify(guard,new QrVerify(code.token()));
    }
}
