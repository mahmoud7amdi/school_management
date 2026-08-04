package com.smartedu.school_management_api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;

    /**
     * Chain 1 — server-rendered pages and static assets.
     *
     * <p>These are shells with no data in them: every page fetches its content from
     * {@code /api/**} with a bearer token, and that call is what actually enforces
     * authorization. Serving the shell to an anonymous visitor leaks nothing, and the
     * client redirects to /login when the first API call comes back 401.
     */
    @Bean
    @org.springframework.core.annotation.Order(1)
    public SecurityFilterChain webChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/", "/login", "/about", "/dashboard/**", "/index.html", "/error",
                        "/assets/**", "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/uploads/**",
                        "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /** Chain 2 — the stateless JSON API. */
    @Bean
    @org.springframework.core.annotation.Order(2)
    public SecurityFilterChain apiChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/login", HttpMethod.POST.name())).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/logout", HttpMethod.POST.name())).permitAll()
                        // The About page is public to read. Editing it is guarded on the
                        // controller, so only the GET is opened here.
                        .requestMatchers(new AntPathRequestMatcher("/api/v1/about", HttpMethod.GET.name())).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
