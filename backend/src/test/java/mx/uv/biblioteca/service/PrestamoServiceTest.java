package mx.uv.biblioteca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PrestamoServiceTest {

    @Mock
    private PrestamoRepository prestamos;

    @Mock
    private LibroRepository libros;

    @Mock
    private SocioRepository socios;

    @InjectMocks
    private PrestamoService service;

    private Libro libro(int totales, int disponibles) {
        Libro l = new Libro();
        l.setId(1);
        l.setTitulo("Clean Code");
        l.setEjemplaresTotales(totales);
        l.setEjemplaresDisponibles(disponibles);
        return l;
    }

    private Socio socio(EstadoSocio estado) {
        Socio s = new Socio();
        s.setId(1);
        s.setNombre("Ana");
        s.setEstado(estado);
        return s;
    }

    private Prestamo prestamo(EstadoPrestamo estado, Libro libro) {
        Prestamo p = new Prestamo();
        p.setId(10);
        p.setLibro(libro);
        p.setSocio(socio(EstadoSocio.ACTIVO));
        p.setFechaPrestamo(LocalDate.of(2026, 11, 20));
        p.setFechaLimite(LocalDate.of(2026, 11, 27));
        p.setEstado(estado);
        return p;
    }

    // ---------- crear ----------

    @Test
    void crear_valido_descuentaEjemplarYFijaFechaLimite_RN02_RN07() {
        Libro l = libro(3, 2);
        when(libros.findById(1)).thenReturn(Optional.of(l));
        when(socios.findById(1)).thenReturn(Optional.of(socio(EstadoSocio.ACTIVO)));
        when(prestamos.countBySocioIdAndEstadoNot(1, EstadoPrestamo.DEVUELTO)).thenReturn(0L);
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo p = service.crear(new PrestamoRequest(1, 1));

        assertEquals(1, l.getEjemplaresDisponibles());
        assertEquals(EstadoPrestamo.ACTIVO, p.getEstado());
        assertEquals(LocalDate.now(), p.getFechaPrestamo());
        assertEquals(LocalDate.now().plusDays(7), p.getFechaLimite());
        verify(libros).save(l);
    }

    @Test
    void crear_sinEjemplares_lanzaConflicto_RN01() {
        Libro l = libro(1, 0);
        when(libros.findById(1)).thenReturn(Optional.of(l));
        when(socios.findById(1)).thenReturn(Optional.of(socio(EstadoSocio.ACTIVO)));
        when(prestamos.countBySocioIdAndEstadoNot(1, EstadoPrestamo.DEVUELTO)).thenReturn(0L);

        assertThrows(ConflictoException.class, () -> service.crear(new PrestamoRequest(1, 1)));
        assertEquals(0, l.getEjemplaresDisponibles());
        verify(prestamos, never()).save(any(Prestamo.class));
    }

    @Test
    void crear_socioSuspendido_lanzaConflicto_RN03() {
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, 1)));
        when(socios.findById(1)).thenReturn(Optional.of(socio(EstadoSocio.SUSPENDIDO)));

        assertThrows(ConflictoException.class, () -> service.crear(new PrestamoRequest(1, 1)));
        verify(prestamos, never()).save(any(Prestamo.class));
    }

    @Test
    void crear_socioConTresPrestamos_lanzaConflicto_RN04() {
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, 1)));
        when(socios.findById(1)).thenReturn(Optional.of(socio(EstadoSocio.ACTIVO)));
        when(prestamos.countBySocioIdAndEstadoNot(1, EstadoPrestamo.DEVUELTO)).thenReturn(3L);

        assertThrows(ConflictoException.class, () -> service.crear(new PrestamoRequest(1, 1)));
        verify(prestamos, never()).save(any(Prestamo.class));
    }

    @Test
    void crear_libroInexistente_lanzaNoEncontrado() {
        when(libros.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(new PrestamoRequest(1, 1)));
    }

    @Test
    void crear_socioInexistente_lanzaNoEncontrado() {
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, 1)));
        when(socios.findById(1)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(new PrestamoRequest(1, 1)));
    }

    // ---------- devolver ----------

    @Test
    void devolver_activo_sumaEjemplarYMarcaDevuelto_RN02() {
        Libro l = libro(3, 1);
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.ACTIVO, l)));
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo p = service.devolver(10);

        assertEquals(EstadoPrestamo.DEVUELTO, p.getEstado());
        assertEquals(LocalDate.now(), p.getFechaDevolucion());
        assertEquals(2, l.getEjemplaresDisponibles());
    }

    @Test
    void devolver_vencido_tambienSePuedeDevolver() {
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.VENCIDO, libro(1, 0))));
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(EstadoPrestamo.DEVUELTO, service.devolver(10).getEstado());
    }

    @Test
    void devolver_nuncaSuperaLosTotales_RN08() {
        Libro l = libro(2, 2); // dato inconsistente: ya están todos disponibles
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.ACTIVO, l)));
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        service.devolver(10);

        assertEquals(2, l.getEjemplaresDisponibles());
        verify(libros, never()).save(l);
    }

    @Test
    void devolver_yaDevuelto_lanzaConflicto() {
        Libro l = libro(3, 3);
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.DEVUELTO, l)));

        assertThrows(ConflictoException.class, () -> service.devolver(10));
        assertEquals(3, l.getEjemplaresDisponibles());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_cambiaFechaLimite() {
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.ACTIVO, libro(1, 0))));
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        Prestamo p = service.actualizar(10, new PrestamoActualizarRequest(LocalDate.of(2026, 12, 4)));

        assertEquals(LocalDate.of(2026, 12, 4), p.getFechaLimite());
        assertNull(p.getFechaDevolucion());
    }

    @Test
    void actualizar_fechaAnteriorAlPrestamo_lanzaDatoInvalido() {
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.ACTIVO, libro(1, 0))));

        assertThrows(DatoInvalidoException.class,
                () -> service.actualizar(10, new PrestamoActualizarRequest(LocalDate.of(2026, 11, 1))));
    }

    @Test
    void actualizar_prestamoDevuelto_lanzaConflicto() {
        when(prestamos.findById(10)).thenReturn(Optional.of(prestamo(EstadoPrestamo.DEVUELTO, libro(1, 1))));

        assertThrows(ConflictoException.class,
                () -> service.actualizar(10, new PrestamoActualizarRequest(LocalDate.of(2026, 12, 4))));
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_devuelto_borra() {
        Prestamo p = prestamo(EstadoPrestamo.DEVUELTO, libro(1, 1));
        when(prestamos.findById(10)).thenReturn(Optional.of(p));

        service.eliminar(10);

        verify(prestamos).delete(p);
    }

    @Test
    void eliminar_activo_lanzaConflicto() {
        Prestamo p = prestamo(EstadoPrestamo.ACTIVO, libro(1, 0));
        when(prestamos.findById(10)).thenReturn(Optional.of(p));

        assertThrows(ConflictoException.class, () -> service.eliminar(10));
        verify(prestamos, never()).delete(p);
    }

    // ---------- listar ----------

    @Test
    void listar_socioYEstado_usaElFiltroCombinado() {
        when(prestamos.findBySocioIdAndEstado(1, EstadoPrestamo.ACTIVO)).thenReturn(List.of());

        service.listar(1, EstadoPrestamo.ACTIVO);

        verify(prestamos).findBySocioIdAndEstado(1, EstadoPrestamo.ACTIVO);
    }

    @Test
    void listar_sinFiltros_regresaTodos() {
        when(prestamos.findAll()).thenReturn(List.of(prestamo(EstadoPrestamo.ACTIVO, libro(1, 0))));

        assertEquals(1, service.listar(null, null).size());
    }
}
