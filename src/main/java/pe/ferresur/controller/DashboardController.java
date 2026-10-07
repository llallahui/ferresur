package pe.ferresur.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import pe.ferresur.model.Categoria;
import pe.ferresur.model.DetalleVenta;
import pe.ferresur.model.PagoVendedor;
import pe.ferresur.model.Venta;

import pe.ferresur.repository.CategoriaRepository;
import pe.ferresur.repository.GastoRepository;
import pe.ferresur.repository.PagoVendedorRepository;
import pe.ferresur.repository.ProductoRepository;
import pe.ferresur.repository.UsuarioRepository;
import pe.ferresur.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Controller
@RequestMapping({"/", "/dashboard"})
public class DashboardController {

    private final ProductoRepository productos;
    private final VentaRepository ventas;
    private final UsuarioRepository usuarios;
    private final CategoriaRepository categorias;
    private final GastoRepository gastos;
    private final PagoVendedorRepository pagos;
    private final ObjectMapper mapper;

    public DashboardController(
            ProductoRepository p,
            VentaRepository v,
            UsuarioRepository u,
            CategoriaRepository c,
            GastoRepository g,
            PagoVendedorRepository pv,
            ObjectMapper m) {

        productos = p;
        ventas = v;
        usuarios = u;
        categorias = c;
        gastos = g;
        pagos = pv;
        mapper = m;
    }

    @GetMapping
    public String dashboard(Model model) {

        LocalDate today = LocalDate.now();

        LocalDateTime ds =
                today.atStartOfDay();

        LocalDateTime de =
                today.plusDays(1)
                        .atStartOfDay()
                        .minusNanos(1);

        LocalDateTime ms =
                today.withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime me =
                ms.plusMonths(1)
                        .minusNanos(1);

        int year = today.getYear();

        LocalDateTime ys =
                LocalDate.of(year, 1, 1)
                        .atStartOfDay();

        LocalDateTime ye =
                LocalDate.of(year + 1, 1, 1)
                        .atStartOfDay()
                        .minusNanos(1);

        // =====================================================
        // VENTAS
        // =====================================================

        BigDecimal vh =
                ventas.sumTotalBetween(ds, de);

        BigDecimal vm =
                ventas.sumTotalBetween(ms, me);

        List<Venta> annual =
                ventas.findByFechaBetweenOrderByFechaAsc(
                        ys,
                        ye
                );

        // =====================================================
        // GRÁFICO MENSUAL
        // =====================================================

        double[] mv = new double[12];

        for (Venta v : annual) {

            if (v.getEstado() ==
                    Venta.EstadoVenta.COMPLETADA) {

                if (v.getFecha() != null &&
                        v.getTotal() != null) {

                    mv[
                        v.getFecha()
                            .getMonthValue() - 1
                    ] += v.getTotal().doubleValue();
                }
            }
        }

        // =====================================================
        // PRODUCTOS POR CATEGORÍA
        // =====================================================

        Map<String, Long> cm =
                new LinkedHashMap<>();

        for (Categoria c :
                categorias.findByActivoTrueOrderByNombreAsc()) {

            // IMPORTANTE:
            // El método correcto es Categoria_Id
            long n =
                    productos
                            .countByCategoria_IdAndActivoTrue(
                                    c.getId()
                            );

            if (n > 0) {
                cm.put(
                        c.getNombre(),
                        n
                );
            }
        }

        // =====================================================
        // UTILIDAD BRUTA
        // =====================================================

        BigDecimal utilidad =
                BigDecimal.ZERO;

        for (Venta v : annual) {

            if (v.getEstado() !=
                    Venta.EstadoVenta.COMPLETADA) {

                continue;
            }

            if (v.getDetalles() == null) {
                continue;
            }

            for (DetalleVenta d :
                    v.getDetalles()) {

                BigDecimal costo =
                        d.getCostoUnitario() == null
                                ? BigDecimal.ZERO
                                : d.getCostoUnitario();

                BigDecimal precio =
                        d.getPrecioUnitario() == null
                                ? BigDecimal.ZERO
                                : d.getPrecioUnitario();

                BigDecimal cantidad =
                        BigDecimal.valueOf(
                                d.getCantidad()
                        );

                utilidad =
                        utilidad.add(
                                precio
                                        .subtract(costo)
                                        .multiply(cantidad)
                        );
            }
        }

        // =====================================================
        // GASTOS DEL MES
        // =====================================================

        BigDecimal gastosMes =
                gastos.sumBetween(ms, me);

        if (gastosMes == null) {
            gastosMes = BigDecimal.ZERO;
        }

        // =====================================================
        // SUELDOS PAGADOS DEL MES
        // =====================================================

        BigDecimal sueldos =
                pagos
                        .findAllByOrderByFechaProgramadaAsc()
                        .stream()
                        .filter(p ->
                                p.getEstado() ==
                                PagoVendedor.Estado.PAGADO
                        )
                        .filter(p ->
                                p.getFechaPago() != null
                        )
                        .filter(p ->
                                !p.getFechaPago()
                                        .isBefore(
                                                ms.toLocalDate()
                                        )
                        )
                        .filter(p ->
                                !p.getFechaPago()
                                        .isAfter(
                                                me.toLocalDate()
                                        )
                        )
                        .map(PagoVendedor::getMonto)
                        .filter(monto ->
                                monto != null
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );

        // =====================================================
        // RESULTADO DEL MES
        // =====================================================

        BigDecimal resultado =
                utilidad
                        .subtract(gastosMes)
                        .subtract(sueldos);

        // =====================================================
        // CRÉDITOS VENCIDOS
        // =====================================================

        long creditosVencidos =
                annual
                        .stream()
                        .filter(v ->
                                v.getSaldoPendiente() != null
                        )
                        .filter(v ->
                                v.getSaldoPendiente()
                                        .compareTo(
                                                BigDecimal.ZERO
                                        ) > 0
                        )
                        .filter(v ->
                                v.getFechaVencimientoCredito() != null
                        )
                        .filter(v ->
                                v.getFechaVencimientoCredito()
                                        .isBefore(today)
                        )
                        .count();

        // =====================================================
        // DATOS PARA EL DASHBOARD
        // =====================================================

        model.addAttribute(
                "ventasHoy",
                vh == null
                        ? BigDecimal.ZERO
                        : vh
        );

        model.addAttribute(
                "ventasMes",
                vm == null
                        ? BigDecimal.ZERO
                        : vm
        );

        model.addAttribute(
                "productosCount",
                productos.countByActivoTrue()
        );

        model.addAttribute(
                "stockBajo",
                productos.countStockBajo()
        );

        model.addAttribute(
                "alertas",
                productos.findStockBajo()
        );

        model.addAttribute(
                "recientes",
                ventas.findTop10ByOrderByFechaDesc()
        );

        model.addAttribute(
                "usuariosActivos",
                usuarios.countByActivoTrue()
        );

        model.addAttribute(
                "fechaTexto",
                today.format(
                        DateTimeFormatter.ofPattern(
                                "EEEE, dd 'de' MMMM yyyy",
                                new Locale("es", "PE")
                        )
                )
        );

        model.addAttribute(
                "mesTexto",
                today.format(
                        DateTimeFormatter.ofPattern(
                                "MMMM yyyy",
                                new Locale("es", "PE")
                        )
                )
        );

        model.addAttribute(
                "chartYear",
                year
        );

        model.addAttribute(
                "monthlyValuesJson",
                json(mv)
        );

        model.addAttribute(
                "monthlyLabelsJson",
                json(
                        List.of(
                                "Ene",
                                "Feb",
                                "Mar",
                                "Abr",
                                "May",
                                "Jun",
                                "Jul",
                                "Ago",
                                "Sep",
                                "Oct",
                                "Nov",
                                "Dic"
                        )
                )
        );

        model.addAttribute(
                "categoryLabelsJson",
                json(
                        new ArrayList<>(
                                cm.keySet()
                        )
                )
        );

        model.addAttribute(
                "categoryValuesJson",
                json(
                        new ArrayList<>(
                                cm.values()
                        )
                )
        );

        model.addAttribute(
                "creditosVencidos",
                creditosVencidos
        );

        model.addAttribute(
                "resultadoMes",
                resultado
        );

        model.addAttribute(
                "utilidadBruta",
                utilidad
        );

        model.addAttribute(
                "gastosMes",
                gastosMes
        );

        model.addAttribute(
                "sueldosMes",
                sueldos
        );

        return "dashboard/index";
    }

    // =====================================================
    // CONVERTIR A JSON
    // =====================================================

    private String json(Object o) {

        try {

            return mapper.writeValueAsString(o);

        } catch (JsonProcessingException e) {

            return "[]";
        }
    }
}