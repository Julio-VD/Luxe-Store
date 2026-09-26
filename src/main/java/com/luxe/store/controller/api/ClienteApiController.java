package com.luxe.store.controller.api;

import com.luxe.store.model.Cliente;
import com.luxe.store.service.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteApiController {

    @Autowired
    private ClienteService clienteService;

    @GetMapping
    public List<Cliente> listarTodos() {
        return clienteService.obtenerTodos();
    }

    @GetMapping("/activos")
    public List<Cliente> listarActivos() {
        return clienteService.obtenerActivos();
    }

    @GetMapping("/documento/{doc}")
    public Cliente buscarPorDocumento(@PathVariable String doc) {
        return clienteService.buscarPorDocumento(doc).orElse(null);
    }

    @PostMapping
    public Cliente guardar(@RequestBody Cliente cliente) {
        return clienteService.guardar(cliente);
    }

    @PutMapping("/{id}")
    public boolean actualizar(@PathVariable Long id, @RequestBody Cliente cliente) {
        cliente.setId(id);
        return clienteService.actualizar(cliente);
    }
}
