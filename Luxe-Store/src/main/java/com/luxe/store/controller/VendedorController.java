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

    @Autowired private com.luxe.store.service.ArqueoCajaService arqueoCajaService;
    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VentaService ventaService;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private com.luxe.store.service.BoletaCorreoService boletaCorreoService;

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
        java.util.List<Venta> ventasHoy = (java.util.List<Venta>) model.getAttribute("misVentas");
        model.addAttribute("cajaOperaciones", ventasHoy.size());
        model.addAttribute("cajaTotal", ventasHoy.stream().mapToDouble(x -> x.getMontoTotal() == null ? 0 : x.getMontoTotal()).sum());
        model.addAttribute("cajaEfectivo", ventasHoy.stream().filter(x -> "Efectivo".equalsIgnoreCase(x.getMetodoPago())).mapToDouble(x -> x.getMontoTotal() == null ? 0 : x.getMontoTotal()).sum());
        model.addAttribute("cajaTarjeta", ventasHoy.stream().filter(x -> "Tarjeta".equalsIgnoreCase(x.getMetodoPago())).mapToDouble(x -> x.getMontoTotal() == null ? 0 : x.getMontoTotal()).sum());
        model.addAttribute("cajaDigital", ventasHoy.stream().filter(x -> !"Efectivo".equalsIgnoreCase(x.getMetodoPago()) && !"Tarjeta".equalsIgnoreCase(x.getMetodoPago())).mapToDouble(x -> x.getMontoTotal() == null ? 0 : x.getMontoTotal()).sum());
        model.addAttribute("nuevoCliente", new Cliente());
        model.addAttribute("arqueoMes",arqueoCajaService.resumen(usuarioLogueado.getId(),java.time.YearMonth.now(ZoneId.of("America/Lima"))));
        var arqueosHoy=arqueoCajaService.historial(usuarioLogueado.getId(),java.time.YearMonth.now(ZoneId.of("America/Lima"))).stream().filter(a->a.fecha.equals(com.luxe.store.service.ArqueoCajaService.hoy())).findFirst();
        model.addAttribute("arqueoHoy",arqueosHoy.orElse(null));
        model.addAttribute("efectivoCerrado",arqueoCajaService.efectivoCerrado(usuarioLogueado));

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

    @GetMapping("/clientes/datos/{id}")
    @ResponseBody
    public ResponseEntity<?> datosCliente(@PathVariable Long id, HttpSession session) {
        Usuario actual=(Usuario)session.getAttribute("usuarioLogueado");
        if(actual==null||!"VENDEDOR".equalsIgnoreCase(actual.getRol()))
            return ResponseEntity.status(403).body(java.util.Map.of("error","Acceso no autorizado"));
        return clienteService.buscarPorId(id).map(c->{
            java.util.Map<String,Object> datos=new java.util.LinkedHashMap<>();
            datos.put("id",c.getId());
            datos.put("tipoDocumento",c.getTipoDocumento()==null?"":c.getTipoDocumento());
            datos.put("numeroDocumento",c.getNumeroDocumento()==null?"":c.getNumeroDocumento());
            datos.put("nombres",c.getNombres()==null?"":c.getNombres());
            datos.put("correo",c.getCorreo()==null?"":c.getCorreo());
            datos.put("telefono",c.getTelefono()==null?"":c.getTelefono());
            return ResponseEntity.ok((Object)datos);
        }).orElseGet(()->ResponseEntity.status(404).body((Object)java.util.Map.of("error","Cliente no encontrado")));
    }
    @PostMapping("/clientes/editar/{id}")
    public String editarCliente(@PathVariable Long id,@RequestParam String tipoDocumento,@RequestParam String numeroDocumento,
       @RequestParam String nombres,@RequestParam String telefono,@RequestParam String correo,HttpSession session,RedirectAttributes ra){
      Usuario actual=(Usuario)session.getAttribute("usuarioLogueado");if(actual==null||!"VENDEDOR".equalsIgnoreCase(actual.getRol()))return "redirect:/";
      try{
        Cliente c=clienteService.buscarPorId(id).orElseThrow(()->new IllegalArgumentException("Cliente no encontrado."));
        if(!"Activo".equalsIgnoreCase(c.getEstado()))throw new IllegalArgumentException("No se puede editar un cliente inactivo.");
        c.setTipoDocumento(tipoDocumento);c.setNumeroDocumento(numeroDocumento);c.setNombres(nombres);c.setTelefono(telefono);c.setCorreo(correo);
        clienteService.guardar(c);ra.addFlashAttribute("mensaje","Datos del cliente actualizados.");
      }catch(IllegalArgumentException e){ra.addFlashAttribute("error",e.getMessage());}
      return "redirect:/vendedor#clientesVendedor";
    }
    @PostMapping("/arqueo/contar")
    public String contar(@RequestParam java.math.BigDecimal monto,HttpSession session,RedirectAttributes ra){
      Usuario u=(Usuario)session.getAttribute("usuarioLogueado");if(u==null||!"VENDEDOR".equalsIgnoreCase(u.getRol()))return "redirect:/";
      try{var a=arqueoCajaService.contar(u,monto);ra.addFlashAttribute("mensaje","Conteo registrado: "+a.estado+". Diferencia S/ "+a.diferencia);}
      catch(IllegalArgumentException e){ra.addFlashAttribute("error",e.getMessage());}
      return "redirect:/vendedor#resumenCaja";
    }
    @PostMapping("/arqueo/forzar")
    public String forzar(HttpSession session,RedirectAttributes ra){
      Usuario u=(Usuario)session.getAttribute("usuarioLogueado");if(u==null||!"VENDEDOR".equalsIgnoreCase(u.getRol()))return "redirect:/";
      try{arqueoCajaService.forzar(u);ra.addFlashAttribute("mensaje","Cuadre forzoso registrado. La diferencia queda visible para administración.");}
      catch(IllegalArgumentException e){ra.addFlashAttribute("error",e.getMessage());}
      return "redirect:/vendedor#resumenCaja";
    }
    @PostMapping("/arqueo/contar-ajax") @ResponseBody
    public ResponseEntity<?> contarAjax(@RequestParam java.math.BigDecimal monto,HttpSession session){
      Usuario u=(Usuario)session.getAttribute("usuarioLogueado");
      if(u==null||!"VENDEDOR".equalsIgnoreCase(u.getRol()))return ResponseEntity.status(403).body(java.util.Map.of("error","Acceso no autorizado"));
      try { var r=arqueoCajaService.contar(u,monto);return ResponseEntity.ok(java.util.Map.of("estado",r.estado,"esperado",r.esperado,"contado",r.contado,"diferencia",r.diferencia,"definitivo",r.definitivo)); }
      catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(java.util.Map.of("error",e.getMessage()));}
    }
    @PostMapping("/arqueo/forzar-ajax") @ResponseBody
    public ResponseEntity<?> forzarAjax(HttpSession session){
      Usuario u=(Usuario)session.getAttribute("usuarioLogueado");
      if(u==null||!"VENDEDOR".equalsIgnoreCase(u.getRol()))return ResponseEntity.status(403).body(java.util.Map.of("error","Acceso no autorizado"));
      try { var r=arqueoCajaService.forzar(u);return ResponseEntity.ok(java.util.Map.of("estado",r.estado,"esperado",r.esperado,"contado",r.contado,"diferencia",r.diferencia,"definitivo",true)); }
      catch(IllegalArgumentException e){return ResponseEntity.badRequest().body(java.util.Map.of("error",e.getMessage()));}
    }
    @GetMapping("/arqueo/historial") @ResponseBody
    public ResponseEntity<?> historial(@RequestParam(required=false) String mes,HttpSession session){
      Usuario u=(Usuario)session.getAttribute("usuarioLogueado");if(u==null||!"VENDEDOR".equalsIgnoreCase(u.getRol()))return ResponseEntity.status(403).build();
      try{return ResponseEntity.ok(arqueoCajaService.resumen(u.getId(),mes==null||mes.isBlank()?java.time.YearMonth.now(ZoneId.of("America/Lima")):java.time.YearMonth.parse(mes)));}
      catch(Exception e){return ResponseEntity.badRequest().body(java.util.Map.of("mensaje","Mes inválido"));}
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
        // Bloqueo obligatorio en servidor: el navegador no puede saltarse el cierre.
        // Se comprueba antes de registrar una nueva venta, preservando pagos digitales.
        if ("Efectivo".equalsIgnoreCase(metodoPago) && arqueoCajaService.efectivoCerrado(usuarioLogueado))
            return "ERROR: La caja de efectivo ya fue cerrada hoy. Puede utilizar tarjeta o pagos digitales.";
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
        if ("Efectivo".equalsIgnoreCase(metodoPago) && arqueoCajaService.efectivoCerrado(usuarioLogueado))
            return "ERROR: El arqueo de efectivo del día ya es definitivo. No se permiten nuevas ventas en efectivo.";
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
            boletaCorreoService.enviarDespuesDeVenta(venta);
            return "OK:" + venta.getNumeroTicket();
        } catch (IllegalArgumentException ex) {
            return "ERROR:" + ex.getMessage();
        }
    }
}
