// =========================================================
// LUXE STORE - GESTOR DE NAVEGACIÓN Y ALMACENAMIENTO LOCAL
// =========================================================

function mostrarSeccion(id, boton) {
    const secciones = document.querySelectorAll(".seccion-sistema");
    secciones.forEach(function(seccion) {
        seccion.classList.remove("activa");
    });
    const seleccion = document.getElementById(id);
    if (seleccion) {
        seleccion.classList.add("activa");
    }
    const botones = document.querySelectorAll(".menu-item");
    botones.forEach(function(item) {
        item.classList.remove("activo");
    });
    if (boton) {
        boton.classList.add("activo");
    }
    cambiarTitulo(id);
}

function cambiarTitulo(id) {
    const titulo = document.getElementById("tituloSeccion");
    if (!titulo) return;
    const nombres = {
        dashboard: "Dashboard",
        productos: "Catálogo de Prendas",
        stock: "Control de Stock",
        ventas: "Historial de Ventas",
        clientes: "Gestión de Clientes",
        terminal: "Terminal de Venta",
        misVentas: "Mis Ventas"
    };
    titulo.textContent = nombres[id] || "LUXE";
}

function buscarProductos() {
    const texto = document.getElementById("buscarProducto").value.toLowerCase();
    const filas = document.querySelectorAll("#tablaProductos tbody tr");
    filas.forEach(function(fila) {
        const contenido = fila.textContent.toLowerCase();
        fila.style.display = contenido.includes(texto) ? "" : "none";
    });
}

// =========================================================
// MÓDULO DE ALMACENAMIENTO LOCAL (LocalStorage)
// Guarda y sincroniza prendas, clientes, ventas y tickets en el navegador
// =========================================================
const LuxeStorage = {
    KEYS: {
        PRODUCTOS: 'luxe_productos',
        CLIENTES: 'luxe_clientes',
        VENTAS: 'luxe_ventas',
        CARRITO: 'luxe_pos_carrito'
    },

    // Sincroniza datos iniciales del backend Spring Boot hacia LocalStorage
    sincronizarConServidor: function(productos, clientes, ventas) {
        try {
            if (productos && Array.isArray(productos) && productos.length > 0) {
                localStorage.setItem(this.KEYS.PRODUCTOS, JSON.stringify(productos));
            }
            if (clientes && Array.isArray(clientes) && clientes.length > 0) {
                localStorage.setItem(this.KEYS.CLIENTES, JSON.stringify(clientes));
            }
            if (ventas && Array.isArray(ventas) && ventas.length > 0) {
                localStorage.setItem(this.KEYS.VENTAS, JSON.stringify(ventas));
            }
            console.log("[LuxeStorage] Sincronización exitosa con almacenamiento local (LocalStorage).");
        } catch (e) {
            console.warn("[LuxeStorage] Error sincronizando con LocalStorage:", e);
        }
    },

    // PRODUCTOS
    getProductos: function() {
        try {
            const data = localStorage.getItem(this.KEYS.PRODUCTOS);
            return data ? JSON.parse(data) : [];
        } catch (e) {
            return [];
        }
    },

    guardarProducto: function(producto) {
        try {
            let lista = this.getProductos();
            const index = lista.findIndex(p => p.id === producto.id || (p.codigo && p.codigo === producto.codigo));
            if (index >= 0) {
                lista[index] = { ...lista[index], ...producto };
            } else {
                if (!producto.id) producto.id = Date.now();
                lista.push(producto);
            }
            localStorage.setItem(this.KEYS.PRODUCTOS, JSON.stringify(lista));
            return lista;
        } catch (e) {
            console.warn("[LuxeStorage] Error guardando producto en LocalStorage:", e);
        }
    },

    eliminarProducto: function(id) {
        try {
            let lista = this.getProductos().filter(p => p.id != id);
            localStorage.setItem(this.KEYS.PRODUCTOS, JSON.stringify(lista));
            return lista;
        } catch (e) {
            console.warn("[LuxeStorage] Error eliminando producto:", e);
        }
    },

    actualizarStock: function(id, cantidad) {
        try {
            let lista = this.getProductos();
            const item = lista.find(p => p.id == id);
            if (item) {
                item.stock = (item.stock || 0) + cantidad;
                localStorage.setItem(this.KEYS.PRODUCTOS, JSON.stringify(lista));
            }
            return lista;
        } catch (e) {
            console.warn("[LuxeStorage] Error actualizando stock:", e);
        }
    },

    // CLIENTES
    getClientES: function() {
        try {
            const data = localStorage.getItem(this.KEYS.CLIENTES);
            return data ? JSON.parse(data) : [];
        } catch (e) {
            return [];
        }
    },

    guardarCliente: function(cliente) {
        try {
            let lista = this.getClientES();
            const index = lista.findIndex(c => c.numeroDocumento === cliente.numeroDocumento);
            if (index >= 0) {
                lista[index] = { ...lista[index], ...cliente };
            } else {
                if (!cliente.id) cliente.id = Date.now();
                lista.push(cliente);
            }
            localStorage.setItem(this.KEYS.CLIENTES, JSON.stringify(lista));
            return lista;
        } catch (e) {
            console.warn("[LuxeStorage] Error guardando cliente:", e);
        }
    },

    // VENTAS
    getVentas: function() {
        try {
            const data = localStorage.getItem(this.KEYS.VENTAS);
            return data ? JSON.parse(data) : [];
        } catch (e) {
            return [];
        }
    },

    guardarVenta: function(venta) {
        try {
            let lista = this.getVentas();
            lista.unshift(venta);
            localStorage.setItem(this.KEYS.VENTAS, JSON.stringify(lista));
            return lista;
        } catch (e) {
            console.warn("[LuxeStorage] Error guardando venta:", e);
        }
    },

    // CARRITO POS
    getCarrito: function() {
        try {
            const data = localStorage.getItem(this.KEYS.CARRITO);
            return data ? JSON.parse(data) : [];
        } catch (e) {
            return [];
        }
    },

    guardarCarrito: function(carrito) {
        try {
            localStorage.setItem(this.KEYS.CARRITO, JSON.stringify(carrito));
        } catch (e) {
            console.warn("[LuxeStorage] Error guardando carrito:", e);
        }
    },

    limpiarCarrito: function() {
        try {
            localStorage.removeItem(this.KEYS.CARRITO);
        } catch (e) {
            console.warn("[LuxeStorage] Error limpiando carrito:", e);
        }
    }
};
