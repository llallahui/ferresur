package pe.ferresur.model;
import jakarta.persistence.*;
@Entity @Table(name="sistema_config",uniqueConstraints=@UniqueConstraint(columnNames="clave")) public class SistemaConfig{
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; @Column(nullable=false,unique=true) private String clave; private String valor;
 public SistemaConfig(){} public SistemaConfig(String c,String v){clave=c;valor=v;} public Long getId(){return id;} public String getClave(){return clave;} public void setClave(String v){clave=v;} public String getValor(){return valor;} public void setValor(String v){valor=v;}
}
