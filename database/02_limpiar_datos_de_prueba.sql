-- Ejecutar una sola vez si la base ferresur ya tenía productos/ventas de pruebas.
-- NO ejecutar en producción.
TRUNCATE TABLE abonos_credito, detalle_ventas, ventas, gastos, cajas RESTART IDENTITY CASCADE;
TRUNCATE TABLE productos RESTART IDENTITY CASCADE;
