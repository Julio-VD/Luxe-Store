BEGIN;
ALTER TABLE ventas
  ALTER COLUMN vuelto TYPE NUMERIC(12,2) USING ROUND(vuelto::numeric, 2),
  ALTER COLUMN monto_total TYPE NUMERIC(12,2) USING ROUND(monto_total::numeric, 2),
  ALTER COLUMN monto_recibido TYPE NUMERIC(12,2) USING ROUND(monto_recibido::numeric, 2);
-- Recalcular vueltos históricos desde importes redondeados.
UPDATE ventas
SET vuelto = GREATEST(0, ROUND(monto_recibido - monto_total, 2))
WHERE monto_recibido IS NOT NULL AND monto_total IS NOT NULL;
COMMIT;
-- Comprobar:
SELECT numero_ticket, monto_total, monto_recibido, vuelto FROM ventas ORDER BY id DESC LIMIT 20;
