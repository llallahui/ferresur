package pe.ferresur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.ferresur.model.Producto;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByNombreAsc();
    List<Producto> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo);
    long countByActivoTrue();
    long countByCategoria_IdAndActivoTrue(Long categoriaId);

    @Query("select count(p) from Producto p where p.activo=true and p.stock <= p.stockMinimo")
    long countStockBajo();

    @Query("select p from Producto p where p.activo=true and p.stock <= p.stockMinimo order by p.stock asc, p.nombre asc")
    List<Producto> findStockBajo();

}
