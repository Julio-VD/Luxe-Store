package com.luxe.store.controller;

import com.luxe.store.dto.VentaDetalleDTO;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
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

    /**
     * Endpoint AJAX protegido: devuelve el detalle de una venta concreta para el modal.
     */
    @GetMapping("/ventas/{id}/detalle")
    @ResponseBody
    public ResponseEntity<?> obtenerDetalleVenta(@PathVariable Long id, HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null || !"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("mensaje", "No tiene permiso para consultar esta venta."));
        }

        if (ventaService.buscarPorId(id).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("mensaje", "La venta solicitada no existe."));
        }

        List<VentaDetalleDTO> detalle = ventaService.obtenerDetalleVenta(id);
        return ResponseEntity.ok(detalle);
    }

    @GetMapping
    public String dashboard(@RequestParam(value = "section", defaultValue = "dashboard") String section, HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null || !"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            return "redirect:/";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        java.util.Set<String> seccionesValidas = java.util.Set.of("dashboard", "productos", "stock", "ventas", "clientes", "seguridad", "vendedores");
        model.addAttribute("activeSection", seccionesValidas.contains(section) ? section : "dashboard");
        model.addAttribute("productos", productoService.obtenerTodos());
        model.addAttribute("categorias", productoService.obtenerTodos().stream().map(Producto::getCategoria).filter(java.util.Objects::nonNull).distinct().sorted().toList());
        model.addAttribute("clientes", clienteService.obtenerTodos());
        model.addAttribute("ventas", ventaService.obtenerTodas());
        model.addAttribute("usuarios", usuarioService.obtenerTodos());
        model.addAttribute("vendedorUsuario", usuarioService.obtenerVendedor().orElse(null));
        model.addAttribute("nuevoVendedor", new Usuario());

        model.addAttribute("totalVentasMonto", ventaService.obtenerTotalVentasMonto());
        model.addAttribute("totalProductos", productoService.obtenerTodos().size());
        model.addAttribute("stockBajoCount", productoService.obtenerTodos().stream().filter(p -> p.getStock() <= 5).count());

        model.addAttribute("nuevoProducto", new Producto());

        return "admin";
    }

    @PostMapping("/productos/guardar")
    public String guardarProducto(@ModelAttribute Producto producto, @RequestParam(value="imagenArchivo", required=false) MultipartFile imagenArchivo,
        @RequestParam(value="stockTallaS", required=false) Integer stockS,
        @RequestParam(value="stockTallaM", required=false) Integer stockM,
        @RequestParam(value="stockTallaL", required=false) Integer stockL,
        @RequestParam(value="stockTallaXL", required=false) Integer stockXL,
        RedirectAttributes redirectAttributes) {
        String base = producto.getCodigo() == null ? "" : producto.getCodigo().trim();
        if (base.isBlank() || !java.util.Set.of("Polos", "Camisas", "Poleras", "Casacas", "Chompas", "Sudaderas", "Vestidos", "Blusas", "Chalecos").contains(producto.getCategoria())) {
            redirectAttributes.addFlashAttribute("error", "Código o categoría de prenda inválidos."); return "redirect:/admin?section=productos";
        }
        producto.setCodigo(base);
        boolean conTallas = stockS != null || stockM != null || stockL != null || stockXL != null;
        java.util.List<String> codigos = new java.util.ArrayList<>();
        if (conTallas) for (String talla : new String[]{"S", "M", "L", "XL"}) codigos.add(base + "-" + talla);
        else codigos.add(base);
        for (String codigoNuevo : codigos) {
            if (productoService.buscarPorCodigo(codigoNuevo).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Código inválido: " + codigoNuevo + " ya está registrado. Utilice otro código.");
                return "redirect:/admin?section=productos";
            }
        }
        try {
            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                String original = imagenArchivo.getOriginalFilename() == null ? "imagen" : imagenArchivo.getOriginalFilename();
                String ext = original.lastIndexOf('.') >= 0 ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
                if (!java.util.Set.of(".jpg", ".jpeg", ".png", ".webp").contains(ext)) throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WEBP.");
                Path dir = Path.of("src/main/resources/static/uploads"); Files.createDirectories(dir);
                String filename = UUID.randomUUID() + ext;
                Files.copy(imagenArchivo.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                producto.setImagenUrl("/uploads/" + filename);
            }
        } catch (Exception ex) { redirectAttributes.addFlashAttribute("error", "No se pudo subir la imagen: " + ex.getMessage()); return "redirect:/admin?section=productos"; }
        boolean porTalla = stockS != null || stockM != null || stockL != null || stockXL != null;
        if (porTalla) {
            String[] tallas = {"S", "M", "L", "XL"};
            Integer[] cantidades = {stockS, stockM, stockL, stockXL};
            for (int i = 0; i < tallas.length; i++) {
                int cantidad = cantidades[i] == null ? 0 : cantidades[i];
                if (cantidad < 0) { redirectAttributes.addFlashAttribute("error", "El stock no puede ser negativo."); return "redirect:/admin?section=productos"; }
                Producto variante = new Producto();
                variante.setCodigo(producto.getCodigo() + "-" + tallas[i]);
                variante.setNombre(producto.getNombre()); variante.setCategoria(producto.getCategoria());
                variante.setColor(producto.getColor()); variante.setTalla(tallas[i]);
                variante.setPrecio(producto.getPrecio()); variante.setStock(cantidad);
                variante.setEstado(cantidad > 0 ? "Activo" : "Agotado"); variante.setImagenUrl(producto.getImagenUrl());
                productoService.guardar(variante);
            }
        } else {
            if (producto.getStock() == null || producto.getStock() < 0) {redirectAttributes.addFlashAttribute("error", "Ingrese un stock válido."); return "redirect:/admin?section=productos";}
            productoService.guardar(producto);
        }
        redirectAttributes.addFlashAttribute("mensaje", "Prenda registrada correctamente.");
        return "redirect:/admin?section=productos";
    }

    @PostMapping("/productos/editar")
    public String editarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes) {
        productoService.actualizar(producto);
        redirectAttributes.addFlashAttribute("mensaje", "Prenda actualizada correctamente.");
        return "redirect:/admin?section=productos";
    }

    @GetMapping("/productos/inhabilitar/{id}")
    public String inhabilitarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.inhabilitar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Producto inhabilitado correctamente.");
        return "redirect:/admin?section=productos";
    }

    @GetMapping("/productos/activar/{id}")
    public String activarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productoService.activar(id)) {
            redirectAttributes.addFlashAttribute("mensaje", "Producto activado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "No se puede activar el producto sin stock disponible.");
        }
        return "redirect:/admin?section=productos";
    }

    @PostMapping("/productos/aumentar-stock")
    public String aumentarStock(@RequestParam Long productoId, @RequestParam int cantidad, RedirectAttributes redirectAttributes) {
        if (productoService.aumentarStock(productoId, cantidad)) {
            redirectAttributes.addFlashAttribute("mensaje", "Stock aumentado correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("mensaje", "No se pudo aumentar el stock. Verifique la prenda y la cantidad.");
        }
        return "redirect:/admin?section=stock";
    }

    @PostMapping("/productos/stock/actualizar")
    public String actualizarStockConteo(@RequestParam Long productoId, @RequestParam int stockActualizado,
                                        @RequestParam(value="imagenArchivo", required=false) MultipartFile imagenArchivo,
                                        @RequestParam String codigo, @RequestParam String nombre, RedirectAttributes ra) {
        try {
            Producto p = productoService.buscarPorId(productoId).orElseThrow(() -> new IllegalArgumentException("Prenda no encontrada."));
            String nuevoCodigo = codigo.trim();
            if (nuevoCodigo.isEmpty() || productoService.buscarPorCodigo(nuevoCodigo).filter(otro -> !otro.getId().equals(productoId)).isPresent())
                throw new IllegalArgumentException("Código inválido: ya está registrado o está vacío.");
            if (imagenArchivo != null && !imagenArchivo.isEmpty()) {
                String original = imagenArchivo.getOriginalFilename() == null ? "" : imagenArchivo.getOriginalFilename();
                String ext = original.lastIndexOf('.') >= 0 ? original.substring(original.lastIndexOf('.')).toLowerCase() : "";
                if (!java.util.Set.of(".jpg", ".jpeg", ".png", ".webp").contains(ext)) throw new IllegalArgumentException("Imagen inválida. Use JPG, PNG o WEBP.");
                Path dir = Path.of("src/main/resources/static/uploads"); Files.createDirectories(dir);
                String archivo = UUID.randomUUID() + ext;
                Files.copy(imagenArchivo.getInputStream(), dir.resolve(archivo), StandardCopyOption.REPLACE_EXISTING);
                p.setImagenUrl("/uploads/" + archivo);
            }
            p.setCodigo(nuevoCodigo); p.setNombre(nombre.trim());
            if (!productoService.actualizar(p) || !productoService.actualizarStock(productoId, stockActualizado)) throw new IllegalArgumentException("No se pudo guardar la prenda.");
            ra.addFlashAttribute("mensaje", "Producto y conteo de stock actualizados correctamente.");
        } catch (Exception e) { ra.addFlashAttribute("error", "No se pudo guardar: " + e.getMessage()); }
        return "redirect:/admin?section=stock";
    }

    @PostMapping("/productos/stock/inhabilitar/{id}")
    public String inhabilitarDesdeStock(@PathVariable Long id, RedirectAttributes ra) {
        if (productoService.inhabilitar(id)) ra.addFlashAttribute("mensaje", "Prenda inhabilitada correctamente.");
        else ra.addFlashAttribute("error", "No se pudo inhabilitar la prenda.");
        return "redirect:/admin?section=stock";
    }

    @PostMapping("/clientes/estado/{id}")
    public String cambiarEstadoCliente(@PathVariable Long id, RedirectAttributes ra) {
        java.util.Optional<com.luxe.store.model.Cliente> cliente = clienteService.buscarPorId(id);
        if (cliente.isPresent()) {
            com.luxe.store.model.Cliente c = cliente.get();
            c.setEstado("Activo".equalsIgnoreCase(c.getEstado()) ? "Inactivo" : "Activo");
            clienteService.guardarEstado(c);
            ra.addFlashAttribute("mensaje", "Estado del cliente actualizado correctamente.");
        } else ra.addFlashAttribute("error", "No se encontró el cliente.");
        return "redirect:/admin?section=clientes";
    }

    @PostMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes ra) {
        if (clienteService.eliminar(id)) ra.addFlashAttribute("mensaje", "Cliente eliminado correctamente.");
        else ra.addFlashAttribute("error", "No se encontró el cliente.");
        return "redirect:/admin?section=clientes";
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productoService.eliminar(id);
        redirectAttributes.addFlashAttribute("mensaje", "Prenda eliminada del catálogo.");
        return "redirect:/admin?section=productos";
    }



    @PostMapping("/vendedores/guardar")
    public String guardarVendedor(@RequestParam String nombreCompleto, @RequestParam String usuario,
                                  @RequestParam String correoUsuario, @RequestParam String contrasenaTemporal,
                                  HttpSession session, RedirectAttributes ra) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"ADMIN".equalsIgnoreCase(actual.getRol())) return "redirect:/";
        try {
            usuarioService.crearVendedor(nombreCompleto, usuario, correoUsuario, contrasenaTemporal);
            ra.addFlashAttribute("mensaje", "Vendedor creado correctamente. Entregue la contraseña temporal de forma segura.");
        } catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin?section=vendedores";
    }

    @PostMapping("/vendedores/estado/{id}")
    public String cambiarEstadoVendedor(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"ADMIN".equalsIgnoreCase(actual.getRol())) return "redirect:/";
        try { usuarioService.cambiarEstadoVendedor(id); ra.addFlashAttribute("mensaje", "Estado del vendedor actualizado."); }
        catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/admin?section=vendedores";
    }

    @PostMapping("/contrasena")
    public String cambiarContrasena(@RequestParam String contrasenaActual,
                                    @RequestParam String nuevaContrasena,
                                    @RequestParam String confirmarContrasena,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"ADMIN".equalsIgnoreCase(actual.getRol())) {
            return "redirect:/";
        }
        if (!nuevaContrasena.equals(confirmarContrasena)) {
            redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden.");
            return "redirect:/admin?section=seguridad";
        }
        try {
            if (usuarioService.cambiarContrasenaAdmin(contrasenaActual, nuevaContrasena)) {
                redirectAttributes.addFlashAttribute("mensaje", "Contraseña del administrador actualizada.");
            } else {
                redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin?section=seguridad";
    }
}
