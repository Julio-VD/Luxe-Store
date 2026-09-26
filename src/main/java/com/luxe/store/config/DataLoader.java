package com.luxe.store.config;

import com.luxe.store.model.*;
import com.luxe.store.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    public CommandLineRunner initDatabase(ProductoRepository productoRepo,
                                          ClienteRepository clienteRepo,
                                          VentaRepository ventaRepo,
                                          UsuarioRepository usuarioRepo) {
        return args -> {
            // Seed Productos
            if (productoRepo.count() == 0) {
                productoRepo.save(new Producto(null, "PL001", "Polo Algodón Premium", "Polos", "M", "Negro", 45.00, 25, "Activo"));
                productoRepo.save(new Producto(null, "CM002", "Camisa Oxford Slim", "Camisas", "L", "Azul", 75.00, 12, "Activo"));
                productoRepo.save(new Producto(null, "PA003", "Pantalón Denim Clásico", "Pantalones", "32", "Azul Oscuro", 95.00, 4, "Activo"));
                productoRepo.save(new Producto(null, "PL004", "Polo Oversize Luxe", "Polos", "L", "Blanco", 55.00, 18, "Activo"));
                productoRepo.save(new Producto(null, "CM005", "Camisa Casual Manga Larga", "Camisas", "M", "Verde", 69.90, 8, "Activo"));
            }

            // Seed Clientes
            if (clienteRepo.count() == 0) {
                clienteRepo.save(new Cliente(null, "DNI", "74829103", "Carlos Mendoza Ruiz", "987654321", "carlos.mendoza@gmail.com", "Activo"));
                clienteRepo.save(new Cliente(null, "DNI", "45910283", "María López Torres", "912345678", "maria.lopez@hotmail.com", "Activo"));
                clienteRepo.save(new Cliente(null, "RUC", "20601234567", "Inversiones Textil S.A.C.", "014258900", "contacto@textil.pe", "Activo"));
            }

            // Seed Usuarios
            if (usuarioRepo.count() == 0) {
                usuarioRepo.save(new Usuario(null, "Administrador Principal", "admin@luxe.pe", "admin", "1234", "ADMIN", "Activo"));
                usuarioRepo.save(new Usuario(null, "Juan Pérez", "vendedor@luxe.pe", "vendedor", "1234", "VENDEDOR", "Activo"));
            }

            // Seed Ventas Iniciales
            if (ventaRepo.count() == 0) {
                Cliente c1 = clienteRepo.findByNumeroDocumento("74829103").orElse(null);
                Producto p1 = productoRepo.findByCodigo("PL001").orElse(null);
                Producto p2 = productoRepo.findByCodigo("CM002").orElse(null);

                if (p1 != null && p2 != null) {
                    List<DetalleVenta> dv = new ArrayList<>();
                    dv.add(new DetalleVenta(null, p1, 2, p1.getPrecio(), p1.getPrecio() * 2));
                    dv.add(new DetalleVenta(null, p2, 1, p2.getPrecio(), p2.getPrecio() * 1));

                    Venta v = new Venta(null, "#TLX-00245", "03/09/2026", c1, "Juan Pérez", dv, "Yape/Plin", 165.00, 165.00, 0.0, "Completado");
                    ventaRepo.save(v);
                }
            }
        };
    }
}
