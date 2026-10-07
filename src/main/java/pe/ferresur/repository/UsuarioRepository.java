package pe.ferresur.repository;
import org.springframework.data.jpa.repository.JpaRepository; import pe.ferresur.model.Rol; import pe.ferresur.model.Usuario; import java.util.Optional;
public interface UsuarioRepository extends JpaRepository<Usuario,Long>{ Optional<Usuario> findByUsername(String username); Optional<Usuario> findByUsernameIgnoreCase(String username); long countByRol(Rol rol); long countByActivoTrue(); }
