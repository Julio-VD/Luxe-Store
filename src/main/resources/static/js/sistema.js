/* ==========================================
   NAVEGACIÓN
========================================== */

function mostrarSeccion(id, boton) {

    const secciones =
        document.querySelectorAll(".seccion-sistema");

    secciones.forEach(function(seccion) {

        seccion.classList.remove("activa");

    });


    const seleccion =
        document.getElementById(id);

    if (seleccion) {

        seleccion.classList.add("activa");

    }


    const botones =
        document.querySelectorAll(".menu-item");

    botones.forEach(function(item) {

        item.classList.remove("activo");

    });


    if (boton) {

        boton.classList.add("activo");

    }


    cambiarTitulo(id);
}


function mostrarSeccionPorId(id) {

    const secciones =
        document.querySelectorAll(".seccion-sistema");

    secciones.forEach(function(seccion) {

        seccion.classList.remove("activa");

    });


    const seleccion =
        document.getElementById(id);

    if (seleccion) {

        seleccion.classList.add("activa");

    }


    cambiarTitulo(id);
}


function cambiarTitulo(id) {

    const titulo =
        document.getElementById("tituloSeccion");

    if (!titulo) {
        return;
    }


    const nombres = {

        dashboard: "Dashboard",

        productos: "Catálogo de Prendas",

        stock: "Control de Stock",

        ventas: "Historial de Ventas",

        usuarios: "Usuarios",

        terminal: "Terminal de Venta",

        catalogo: "Catálogo",

        misVentas: "Mis Ventas"

    };


    titulo.textContent =
        nombres[id] || "LUXE";
}



/* ==========================================
   BÚSQUEDA DE PRODUCTOS ADMIN
========================================== */

function buscarProductos() {

    const texto =
        document
        .getElementById("buscarProducto")
        .value
        .toLowerCase();


    const filas =
        document.querySelectorAll(
            "#tablaProductos tbody tr"
        );


    filas.forEach(function(fila) {

        const contenido =
            fila.textContent.toLowerCase();


        if (contenido.includes(texto)) {

            fila.style.display = "";

        } else {

            fila.style.display = "none";

        }

    });
}



/* ==========================================
   MODAL PRODUCTO
========================================== */

function guardarProducto() {

    const modal =
        bootstrap.Modal.getInstance(
            document.getElementById("modalProducto")
        );


    if (modal) {

        modal.hide();

    }


    alert(
        "La prenda fue registrada correctamente."
    );
}


function actualizarProducto() {

    const modal =
        bootstrap.Modal.getInstance(
            document.getElementById("modalEditar")
        );


    if (modal) {

        modal.hide();

    }


    alert(
        "Los datos de la prenda fueron actualizados."
    );
}


function inhabilitarProducto() {

    const confirmar =
        confirm(
            "¿Desea inhabilitar esta prenda?\n\n" +
            "La prenda dejará de aparecer en el terminal de venta."
        );


    if (confirmar) {

        alert(
            "La prenda fue inhabilitada correctamente."
        );

    }

}



/* ==========================================
   USUARIOS
========================================== */

function guardarUsuario() {

    const modal =
        bootstrap.Modal.getInstance(
            document.getElementById("modalUsuario")
        );


    if (modal) {

        modal.hide();

    }


    alert(
        "El usuario fue registrado correctamente."
    );
}



/* ==========================================
   CARRITO DEL VENDEDOR
========================================== */

let carrito = [];


function agregarProducto(nombre, precio) {

    const producto =
        carrito.find(function(item) {

            return item.nombre === nombre;

        });


    if (producto) {

        producto.cantidad++;

    } else {

        carrito.push({

            nombre: nombre,

            precio: precio,

            cantidad: 1

        });

    }


    actualizarTicket();
}



/* ==========================================
   ACTUALIZAR TICKET
========================================== */

function actualizarTicket() {

    const lista =
        document.getElementById("listaTicket");

    const totalElemento =
        document.getElementById("totalVenta");


    if (!lista || !totalElemento) {
        return;
    }


    lista.innerHTML = "";


    let total = 0;


    if (carrito.length === 0) {

        lista.innerHTML = `

            <div class="ticket-vacio">

                <i class="bi bi-cart-x"></i>

                <p>
                    No hay productos agregados
                </p>

            </div>

        `;

    }


    carrito.forEach(function(producto, indice) {

        const subtotal =
            producto.precio *
            producto.cantidad;


        total += subtotal;


        lista.innerHTML += `

            <div class="ticket-item">

                <div>

                    <strong>
                        ${producto.nombre}
                    </strong>

                    <br>

                    ${producto.cantidad}
                    x S/ ${producto.precio.toFixed(2)}

                </div>

                <div class="text-end">

                    <strong>
                        S/ ${subtotal.toFixed(2)}
                    </strong>

                    <br>

                    <button
                        onclick="eliminarProducto(${indice})">

                        <i class="bi bi-trash"></i>

                    </button>

                </div>

            </div>

        `;

    });


    totalElemento.textContent =
        "S/ " + total.toFixed(2);


    calcularVuelto();
}



/* ==========================================
   ELIMINAR PRODUCTO
========================================== */

function eliminarProducto(indice) {

    carrito.splice(indice, 1);

    actualizarTicket();
}



/* ==========================================
   BUSCAR EN POS
========================================== */

function buscarPOS() {

    const input =
        document.getElementById("busquedaPOS");


    if (!input) {
        return;
    }


    const texto =
        input.value.toLowerCase();


    const productos =
        document.querySelectorAll(".producto-pos");


    productos.forEach(function(producto) {

        const nombre =
            producto.dataset.nombre;


        if (nombre.includes(texto)) {

            producto.style.display = "";

        } else {

            producto.style.display = "none";

        }

    });
}



/* ==========================================
   CALCULAR VUELTO
========================================== */

const montoInput =
    document.getElementById("montoRecibido");


if (montoInput) {

    montoInput.addEventListener(
        "input",
        calcularVuelto
    );

}


function calcularVuelto() {

    const totalElemento =
        document.getElementById("totalVenta");


    const montoElemento =
        document.getElementById("montoRecibido");


    const vueltoElemento =
        document.getElementById("vuelto");


    if (
        !totalElemento ||
        !montoElemento ||
        !vueltoElemento
    ) {

        return;

    }


    const total =
        parseFloat(
            totalElemento.textContent
                .replace("S/", "")
        ) || 0;


    const monto =
        parseFloat(
            montoElemento.value
        ) || 0;


    const vuelto =
        monto - total;


    if (vuelto >= 0) {

        vueltoElemento.textContent =
            "S/ " + vuelto.toFixed(2);

    } else {

        vueltoElemento.textContent =
            "S/ 0.00";

    }

}



/* ==========================================
   PROCESAR VENTA
========================================== */

function procesarVenta() {

    if (carrito.length === 0) {

        alert(
            "Debe agregar al menos un producto al ticket."
        );

        return;

    }


    const totalElemento =
        document.getElementById("totalVenta");


    const montoElemento =
        document.getElementById("montoRecibido");


    const metodoElemento =
        document.getElementById("metodoPago");


    const total =
        parseFloat(
            totalElemento.textContent
                .replace("S/", "")
        );


    const monto =
        parseFloat(
            montoElemento.value
        );


    if (isNaN(monto)) {

        alert(
            "Ingrese el monto recibido."
        );

        return;

    }


    if (monto < total) {

        alert(
            "El monto recibido es insuficiente."
        );

        return;

    }


    const vuelto =
        monto - total;


    alert(

        "VENTA PROCESADA CORRECTAMENTE\n\n" +

        "Total: S/ " +
        total.toFixed(2) +

        "\nMétodo: " +
        metodoElemento.value +

        "\nVuelto: S/ " +
        vuelto.toFixed(2) +

        "\n\nTicket generado: #TLX-00246"

    );


    cancelarVenta();

}



/* ==========================================
   CANCELAR VENTA
========================================== */

function cancelarVenta() {

    carrito = [];

    actualizarTicket();


    const monto =
        document.getElementById("montoRecibido");


    if (monto) {

        monto.value = "";

    }


    const vuelto =
        document.getElementById("vuelto");


    if (vuelto) {

        vuelto.textContent =
            "S/ 0.00";

    }

}