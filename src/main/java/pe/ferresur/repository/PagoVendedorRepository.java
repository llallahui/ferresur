package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository; import pe.ferresur.model.PagoVendedor; import java.util.List;
public interface PagoVendedorRepository extends JpaRepository<PagoVendedor,Long>{ List<PagoVendedor> findAllByOrderByFechaProgramadaAsc(); long countByEstado(PagoVendedor.Estado estado); }
