package pe.ferresur.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pe.ferresur.model.DetalleVenta;
import pe.ferresur.model.PagoVendedor;
import pe.ferresur.model.Producto;
import pe.ferresur.model.Venta;
import pe.ferresur.repository.GastoRepository;
import pe.ferresur.repository.PagoVendedorRepository;
import pe.ferresur.repository.ProductoRepository;
import pe.ferresur.repository.VentaRepository;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private final VentaRepository ventas;
    private final ProductoRepository productos;
    private final GastoRepository gastos;
    private final PagoVendedorRepository pagos;

    public ReporteController(
            VentaRepository v,
            ProductoRepository p,
            GastoRepository g,
            PagoVendedorRepository pv) {

        ventas = v;
        productos = p;
        gastos = g;
        pagos = pv;
    }

    // =========================================================
    // REPORTE DE VENTAS
    // =========================================================
    @GetMapping
    public String ventas(
            @RequestParam(required = false) String desde,
            @RequestParam(required = false) String hasta,
            Model model) {

        LocalDate hoy = LocalDate.now();

        LocalDate d = parseDate(
                desde,
                hoy.withDayOfMonth(1)
        );

        LocalDate h = parseDate(
                hasta,
                hoy
        );

        if (h.isBefore(d)) {
            h = d;
        }

        final LocalDate fechaInicio = d;
        final LocalDate fechaFin = h;

        LocalDateTime inicio = fechaInicio.atStartOfDay();

        LocalDateTime fin = fechaFin
                .plusDays(1)
                .atStartOfDay()
                .minusNanos(1);
        // -----------------------------------------------------
        // VENTAS DEL PERIODO
        // -----------------------------------------------------

        var lista = ventas.findByFechaBetweenOrderByFechaDesc(
                inicio,
                fin
        );

        BigDecimal total = ventas.sumTotalBetween(
                inicio,
                fin
        );

        if (total == null) {
            total = BigDecimal.ZERO;
        }

        // -----------------------------------------------------
        // CANTIDAD DE TRANSACCIONES COMPLETADAS
        // -----------------------------------------------------

        long transacciones = lista
                .stream()
                .filter(v -> v.getEstado() == Venta.EstadoVenta.COMPLETADA)
                .count();

        // -----------------------------------------------------
        // UTILIDAD BRUTA
        // -----------------------------------------------------

        BigDecimal utilidad = BigDecimal.ZERO;

        for (Venta venta : lista) {

            if (venta.getEstado() != Venta.EstadoVenta.COMPLETADA) {
                continue;
            }

            if (venta.getDetalles() == null) {
                continue;
            }

            for (DetalleVenta detalle : venta.getDetalles()) {

                BigDecimal precioVenta =
                        detalle.getPrecioUnitario();

                BigDecimal costo =
                        detalle.getCostoUnitario();

                if (precioVenta == null) {
                    precioVenta = BigDecimal.ZERO;
                }

                if (costo == null) {
                    costo = BigDecimal.ZERO;
                }

                BigDecimal cantidad =
                        BigDecimal.valueOf(detalle.getCantidad());

                BigDecimal utilidadDetalle =
                        precioVenta
                                .subtract(costo)
                                .multiply(cantidad);

                utilidad = utilidad.add(utilidadDetalle);
            }
        }

        // -----------------------------------------------------
        // GASTOS DEL PERIODO
        // -----------------------------------------------------

        BigDecimal gast = gastos.sumBetween(
                inicio,
                fin
        );

        if (gast == null) {
            gast = BigDecimal.ZERO;
        }

        // -----------------------------------------------------
        // SUELDOS PAGADOS DEL PERIODO
        // -----------------------------------------------------

        BigDecimal sueldos = pagos
                .findAllByOrderByFechaProgramadaAsc()
                .stream()
                .filter(p -> p.getEstado() == PagoVendedor.Estado.PAGADO)
                .filter(p -> p.getFechaPago() != null)
                .filter(p -> !p.getFechaPago().isBefore(fechaInicio))
                .filter(p -> !p.getFechaPago().isAfter(fechaFin))
                .map(PagoVendedor::getMonto)
                .filter(monto -> monto != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        // -----------------------------------------------------
        // RESULTADO FINAL
        // -----------------------------------------------------

        BigDecimal resultado = utilidad
                .subtract(gast)
                .subtract(sueldos);

        // -----------------------------------------------------
        // TOTAL DE ITEMS VENDIDOS
        // -----------------------------------------------------

        Long items = ventas.sumItemsBetween(
                inicio,
                fin
        );

        if (items == null) {
            items = 0L;
        }

        // -----------------------------------------------------
        // PROMEDIO POR TRANSACCIÓN
        // -----------------------------------------------------

        BigDecimal promedio = BigDecimal.ZERO;

        if (transacciones > 0) {

            promedio = total.divide(
                    BigDecimal.valueOf(transacciones),
                    2,
                    RoundingMode.HALF_UP
            );
        }

        // -----------------------------------------------------
        // DATOS PARA LA VISTA
        // -----------------------------------------------------

        model.addAttribute("ventas", lista);

        model.addAttribute("desde", d);

        model.addAttribute("hasta", h);

        model.addAttribute("total", total);

        model.addAttribute(
                "transacciones",
                transacciones
        );

        model.addAttribute(
                "items",
                items
        );

        model.addAttribute(
                "promedio",
                promedio
        );

        model.addAttribute(
                "stockBajoCount",
                productos.countStockBajo()
        );

        model.addAttribute(
                "stockBajoList",
                productos.findStockBajo()
        );

        model.addAttribute(
                "utilidad",
                utilidad
        );

        model.addAttribute(
                "gastos",
                gast
        );

        model.addAttribute(
                "sueldos",
                sueldos
        );

        model.addAttribute(
                "resultado",
                resultado
        );

        return "reportes/index";
    }

    // =========================================================
    // REPORTE DE INVENTARIO
    // =========================================================
    @GetMapping("/inventario")
    public String inventario(Model model) {

        var lista =
                productos.findByActivoTrueOrderByNombreAsc();

        model.addAttribute(
                "productos",
                lista
        );

        model.addAttribute(
                "stockBajo",
                productos.findStockBajo()
        );

        BigDecimal valorInventario = lista
                .stream()
                .map(Producto::getValorStock)
                .filter(valor -> valor != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );

        model.addAttribute(
                "valorInventario",
                valorInventario
        );

        model.addAttribute(
                "stockBajoCount",
                productos.countStockBajo()
        );

        long totalUnidades = lista
                .stream()
                .mapToLong(Producto::getStock)
                .sum();

        model.addAttribute(
                "totalUnidades",
                totalUnidades
        );

        return "reportes/inventario";
    }

    // =========================================================
    // CONVERTIR FECHA
    // =========================================================
    private LocalDate parseDate(
            String valor,
            LocalDate fechaPorDefecto) {

        try {

            if (valor == null || valor.isBlank()) {
                return fechaPorDefecto;
            }

            return LocalDate.parse(valor);

        } catch (Exception e) {

            return fechaPorDefecto;
        }
    }
}