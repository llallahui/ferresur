package pe.ferresur.model;
import jakarta.persistence.*; import jakarta.validation.constraints.DecimalMin; import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Table(name="abonos_credito") public class AbonoCredito{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private LocalDateTime fecha=LocalDateTime.now(); @ManyToOne(fetch=FetchType.EAGER,optional=false) private Venta venta; @ManyToOne(fetch=FetchType.EAGER) private Usuario usuario; @DecimalMin("0.01") private BigDecimal monto;
 public Long getId(){return id;} public LocalDateTime getFecha(){return fecha;} public Venta getVenta(){return venta;} public void setVenta(Venta v){venta=v;} public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;} public BigDecimal getMonto(){return monto;} public void setMonto(BigDecimal v){monto=v;}
}
