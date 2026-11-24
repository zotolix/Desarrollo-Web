package mx.uv.biblioteca.repository;

import java.util.List;
import mx.uv.biblioteca.model.EstadoPrestamo;
import mx.uv.biblioteca.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {

    // RN-04: cuenta los préstamos del socio que todavía no se devuelven (ACTIVO o VENCIDO)
    long countBySocioIdAndEstadoNot(Integer socioId, EstadoPrestamo estado);

    // RN-06: no se elimina un libro que tiene préstamos
    boolean existsByLibroId(Integer libroId);

    // Filtros de GET /api/prestamos
    List<Prestamo> findBySocioId(Integer socioId);

    List<Prestamo> findByEstado(EstadoPrestamo estado);

    List<Prestamo> findBySocioIdAndEstado(Integer socioId, EstadoPrestamo estado);
}
