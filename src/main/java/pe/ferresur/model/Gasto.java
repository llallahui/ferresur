package pe.ferresur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Table(name="gastos")
public class Gasto {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private LocalDateTime fecha=LocalDateTime.now();
    @Enumerated(EnumType.STRING) private Tipo tipo;
    @NotBlank private String descripcion;
    @NotNull @DecimalMin("0.01") private BigDecimal monto;
    private Integer cantidad;
    @ManyToOne(fetch=FetchType.EAGER) private Producto producto;
    @ManyToOne(fetch=FetchType.EAGER) private Usuario usuario;
    public enum Tipo{COMPRA_PRODUCTOS,GASTO_EXTERNO}
    public Long getId(){return id;} public LocalDateTime getFecha(){return fecha;} public void setFecha(LocalDateTime v){fecha=v;}
    public Tipo getTipo(){return tipo;} public void setTipo(Tipo v){tipo=v;} public String getDescripcion(){return descripcion;} public void setDescripcion(String v){descripcion=v;}
    public BigDecimal getMonto(){return monto;} public void setMonto(BigDecimal v){monto=v;} public Integer getCantidad(){return cantidad;} public void setCantidad(Integer v){cantidad=v;}
    public Producto getProducto(){return producto;} public void setProducto(Producto v){producto=v;} public Usuario getUsuario(){return usuario;} public void setUsuario(Usuario v){usuario=v;}
}
