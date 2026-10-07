package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository; import org.springframework.data.jpa.repository.Query; import org.springframework.data.repository.query.Param; import pe.ferresur.model.*; import java.math.BigDecimal; import java.time.LocalDateTime; import java.util.*;
public interface VentaRepository extends JpaRepository<Venta,Long>{
 Optional<Venta> findByNumero(String numero); Optional<Venta> findTopByOrderByIdDesc(); List<Venta> findTop10ByOrderByFechaDesc(); List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime d,LocalDateTime h); List<Venta> findByFechaBetweenOrderByFechaAsc(LocalDateTime d,LocalDateTime h);
 List<Venta> findByClienteAndSaldoPendienteGreaterThanOrderByFechaAsc(Cliente cliente,BigDecimal saldo);
 long countByEstado(Venta.EstadoVenta estado); long countBySaldoPendienteGreaterThan(BigDecimal saldo);
 @Query("select coalesce(sum(v.total),0) from Venta v where v.estado='COMPLETADA' and v.fecha between :d and :h") BigDecimal sumTotalBetween(@Param("d")LocalDateTime d,@Param("h")LocalDateTime h);
 @Query("select coalesce(sum(d.cantidad),0) from DetalleVenta d where d.venta.estado='COMPLETADA' and d.venta.fecha between :d and :h") Long sumItemsBetween(@Param("d")LocalDateTime d,@Param("h")LocalDateTime h);
}
