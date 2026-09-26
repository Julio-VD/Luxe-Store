package com.luxe.store.controller;

import com.luxe.store.model.Producto;
import com.luxe.store.model.Usuario;
import com.luxe.store.service.ClienteService;
import com.luxe.store.service.ProductoService;
import com.luxe.store.service.UsuarioService;
import com.luxe.store.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VentaService ventaService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String dashboard(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            return "redirect:/";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("ventas", ventaService.obtenerTodas());
        model.addAttribute("usuarios", usuarioService.obtenerTodos());

        // Métricas para los cuadros del Dashboard
        model.addAttribute("totalVentasMonto", ventaService.obtenerTotalVentasMonto());
        model.addAttribute("totalProductos", productoService.obtenerTodos().size());
        model.addAttribute("stockBajoCount", productoService.obtenerTodos().stream().filter(p -> p.getStock() <= 5).count());

        model.addAttribute("nuevoProducto", new Producto());

        return "admin";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        productoService.guardar(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Prenda registrada correctamente.");
        return "redirect:/admin";
    }

    @PostMapping("/productos/editar")
    public String editarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        productoService.actualizar(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Prenda actualizada correctamente.");
        return "redirect:/admin";
    }

    @GetMapping("/productos/inhabilitar/{id}")
    public String inhabilitarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.inhabilitar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Estado del producto cambiado a Inactivo.");
        return "redirect:/admin";
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Prenda eliminada del catálogo.");
        return "redirect:/admin";
    }
}
