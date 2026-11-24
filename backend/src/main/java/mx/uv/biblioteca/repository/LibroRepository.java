package mx.uv.biblioteca.repository;

import mx.uv.biblioteca.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroRepository extends JpaRepository<Libro, Integer> {

    boolean existsByIsbn(String isbn);

    // Para PUT: el ISBN puede repetirse solo si es del mismo libro
    boolean existsByIsbnAndIdNot(String isbn, Integer id);

    // RN-06: no se elimina una categoría que tiene libros
    boolean existsByCategoriaId(Integer categoriaId);
}
