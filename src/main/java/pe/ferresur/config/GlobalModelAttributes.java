package pe.ferresur.config;
import org.springframework.web.bind.annotation.ControllerAdvice; import org.springframework.web.bind.annotation.ModelAttribute; import pe.ferresur.repository.*; import java.math.BigDecimal;
@ControllerAdvice public class GlobalModelAttributes{
 private final ProductoRepository productos; private final PagoVendedorRepository pagos; private final VentaRepository ventas;
 public GlobalModelAttributes(ProductoRepository p,PagoVendedorRepository s,VentaRepository v){productos=p;pagos=s;ventas=v;}
 @ModelAttribute("notificaciones") public long notificaciones(){return productos.countStockBajo()+pagos.countByEstado(pe.ferresur.model.PagoVendedor.Estado.PENDIENTE)+ventas.countBySaldoPendienteGreaterThan(BigDecimal.ZERO);}
 @ModelAttribute("stockBajoCount") public long stock(){return productos.countStockBajo();}
 @ModelAttribute("pagosPendientes") public long pagos(){return pagos.countByEstado(pe.ferresur.model.PagoVendedor.Estado.PENDIENTE);}
}
