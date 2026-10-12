package univ_notice.backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(
            errors ->
                errors.authenticationEntryPoint(
                    (request, response, exception) -> {
                      response.setStatus(401);
                      response.setContentType("application/json;charset=UTF-8");
                      response.getWriter().write("{\"message\":\"로그인이 필요합니다.\"}");
                    }))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(HttpMethod.POST, "/auth/signup", "/auth/login")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/notice")
                    .permitAll() // Ingestion key is checked in the controller.
                    .requestMatchers(HttpMethod.GET, "/notice/user")
                    .authenticated()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/",
                        "/login",
                        "/signup",
                        "/mypage",
                        "/notice",
                        "/notice/search",
                        "/notice/departments",
                        "/css/**",
                        "/js/**",
                        "/img/**",
                        "/favicon.ico")
                    .permitAll()
                    .requestMatchers("/error")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
