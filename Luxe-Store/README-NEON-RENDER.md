# Luxe-Store — Neon PostgreSQL y Render

## Configuración
La aplicación usa Spring Boot 3.2.5, Java 17, Spring Data JPA y PostgreSQL. Las tablas se crean/actualizan con Hibernate (`spring.jpa.hibernate.ddl-auto=update`). No subas credenciales al repositorio.

## Variables de entorno requeridas
- `DATABASE_URL`: URL JDBC de Neon, con formato `jdbc:postgresql://HOST/BASE?sslmode=require`
- `DB_USERNAME`: usuario de Neon
- `DB_PASSWORD`: contraseña del usuario de Neon
- `ADMIN_INITIAL_PASSWORD`: contraseña inicial para el usuario `admin`, solo se usa al crear la cuenta por primera vez
- `VENDEDOR_INITIAL_PASSWORD`: contraseña inicial para `vendedor`, solo se usa al crear la cuenta por primera vez

En Neon, abre **Connect** y selecciona el formato JDBC si está disponible. Copia host, database y username desde allí; la contraseña se obtiene o restablece desde el panel de Neon. No compartas estos valores públicamente.

## Cuentas iniciales
Al iniciar por primera vez con una base vacía se crean `admin` y `vendedor`. Sus contraseñas se toman de las variables `ADMIN_INITIAL_PASSWORD` y `VENDEDOR_INITIAL_PASSWORD`. Si no defines variables, los valores de respaldo son `CambiarAdmin123!` y `CambiarVendedor123!`; cámbialos antes de publicar. Las contraseñas se almacenan con BCrypt.

Si la tabla `usuarios` ya existe con cuentas antiguas en texto plano, no se autenticarán con el nuevo método BCrypt. Usa una base de datos nueva o migra las contraseñas antes de activar esta versión.

## Render
Crea un Web Service desde el repositorio GitHub, selecciona entorno **Docker** y deja que Render use el `Dockerfile`. Configura todas las variables de entorno anteriores en **Environment**. No añadas el archivo `.env` al repositorio.

## Importante
El proyecto se adapta para persistir usuarios, productos, clientes, ventas y detalles de venta. Los datos de ejemplo del `DataLoader` solo se insertan cuando la tabla correspondiente está vacía. `ddl-auto=update` es apropiado para el primer despliegue de este proyecto académico; para producción real, conviene usar migraciones versionadas (Flyway) y copias de seguridad.
