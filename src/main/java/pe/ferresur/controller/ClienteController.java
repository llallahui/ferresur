package pe.ferresur.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import pe.ferresur.model.AbonoCredito;
import pe.ferresur.model.Cliente;
import pe.ferresur.model.Venta;
import pe.ferresur.repository.AbonoCreditoRepository;
import pe.ferresur.repository.ClienteRepository;
import pe.ferresur.repository.UsuarioRepository;
import pe.ferresur.repository.VentaRepository;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository repo;
    private final VentaRepository ventas;
    private final AbonoCreditoRepository abonos;
    private final UsuarioRepository usuarios;

    public ClienteController(
            ClienteRepository r,
            VentaRepository v,
            AbonoCreditoRepository a,
            UsuarioRepository u) {

        repo = r;
        ventas = v;
        abonos = a;
        usuarios = u;
    }

    // =========================================================
    // LISTAR CLIENTES
    // =========================================================
    @GetMapping
    public String listar(Model model) {

        var clientes = repo.findByActivoTrueOrderByNombreAsc();

        Map<Long, BigDecimal> mapa = new HashMap<>();

        for (Cliente c : clientes) {
            mapa.put(c.getId(), deuda(c.getId()));
        }

        model.addAttribute("clientes", clientes);
        model.addAttribute("deudasPorCliente", mapa);
        model.addAttribute("stockBajoCount", 0);

        model.addAttribute("total", clientes.size());

        model.addAttribute(
                "deudores",
                mapa.values()
                        .stream()
                        .filter(x -> x.compareTo(BigDecimal.ZERO) > 0)
                        .count()
        );

        model.addAttribute(
                "deudaTotal",
                mapa.values()
                        .stream()
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return "clientes/index";
    }

    // =========================================================
    // NUEVO CLIENTE
    // =========================================================
    @GetMapping("/nuevo")
    public String nuevo(Model model) {

        model.addAttribute("cliente", new Cliente());
        model.addAttribute("stockBajoCount", 0);

        return "clientes/form";
    }

    // =========================================================
    // EDITAR CLIENTE
    // =========================================================
    @GetMapping("/editar/{id}")
    public String editar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "cliente",
                repo.findById(id).orElseThrow()
        );

        model.addAttribute("stockBajoCount", 0);

        return "clientes/form";
    }

    // =========================================================
    // GUARDAR CLIENTE
    // =========================================================
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute Cliente c) {

        if (c.getDocumento() != null &&
                c.getDocumento().isBlank()) {

            c.setDocumento(null);
        }

        repo.save(c);

        return "redirect:/clientes";
    }

    // =========================================================
    // ELIMINAR / DESACTIVAR CLIENTE
    // =========================================================
    @PostMapping("/eliminar/{id}")
    public String eliminar(
            @PathVariable Long id) {

        repo.findById(id).ifPresent(c -> {

            c.setActivo(false);

            repo.save(c);
        });

        return "redirect:/clientes";
    }

    // =========================================================
    // DETALLE DEL CLIENTE
    // =========================================================
    @GetMapping("/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model) {

        Cliente c = repo.findById(id)
                .orElseThrow();

        var deudas =
                ventas.findByClienteAndSaldoPendienteGreaterThanOrderByFechaAsc(
                        c,
                        BigDecimal.ZERO
                );

        BigDecimal deudaTotal = deudas
                .stream()
                .map(Venta::getSaldoPendiente)
                .filter(saldo -> saldo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("cliente", c);
        model.addAttribute("deudas", deudas);
        model.addAttribute("deudaTotal", deudaTotal);
        model.addAttribute("stockBajoCount", 0);

        return "clientes/detalle";
    }

    // =========================================================
    // REGISTRAR ABONO DE CRÉDITO
    // =========================================================
    @PostMapping("/deuda/{ventaId}/abono")
    public String abono(
            @PathVariable Long ventaId,
            @RequestParam BigDecimal monto,
            Authentication auth) {

        Venta v = ventas.findById(ventaId)
                .orElseThrow();

        if (v.getSaldoPendiente() != null
                && v.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0
                && monto != null
                && monto.compareTo(BigDecimal.ZERO) > 0
                && monto.compareTo(v.getSaldoPendiente()) <= 0) {

            AbonoCredito a = new AbonoCredito();

            a.setVenta(v);
            a.setMonto(monto);

            a.setUsuario(
                    usuarios
                            .findByUsernameIgnoreCase(auth.getName())
                            .orElse(null)
            );

            abonos.save(a);

            v.setSaldoPendiente(
                    v.getSaldoPendiente().subtract(monto)
            );

            ventas.save(v);
        }

        return "redirect:/clientes/" + v.getCliente().getId();
    }

    // =========================================================
    // CALCULAR DEUDA TOTAL DE UN CLIENTE
    // =========================================================
    private BigDecimal deuda(Long id) {

        Cliente cliente = repo.findById(id)
                .orElseThrow();

        return ventas
                .findByClienteAndSaldoPendienteGreaterThanOrderByFechaAsc(
                        cliente,
                        BigDecimal.ZERO
                )
                .stream()
                .map(Venta::getSaldoPendiente)
                .filter(saldo -> saldo != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}