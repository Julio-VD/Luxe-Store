package com.luxe.store.controller;

import com.luxe.store.model.Usuario;
import com.luxe.store.service.VentaService;
import com.luxe.store.service.ProductoService;
import com.luxe.store.model.Producto;
import com.luxe.store.model.Venta;
import com.luxe.store.model.DetalleVenta;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.LinkedHashMap;
import java.util.Map;
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
    @Autowired
    private ProductoService productoService;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    @GetMapping
    public String verReportes(@org.springframework.web.bind.annotation.RequestParam(value="periodo", defaultValue="mes") String periodo, HttpSession session, Model model) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuarioLogueado == null || !"ADMIN".equalsIgnoreCase(usuarioLogueado.getRol())) {
            return "redirect:/";
        }

        model.addAttribute("usuarioLogueado", usuarioLogueado);
        if (!java.util.Set.of("dia", "semana", "mes", "todo").contains(periodo)) periodo = "mes";
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Lima"));
        LocalDate inicio = switch (periodo) { case "dia" -> hoy; case "semana" -> hoy.with(java.time.DayOfWeek.MONDAY); case "mes" -> hoy.withDayOfMonth(1); default -> LocalDate.of(1900,1,1); };
        java.util.List<Venta> ventasFiltradas = ventaService.obtenerTodas().stream()
                .filter(v -> "Completado".equalsIgnoreCase(v.getEstado()))
                .filter(v -> { LocalDate fecha = parseFecha(v.getFecha()); return fecha != null && !fecha.isBefore(inicio) && !fecha.isAfter(hoy); })
                .toList();
        Map<String, Double> ventasCategoria = new LinkedHashMap<>();
        Map<String, Double> ventasPeriodo = new LinkedHashMap<>();
        Map<String, Integer> ventasPago = new LinkedHashMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        for (Venta v : ventasFiltradas) {
            String pago = v.getMetodoPago() == null ? "Sin especificar" : v.getMetodoPago();
            ventasPago.merge(pago, 1, Integer::sum);
            LocalDate fecha = parseFecha(v.getFecha());
            String etiqueta = periodo.equals("dia") ? fecha.format(DateTimeFormatter.ofPattern("dd/MM")) :
                    periodo.equals("semana") ? fecha.getDayOfWeek().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("es", "PE")) + " " + fecha.format(DateTimeFormatter.ofPattern("dd/MM")) :
                    fecha.format(DateTimeFormatter.ofPattern("MM/yyyy"));
            ventasPeriodo.merge(etiqueta, v.getMontoTotal() == null ? 0.0 : v.getMontoTotal(), Double::sum);
            if (v.getDetalles() != null) for (DetalleVenta d : v.getDetalles()) {
                String categoria = d.getProducto() != null && d.getProducto().getCategoria() != null ? d.getProducto().getCategoria() : "Sin categoría";
                ventasCategoria.merge(categoria, d.getSubtotal() == null ? 0.0 : d.getSubtotal(), Double::sum);
            }
        }
        Map<String, Integer> stockPorCategoria = new LinkedHashMap<>();
        for (Producto pr : productoService.obtenerTodos()) {
            String categoria = pr.getCategoria() == null || pr.getCategoria().isBlank() ? "Sin categoría" : pr.getCategoria();
            stockPorCategoria.merge(categoria, pr.getStock() == null ? 0 : pr.getStock(), Integer::sum);
        }
        model.addAttribute("periodoSeleccionado", periodo);
        model.addAttribute("cantidadVentasFiltradas", ventasFiltradas.size());
        model.addAttribute("montoVentasFiltradas", ventasFiltradas.stream().mapToDouble(v -> v.getMontoTotal() == null ? 0.0 : v.getMontoTotal()).sum());
        model.addAttribute("ventasPorCategoria", ventasCategoria);
        model.addAttribute("ventasPorMes", ventasPeriodo);
        model.addAttribute("ventasPorMetodoPago", ventasPago);
        model.addAttribute("stockPorCategoria", stockPorCategoria);
        model.addAttribute("categoriasLabels", ventasCategoria.isEmpty() ? java.util.List.of("Sin ventas en el período") : new java.util.ArrayList<>(ventasCategoria.keySet()));
        model.addAttribute("categoriasValores", ventasCategoria.isEmpty() ? java.util.List.of(0) : new java.util.ArrayList<>(ventasCategoria.values()));
        model.addAttribute("mesesLabels", ventasPeriodo.isEmpty() ? java.util.List.of("Sin ventas en el período") : new java.util.ArrayList<>(ventasPeriodo.keySet()));
        model.addAttribute("mesesValores", ventasPeriodo.isEmpty() ? java.util.List.of(0) : new java.util.ArrayList<>(ventasPeriodo.values()));
        model.addAttribute("pagosLabels", ventasPago.isEmpty() ? java.util.List.of("Sin ventas en el período") : new java.util.ArrayList<>(ventasPago.keySet()));
        model.addAttribute("pagosValores", ventasPago.isEmpty() ? java.util.List.of(0) : new java.util.ArrayList<>(ventasPago.values()));
        model.addAttribute("stockLabels", stockPorCategoria.isEmpty() ? java.util.List.of("Sin productos") : new java.util.ArrayList<>(stockPorCategoria.keySet()));
        model.addAttribute("stockValores", stockPorCategoria.isEmpty() ? java.util.List.of(0) : new java.util.ArrayList<>(stockPorCategoria.values()));
        return "reportes";
    }
    private LocalDate parseFecha(String valor) {
        if (valor == null || valor.isBlank()) return null;
        String[] patrones = {"dd/MM/yyyy HH:mm:ss", "dd/MM/yyyy HH:mm", "dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};
        for (String patron : patrones) {
            try { return LocalDate.parse(valor.length() > patron.length() ? valor.substring(0, patron.length()) : valor, DateTimeFormatter.ofPattern(patron)); }
            catch (Exception ignored) { }
        }
        return null;
    }
}
