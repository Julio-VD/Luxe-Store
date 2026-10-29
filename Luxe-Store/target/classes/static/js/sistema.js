// =========================================================
// LUXE STORE - FUNCIONES DE NAVEGACIÓN Y VISTA (THYMELEAF)
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
    document.querySelectorAll(".menu-item[data-section]").forEach(function(item) {
        item.classList.toggle("activo", item.getAttribute("data-section") === id);
    });
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
        misVentas: "Mis Ventas",
        seguridad: "Seguridad de la cuenta",
        vendedores: "Administrar vendedores",
        seguridadVendedor: "Seguridad de la cuenta"
    };
    titulo.textContent = nombres[id] || "LUXE";
}

document.addEventListener("DOMContentLoaded", function() {
    const seccionInicial = document.body.getAttribute("data-active-section");
    if (seccionInicial && document.getElementById(seccionInicial)) {
        mostrarSeccion(seccionInicial, document.querySelector('.menu-item[data-section="' + seccionInicial + '"]'));
    }
});

function buscarProductos() {
    const texto = document.getElementById("buscarProducto").value.toLowerCase();
    const filas = document.querySelectorAll("#tablaProductos tbody tr");
    filas.forEach(function(fila) {
        const contenido = fila.textContent.toLowerCase();
        fila.style.display = contenido.includes(texto) ? "" : "none";
    });
}


// =========================================================
// HISTORIAL DE VENTAS: CARGA DEL DETALLE EN MODAL (DTO)
// =========================================================
document.addEventListener("DOMContentLoaded", function () {
    const modalElemento = document.getElementById("modalDetalleVenta");
    if (!modalElemento || !window.bootstrap) return;

    const modalDetalle = bootstrap.Modal.getOrCreateInstance(modalElemento);
    const cargando = document.getElementById("detalleVentaCargando");
    const error = document.getElementById("detalleVentaError");
    const contenido = document.getElementById("detalleVentaContenido");
    const tbody = document.getElementById("detalleVentaProductos");

    const mostrar = (elemento, visible) => elemento.classList.toggle("d-none", !visible);
    const texto = (valor, alternativo = "-") => valor === null || valor === undefined || valor === "" ? alternativo : String(valor);
    const moneda = (valor) => new Intl.NumberFormat("es-PE", { style: "currency", currency: "PEN" }).format(Number(valor || 0));

    function agregarCelda(fila, valor, clases = "") {
        const celda = document.createElement("td");
        celda.textContent = texto(valor);
        if (clases) celda.className = clases;
        fila.appendChild(celda);
    }

    document.querySelectorAll(".btn-ver-detalle-venta").forEach(function (boton) {
        boton.addEventListener("click", async function () {
            const url = boton.dataset.url;
            const ticket = boton.dataset.ticket || "Venta";
            document.getElementById("detalleVentaTicket").textContent = ticket;
            tbody.replaceChildren();
            error.textContent = "";
            mostrar(error, false);
            mostrar(contenido, false);
            mostrar(cargando, true);
            modalDetalle.show();

            try {
                const respuesta = await fetch(url, {
                    method: "GET",
                    headers: { "Accept": "application/json" },
                    credentials: "same-origin"
                });

                if (!respuesta.ok) {
                    let mensaje = "No se pudo consultar el detalle de la venta.";
                    try {
                        const cuerpo = await respuesta.json();
                        if (cuerpo && cuerpo.mensaje) mensaje = cuerpo.mensaje;
                    } catch (_) { /* La respuesta puede no contener JSON. */ }
                    throw new Error(mensaje);
                }

                const detalles = await respuesta.json();
                if (!Array.isArray(detalles) || detalles.length === 0) {
                    throw new Error("Esta venta no tiene productos de detalle registrados.");
                }

                const venta = detalles[0];
                document.getElementById("detalleVentaFecha").textContent = texto(venta.fecha);
                document.getElementById("detalleVentaCliente").textContent = texto(venta.nombresCliente, "Cliente General");
                const documento = [venta.tipoDocumento, venta.numeroDocumento].filter(Boolean).join(": ");
                document.getElementById("detalleVentaDocumento").textContent = documento || "Documento no registrado";
                document.getElementById("detalleVentaVendedor").textContent = texto(venta.vendedor);
                document.getElementById("detalleVentaPago").textContent = texto(venta.metodoPago);
                document.getElementById("detalleVentaEstado").textContent = texto(venta.estado);
                document.getElementById("detalleVentaTotal").textContent = moneda(venta.montoTotal);

                detalles.forEach(function (detalle) {
                    const fila = document.createElement("tr");
                    agregarCelda(fila, detalle.codigoProducto);
                    agregarCelda(fila, detalle.nombreProducto);
                    agregarCelda(fila, detalle.categoria);
                    agregarCelda(fila, detalle.talla);
                    agregarCelda(fila, detalle.color);
                    agregarCelda(fila, detalle.cantidad, "text-end");
                    agregarCelda(fila, moneda(detalle.precioUnitario), "text-end text-nowrap");
                    agregarCelda(fila, moneda(detalle.subtotal), "text-end text-nowrap fw-semibold");
                    tbody.appendChild(fila);
                });

                mostrar(cargando, false);
                mostrar(contenido, true);
            } catch (excepcion) {
                mostrar(cargando, false);
                error.textContent = excepcion.message || "Ocurrió un error al consultar la venta.";
                mostrar(error, true);
            }
        });
    });
});
