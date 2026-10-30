ALTER TABLE productos ADD COLUMN IF NOT EXISTS imagen BYTEA;

-- Verificar imágenes almacenadas en JPG:
-- SELECT id, codigo, octet_length(imagen) AS bytes_jpg FROM productos;

