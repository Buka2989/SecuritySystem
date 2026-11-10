package kz.school.security.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.web.cors.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.*;
import kz.school.security.security.JwtFilter;

@Configuration @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return new Argon2PasswordEncoder(16,32,1,65536,3); }
    @Bean FilterRegistrationBean<JwtFilter> disableServletFilter(JwtFilter filter) {
        var bean = new FilterRegistrationBean<>(filter); bean.setEnabled(false); return bean;
    }
    @Bean SecurityFilterChain security(HttpSecurity http, JwtFilter jwt, ObjectMapper json) throws Exception {
        return http.csrf(c -> c.disable()).cors(c -> {}).sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a.requestMatchers("/", "/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/auth/login","/users","/users/").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e.authenticationEntryPoint((req,res,ex) -> {
                res.setStatus(401); res.setContentType("application/json"); json.writeValue(res.getOutputStream(),Map.of("detail","Not authenticated"));
            }).accessDeniedHandler((req,res,ex) -> {
                res.setStatus(403); res.setContentType("application/json"); json.writeValue(res.getOutputStream(),Map.of("detail","You do not have permission to access this resource"));
            }))
            .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean CorsConfigurationSource cors(@Value("${app.cors-origins}") String origins) {
        var config = new CorsConfiguration(); config.setAllowedOrigins(Arrays.asList(origins.split(",")));
        config.setAllowedMethods(List.of("GET","POST","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        var source = new UrlBasedCorsConfigurationSource(); source.registerCorsConfiguration("/**",config); return source;
    }
    @Bean OpenAPI openApi() { return new OpenAPI().components(new Components().addSecuritySchemes("bearerAuth",
        new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth")); }
}
