package pe.ferresur.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Entity
@Table(name="clientes")
public class Cliente {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @NotBlank(message="El nombre es obligatorio") private String nombre;
    @Column(unique=true) private String documento;
    private String telefono;
    private String email;
    private String direccion;
    @Column(precision=12, scale=2) private BigDecimal limiteCredito=BigDecimal.ZERO;
    private boolean activo=true;
    public Cliente(){}
    public Long getId(){return id;} public String getNombre(){return nombre;} public void setNombre(String v){nombre=v;}
    public String getDocumento(){return documento;} public void setDocumento(String v){documento=v;}
    public String getTelefono(){return telefono;} public void setTelefono(String v){telefono=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getDireccion(){return direccion;} public void setDireccion(String v){direccion=v;}
    public BigDecimal getLimiteCredito(){return limiteCredito;} public void setLimiteCredito(BigDecimal v){limiteCredito=v;}
    public boolean isActivo(){return activo;} public void setActivo(boolean v){activo=v;}
}
