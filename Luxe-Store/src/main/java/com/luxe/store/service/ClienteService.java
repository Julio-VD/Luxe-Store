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
        validarCliente(cliente);
        cliente.setNumeroDocumento(cliente.getNumeroDocumento().trim());
        if (cliente.getTelefono() != null) cliente.setTelefono(cliente.getTelefono().trim());
        if (cliente.getCorreo() != null) cliente.setCorreo(cliente.getCorreo().trim().toLowerCase());
        if (cliente.getEstado() == null) cliente.setEstado("Activo");
        return clienteRepository.save(cliente);
    }

    private void validarCliente(Cliente c) {
        if (c.getTipoDocumento() == null || !(c.getTipoDocumento().equalsIgnoreCase("DNI") || c.getTipoDocumento().equalsIgnoreCase("RUC")))
            throw new IllegalArgumentException("Seleccione DNI o RUC.");
        int longitud = c.getTipoDocumento().equalsIgnoreCase("DNI") ? 8 : 11;
        if (c.getNumeroDocumento() == null || !c.getNumeroDocumento().trim().matches("\\d{" + longitud + "}"))
            throw new IllegalArgumentException("El " + c.getTipoDocumento().toUpperCase() + " debe contener exactamente " + longitud + " dígitos.");
        if (c.getNombres() == null || !c.getNombres().trim().matches("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]{2,100}"))
            throw new IllegalArgumentException("El nombre debe contener solo letras y espacios (2 a 100 caracteres).");
        if (c.getTelefono() == null || !c.getTelefono().trim().matches("9\\d{8}"))
            throw new IllegalArgumentException("El teléfono es obligatorio: debe tener 9 dígitos y empezar con 9.");
        if (c.getCorreo() == null || !c.getCorreo().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"))
            throw new IllegalArgumentException("El correo es obligatorio y debe tener un formato válido.");
        Optional<Cliente> existente = clienteRepository.findByNumeroDocumento(c.getNumeroDocumento().trim());
        if (existente.isPresent() && (c.getId() == null || !existente.get().getId().equals(c.getId())))
            throw new IllegalArgumentException("Ya existe un cliente con ese documento.");
    }

    public boolean actualizar(Cliente clienteActualizado) {
        if (clienteActualizado.getId() != null && clienteRepository.existsById(clienteActualizado.getId())) {
            clienteRepository.save(clienteActualizado);
            return true;
        }
        return false;
    }

    public Cliente guardarEstado(Cliente cliente) { return clienteRepository.save(cliente); }

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

    // Baja lógica para conservar las relaciones con ventas históricas.
    public boolean eliminar(Long id) {
        Optional<Cliente> opt = clienteRepository.findById(id);
        if (opt.isPresent()) {
            Cliente cliente = opt.get();
            cliente.setEstado("Inactivo");
            clienteRepository.save(cliente);
            return true;
        }
        return false;
    }
}
