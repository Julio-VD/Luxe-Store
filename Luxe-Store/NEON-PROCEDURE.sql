-- Procedimiento usado por VentaService.registrarVenta.
-- Cada llamada se ejecuta dentro de la misma transacción de Spring Boot.
CREATE OR REPLACE PROCEDURE descontar_stock_venta(
    IN p_producto_id BIGINT,
    IN p_cantidad INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_nombre TEXT;
    v_stock INTEGER;
    v_estado TEXT;
BEGIN
    IF p_producto_id IS NULL OR p_cantidad IS NULL OR p_cantidad <= 0 THEN
        RAISE EXCEPTION 'Producto o cantidad inválida';
    END IF;

    SELECT nombre, stock, estado INTO v_nombre, v_stock, v_estado
    FROM productos WHERE id = p_producto_id FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'El producto % no existe', p_producto_id;
    END IF;
    IF LOWER(COALESCE(v_estado, '')) <> 'activo' THEN
        RAISE EXCEPTION 'La prenda % no está activa', v_nombre;
    END IF;
    IF COALESCE(v_stock, 0) < p_cantidad THEN
        RAISE EXCEPTION 'Stock insuficiente para %', v_nombre;
    END IF;

    UPDATE productos
    SET stock = stock - p_cantidad,
        estado = CASE WHEN stock - p_cantidad = 0 THEN 'Agotado' ELSE estado END
    WHERE id = p_producto_id;
END;
$$;

-- No ejecutar CALL de prueba sobre productos reales: descontaría stock.
-- Para verificar que existe:
-- SELECT proname FROM pg_proc WHERE proname = 'descontar_stock_venta';
