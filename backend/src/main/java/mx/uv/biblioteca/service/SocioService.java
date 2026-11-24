package mx.uv.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import mx.uv.biblioteca.dto.SocioActualizarRequest;
import mx.uv.biblioteca.dto.SocioRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Socio;
import mx.uv.biblioteca.repository.SocioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class SocioService {

    private final SocioRepository repo;

    public SocioService(SocioRepository repo) {
        this.repo = repo;
    }

    /** Lista socios. Los filtros son opcionales y se pueden combinar. */
    public List<Socio> listar(String nombre, EstadoSocio estado) {
        boolean hayNombre = nombre != null && !nombre.isBlank();
        if (hayNombre && estado != null) {
            return repo.findByNombreContainingIgnoreCaseAndEstado(nombre.trim(), estado);
        }
        if (hayNombre) {
            return repo.findByNombreContainingIgnoreCase(nombre.trim());
        }
        if (estado != null) {
            return repo.findByEstado(estado);
        }
        return repo.findAll();
    }

    public Socio obtener(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el socio " + id));
    }

    public Socio crear(SocioRequest req) {
        if (repo.existsByCorreo(req.correo())) { // RN-05
            throw new ConflictoException("Ya existe un socio con el correo " + req.correo());
        }
        Socio s = new Socio();
        s.setNombre(req.nombre());
        s.setCorreo(req.correo());
        s.setTelefono(req.telefono());
        s.setEstado(EstadoSocio.ACTIVO);
        s.setFechaRegistro(LocalDate.now());
        return repo.save(s);
    }

    public Socio actualizar(Integer id, SocioActualizarRequest req) {
        Socio s = obtener(id);
        if (repo.existsByCorreoAndIdNot(req.correo(), id)) { // RN-05
            throw new ConflictoException("Otro socio ya usa el correo " + req.correo());
        }
        s.setNombre(req.nombre());
        s.setCorreo(req.correo());
        s.setTelefono(req.telefono());
        s.setEstado(req.estado()); // aquí se suspende o reactiva a un socio (RN-03)
        return repo.save(s);
    }

    /**
     * RN-06: no se elimina un socio con préstamos. La llave foránea de la base de datos
     * lo impide y aquí se traduce a un 409 con un mensaje claro.
     */
    public void eliminar(Integer id) {
        Socio s = obtener(id);
        try {
            repo.delete(s);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictoException(
                    "No se puede eliminar el socio " + id + " porque tiene préstamos registrados");
        }
    }
}
