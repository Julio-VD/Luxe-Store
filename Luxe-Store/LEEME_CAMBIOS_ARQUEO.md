# Luxe Store — boletas, clientes y arqueo

## Implementado
- Cabecera del correo: nuevo minilogo LUXE (archivo `src/main/resources/static/img/luxe-correo.png`) de tamaño reducido. El correo no incluye una segunda imagen de pie en el HTML generado por `BoletaCorreoService`.
- Interfaz adicional **Gestión de Clientes** para vendedores: búsqueda, alta y edición de datos de contacto, con validación existente en `ClienteService`.
- **Arqueo del día**: conteo de efectivo, comparación con ventas en efectivo del vendedor, diferencia positiva/sobrante o negativa/faltante, reconteo antes de cerrar y confirmación forzosa con registro de la diferencia.
- Historial mensual de faltantes y sobrantes por vendedor, disponible en el resumen de caja y en un modal de administración (botón de ojo).
- Historial persistido en tabla `arqueos_caja`; Hibernate `ddl-auto=update` crea la tabla si la cuenta de PostgreSQL tiene permisos DDL. No elimina ni modifica tablas existentes intencionalmente.

## Reglas importantes
- Si no hay arqueo en un día, se interpreta como **conforme sin diferencia** para los resúmenes; no se crea un registro falso.
- El reconteo conserva los intentos anteriores en la BD, pero los acumulados mensuales consideran solo el último resultado de cada día.
- Si el conteo coincide, queda conforme y definitivo. Si difiere, se puede volver a contar o confirmar un cuadre forzoso, dejando trazabilidad de la diferencia.
- El efectivo esperado se calcula solo con ventas en efectivo del vendedor en ese día. **No contempla fondo inicial de caja, ingresos o egresos manuales, ni devoluciones**. Para un arqueo contable real se debe registrar el fondo inicial y los movimientos no asociados a ventas.
- El nombre del vendedor en la venta se compara con el nombre actual del usuario, porque así funciona el modelo de ventas preexistente. Si se renombra un vendedor, podría perderse la vinculación histórica por nombre.
- No se garantiza que Gmail clasifique los mensajes como Principal. Reputación del remitente y filtros del destinatario no se controlan desde el código.

## Despliegue y pruebas
1. Hacer copia de seguridad de Neon y probar primero en un entorno de pruebas.
2. Mantener en Render las variables `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `GOOGLE_REFRESH_TOKEN`, `GMAIL_SENDER`.
3. Compilar con Maven y Java 17: `mvn clean package -DskipTests` (requiere Maven instalado; el wrapper del ZIP original no trae su JAR de soporte).
4. Verificar venta, correo y ticket; editar un cliente y comprobar su correo; contar efectivo con diferencia; reconteo; forzoso; filtro mensual; modal del administrador; diseño en móvil.
5. Comprobar los logs de Render para errores de Gmail.

## Limitación de validación
Se validó la sintaxis de JavaScript incrustado y la estructura HTML. **No se ejecutó compilación Maven, pruebas integrales ni envío real de Gmail** en este entorno. No desplegar directamente a producción sin probarlo.
