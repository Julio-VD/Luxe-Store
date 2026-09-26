package com.luxe.store.controller;

import com.luxe.store.model.Usuario;
import com.luxe.store.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes")
public class ReportesController {

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public String verReportes(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("ventasPorCategoria", ventaService.obtenerVentasPorCategoria());
        model.addAttribute("ventasPorMes", ventaService.obtenerVentasPorMes());
        model.addAttribute("ventasPorMetodoPago", ventaService.obtenerVentasPorMetodoPago());

        return "reportes";
    }
}
