package pe.ferresur.config;
import org.springframework.boot.CommandLineRunner; import org.springframework.context.annotation.Bean; import org.springframework.context.annotation.Configuration; import org.springframework.security.crypto.password.PasswordEncoder; import pe.ferresur.model.*; import pe.ferresur.repository.*;
import java.math.BigDecimal;
@Configuration public class DataInitializer{
 @Bean CommandLineRunner initData(UsuarioRepository usuarios,CategoriaRepository categorias,ProductoRepository productos,PasswordEncoder encoder,SistemaConfigRepository config,VentaRepository ventas,AbonoCreditoRepository abonos,GastoRepository gastos,CajaRepository cajas,ClienteRepository clientes){return args->{
  if(usuarios.count()==0){Usuario a=new Usuario();a.setNombreCompleto("Admin FerreSur");a.setUsername("admin");a.setPassword(encoder.encode("admin123"));a.setRol(Rol.ADMINISTRADOR);usuarios.save(a);Usuario v=new Usuario();v.setNombreCompleto("Vendedor FerreSur");v.setUsername("vendedor");v.setPassword(encoder.encode("vendedor123"));v.setRol(Rol.VENDEDOR);usuarios.save(v);}
  String[] nombres={"Herramientas","Herramientas Eléctricas","Construcción","Pinturas","Plomería","Otros"};for(String n:nombres)if(categorias.findByNombreIgnoreCase(n).isEmpty())categorias.save(new Categoria(n));
  // Primera ejecución de esta versión: limpiar datos de prueba anteriores para dejar el sistema listo para pruebas.
  if(config.findByClave("DATOS_LIMPIOS_V2").isEmpty()){
    abonos.deleteAll(); ventas.deleteAll(); gastos.deleteAll(); cajas.deleteAll(); productos.deleteAll(); clientes.deleteAll();
    config.save(new SistemaConfig("DATOS_LIMPIOS_V2","ok"));
  }
  if(Boolean.parseBoolean(System.getenv().getOrDefault("FERRESUR_SEED_PRODUCTS","false")) && productos.count()==0){add(productos,categorias,"FER-001","Martillo","Martillo de acero","25.50",45,5,"Herramientas");add(productos,categorias,"FER-002","Taladro 500W","Taladro percutor","189.90",8,5,"Herramientas Eléctricas");}
 };}
 private void add(ProductoRepository pr,CategoriaRepository cr,String c,String n,String d,String precio,int stock,int min,String cat){Producto x=new Producto();x.setCodigo(c);x.setNombre(n);x.setDescripcion(d);x.setPrecio(new BigDecimal(precio));x.setCosto(new BigDecimal(precio).multiply(new BigDecimal("0.70")));x.setStock(stock);x.setStockMinimo(min);x.setCategoria(cr.findByNombreIgnoreCase(cat).orElseThrow());pr.save(x);}
}
