package com.luxe.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    // Mapeo exclusivo para Angular SPA en /spa para evitar conflicto ambiguo con Thymeleaf (Rúbrica Avance 2)
    @GetMapping("/spa")
    public String forwardToAngular() {
        return "forward:/index.html";
    }
}
