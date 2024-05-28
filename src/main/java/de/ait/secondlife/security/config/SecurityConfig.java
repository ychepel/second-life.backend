package de.ait.secondlife.security.config;

import de.ait.secondlife.security.filters.TokenFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenFilter filter;

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(x -> x
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(x -> x
//                        .requestMatchers(HttpMethod.GET, "/v1/auth/admin/logout").hasRole("ADMIN")
//                        .requestMatchers(HttpMethod.GET, "/v1/auth/user/logout").hasRole("USER")
//                        .requestMatchers(HttpMethod.POST,
//                                "/v1/auth/admin/login",
//                                "/v1/auth/admin/access",
//                                "/v1/auth/user/login",
//                                "/v1/auth/user/access"
//                        ).permitAll()
//                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
//                        .requestMatchers(HttpMethod.POST, "/v1/users/register").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/v1/offer/**").permitAll()
//                        .requestMatchers(HttpMethod.GET, "/v1/offer/user/**").authenticated()
//                        .requestMatchers(HttpMethod.POST, "/v1/offer/**").authenticated()
//                        .requestMatchers(HttpMethod.PUT, "/v1/offer/**").authenticated()
//                        .requestMatchers(HttpMethod.DELETE, "/v1/offer/**").authenticated()
                        .anyRequest().permitAll())
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterAfter(filter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().addSecurityItem(new SecurityRequirement().
                        addList("Bearer Authentication"))
                .components(new Components().addSecuritySchemes
                        ("Bearer Authentication", createAPIKeyScheme()));
    }

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }
}
