package com.luxe.store.controller;

import com.luxe.store.model.Usuario;
import com.luxe.store.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import com.luxe.store.service.LoginAttemptService;
import java.util.List;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired private SecurityContextRepository securityContextRepository;
    @Autowired private LoginAttemptService loginAttempts;

    @GetMapping("/")
    public String index(HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado != null) {
            if ("ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
                return "redirect:/admin";
            } else {
                return "redirect:/vendedor";
            }
        }
        return "index";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "redirect:/";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam("usuario") String usuario,
                                @RequestParam("contrasena") String contrasena,
                                HttpSession session, HttpServletRequest request, HttpServletResponse response,
                                Model model) {
        String clave = usuario.trim().toLowerCase(Locale.ROOT);
        if (loginAttempts.bloqueado(clave)) {
            model.addAttribute("error", "Demasiados intentos. Intente nuevamente en 15 minutos.");
            return "index";
        }
        Optional<Usuario> userOpt = usuarioService.autenticar(usuario, contrasena);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            if (!"ADMIN".equalsIgnoreCase(user.getRol()) && !"VENDEDOR".equalsIgnoreCase(user.getRol())) {
                model.addAttribute("error", "Usuario o contraseña incorrectos.");
                return "index";
            }
            loginAttempts.exito(clave);
            request.changeSessionId();
            new HttpSessionCsrfTokenRepository().saveToken(null, request, response);
            var authentication = UsernamePasswordAuthenticationToken.authenticated(user.getUsuario(), null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + user.getRol().toUpperCase(Locale.ROOT))));
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            session.setAttribute("usuarioLogueado", user);

            if ("ADMIN".equalsIgnoreCase(user.getRol())) {
                return "redirect:/admin";
            } else {
                return "redirect:/vendedor";
            }
        } else {
            loginAttempts.fallo(clave);
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "index";
        }
    }

}
