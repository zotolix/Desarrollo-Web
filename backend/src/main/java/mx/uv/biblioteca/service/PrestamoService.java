package mx.uv.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import mx.uv.biblioteca.dto.PrestamoActualizarRequest;
import mx.uv.biblioteca.dto.PrestamoRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.DatoInvalidoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.EstadoPrestamo;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.model.Prestamo;
import mx.uv.biblioteca.model.Socio;
import mx.uv.biblioteca.repository.LibroRepository;
import mx.uv.biblioteca.repository.PrestamoRepository;
import mx.uv.biblioteca.repository.SocioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrestamoService {

    private static final int MAX_PRESTAMOS_ACTIVOS = 3; // RN-04
    private static final int DIAS_PRESTAMO = 7;         // RN-07

    private final PrestamoRepository prestamos;
    private final LibroRepository libros;
    private final SocioRepository socios;

    public PrestamoService(PrestamoRepository prestamos, LibroRepository libros, SocioRepository socios) {
        this.prestamos = prestamos;
        this.libros = libros;
        this.socios = socios;
    }

    /** Lista préstamos. Los filtros son opcionales y se pueden combinar. */
    public List<Prestamo> listar(Integer socioId, EstadoPrestamo estado) {
        if (socioId != null && estado != null) {
            return prestamos.findBySocioIdAndEstado(socioId, estado);
        }
        if (socioId != null) {
            return prestamos.findBySocioId(socioId);
        }
        if (estado != null) {
            return prestamos.findByEstado(estado);
        }
        return prestamos.findAll();
    }

    public Prestamo obtener(Integer id) {
        return prestamos.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el préstamo " + id));
    }

    @Transactional
    public Prestamo crear(PrestamoRequest req) {
        Libro libro = libros.findById(req.libroId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + req.libroId()));
        Socio socio = socios.findById(req.socioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el socio " + req.socioId()));

        if (socio.getEstado() == EstadoSocio.SUSPENDIDO) { // RN-03
            throw new ConflictoException("El socio " + socio.getId() + " está suspendido");
        }
        // RN-04: cuentan los préstamos sin devolver (ACTIVO o VENCIDO)
        if (prestamos.countBySocioIdAndEstadoNot(socio.getId(), EstadoPrestamo.DEVUELTO) >= MAX_PRESTAMOS_ACTIVOS) {
            throw new ConflictoException("El socio ya tiene " + MAX_PRESTAMOS_ACTIVOS + " préstamos activos");
        }
        if (libro.getEjemplaresDisponibles() <= 0) { // RN-01
            throw new ConflictoException("No hay ejemplares disponibles del libro " + libro.getId());
        }

        libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() - 1); // RN-02
        libros.save(libro);

        Prestamo p = new Prestamo();
        p.setLibro(libro);
        p.setSocio(socio);
        p.setFechaPrestamo(LocalDate.now());
        p.setFechaLimite(LocalDate.now().plusDays(DIAS_PRESTAMO)); // RN-07
        p.setEstado(EstadoPrestamo.ACTIVO);
        return prestamos.save(p);
    }

    /** Registra la devolución: el préstamo queda DEVUELTO y el libro recupera un ejemplar (RN-02). */
    @Transactional
    public Prestamo devolver(Integer id) {
        Prestamo p = obtener(id);
        if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ConflictoException("El préstamo " + id + " ya fue devuelto");
        }
        Libro libro = p.getLibro();
        // RN-08: los disponibles nunca superan a los totales
        if (libro.getEjemplaresDisponibles() < libro.getEjemplaresTotales()) {
            libro.setEjemplaresDisponibles(libro.getEjemplaresDisponibles() + 1);
            libros.save(libro);
        }
        p.setFechaDevolucion(LocalDate.now());
        p.setEstado(EstadoPrestamo.DEVUELTO);
        return prestamos.save(p);
    }

    /** Cambia la fecha límite (por ejemplo, para renovar el préstamo). */
    @Transactional
    public Prestamo actualizar(Integer id, PrestamoActualizarRequest req) {
        Prestamo p = obtener(id);
        if (p.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new ConflictoException("No se puede modificar el préstamo " + id + " porque ya fue devuelto");
        }
        if (req.fechaLimite().isBefore(p.getFechaPrestamo())) {
            throw new DatoInvalidoException("La fecha límite no puede ser anterior a la fecha de préstamo");
        }
        p.setFechaLimite(req.fechaLimite());
        return prestamos.save(p);
    }

    /** Solo se eliminan préstamos ya devueltos, para no perder el control de un libro prestado. */
    public void eliminar(Integer id) {
        Prestamo p = obtener(id);
        if (p.getEstado() != EstadoPrestamo.DEVUELTO) {
            throw new ConflictoException("Solo se pueden eliminar préstamos devueltos");
        }
        prestamos.delete(p);
    }
}
