package kz.school.security.service;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import kz.school.security.entity.User;
import kz.school.security.repository.UserRepository;
import kz.school.security.security.JwtService;
import kz.school.security.exception.ApiException;
import kz.school.security.dto.DtoMapper;
import static kz.school.security.dto.ApiDtos.*;

@Service @Transactional
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final Clock clock;
    public UserService(UserRepository users, PasswordEncoder passwords, JwtService jwt, Clock clock) {
        this.users=users; this.passwords=passwords; this.jwt=jwt; this.clock=clock;
    }
    public Token login(Login data) {
        User user=users.findByUsername(data.username()).orElseThrow(() -> new ApiException(401,"Invalid username or password"));
        boolean matches;
        try { matches=passwords.matches(data.password(),user.passwordHash); } catch (IllegalArgumentException e) { matches=false; }
        if (!matches) throw new ApiException(401,"Invalid username or password");
        if (!"ACTIVE".equals(user.status)) throw new ApiException(403,"User is blocked");
        return new Token(jwt.create(user.id,user.role),"bearer");
    }
    public List<UserResponse> list() { return users.findAll().stream().map(DtoMapper::user).toList(); }
    public UserResponse create(UserCreate data, User actor) {
        boolean admin=actor!=null && "ADMIN".equals(actor.role);
        if (!admin && (!Set.of("PARENT","TRUSTED_PERSON").contains(data.role()) || (data.status()!=null && !"ACTIVE".equals(data.status()))))
            throw new ApiException(403,"Only an administrator can create privileged or blocked users");
        if (users.findByUsername(data.username()).isPresent()) throw new ApiException(409,"Username already exists");
        User u=new User(); u.fullName=data.fullName(); u.username=data.username(); u.passwordHash=passwords.encode(data.password());
        u.role=data.role(); u.status=data.status()==null?"ACTIVE":data.status(); u.createdAt=LocalDateTime.now(clock); u.updatedAt=u.createdAt;
        return DtoMapper.user(users.save(u));
    }
    public UserResponse profile(User actor, ProfileUpdate data) {
        User u=users.lockById(actor.id).orElseThrow(() -> ApiException.missing("User not found"));
        u.fullName=data.fullName(); u.updatedAt=LocalDateTime.now(clock); return DtoMapper.user(u);
    }
    public UserResponse status(UUID id, StatusUpdate data) {
        User u=users.lockById(id).orElseThrow(() -> ApiException.missing("User not found"));
        u.status=data.status(); u.updatedAt=LocalDateTime.now(clock); return DtoMapper.user(u);
    }
    public void bootstrap(String username, String password) {
        if (username.isBlank() && password.isBlank()) return;
        if (username.isBlank() || password.length()<12) throw new IllegalArgumentException("Bootstrap needs a username and a password of at least 12 characters");
        // Never reset or overwrite an existing database account.
        if (users.count()!=0) return;
        User actor=new User(); actor.role="ADMIN";
        create(new UserCreate("Administrator",username,password,"ADMIN","ACTIVE"),actor);
    }
}
