# Luxe Store — versión limpia (Neon + Render)

Aplicación Spring Boot 3 / Java 17 con Thymeleaf, Bootstrap, Spring Data JPA y PostgreSQL.

## Estructura
Este ZIP contiene una sola copia del proyecto. No incluye `.git`, `target/` ni clases compiladas; así se evita subir versiones duplicadas del código. Cada controlador está en `src/main/java/com/luxe/store/controller/`.

## Variables de entorno en Render
En el servicio web, abre **Environment** y configura:
- `DATABASE_URL`: URL JDBC de Neon, por ejemplo `jdbc:postgresql://HOST/BASE?sslmode=require` (usa los datos actuales de Neon).
- `DB_USERNAME`: usuario de Neon.
- `DB_PASSWORD`: contraseña actual de Neon.
- `ADMIN_INITIAL_PASSWORD`: contraseña inicial del administrador, mínimo 10 caracteres.
- `VENDEDOR_INITIAL_PASSWORD`: contraseña inicial del vendedor, mínimo 10 caracteres.

Las dos variables `*_INITIAL_PASSWORD` solo se utilizan cuando la tabla `usuarios` está vacía. No cambian las contraseñas de cuentas ya existentes.

**Seguridad:** la contraseña de Neon que estaba escrita en una configuración anterior debe considerarse expuesta. Rótala desde Neon y actualiza `DB_PASSWORD` en Render. No reutilices esa contraseña. No subas `.env` ni credenciales a GitHub.

## Cambiar la contraseña del administrador
1. Inicia sesión con la cuenta `admin` existente.
2. En el panel de administración, baja hasta **Seguridad de la cuenta**.
3. Ingresa la contraseña actual, la nueva (mínimo 10 caracteres) y la confirmación.
4. Pulsa **Actualizar contraseña**. Se guarda en Neon con BCrypt.

Si el administrador ya existe en la base, cambiar `ADMIN_INITIAL_PASSWORD` en Render no cambia su contraseña; utiliza el formulario anterior.

## Despliegue
El proyecto incluye `Dockerfile`. En Render selecciona entorno **Docker** y configura las variables anteriores. La aplicación escucha en el puerto que Render proporciona mediante `PORT`.

## Ejecución local
Requiere Java 17. Define las variables de entorno de base de datos y credenciales iniciales antes de ejecutar:
- Windows: `mvnw.cmd spring-boot:run`
- Linux/macOS: `./mvnw spring-boot:run`

Abre `http://localhost:8080`.

## Correcciones incluidas
- Una sola raíz de proyecto y sin artefactos compilados/metadata Git duplicada.
- Configuración de Neon por variables de entorno; no se guarda la contraseña de la base en el código.
- Dependencias explícitas para JPA y PostgreSQL.
- Eliminado el acceso automático que creaba una sesión de administrador/vendedor sin iniciar sesión.
- Protección de rutas de administración, reportes y vendedor según rol.
- Formulario para cambiar la contraseña del administrador con validación y BCrypt.


## Restablecer contraseña del administrador

Configura temporalmente `ADMIN_RESET_TOKEN` en Render con un valor aleatorio de al menos 24 caracteres, vuelve a desplegar y visita `/recuperar-admin` en el dominio de tu servicio. Ingresa el token y una contraseña nueva de al menos 10 caracteres. Tras confirmar que puedes iniciar sesión como `admin`, elimina `ADMIN_RESET_TOKEN` de Render y vuelve a desplegar. No subas el token ni credenciales al repositorio. Esto no borra los datos de Neon.
