# Luxe Store — fondo diario y seguridad

## Arqueo
- Cada vendedor inicia cada día (zona America/Lima) con **S/ 400.00** como fondo inicial.
- Efectivo esperado = S/ 400.00 + total de ventas en efectivo de ese vendedor y día.
- Diferencia = efectivo contado - efectivo esperado. El fondo inicial **no es una venta ni ingreso**.
- Días sin arqueo siguen siendo considerados conformes para el historial, sin imputar diferencia.
- El fondo es un valor fijo de negocio en `ArqueoCajaService.FONDO_INICIAL` y se aplica por fecha; no requiere migración de Neon.
- Importante: la asignación de ventas sigue identificando al vendedor por nombre completo. Si se cambia su nombre, podrían perderse coincidencias históricas. Antes de producción, migrar a identificador de vendedor estable.
- Si hay retiros, devoluciones o ingresos extra de efectivo, el arqueo requiere un módulo adicional de movimientos de caja para reflejarlos.

## Seguridad implementada
- `UsuarioService`: contraseñas cifradas con BCrypt y verificación de cuenta activa (ya existía).
- `LoginAttemptService`: bloqueo temporal de 15 minutos tras 5 fallos por IP y usuario (nuevo; memoria de instancia, no distribuido).
- `LoginController`: renovación de identificador de sesión al autenticar y expiración por inactividad de 30 minutos (nuevo).
- `SessionAccessConfig`: verifica rol, estado activo y hash de contraseña vigente contra Neon en rutas protegidas, invalida sesiones obsoletas; cabeceras HTTP de protección (nuevo).
- `application.properties`: cookies HttpOnly, Secure y SameSite=Lax, y mensajes de error internos ocultos (nuevo).

## Límites y próximos controles imprescindibles
- Estas medidas **no garantizan** inmunidad a ataques. Falta protección CSRF completa para formularios y APIs mutables, MFA para administradores, rate limiting centralizado en Redis/WAF, auditoría de cambios y pruebas de penetración.
- El limitador de intentos se reinicia al reiniciar Render y no se comparte entre réplicas.
- `SESSION_COOKIE_SECURE=true` requiere acceder por HTTPS; no iniciar sesión por HTTP local salvo `SESSION_COOKIE_SECURE=false` en entorno local.
- El archivo `NEON-VERIFICACION-ARQUEO-SEGURIDAD.sql` solo consulta tablas y hashes; no modifica datos.
- En Render, no colocar credenciales en código ni subir `.env` a GitHub. Configurar secretos en Environment, activar HTTPS y restringir acceso al proyecto.

## Despliegue
1. Hacer respaldo de Neon y conservar ZIP anterior.
2. Desplegar esta versión en servicio de pruebas antes de producción.
3. Validar acceso admin/vendedor, cierre de sesión, cambio de contraseña, 5 intentos fallidos, venta y arqueo con 400 + ventas.
4. Revisar logs y confirmar que correo, boletas, ventas e inventario siguen operando.
