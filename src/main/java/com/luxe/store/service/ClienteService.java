package com.luxe.store.service;

import com.luxe.store.model.Cliente;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ClienteService {

    private final List<Cliente> clientes = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(4);

    public ClienteService() {
        // Clientes iniciales de prueba
        clientes.add(new Cliente(1L, "DNI", "74829103", "Carlos Mendoza Ruiz", "987654321", "carlos.mendoza@gmail.com", "Activo"));
        clientes.add(new Cliente(2L, "DNI", "45910283", "María López Torres", "912345678", "maria.lopez@hotmail.com", "Activo"));
        clientes.add(new Cliente(3L, "RUC", "20601234567", "Inversiones Textil S.A.C.", "014258900", "contacto@textil.pe", "Activo"));
    }

    public List<Cliente> obtenerTodos() {
        return new ArrayList<>(clientes);
    }

    public List<Cliente> obtenerActivos() {
        return clientes.stream()
                .filter(c -> "Activo".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clientes.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    public Optional<Cliente> buscarPorDocumento(String doc) {
        if (doc == null) return Optional.empty();
        return clientes.stream()
                .filter(c -> c.getNumeroDocumento().equalsIgnoreCase(doc.trim()))
                .findFirst();
    }

    public List<Cliente> buscarPorTermino(String termino) {
        if (termino == null || termino.trim().isEmpty()) {
            return obtenerTodos();
        }
        String q = termino.toLowerCase();
        return clientes.stream()
                .filter(c -> c.getNombres().toLowerCase().contains(q) ||
                             c.getNumeroDocumento().contains(q))
                .collect(Collectors.toList());
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getId() == null) {
            cliente.setId(idGenerator.getAndIncrement());
            if (cliente.getEstado() == null) {
                cliente.setEstado("Activo");
            }
            clientes.add(cliente);
        } else {
            actualizar(cliente);
        }
        return cliente;
    }

    public boolean actualizar(Cliente clienteActualizado) {
        Optional<Cliente> opt = buscarPorId(clienteActualizado.getId());
        if (opt.isPresent()) {
            Cliente c = opt.get();
            c.setTipoDocumento(clienteActualizado.getTipoDocumento());
            c.setNumeroDocumento(clienteActualizado.getNumeroDocumento());
            c.setNombres(clienteActualizado.getNombres());
            c.setTelefono(clienteActualizado.getTelefono());
            c.setCorreo(clienteActualizado.getCorreo());
            if (clienteActualizado.getEstado() != null) {
                c.setEstado(clienteActualizado.getEstado());
            }
            return true;
        }
        return false;
    }

    public boolean inhabilitar(Long id) {
        Optional<Cliente> opt = buscarPorId(id);
        if (opt.isPresent()) {
            opt.get().setEstado("Inactivo");
            return true;
        }
        return false;
    }

    public boolean eliminar(Long id) {
        return clientes.removeIf(c -> c.getId().equals(id));
    }
}
