-- Ejecutar en Neon SQL Editor. Consultas de solo lectura.
-- El fondo inicial de S/ 400 se calcula en Java, no se suma a las ventas ni se guarda como una venta.
SELECT table_name FROM information_schema.tables WHERE table_schema='public' AND table_name IN ('usuarios','ventas','arqueos_caja');
SELECT column_name, data_type FROM information_schema.columns WHERE table_schema='public' AND table_name='arqueos_caja' ORDER BY ordinal_position;
SELECT id, usuario, rol, estado, (contrasena LIKE '$2%') AS clave_parece_bcrypt FROM usuarios ORDER BY id;
-- NO actualizar contrasenas directamente en SQL ni ejecutar UPDATE masivos sobre arqueos.
