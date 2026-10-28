package com.luxe.store.controller;

import com.luxe.store.model.Cliente;
import com.luxe.store.model.DetalleVenta;
import com.luxe.store.model.Producto;
import com.luxe.store.model.Usuario;
import com.luxe.store.model.Venta;
import com.luxe.store.service.ClienteService;
import com.luxe.store.service.ProductoService;
import com.luxe.store.service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

@Controller
@RequestMapping("/vendedor")
public class VendedorController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private VentaService ventaService;

    @GetMapping
    public String terminalVenta(HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null) {
            usuarioLogueado = new Usuario(2L, "Juan Pérez", "vendedor@luxe.pe", "vendedor", "1234", "VENDEDOR", "Activo");
            session.setAttribute("usuarioLogueado", usuarioLogueado);
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        model.addAttribute("productos", productoService.obtenerActivos());
        model.addAttribute("clientes", clienteService.obtenerActivos());
        model.addAttribute("misVentas", ventaService.obtenerTodas());
        model.addAttribute("nuevoCliente", new Cliente());

        return "vendedor";
    }

    @PostMapping("/clientes/guardar")
    public String guardarClienteFromPos(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        clienteService.guardar(cliente);
        redirectAttributes.addFlashAttribute("mensaje", "Cliente registrado exitosamente.");
        return "redirect:/vendedor";
    }

    @PostMapping("/ventas/procesar")
    @ResponseBody
    public String procesarVenta(@RequestParam(value = "clienteDocumento", required = false) String doc,
                                @RequestParam("metodoPago") String metodoPago,
                                @RequestParam("montoRecibido") Double montoRecibido,
                                @RequestParam("detallesStr") String detallesStr,
                                HttpSession session) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        String nombreVendedor = usuarioLogueado != null ? usuarioLogueado.getNombreCompleto() : "Cajero";

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

        venta.setMontoTotal(totalCalculado);
        venta.setVuelto(Math.max(0.0, montoRecibido - totalCalculado));

        ventaService.registrarVenta(venta);

        return "OK:" + venta.getNumeroTicket();
    }
}
