package pe.ferresur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.ferresur.model.Categoria;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    List<Categoria> findByActivoTrueOrderByNombreAsc();
}
