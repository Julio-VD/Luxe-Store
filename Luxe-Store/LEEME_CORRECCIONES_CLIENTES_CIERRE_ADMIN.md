# Correcciones focalizadas

1. Editar cliente: el modal se precarga con los datos del botón y consulta la ficha actual desde `/vendedor/clientes/datos/{id}` (solo vendedor autenticado).
2. Cierre diario: un arqueo definitivo (CONFORME o FORZOSO) bloquea nuevas ventas en efectivo de ese vendedor durante el día de Lima. Tarjeta y pagos digitales siguen permitidos. Validación en servidor y aviso en el POS.
3. Administración: selección de mes corregida para el historial del vendedor (antes dependía del formato regional del navegador y podía producir un mes vacío).
4. Fondo inicial de S/ 400 y correo existentes: sin cambios.

No se requiere SQL nuevo para estos cambios. Conservar la base de datos Neon existente.

## Pruebas sugeridas
- Abrir Editar en varios clientes: documento, nombre, correo y teléfono deben corresponder al cliente elegido.
- Registrar cuadre conforme, intentar nueva venta en efectivo: debe rechazarla sin crear ticket ni descontar stock.
- Después del cierre, probar tarjeta: debe continuar operativa con la verificación de pago existente.
- Abrir ojo de cada vendedor en Admin: mes YYYY-MM y datos separados por ID.
- Probar reconteo previo a un cierre definitivo.

No se pudo ejecutar Maven en este entorno: el ZIP no trae `.mvn/wrapper` y `mvn` no está instalado. Se validó sintaxis JavaScript con Node.
