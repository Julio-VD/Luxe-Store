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
            usuarioLogueado = new Usuario(1L, "Administrador Principal", "admin@luxe.pe", "admin", "1234", "ADMIN", "Activo");
            session.setAttribute("usuarioLogueado", usuarioLogueado);
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("ventas", ventaService.obtenerTodas());
        model.addAttribute("usuarios", usuarioService.obtenerTodos());

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
        java.util.Optional<Producto> opt = productoService.buscarPorId(id);
        if (opt.isPresent()) {
            boolean iraInactivo = !"Inactivo".equalsIgnoreCase(opt.get().getEstado());
            productoService.inhabilitar(id);
            if (iraInactivo) {
                redirectAttributes.addFlashAttribute("mensaje", "Prenda inhabilitada (desactivada de las ventas).");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Prenda reactivada en el catálogo.");
            }
        }
        return "redirect:/admin";
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Prenda eliminada del catálogo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la prenda porque está vinculada al historial de ventas. Puede inhabilitarla para darla de baja sin romper el historial.");
        }
        return "redirect:/admin";
    }
}
