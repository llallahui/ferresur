package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferresur.model.Cliente;
import java.util.List;
import java.util.Optional;
public interface ClienteRepository extends JpaRepository<Cliente,Long>{
 List<Cliente> findByActivoTrueOrderByNombreAsc(); Optional<Cliente> findByDocumento(String documento);
}
