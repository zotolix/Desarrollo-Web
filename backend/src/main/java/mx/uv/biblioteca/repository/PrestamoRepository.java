package mx.uv.biblioteca.repository;

import mx.uv.biblioteca.model.EstadoPrestamo;
import mx.uv.biblioteca.model.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository extends JpaRepository<Prestamo, Integer> {
    long countBySocioIdAndEstado(Integer socioId, EstadoPrestamo estado);
}
