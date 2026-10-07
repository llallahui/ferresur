package pe.ferresur.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String numero;

    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id")
    private Usuario vendedor;

    @Column(nullable = false)
    private BigDecimal total = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    private TipoPago tipoPago = TipoPago.EFECTIVO;

    @Column(precision=12, scale=2) private BigDecimal montoRecibido = BigDecimal.ZERO;
    @Column(precision=12, scale=2) private BigDecimal vuelto = BigDecimal.ZERO;
    @Column(precision=12, scale=2) private BigDecimal saldoPendiente = BigDecimal.ZERO;
    private java.time.LocalDate fechaVencimientoCredito;

    @Enumerated(EnumType.STRING)
    private EstadoVenta estado = EstadoVenta.COMPLETADA;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<DetalleVenta> detalles = new ArrayList<>();

    public enum EstadoVenta { COMPLETADA, ANULADA }
    public enum TipoPago { EFECTIVO, YAPE, TARJETA, CREDITO }

    public Long getId() { return id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getVendedor() { return vendedor; }
    public void setVendedor(Usuario vendedor) { this.vendedor = vendedor; }
    public Cliente getCliente(){return cliente;} public void setCliente(Cliente cliente){this.cliente=cliente;}
    public TipoPago getTipoPago(){return tipoPago;} public void setTipoPago(TipoPago tipoPago){this.tipoPago=tipoPago;}
    public BigDecimal getMontoRecibido(){return montoRecibido;} public void setMontoRecibido(BigDecimal v){montoRecibido=v;}
    public BigDecimal getVuelto(){return vuelto;} public void setVuelto(BigDecimal v){vuelto=v;}
    public BigDecimal getSaldoPendiente(){return saldoPendiente;} public void setSaldoPendiente(BigDecimal v){saldoPendiente=v;}
    public java.time.LocalDate getFechaVencimientoCredito(){return fechaVencimientoCredito;} public void setFechaVencimientoCredito(java.time.LocalDate v){fechaVencimientoCredito=v;}
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public EstadoVenta getEstado() { return estado; }
    public void setEstado(EstadoVenta estado) { this.estado = estado; }
    public List<DetalleVenta> getDetalles() { return detalles; }
    public void addDetalle(DetalleVenta detalle) { detalles.add(detalle); detalle.setVenta(this); }
}
