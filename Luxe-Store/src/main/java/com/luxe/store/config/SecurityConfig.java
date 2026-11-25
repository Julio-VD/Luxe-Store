package com.luxe.store.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.multipart.support.MultipartFilter;
import jakarta.servlet.DispatcherType;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository) throws Exception {
        http.securityContext(context -> context.securityContextRepository(repository))
            .authorizeHttpRequests(auth -> auth
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                .requestMatchers("/", "/login", "/recuperar-admin", "/css/**", "/js/**", "/img/**", "/favicon.ico").permitAll()
                .requestMatchers("/admin", "/admin/**", "/reportes", "/reportes/**").hasRole("ADMIN")
                .requestMatchers("/vendedor", "/vendedor/**").hasRole("VENDEDOR")
                .anyRequest().authenticated())
            .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> response.sendRedirect(request.getContextPath() + "/")))
            .requestCache(cache -> cache.disable())
            .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").invalidateHttpSession(true).deleteCookies("JSESSIONID"));
        // CSRF remains enabled; existing login controller saves the authenticated context.
        return http.build();
    }

    // Resolve multipart forms before CSRF validation (product images and POS forms).
    @Bean
    public FilterRegistrationBean<MultipartFilter> securityMultipartFilter() {
        FilterRegistrationBean<MultipartFilter> registration = new FilterRegistrationBean<>(new MultipartFilter());
        registration.setOrder(-101);
        return registration;
    }
}
