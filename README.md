# FerreSur Ayacucho — Sistema de Gestión

Aplicación web con Spring Boot 3.5.7, Java 21, Thymeleaf, Spring Security y PostgreSQL.

## Funciones
- Dashboard con ventas mensuales y distribución por categorías.
- Indicador de ganancia/pérdida usando costo de productos, gastos y sueldos pagados.
- Productos: alta, edición, eliminación lógica, stock mínimo y costo.
- Ventas: carrito, efectivo, Yape, tarjeta y crédito.
- Efectivo: monto recibido y vuelto.
- Crédito: cliente, fecha de crédito, vencimiento, saldo y abonos.
- Boleta imprimible.
- Clientes y cuentas por cobrar.
- Caja: apertura, ventas en efectivo, gastos, compras de productos y cierre.
- Sueldos: programación y confirmación de pagos.
- Usuarios: administrador y vendedor con permisos diferentes.
- Contraseñas con confirmación al crear usuario y cambio de contraseña desde Mi perfil.
- Nombre de usuario único.
- Notificaciones de stock bajo, créditos vencidos y pagos de vendedores pendientes.

## Roles
Administrador: productos, usuarios, reportes, sueldos, caja, clientes y ventas.
Vendedor: dashboard, ventas, clientes, caja y consulta de productos. No puede editar/eliminar productos, gestionar usuarios, reportes ni sueldos.

## Credenciales de una instalación nueva
- Administrador: `admin` / `admin123`
- Vendedor: `vendedor` / `vendedor123`

## PostgreSQL
Base de datos: `ferresur`
Usuario por defecto: `postgres`
Puerto web: `8081`

Puede configurar la contraseña mediante la variable de entorno `FERRESUR_DB_PASSWORD`.
También se aceptan `FERRESUR_DB_URL` y `FERRESUR_DB_USERNAME`.

## Datos de prueba
El proyecto inicia sin productos ni ventas para poder probar el sistema desde cero. En la primera ejecución de esta versión se limpia una sola vez el catálogo/datos de prueba anteriores y se registra una marca de inicialización.

Si desea cargar dos productos de demostración, establezca antes de iniciar:
`FERRESUR_SEED_PRODUCTS=true`

No se recomienda usar datos demo en producción.


## Despliegue gratuito en Render

Este proyecto incluye un `Dockerfile` para desplegar FerreSur como servicio web en Render.
Render proporciona la variable `PORT`, por lo que el servidor ahora usa `PORT` y mantiene
`8081` como valor predeterminado cuando se ejecuta localmente.

Variables de entorno para PostgreSQL:
- `FERRESUR_DB_URL`: URL JDBC completa, por ejemplo `jdbc:postgresql://HOST:5432/ferresur`
- `FERRESUR_DB_USERNAME`: usuario de PostgreSQL.
- `FERRESUR_DB_PASSWORD`: contraseña de PostgreSQL.

Para Render, configure estas variables en el apartado Environment del servicio.
