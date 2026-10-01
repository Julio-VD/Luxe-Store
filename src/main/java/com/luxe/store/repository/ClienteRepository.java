package com.luxe.store.repository;

import com.luxe.store.model.Cliente;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ClienteRepository {

    private final Map<Long, Cliente> data = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    public List<Cliente> findAll() {
        return new ArrayList<>(data.values());
    }

    public List<Cliente> findByEstado(String estado) {
        return data.values().stream()
                .filter(c -> estado != null && estado.equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());
    }

    public Optional<Cliente> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(data.get(id));
    }

    public Optional<Cliente> findByNumeroDocumento(String doc) {
        if (doc == null) return Optional.empty();
        return data.values().stream()
                .filter(c -> doc.trim().equalsIgnoreCase(c.getNumeroDocumento() != null ? c.getNumeroDocumento().trim() : ""))
                .findFirst();
    }

    public Cliente save(Cliente cliente) {
        if (cliente.getId() == null || cliente.getId() <= 0) {
            cliente.setId(idSeq.incrementAndGet());
        } else {
            idSeq.updateAndGet(curr -> Math.max(curr, cliente.getId()));
        }
        data.put(cliente.getId(), cliente);
        return cliente;
    }

    public boolean existsById(Long id) {
        return id != null && data.containsKey(id);
    }

    public void deleteById(Long id) {
        if (id != null) {
            data.remove(id);
        }
    }

    public long count() {
        return data.size();
    }
}
