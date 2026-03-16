package com.healthtracker.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    private final JwtAuthFilter jwtAuthFilter;
    private final boolean devRelaxed;

    public SecurityConfig(
        JwtAuthFilter jwtAuthFilter,
        @Value("${security.dev-relaxed:false}") boolean devRelaxed
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.devRelaxed = devRelaxed;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                if (devRelaxed) {
                    auth.requestMatchers("/api/**").permitAll();
                } else {
                    auth.requestMatchers(
                        "/api/health",
                        "/api/ai/**",
                        "/api/ai/callback",
                        "/api/user/register",
                        "/api/user/login",
                        "/api/auth/**",
                        "/api/docs/**",
                        "/api/docs-ui/**",
                        "/api/swagger-ui/**",
                        "/swagger-ui/**",
                        "/api/admin/**",
                        "/api/admin/logs/**",
                        "/logs.html",
                        "/uploads/**"
                    ).permitAll();
                }
                auth.anyRequest().authenticated();
            })
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("http://*:*");
        config.addAllowedOriginPattern("https://*:*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setMaxAge(3600L);
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
