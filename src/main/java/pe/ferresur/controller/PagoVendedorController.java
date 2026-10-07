package pe.ferresur.controller;
import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.*; import pe.ferresur.model.*; import pe.ferresur.repository.*; import java.time.LocalDate;
@Controller @RequestMapping("/sueldos") public class PagoVendedorController{
 private final PagoVendedorRepository pagos; private final UsuarioRepository usuarios; public PagoVendedorController(PagoVendedorRepository p,UsuarioRepository u){pagos=p;usuarios=u;}
 @GetMapping public String index(Model m){m.addAttribute("pagos",pagos.findAllByOrderByFechaProgramadaAsc());m.addAttribute("vendedores",usuarios.findAll().stream().filter(u->u.getRol()==Rol.VENDEDOR).toList());m.addAttribute("pendientes",pagos.countByEstado(PagoVendedor.Estado.PENDIENTE));m.addAttribute("hoy",LocalDate.now());m.addAttribute("stockBajoCount",0);return "sueldos/index";}
 @PostMapping("/guardar") public String guardar(@RequestParam Long vendedorId,@RequestParam java.math.BigDecimal monto,@RequestParam LocalDate fechaProgramada,@RequestParam(required=false) String observacion){PagoVendedor p=new PagoVendedor();p.setVendedor(usuarios.findById(vendedorId).orElseThrow());p.setMonto(monto);p.setFechaProgramada(fechaProgramada);p.setObservacion(observacion);pagos.save(p);return "redirect:/sueldos";}
 @PostMapping("/{id}/pagar") public String pagar(@PathVariable Long id){pagos.findById(id).ifPresent(p->{p.setEstado(PagoVendedor.Estado.PAGADO);p.setFechaPago(LocalDate.now());pagos.save(p);});return "redirect:/sueldos";}
}
