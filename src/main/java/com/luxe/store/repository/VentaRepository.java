package com.luxe.store.repository;

import com.luxe.store.model.Venta;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class VentaRepository {

    private final Map<Long, Venta> data = new ConcurrentHashMap<>();
    private final AtomicLong idSeq = new AtomicLong(0);

    public List<Venta> findAll() {
        return new ArrayList<>(data.values());
    }

    public Optional<Venta> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(data.get(id));
    }

    public Optional<Venta> findByNumeroTicket(String ticket) {
        if (ticket == null) return Optional.empty();
        return data.values().stream()
                .filter(v -> ticket.equalsIgnoreCase(v.getNumeroTicket()))
                .findFirst();
    }

    public Venta save(Venta venta) {
        if (venta.getId() == null || venta.getId() <= 0) {
            venta.setId(idSeq.incrementAndGet());
        } else {
            idSeq.updateAndGet(curr -> Math.max(curr, venta.getId()));
        }
        data.put(venta.getId(), venta);
        return venta;
    }

    public long count() {
        return data.size();
    }
}
