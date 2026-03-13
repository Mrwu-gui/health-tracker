package com.healthtracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/health", "/api/user/register", "/api/user/login", "/api/auth/**", "/api/docs/**", "/api/docs-ui/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOrigin("http://139.196.155.234");
        config.addAllowedOrigin("http://172.24.51.229");
        config.addAllowedOriginPattern("http://139.196.155.234:*");
        config.addAllowedOriginPattern("http://172.24.51.229:*");
        // TODO: Re-enable when domain is verified
        // config.addAllowedOrigin("http://datewell.xyz");
        // config.addAllowedOriginPattern("http://datewell.xyz:*");
        // config.addAllowedOrigin("https://datewell.xyz");
        // config.addAllowedOriginPattern("https://datewell.xyz:*");
        // config.addAllowedOrigin("http://www.datewell.xyz");
        // config.addAllowedOriginPattern("http://www.datewell.xyz:*");
        // config.addAllowedOrigin("https://www.datewell.xyz");
        // config.addAllowedOriginPattern("https://www.datewell.xyz:*");
        config.addAllowedOriginPattern("https://139.196.155.234:*");
        config.addAllowedOriginPattern("https://localhost:*");
        config.addAllowedOriginPattern("https://127.0.0.1:*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setMaxAge(3600L);
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
