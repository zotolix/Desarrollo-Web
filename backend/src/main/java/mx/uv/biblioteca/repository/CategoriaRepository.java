package mx.uv.biblioteca.repository;

import mx.uv.biblioteca.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    boolean existsByNombre(String nombre);

    // Para PUT: el nombre puede repetirse solo si es de la misma categoría
    boolean existsByNombreAndIdNot(String nombre, Integer id);
}
