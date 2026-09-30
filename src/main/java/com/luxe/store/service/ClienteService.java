package com.luxe.store.service;

import com.luxe.store.model.Cliente;
import com.luxe.store.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public List<Cliente> obtenerActivos() {
        return clienteRepository.findByEstado("Activo");
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Optional<Cliente> buscarPorDocumento(String doc) {
        if (doc == null) return Optional.empty();
        return clienteRepository.findByNumeroDocumento(doc.trim());
    }

    public Cliente guardar(Cliente cliente) {
        if (cliente.getEstado() == null) {
            cliente.setEstado("Activo");
        }
        return clienteRepository.save(cliente);
    }

    public boolean actualizar(Cliente clienteActualizado) {
        if (clienteActualizado.getId() != null && clienteRepository.existsById(clienteActualizado.getId())) {
            clienteRepository.save(clienteActualizado);
            return true;
        }
        return false;
    }

    public boolean inhabilitar(Long id) {
        Optional<Cliente> opt = clienteRepository.findById(id);
        if (opt.isPresent()) {
            Cliente c = opt.get();
            c.setEstado("Inactivo");
            clienteRepository.save(c);
            return true;
        }
        return false;
    }

    public boolean eliminar(Long id) {
        if (clienteRepository.existsById(id)) {
            clienteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
