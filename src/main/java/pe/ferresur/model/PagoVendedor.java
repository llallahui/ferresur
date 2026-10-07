package pe.ferresur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Table(name="pagos_vendedores")
public class PagoVendedor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.EAGER, optional=false) private Usuario vendedor;
    private LocalDate fechaProgramada;
    private LocalDate fechaPago;
    @NotNull @DecimalMin("0.01") private BigDecimal monto;
    @Enumerated(EnumType.STRING) private Estado estado=Estado.PENDIENTE;
    private String observacion;
    public enum Estado{PENDIENTE,PAGADO}
    public Long getId(){return id;} public Usuario getVendedor(){return vendedor;} public void setVendedor(Usuario v){vendedor=v;}
    public LocalDate getFechaProgramada(){return fechaProgramada;} public void setFechaProgramada(LocalDate v){fechaProgramada=v;}
    public LocalDate getFechaPago(){return fechaPago;} public void setFechaPago(LocalDate v){fechaPago=v;}
    public BigDecimal getMonto(){return monto;} public void setMonto(BigDecimal v){monto=v;}
    public Estado getEstado(){return estado;} public void setEstado(Estado v){estado=v;} public String getObservacion(){return observacion;} public void setObservacion(String v){observacion=v;}
}
