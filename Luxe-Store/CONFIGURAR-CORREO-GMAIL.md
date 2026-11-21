# Gmail API para LUXE (Spring Boot / Render)

El envío usa Gmail API OAuth 2.0 y NO requiere dominio ni subir el logo a Neon. El logo está en `src/main/resources/static/img/luxe-correo.png` y viaja adjunto en línea con CID.

## Variables de entorno en Render

- `GMAIL_SENDER=luxestoree2026@gmail.com`
- `GOOGLE_CLIENT_ID=...`
- `GOOGLE_CLIENT_SECRET=...`
- `GOOGLE_REFRESH_TOKEN=...`

El proyecto OAuth debe tener Gmail API habilitada, permiso `gmail.send` y la cuenta remitente autorizada. No publicar tokens ni secretos en GitHub. El refresh token de una aplicación OAuth externa en estado Testing puede caducar a los 7 días; para producción revisar la publicación y los requisitos de verificación de Google.

El envío ocurre después de guardar la venta; si Gmail falla, la venta se conserva. El código actual no mantiene una cola de reintentos persistente ni garantiza entrega final en bandeja de entrada. El control existente de solicitudId evita reenvíos por doble clic dentro de la misma sesión; para garantía entre instancias se requiere idempotencia persistente.

La constancia enviada no es un comprobante tributario autorizado por SUNAT.

No se necesitan cambios en Neon para el logo ni para Gmail.
