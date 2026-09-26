package com.luxe.store.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = { "/", "/login", "/admin", "/vendedor", "/reportes" })
    public String forwardToAngular() {
        return "forward:/index.html";
    }
}
