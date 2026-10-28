package com.luxe.store.controller;

import com.luxe.store.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RecuperarAdminController {
    private final UsuarioService usuarioService;

    public RecuperarAdminController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/recuperar-admin")
    public String mostrarFormulario() {
        return "recuperar-admin";
    }

    @PostMapping("/recuperar-admin")
    public String restablecer(@RequestParam String token,
                              @RequestParam String nuevaContrasena,
                              @RequestParam String confirmarContrasena,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        String tokenConfigurado = System.getenv("ADMIN_RESET_TOKEN");
        if (tokenConfigurado == null || tokenConfigurado.length() < 24) {
            model.addAttribute("error", "La recuperación no está habilitada. Configure ADMIN_RESET_TOKEN en Render.");
            return "recuperar-admin";
        }
        if (!java.security.MessageDigest.isEqual(tokenConfigurado.getBytes(java.nio.charset.StandardCharsets.UTF_8), token.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            model.addAttribute("error", "El código de recuperación no es válido.");
            return "recuperar-admin";
        }
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            model.addAttribute("error", "Las contraseñas nuevas no coinciden.");
            return "recuperar-admin";
        }
        try {
            if (!usuarioService.restablecerContrasenaAdmin(nuevaContrasena)) {
                model.addAttribute("error", "No se encontró la cuenta admin con rol de administrador. Revise la tabla usuarios en Neon.");
                return "recuperar-admin";
            }
            session.invalidate();
            redirectAttributes.addFlashAttribute("mensaje", "Contraseña restablecida. Inicie sesión con usuario admin y la nueva contraseña.");
            return "redirect:/";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "recuperar-admin";
        }
    }
}
