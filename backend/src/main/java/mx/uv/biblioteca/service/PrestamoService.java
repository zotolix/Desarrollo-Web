package mx.uv.biblioteca.service;

import java.time.LocalDate;
import java.util.List;
import mx.uv.biblioteca.dto.PrestamoRequest;
import mx.uv.biblioteca.exception.ConflictoException;
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

    public List<Prestamo> listar() {
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
        if (prestamos.countBySocioIdAndEstado(socio.getId(), EstadoPrestamo.ACTIVO) >= MAX_PRESTAMOS_ACTIVOS) {
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
        p.setFechaLimite(LocalDate.now().plusDays(DIAS_PRESTAMO));
        p.setEstado(EstadoPrestamo.ACTIVO);
        return prestamos.save(p);
    }
}
