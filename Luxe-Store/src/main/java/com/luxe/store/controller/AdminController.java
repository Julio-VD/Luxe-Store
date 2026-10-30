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
    private com.luxe.store.repository.VentaRepository ventaRepository;

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

    /** Historial de compras asociado al ID del cliente; acceso exclusivo del administrador. */
    @GetMapping("/clientes/{id}/compras")
    @ResponseBody
    public ResponseEntity<?> historialComprasCliente(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("mensaje", "Acceso no autorizado."));
        }
        if (clienteService.obtenerTodos().stream().noneMatch(c -> id.equals(c.getId()))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(java.util.Map.of("mensaje", "Cliente no encontrado."));
        }
        java.util.List<java.util.Map<String, Object>> compras = ventaRepository
                .findByCliente_IdOrderByIdDesc(id).stream().map(v -> {
                    java.util.Map<String, Object> fila = new java.util.LinkedHashMap<>();
                    fila.put("id", v.getId());
                    fila.put("ticket", v.getNumeroTicket());
                    fila.put("fecha", v.getFecha());
                    fila.put("vendedor", v.getVendedor());
                    fila.put("metodoPago", v.getMetodoPago());
                    fila.put("total", v.getMontoTotal());
                    fila.put("estado", v.getEstado());
                    return fila;
                }).toList();
        return ResponseEntity.ok(compras);
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
        // Las prendas nuevas aparecen primero (ID descendente). Las editadas recientemente
        // se priorizan por código base durante la sesión, incluyendo todas sus tallas.
        java.util.List<Producto> productosOrdenados = new java.util.ArrayList<>(productoService.obtenerTodos());
        @SuppressWarnings("unchecked")
        java.util.List<String> basesRecientes = (java.util.List<String>) session.getAttribute("basesPrendasRecientes");
        java.util.Map<String, Integer> prioridades = new java.util.HashMap<>();
        if (basesRecientes != null) {
            for (int i = 0; i < basesRecientes.size(); i++) prioridades.put(basesRecientes.get(i), i);
        }
        productosOrdenados.sort(java.util.Comparator
            .comparingInt((Producto p) -> prioridades.getOrDefault(codigoBase(p.getCodigo()).toUpperCase(java.util.Locale.ROOT), Integer.MAX_VALUE))
            .thenComparing(Producto::getId, java.util.Comparator.nullsLast(java.util.Comparator.reverseOrder())));
        model.addAttribute("productos", productosOrdenados);
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
        @RequestParam(value="agregarTallas", required=false, defaultValue="false") boolean agregarTallas,
        @RequestParam(value="tallaOriginal", required=false) String tallaOriginal, RedirectAttributes redirectAttributes, HttpSession session) {
        String base = codigoBase(producto.getCodigo() == null ? "" : producto.getCodigo().trim());
        if (base.isBlank()) { redirectAttributes.addFlashAttribute("error", "Seleccione un código de prenda válido."); return "redirect:/admin?section=productos"; }
        String[] tallas = {"S", "M", "L", "XL"};
        Integer[] cantidades = {stockS, stockM, stockL, stockXL};
        java.util.List<Producto> existentes = productoService.obtenerTodos().stream()
                .filter(p -> codigoBase(p.getCodigo()).equalsIgnoreCase(base)).toList();
        if (agregarTallas && existentes.isEmpty()) { redirectAttributes.addFlashAttribute("error", "Seleccione una prenda existente."); return "redirect:/admin?section=productos"; }
        if (!agregarTallas && !existentes.isEmpty()) { redirectAttributes.addFlashAttribute("error", "Este código ya está registrado."); return "redirect:/admin?section=productos"; }
        Producto original = agregarTallas ? existentes.get(0) : producto;
        if (agregarTallas) {
            producto.setNombre(original.getNombre()); producto.setCategoria(original.getCategoria());
            producto.setColor(original.getColor()); producto.setPrecio(original.getPrecio());
            producto.setImagen(original.getImagen());
        }
        if (java.util.Arrays.stream(cantidades).anyMatch(n -> n != null && n < 0)) {
            redirectAttributes.addFlashAttribute("error", "El stock no puede ser negativo."); return "redirect:/admin?section=productos";
        }
        // Las prendas antiguas sin sufijo se asignan a una talla sin crear otro ID.
        java.util.List<Producto> sinTalla = existentes.stream().filter(x -> x.getCodigo().equalsIgnoreCase(base)).toList();
        if (agregarTallas && !sinTalla.isEmpty()) {
            if (tallaOriginal == null || !java.util.Set.of("S","M","L","XL").contains(tallaOriginal)) {
                redirectAttributes.addFlashAttribute("error", "Seleccione la talla original de la prenda sin sufijo."); return "redirect:/admin?section=productos";
            }
            if (existentes.stream().anyMatch(x -> x.getCodigo().equalsIgnoreCase(base+"-"+tallaOriginal))) {
                redirectAttributes.addFlashAttribute("error", "La talla original ya está registrada."); return "redirect:/admin?section=productos";
            }
            Producto antiguo = sinTalla.get(0);
            antiguo.setCodigo(base+"-"+tallaOriginal); antiguo.setTalla(tallaOriginal);
            productoService.guardar(antiguo);
        }
        int nuevas = 0;
        for (int i=0;i<tallas.length;i++) {
            String codigoCandidato = base + "-" + tallas[i];
            if (cantidades[i] != null && cantidades[i] > 0 && existentes.stream().noneMatch(x -> x.getCodigo().equalsIgnoreCase(codigoCandidato))) nuevas++;
        }
        if (nuevas == 0 && sinTalla.isEmpty()) { redirectAttributes.addFlashAttribute("error", "Ingrese stock mayor a cero en al menos una talla nueva."); return "redirect:/admin?section=productos"; }
        try {
            if (!agregarTallas && imagenArchivo != null && !imagenArchivo.isEmpty())
                producto.setImagen(com.luxe.store.service.ImagenJpgService.convertirAJpg(imagenArchivo));
            for (int i=0;i<tallas.length;i++) {
                if (cantidades[i] == null || cantidades[i] <= 0) continue;
                String nuevoCodigo = base + "-" + tallas[i];
                if (productoService.buscarPorCodigo(nuevoCodigo).isPresent()) continue;
                Producto variante = new Producto();
                variante.setCodigo(nuevoCodigo); variante.setTalla(tallas[i]);
                variante.setNombre(producto.getNombre()); variante.setCategoria(producto.getCategoria());
                variante.setColor(producto.getColor()); variante.setPrecio(producto.getPrecio());
                variante.setImagen(producto.getImagen());
                variante.setStock(cantidades[i]); variante.setEstado("Activo");
                productoService.guardar(variante);
            }
            marcarPrendaReciente(session, base);
            redirectAttributes.addFlashAttribute("mensaje", agregarTallas ? "Tallas añadidas correctamente." : "Prenda añadida correctamente.");
        } catch (Exception ex) { redirectAttributes.addFlashAttribute("error", "No se pudo guardar la prenda: " + ex.getMessage()); }
        return "redirect:/admin?section=productos";
    }

    @PostMapping("/productos/asignar-talla")
    public String asignarTalla(@RequestParam Long productoId, @RequestParam String talla, RedirectAttributes ra, HttpSession session) {
        if (!java.util.Set.of("S","M","L","XL").contains(talla)) { ra.addFlashAttribute("error","Talla inválida."); return "redirect:/admin?section=productos"; }
        java.util.Optional<Producto> opt = productoService.buscarPorId(productoId);
        if (opt.isEmpty()) { ra.addFlashAttribute("error","Prenda no encontrada."); return "redirect:/admin?section=productos"; }
        Producto prod = opt.get(); String base = codigoBase(prod.getCodigo());
        if (!prod.getCodigo().equalsIgnoreCase(base)) { ra.addFlashAttribute("error","Esta prenda ya tiene talla."); return "redirect:/admin?section=productos"; }
        if (productoService.buscarPorCodigo(base+"-"+talla).isPresent()) { ra.addFlashAttribute("error","La talla ya existe."); return "redirect:/admin?section=productos"; }
        prod.setCodigo(base+"-"+talla); prod.setTalla(talla); productoService.guardar(prod);
        marcarPrendaReciente(session,base);
        ra.addFlashAttribute("mensaje","Talla asignada correctamente; se conservaron el stock y las ventas anteriores.");
        return "redirect:/admin?section=productos";
    }

    @PostMapping("/productos/editar")
    public String editarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttributes, HttpSession session) {
        productoService.actualizar(producto);
        marcarPrendaReciente(session, producto.getCodigo());
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

    private void marcarPrendaReciente(HttpSession session, String codigo) {
        if (codigo == null || codigo.isBlank()) return;
        String base = codigoBase(codigo).toUpperCase(java.util.Locale.ROOT);
        @SuppressWarnings("unchecked")
        java.util.List<String> anteriores = (java.util.List<String>) session.getAttribute("basesPrendasRecientes");
        java.util.List<String> recientes = anteriores == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(anteriores);
        recientes.remove(base);
        recientes.add(0, base);
        if (recientes.size() > 100) recientes = new java.util.ArrayList<>(recientes.subList(0, 100));
        session.setAttribute("basesPrendasRecientes", recientes);
    }

    private static String codigoBase(String codigo) {
        return codigo == null ? "" : codigo.replaceFirst("(?i)-(S|M|L|XL)$", "");
    }

    @GetMapping("/productos/siguiente-codigo")
    @ResponseBody
    public ResponseEntity<?> siguienteCodigo(@RequestParam String categoria, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || !"ADMIN".equalsIgnoreCase(usuario.getRol())) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        java.util.Map<String, String> prefijos = java.util.Map.of(
            "Polos", "PL", "Camisas", "CM", "Poleras", "PO", "Casacas", "CS",
            "Chompas", "CH", "Sudaderas", "SU", "Vestidos", "VE", "Blusas", "BL", "Chalecos", "CL", "Pantalones", "PA");
        String prefijo = prefijos.get(categoria);
        if (prefijo == null) return ResponseEntity.badRequest().body(java.util.Map.of("error", "Categoría inválida"));
        java.util.Set<String> usados = new java.util.HashSet<>();
        for (Producto producto : productoService.obtenerTodos()) usados.add(codigoBase(producto.getCodigo()).toUpperCase());
        int numero = 1;
        while (usados.contains(String.format("%s%03d", prefijo, numero))) numero++;
        return ResponseEntity.ok(java.util.Map.of("codigo", String.format("%s%03d", prefijo, numero)));
    }

    @PostMapping("/productos/stock/actualizar")
    public String actualizarStockConteo(@RequestParam Long productoId, @RequestParam int stockActualizado,
                                        @RequestParam(value="imagenArchivo", required=false) MultipartFile imagenArchivo,
                                        @RequestParam String nombre, @RequestParam String color, RedirectAttributes ra, HttpSession session) {
        try {
            if (stockActualizado < 0 || nombre.isBlank() || color.isBlank()) throw new IllegalArgumentException("Nombre o stock inválido.");
            Producto seleccionado = productoService.buscarPorId(productoId).orElseThrow(() -> new IllegalArgumentException("Prenda no encontrada."));
            byte[] nuevaImagen = imagenArchivo != null && !imagenArchivo.isEmpty()
                ? com.luxe.store.service.ImagenJpgService.convertirAJpg(imagenArchivo) : null;
            String base = codigoBase(seleccionado.getCodigo());
            for (Producto variante : productoService.obtenerTodos()) {
                if (!codigoBase(variante.getCodigo()).equalsIgnoreCase(base)) continue;
                variante.setNombre(nombre.trim());
                variante.setColor(color.trim());
                if (nuevaImagen != null) variante.setImagen(nuevaImagen);
                if (!productoService.actualizar(variante)) throw new IllegalArgumentException("No se pudo actualizar la prenda.");
            }
            if (!productoService.actualizarStock(productoId, stockActualizado)) throw new IllegalArgumentException("No se pudo actualizar el stock.");
            marcarPrendaReciente(session, seleccionado.getCodigo());
            ra.addFlashAttribute("mensaje", "Nombre, color e imagen sincronizados entre tallas; stock actualizado solo en la talla seleccionada.");
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
        if (clienteService.eliminar(id)) ra.addFlashAttribute("mensaje", "Cliente retirado del listado. Su historial de ventas se conserva.");
        else ra.addFlashAttribute("error", "No se encontró el cliente.");
        return "redirect:/admin?section=clientes";
    }

    @GetMapping("/productos/eliminar/{id}")
    public String eliminarProducto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (productoService.eliminar(id)) {
                redirectAttributes.addFlashAttribute("mensaje", "Prenda eliminada del catálogo.");
            } else {
                redirectAttributes.addFlashAttribute("error", "La prenda ya no existe.");
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            productoService.inhabilitar(id);
            redirectAttributes.addFlashAttribute("error", "La prenda tiene ventas registradas. Se inhabilitó para conservar los tickets y el historial.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la prenda. Compruebe sus relaciones con otras operaciones.");
        }
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
