package org.dubini.backofficeAPI.config;

import org.dubini.backofficeAPI.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
        System.out.println("=== SecurityConfig initialized ===");
        System.out.println("JwtFilter bean: " + jwtFilter);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        System.out.println("=== Configuring Security Filter Chain ===");

        SecurityFilterChain chain = http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> {
                    System.out.println("Configuring authorization rules:");
                    System.out.println("- Public: /, /login, /error");
                    System.out.println(
                            "- Public static: /styles/**, /scripts/**, /assets/**, /images/**, /img/**, /favicon.ico, /webjars/**");
                    System.out.println("- Public POST: /api/auth/login");
                    System.out.println("- All other requests: AUTHENTICATED");

                    auth
                            .requestMatchers("/", "/login", "/error").permitAll()

                            .requestMatchers("/styles/**", "/scripts/**", "/assets/**",
                                    "/images/**", "/img/**", "/favicon.ico",
                                    "/webjars/**", "/static/**", "/css/**", "/js/**")
                            .permitAll()

                            .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()

                            .anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();

        System.out.println("=== Security Filter Chain configured successfully ===");
        return chain;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}