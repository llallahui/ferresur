package pe.ferresur.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Table(name="cajas")
public class Caja {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    private LocalDate fecha=LocalDate.now();
    @Column(nullable=false, precision=12, scale=2) private BigDecimal montoInicial=BigDecimal.ZERO;
    @Column(precision=12, scale=2) private BigDecimal montoFinal=BigDecimal.ZERO;
    private LocalDateTime apertura=LocalDateTime.now();
    private LocalDateTime cierre;
    @Enumerated(EnumType.STRING) private Estado estado=Estado.ABIERTA;
    @ManyToOne(fetch=FetchType.EAGER) private Usuario abiertoPor;
    @ManyToOne(fetch=FetchType.EAGER) private Usuario cerradoPor;
    private String observaciones;
    public enum Estado{ABIERTA,CERRADA}
    public Long getId(){return id;} public LocalDate getFecha(){return fecha;} public void setFecha(LocalDate v){fecha=v;}
    public BigDecimal getMontoInicial(){return montoInicial;} public void setMontoInicial(BigDecimal v){montoInicial=v;}
    public BigDecimal getMontoFinal(){return montoFinal;} public void setMontoFinal(BigDecimal v){montoFinal=v;}
    public LocalDateTime getApertura(){return apertura;} public void setApertura(LocalDateTime v){apertura=v;}
    public LocalDateTime getCierre(){return cierre;} public void setCierre(LocalDateTime v){cierre=v;}
    public Estado getEstado(){return estado;} public void setEstado(Estado v){estado=v;}
    public Usuario getAbiertoPor(){return abiertoPor;} public void setAbiertoPor(Usuario v){abiertoPor=v;}
    public Usuario getCerradoPor(){return cerradoPor;} public void setCerradoPor(Usuario v){cerradoPor=v;}
    public String getObservaciones(){return observaciones;} public void setObservaciones(String v){observaciones=v;}
}
