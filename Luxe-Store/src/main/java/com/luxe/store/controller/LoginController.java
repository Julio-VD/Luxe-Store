package com.luxe.store.controller;

import com.luxe.store.model.Usuario;
import com.luxe.store.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
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
                                HttpSession session,
                                Model model) {
        Optional<Usuario> userOpt = usuarioService.autenticar(usuario, contrasena);

        if (userOpt.isPresent()) {
            Usuario user = userOpt.get();
            session.setAttribute("usuarioLogueado", user);

            if ("ADMIN".equalsIgnoreCase(user.getRol())) {
                return "redirect:/admin";
            } else {
                return "redirect:/vendedor";
            }
        } else {
            model.addAttribute("error", "Usuario o contraseña incorrectos.");
            return "index";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
