package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferresur.model.Caja;
import java.time.LocalDate; import java.util.Optional;
public interface CajaRepository extends JpaRepository<Caja,Long>{ Optional<Caja> findByFechaAndEstado(LocalDate fecha,Caja.Estado estado); Optional<Caja> findTopByOrderByFechaDesc(); }
