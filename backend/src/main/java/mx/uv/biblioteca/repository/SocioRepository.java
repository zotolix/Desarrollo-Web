package mx.uv.biblioteca.repository;

import java.util.List;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Socio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SocioRepository extends JpaRepository<Socio, Integer> {

    boolean existsByCorreo(String correo);

    // Para PUT: el correo puede repetirse solo si es del mismo socio
    boolean existsByCorreoAndIdNot(String correo, Integer id);

    // Filtros de GET /api/socios
    List<Socio> findByNombreContainingIgnoreCase(String nombre);

    List<Socio> findByEstado(EstadoSocio estado);

    List<Socio> findByNombreContainingIgnoreCaseAndEstado(String nombre, EstadoSocio estado);
}
