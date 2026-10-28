# Luxe Store — Sistema Web de Gestión de Ventas (POS)

[![Java 17](https://img.shields.io/badge/Java-17%2B-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.1-005F0F?logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![Chart.js](https://img.shields.io/badge/Chart.js-4.x-FF6384?logo=chartdotjs&logoColor=white)](https://www.chartjs.org/)
[![Maven Wrapper](https://img.shields.io/badge/Maven%20Wrapper-3.6.3-C71A36?logo=apachemaven&logoColor=white)](https://maven.apache.org/)

Aplicación web monolítica desarrollada con **Spring Boot** y **Thymeleaf** para la gestión comercial y punto de venta (POS) de la tienda de ropa textil **Luxe Store**. 

Proyecto desarrollado para el curso **Marcos de Desarrollo Web** — **Avance de Proyecto Final 2 (APF2)** en la **Universidad Tecnológica del Perú (UTP)**.

---

## 📋 Información Académica

- **Institución:** Universidad Tecnológica del Perú (UTP)
- **Carrera:** Ingeniería de Sistemas e Informática
- **Curso:** Marcos de Desarrollo Web
- **Entrega:** Avance de Proyecto Final 2 (APF2)
- **Docente:** Moreno Cueva, Maximo Alberto
- **Sección:** 41554 &nbsp;|&nbsp; **Ciclo:** 6 &nbsp;|&nbsp; **Año:** 2026

### Integrantes del Equipo
1. **Aguilar Roldan, Sandro Aharon**
2. **Ramirez Hoyos, Joshua Natanael**
3. **Salazar Rodriguez, Adriano Jerith**
4. **Vargas Diaz, Julio Sebastian**

---

## 🚀 Arquitectura y Tecnologías

El sistema sigue una **Arquitectura en Capas (Layered Architecture)** desacoplada:

```
[ Navegador Web ]
       │
       ▼  (Peticiones HTTP GET / POST)
[ Capa de Controladores ]   -> @Controller (Spring MVC)
       │
       ▼  (Lógica de negocio y reglas)
[ Capa de Servicios ]       -> @Service (Producto, Cliente, Venta, Usuario)
       │
       ▼  (Almacenamiento en memoria del servidor)
[ Capa de Repositorios ]    -> ConcurrentHashMap + AtomicLong (Thread-Safe)
       │
       ▼  (Inyección en Modelo)
[ Capa de Presentación ]    -> Vistas Thymeleaf + Bootstrap 5 + Chart.js
```

- **Backend:** Java 17, Spring Boot 3.2.5 (Spring MVC, DevTools).
- **Almacenamiento:** Repositorios en memoria del servidor con colecciones concurrentes (`ConcurrentHashMap` y `AtomicLong`), garantizando thread-safety y ejecución inmediata (*zero-setup*).
- **Frontend:** HTML5, CSS3, Thymeleaf 3.1 con modularización de componentes (`templates/fragments/comunes.html`), Bootstrap 5.3, Bootstrap Icons 1.11 y Chart.js.
- **Construcción y Portabilidad:** Maven Wrapper integrado (`mvnw` / `mvnw.cmd`).

---

## 🖥️ Módulos y Vistas del Sistema

### 1. Inicio de Sesión (`index.html` — `/`, `/login`)
- Formulario de autenticación con feedback de credenciales incorrectas mediante alertas Bootstrap.
- Redirección automática basada en el rol de usuario autenticado (`ADMIN` ➔ `/admin`, `VENDEDOR` ➔ `/vendedor`).

### 2. Panel Administrativo (`admin.html` — `/admin`)
- **Dashboard:** Tarjetas KPI de ventas totales, total de prendas, alertas de stock bajo ($\le 5$) y clientes registrados.
- **Catálogo de Prendas:** Listado interactivo con búsqueda en tiempo real, registro de nueva prenda y edición.
- **Control de Inventario:** Visualización de stock con badges de estado (disponible / *Agotado* sin inhabilitar el producto) y modal de reabastecimiento directo (`POST /admin/productos/aumentar-stock`).
- **Historial y Clientes:** Tablas de ventas registradas y directorio de clientes.

### 3. Terminal Punto de Venta POS (`vendedor.html` — `/vendedor`)
- **Catálogo Visual:** Tarjetas de prendas con fotografía real, talla, color, precio y disponibilidad.
- **Carrito Reactivo:** Cálculo automático en tiempo real de subtotal, 18% de IGV y total a pagar.
- **Cobro y Validación:** Selector de cliente (con modal de registro en caliente), ingreso de dinero recibido y validación de vuelto.
- **Comprobante:** Emisión y visualización inmediata del ticket de venta `#TLX` en pantalla.
- **Persistencia Temporal:** El carrito del cajero sobrevive a recargas accidentales (`F5`) mediante `sessionStorage`.

### 4. Reportes Estadísticos (`reportes.html` — `/reportes`)
Navegación interactiva en 2 páginas con **Chart.js**:
- **Página 1:**
  - *Gráfico de Barras:* Ventas totales en soles (S/) por categoría textil (Polos, Camisas, Pantalones, Casacas).
  - *Gráfico Lineal:* Tendencia cronológica mensual de facturación e ingresos.
- **Página 2:**
  - *Gráfico Circular / Dona:* Participación porcentual por método de pago (Efectivo, Tarjeta, Yape/Plin).
  - *Gráfico de Barras Horizontales:* Balance de existencias físicas disponibles en almacén.

---

## 🔑 Credenciales de Prueba (DataLoader)

El componente `DataLoader` precarga automáticamente usuarios y datos de demostración al iniciar el servidor:

| Rol | Usuario | Contraseña | Acceso asignado |
| :--- | :--- | :--- | :--- |
| **Administrador** | `admin` | `1234` | Panel completo `/admin`, catálogo CRUD, stock y `/reportes` |
| **Vendedor / Cajero** | `vendedor` | `1234` | Exclusivamente a la Terminal POS `/vendedor` |

---

## ⚙️ Instrucciones de Ejecución

No se requiere tener Apache Maven instalado en el sistema. El repositorio incluye el **Maven Wrapper**:

### Requisitos Previos
- **Java Development Kit (JDK) 17 o superior** instalado y configurado en el `PATH` (o variable `JAVA_HOME`).
- **Git** para clonar el proyecto.

### Pasos para Ejecutar

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/Julio-VD/Luxe-Store.git
   cd Luxe-Store
   ```

2. **Iniciar la aplicación:**
   - **En Windows (PowerShell o CMD):**
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
   - **En Linux o macOS (Terminal):**
     ```bash
     ./mvnw spring-boot:run
     ```

3. **Abrir en el navegador:**
   Acceder a: **[http://localhost:8080](http://localhost:8080)**

---

## 📁 Estructura del Proyecto

```text
Luxe-Store/
├── .mvn/wrapper/                  # Binarios y propiedades del Maven Wrapper
├── mvnw                           # Script de ejecución para Linux/macOS
├── mvnw.cmd                       # Script de ejecución para Windows
├── pom.xml                        # Configuración de dependencias Maven
└── src/
    └── main/
        ├── java/com/luxe/store/
        │   ├── LuxeStoreApplication.java   # Clase principal Spring Boot
        │   ├── config/
        │   │   └── DataLoader.java         # Semilla de datos iniciales en memoria
        │   ├── controller/
        │   │   ├── AdminController.java    # Rutas del panel administrativo
        │   │   ├── LoginController.java    # Control de acceso y sesiones
        │   │   ├── ReportesController.java # Rutas de gráficos analíticos
        │   │   └── VendedorController.java # Rutas del terminal POS
        │   ├── model/                      # 5 clases modelo POJO
        │   │   ├── Cliente.java
        │   │   ├── DetalleVenta.java
        │   │   ├── Producto.java
        │   │   ├── Usuario.java
        │   │   └── Venta.java
        │   ├── repository/                 # Repositorios en memoria del servidor
        │   │   ├── ClienteRepository.java
        │   │   ├── ProductoRepository.java
        │   │   ├── UsuarioRepository.java
        │   │   └── VentaRepository.java
        │   └── service/                    # Capa de lógica de negocio y reglas
        │       ├── ClienteService.java
        │       ├── ProductoService.java
        │       ├── UsuarioService.java
        │       └── VentaService.java
        └── resources/
            ├── application.properties      # Configuración del servidor (puerto 8080)
            ├── static/                     # Recursos estáticos web
            │   ├── css/estilos.css         # Paleta corporativa Luxe y estilos UI
            │   ├── img/productos/          # Fotografías reales del catálogo textil
            │   └── js/sistema.js           # Lógica frontend interactiva
            └── templates/                  # Vistas dinámicas Thymeleaf
                ├── index.html              # Vista Login
                ├── admin.html              # Vista Administrador
                ├── vendedor.html           # Vista Vendedor (POS)
                ├── reportes.html           # Vista Reportes con Chart.js
                └── fragments/
                    └── comunes.html        # Fragmentos reutilizables (head, sidebar, alertas)
```

---

## 📊 Matriz de Cumplimiento de la Rúbrica (APF2)

| Criterio Evaluado | Requisito de la Rúbrica | Implementación en Luxe Store | Estado |
| :--- | :--- | :--- | :---: |
| **1. Clases Modelo** | Mínimo 3 clases modelo. | 5 clases POJO: `Producto`, `Cliente`, `Usuario`, `Venta`, `DetalleVenta`. | **Cumple** |
| **2. Clase de Servicios** | Procesos de adición, listado, consultas, eliminación y búsquedas. | 4 servicios especializados (`ProductoService`, `ClienteService`, `VentaService`, `UsuarioService`) con CRUD, filtrado y control de stock. | **Cumple** |
| **3. Controlador Web** | Controlador conectado a páginas HTML. | 4 controladores `@Controller`: `LoginController`, `AdminController`, `VendedorController`, `ReportesController`. | **Cumple** |
| **4. 4 Páginas Thymeleaf** | 4 páginas con formularios y acciones. | `index.html` (login), `admin.html` (catálogo y stock), `vendedor.html` (POS) y `reportes.html`. | **Cumple** |
| **5. 2 Páginas de Gráficos** | 2 páginas con gráficos de barra, lineal y círculos. | `reportes.html` en 2 páginas con Chart.js: Barras, Lineal, Circular (Doughnut) y Horizontales. | **Cumple** |
| **6. Menú con Bootstrap** | Menú responsivo que enlace las páginas. | Menú lateral (`sidebar`) estilizado con Bootstrap 5 y Bootstrap Icons en todas las vistas. | **Cumple** |

---

## 🔮 Próximos Pasos — Avance de Proyecto Final 3 (APF3)

Para la entrega final del proyecto se contempla:
1. **Persistencia Relacional:** Migración del almacenamiento en memoria a base de datos **PostgreSQL** mediante Spring Data JPA.
2. **Seguridad y Criptografía:** Integración de **Spring Security** con roles en base de datos, protección CSRF y contraseñas cifradas con `BCryptPasswordEncoder`.
3. **Generación Documental:** Emisión y descarga de comprobantes oficiales (boletas y facturas) en formato **PDF** con iText / OpenPDF.
4. **Pruebas Automatizadas:** Implementación de suite de pruebas unitarias y de integración con **JUnit 5** y **Mockito**.
