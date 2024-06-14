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
                        .requestMatchers(HttpMethod.GET, "/v1/auth/admin/logout").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/auth/user/logout").hasRole("USER")
                        .requestMatchers(HttpMethod.POST,
                                "/v1/auth/admin/login",
                                "/v1/auth/admin/access",
                                "/v1/auth/user/login",
                                "/v1/auth/user/access"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/users/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/me").hasRole("USER")
                        .requestMatchers(HttpMethod.PATCH, "/v1/users/{id}/set-location").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/categories", "/v1/categories/{category-id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/categories").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/v1/categories/{category-id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/categories/{category-id}/set-active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/categories/{category-id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/v1/offers/all", "/v1/offers/{id}", "/v1/offers/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/offers/user/{id}").authenticated()
                        .requestMatchers(HttpMethod.POST, "/v1/offers").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/v1/offers", "/v1/offers/recover/{id}").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/offers/{id}","/v1/images").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/locations", "/v1/locations/{id}").permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/images").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/v1/offers/{id}").authenticated()
                        .requestMatchers(HttpMethod.GET, "/v1/locations","/v1/locations/{id}").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/v1/offers/{id}/reject").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/v1/offers/{id}/start-auction").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/v1/offers/{id}/cancel").hasRole("USER")
                        .requestMatchers(HttpMethod.DELETE, "/v1/offers/{id}/block-by-admin").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/v1/bids").hasRole("USER")
                        .anyRequest().denyAll()
                )
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
