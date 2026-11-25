# Boleta por correo en LUXE

Se añadió envío opcional de correo HTML mediante la API de Resend, sin alterar las credenciales de Neon ni el flujo de ventas.

## Variables de entorno (Render > Environment, o entorno local)

- `RESEND_API_KEY`: clave API privada de Resend.
- `RESEND_FROM`: remitente válido de un dominio verificado, por ejemplo `LUXE <ventas@tudominio.com>`.

En Resend debes verificar tu dominio y configurar los registros DNS solicitados. No pongas un dominio ficticio: Resend devolverá 403. No pongas una dirección sin formato: devolverá 422.

Se envía un correo **solo si** la venta terminó correctamente y el cliente seleccionado tiene correo. Una venta de cliente general no envía correo. La API puede tardar o fallar: esto no revierte la venta; revisa los logs de Render si el mensaje no llega. El correo es constancia de compra, **no boleta electrónica SUNAT**. Para boletas tributarias necesitarías un proveedor autorizado y la integración correspondiente.

## Modificaciones
- `AdminController.java` y `admin.html`: código automático en el servidor al guardar; el modal de éxito muestra el código base.
- `BoletaCorreoService.java`: plantilla HTML responsiva y envío asíncrono vía API HTTPS de Resend.
- `VendedorController.java`: invoca envío luego del registro y calcula indicadores de caja por vendedor y día.
- `vendedor.html`: nueva sección Resumen de Caja, con acceso desde el menú lateral.

No se requiere modificar la base de datos. Para producción con varias instancias, conviene reemplazar la asignación secuencial del código por un mecanismo transaccional/único con reintentos; la restricción de código único de PostgreSQL sigue siendo la última protección.
