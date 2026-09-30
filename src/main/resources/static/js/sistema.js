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
