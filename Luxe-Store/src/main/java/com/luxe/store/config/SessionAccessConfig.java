package com.luxe.store.config;

import com.luxe.store.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SessionAccessConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
                    throws Exception {
                String path = request.getRequestURI();
                String requiredRole = path.startsWith("/admin") || path.startsWith("/reportes")
                        ? "ADMIN" : path.startsWith("/vendedor") ? "VENDEDOR" : null;
                if (requiredRole == null) return true;

                HttpSession session = request.getSession(false);
                Usuario user = session == null ? null : (Usuario) session.getAttribute("usuarioLogueado");
                if (user == null) {
                    response.sendRedirect("/");
                    return false;
                }
                if (!requiredRole.equalsIgnoreCase(user.getRol())) {
                    response.sendRedirect("ADMIN".equalsIgnoreCase(user.getRol()) ? "/admin" : "/vendedor");
                    return false;
                }
                return true;
            }
        }).addPathPatterns("/admin/**", "/vendedor/**", "/reportes/**");
    }
}
