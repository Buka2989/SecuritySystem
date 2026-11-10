package kz.school.security.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import kz.school.security.entity.User;
import kz.school.security.repository.UserRepository;
import kz.school.security.exception.ApiException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserRepository users;
    private final ObjectMapper json;
    public JwtFilter(JwtService jwt, UserRepository users, ObjectMapper json) { this.jwt=jwt; this.users=users; this.json=json; }
    @Override protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        String auth = req.getHeader("Authorization");
        if (auth != null) {
            try {
                if (!auth.regionMatches(true,0,"Bearer ",0,7)) throw new ApiException(401,"Invalid or expired token");
                User user = users.findById(jwt.validate(auth.substring(7))).orElseThrow(() -> new ApiException(401,"User not found"));
                if (!"ACTIVE".equals(user.status)) throw new ApiException(403,"User is blocked");
                // Role is taken from the database on every request, not from a stale token claim.
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null,
                    List.of(new SimpleGrantedAuthority("ROLE_"+user.role))));
            } catch (ApiException e) {
                SecurityContextHolder.clearContext();
                res.setStatus(e.status); res.setContentType("application/json");
                json.writeValue(res.getOutputStream(),Map.of("detail",e.getMessage())); return;
            }
        }
        chain.doFilter(req,res);
    }
}
