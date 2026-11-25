# Spring Security
- Dependencia starter-security (Spring Boot 3.2.5).
- SecurityConfig: SecurityFilterChain, permisos ADMIN/VENDEDOR, CSRF, logout POST, cabeceras predeterminadas.
- LoginController conserva autenticación BCrypt y registra el contexto autenticado en Spring Security; rota ID de sesión y limita intentos por identificador.
- SessionAccessConfig comprueba estado, rol y hash vigente en Neon en rutas protegidas.
- Formularios Thymeleaf y Fetch de ventas/arqueos adaptados a CSRF; operaciones de catálogo que modifican datos pasan a POST.
- No se requiere SQL nuevo. No se modificaron propiedades de conexión ni correo.
- Pruebas pendientes en Render: login ambos roles, acceso cruzado 403, venta, arqueo, edición cliente, carga de imagen, cambio de contraseña y logout.
- Compilar: mvn clean package (Java 17). No se pudo ejecutar Maven en el entorno de edición.
