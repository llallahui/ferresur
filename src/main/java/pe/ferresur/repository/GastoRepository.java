package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param;
import pe.ferresur.model.Gasto; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.List;
public interface GastoRepository extends JpaRepository<Gasto,Long>{
 List<Gasto> findByFechaBetweenOrderByFechaDesc(LocalDateTime d,LocalDateTime h);
 @Query("select coalesce(sum(g.monto),0) from Gasto g where g.fecha between :d and :h") BigDecimal sumBetween(@Param("d")LocalDateTime d,@Param("h")LocalDateTime h);
}
