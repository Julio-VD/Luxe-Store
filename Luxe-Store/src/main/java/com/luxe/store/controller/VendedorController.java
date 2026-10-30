package com.luxe.store.controller;

import com.luxe.store.model.Cliente;
import com.luxe.store.model.DetalleVenta;
import com.luxe.store.model.Producto;
import com.luxe.store.model.Usuario;
import com.luxe.store.model.Venta;
import com.luxe.store.dto.VentaDetalleDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.List;
import com.luxe.store.service.ClienteService;
import com.luxe.store.service.ProductoService;
import com.luxe.store.service.VentaService;
import com.luxe.store.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VentaService ventaService;
    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String terminalVenta(@RequestParam(value = "seguridad", required = false) String seguridad, HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null || !"VENDEDOR".equalsIgnoreCase(usuarioLogueado.getRol())) {
            return "redirect:/";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("activeSection", seguridad != null ? "seguridadVendedor" : "terminal");
        java.util.List<Producto> activos = productoService.obtenerActivos();
        java.util.Map<String, Producto> grupos = new java.util.LinkedHashMap<>();
        for (Producto item : activos) {
            String base = item.getCodigo() == null ? "" : item.getCodigo().replaceFirst("(?i)-(S|M|L|XL)$", "");
            grupos.putIfAbsent(base, item);
        }
        model.addAttribute("productosAgrupados", grupos.values());
        model.addAttribute("variantesPos", activos.stream().map(item -> java.util.Map.of(
                "id", item.getId(), "codigo", item.getCodigo(), "nombre", item.getNombre(),
                "precio", item.getPrecio() == null ? 0 : item.getPrecio(),
                "stock", item.getStock() == null ? 0 : item.getStock(),
                "talla", item.getTalla() == null ? "Única" : item.getTalla(), "estado", item.getEstado())).toList());
        model.addAttribute("productos", activos);
        model.addAttribute("clientes", clienteService.obtenerActivos());
        String fechaHoy = LocalDate.now(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        model.addAttribute("misVentas", ventaService.obtenerTodas().stream()
                .filter(v -> v.getVendedor() != null && v.getVendedor().equalsIgnoreCase(usuarioLogueado.getNombreCompleto()))
                .filter(v -> v.getFecha() != null && v.getFecha().startsWith(fechaHoy))
                .toList());
        model.addAttribute("nuevoCliente", new Cliente());

        return "vendedor";
    }

    /** Detalle de una venta, visible solamente para el vendedor que la registró. */
    @GetMapping("/ventas/{id}/detalle")
    @ResponseBody
    public ResponseEntity<?> detalleVenta(@PathVariable Long id, HttpSession session) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"VENDEDOR".equalsIgnoreCase(actual.getRol())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("mensaje", "Sesión no válida."));
        }
        Optional<Venta> venta = ventaService.buscarPorId(id);
        if (venta.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of("mensaje", "Venta no encontrada."));
        if (venta.get().getVendedor() == null || !venta.get().getVendedor().equalsIgnoreCase(actual.getNombreCompleto())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(java.util.Map.of("mensaje", "No puede consultar ventas de otro vendedor."));
        }
        List<VentaDetalleDTO> detalles = ventaService.obtenerDetalleVenta(id);
        return ResponseEntity.ok(detalles);
    }

    @PostMapping("/clientes/guardar")
    public String guardarClienteFromPos(@ModelAttribute Cliente cliente, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"VENDEDOR".equalsIgnoreCase(actual.getRol())) return "redirect:/";
        try { clienteService.guardar(cliente); redirectAttributes.addFlashAttribute("mensaje", "Cliente registrado exitosamente."); }
        catch (IllegalArgumentException e) { redirectAttributes.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/vendedor";
    }

    @PostMapping("/contrasena")
    public String cambiarContrasena(@RequestParam String contrasenaActual, @RequestParam String nuevaContrasena,
                                    @RequestParam String confirmarContrasena, HttpSession session, RedirectAttributes ra) {
        Usuario actual = (Usuario) session.getAttribute("usuarioLogueado");
        if (actual == null || !"VENDEDOR".equalsIgnoreCase(actual.getRol())) return "redirect:/";
        if (!nuevaContrasena.equals(confirmarContrasena)) ra.addFlashAttribute("error", "Las contraseñas nuevas no coinciden.");
        else try {
            if (usuarioService.cambiarContrasenaUsuario(actual.getId(), contrasenaActual, nuevaContrasena)) ra.addFlashAttribute("mensaje", "Contraseña actualizada correctamente.");
            else ra.addFlashAttribute("error", "La contraseña actual es incorrecta.");
        } catch (IllegalArgumentException e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/vendedor?seguridad=1";
    }

    @PostMapping("/ventas/procesar")
    @ResponseBody
    public String procesarVenta(@RequestParam(value = "clienteDocumento", required = false) String doc,
                                @RequestParam("metodoPago") String metodoPago,
                                @RequestParam("montoRecibido") Double montoRecibido,
                                @RequestParam("detallesStr") String detallesStr,
                                @RequestParam(value = "solicitudId", required = false) String solicitudId,
                                HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null || !"VENDEDOR".equalsIgnoreCase(usuarioLogueado.getRol())) {
            return "ERROR: Sesión no válida";
        }
        // La misma solicitud devuelve siempre el ticket original, sin descontar stock otra vez.
        if (solicitudId == null || !solicitudId.matches("[a-fA-F0-9-]{36}")) {
            return "ERROR: Identificador de operación inválido. Actualice la página.";
        }
        synchronized (session) {
            @SuppressWarnings("unchecked")
            java.util.Map<String, String> procesadas = (java.util.Map<String, String>) session.getAttribute("ventasProcesadas");
            if (procesadas == null) {
                procesadas = new java.util.LinkedHashMap<>();
                session.setAttribute("ventasProcesadas", procesadas);
            }
            String previa = procesadas.get(solicitudId);
            if (previa != null) return previa;
            String resultado = ejecutarVenta(doc, metodoPago, montoRecibido, detallesStr, usuarioLogueado);
            if (resultado.startsWith("OK:")) procesadas.put(solicitudId, resultado);
            return resultado;
        }
    }

    private String ejecutarVenta(String doc, String metodoPago, Double montoRecibido,
                                String detallesStr, Usuario usuarioLogueado) {
        String nombreVendedor = usuarioLogueado.getNombreCompleto();
        if (montoRecibido == null || !Double.isFinite(montoRecibido) || montoRecibido < 0) return "ERROR: Monto inválido";

        Cliente cliente = null;
        if (doc != null && !doc.trim().isEmpty()) {
            Optional<Cliente> opt = clienteService.buscarPorDocumento(doc);
            if (opt.isPresent()) {
                cliente = opt.get();
            }
        }

        Venta venta = new Venta();
        venta.setFecha(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        venta.setCliente(cliente);
        venta.setVendedor(nombreVendedor);
        venta.setMetodoPago(metodoPago);
        venta.setMontoRecibido(montoRecibido);

        double totalCalculado = 0.0;

        if (detallesStr != null && !detallesStr.isEmpty()) {
            String[] items = detallesStr.split(";");
            for (String item : items) {
                if (item.trim().isEmpty()) continue;
                String[] parts = item.split(":");
                if (parts.length == 2) {
                    Long productoId = Long.parseLong(parts[0]);
                    int cantidad = Integer.parseInt(parts[1]);

                    Optional<Producto> pOpt = productoService.buscarPorId(productoId);
                    if (pOpt.isPresent()) {
                        Producto p = pOpt.get();
                        double subtotal = p.getPrecio() * cantidad;
                        totalCalculado += subtotal;

                        DetalleVenta dv = new DetalleVenta(null, p, cantidad, p.getPrecio(), subtotal);
                        venta.getDetalles().add(dv);
                    }
                }
            }
        }

        BigDecimal total = BigDecimal.valueOf(totalCalculado).setScale(2, RoundingMode.HALF_UP);
        BigDecimal recibido = BigDecimal.valueOf(montoRecibido).setScale(2, RoundingMode.HALF_UP);
        if ("Efectivo".equalsIgnoreCase(metodoPago) && recibido.compareTo(total) < 0)
            return "ERROR: El monto recibido es menor que el total.";
        if (!"Efectivo".equalsIgnoreCase(metodoPago)) recibido = total;
        venta.setMontoTotal(total.doubleValue());
        venta.setMontoRecibido(recibido.doubleValue());
        venta.setVuelto(recibido.subtract(total).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP).doubleValue());

        try {
            ventaService.registrarVenta(venta);
            return "OK:" + venta.getNumeroTicket();
        } catch (IllegalArgumentException ex) {
            return "ERROR:" + ex.getMessage();
        }
    }
}
