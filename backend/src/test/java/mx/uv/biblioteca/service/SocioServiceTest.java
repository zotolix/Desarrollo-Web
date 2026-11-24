package mx.uv.biblioteca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import mx.uv.biblioteca.dto.SocioActualizarRequest;
import mx.uv.biblioteca.dto.SocioRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Socio;
import mx.uv.biblioteca.repository.SocioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class SocioServiceTest {

    @Mock
    private SocioRepository repo;

    @InjectMocks
    private SocioService service;

    private Socio socio(int id, String nombre, String correo) {
        Socio s = new Socio();
        s.setId(id);
        s.setNombre(nombre);
        s.setCorreo(correo);
        s.setEstado(EstadoSocio.ACTIVO);
        s.setFechaRegistro(LocalDate.now());
        return s;
    }

    // ---------- crear ----------

    @Test
    void crear_asignaEstadoActivoYFechaDeHoy() {
        when(repo.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(repo.save(any(Socio.class))).thenAnswer(inv -> inv.getArgument(0));

        Socio creado = service.crear(new SocioRequest("Ana", "ana@correo.com", "2281234567"));

        assertEquals(EstadoSocio.ACTIVO, creado.getEstado());
        assertEquals(LocalDate.now(), creado.getFechaRegistro());
        assertEquals("Ana", creado.getNombre());
    }

    @Test
    void crear_correoRepetido_lanzaConflictoYNoGuarda() {
        when(repo.existsByCorreo("ana@correo.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.crear(new SocioRequest("Ana", "ana@correo.com", null)));
        verify(repo, never()).save(any(Socio.class));
    }

    // ---------- obtener ----------

    @Test
    void obtener_noExiste_lanzaNoEncontrado() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99));
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_cambiaDatosYEstado() {
        Socio existente = socio(1, "Ana", "ana@correo.com");
        when(repo.findById(1)).thenReturn(Optional.of(existente));
        when(repo.existsByCorreoAndIdNot("nueva@correo.com", 1)).thenReturn(false);
        when(repo.save(any(Socio.class))).thenAnswer(inv -> inv.getArgument(0));

        Socio r = service.actualizar(1,
                new SocioActualizarRequest("Ana María", "nueva@correo.com", "228", EstadoSocio.SUSPENDIDO));

        assertEquals("Ana María", r.getNombre());
        assertEquals("nueva@correo.com", r.getCorreo());
        assertEquals(EstadoSocio.SUSPENDIDO, r.getEstado());
    }

    @Test
    void actualizar_correoDeOtroSocio_lanzaConflicto() {
        when(repo.findById(1)).thenReturn(Optional.of(socio(1, "Ana", "ana@correo.com")));
        when(repo.existsByCorreoAndIdNot("luis@correo.com", 1)).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> service.actualizar(1,
                        new SocioActualizarRequest("Ana", "luis@correo.com", null, EstadoSocio.ACTIVO)));
        verify(repo, never()).save(any(Socio.class));
    }

    @Test
    void actualizar_noExiste_lanzaNoEncontrado() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> service.actualizar(99,
                        new SocioActualizarRequest("X", "x@correo.com", null, EstadoSocio.ACTIVO)));
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_sinPrestamos_borraElSocio() {
        Socio existente = socio(1, "Ana", "ana@correo.com");
        when(repo.findById(1)).thenReturn(Optional.of(existente));

        service.eliminar(1);

        verify(repo).delete(existente);
    }

    @Test
    void eliminar_conPrestamos_lanzaConflicto() {
        Socio existente = socio(1, "Ana", "ana@correo.com");
        when(repo.findById(1)).thenReturn(Optional.of(existente));
        doThrow(new DataIntegrityViolationException("fk_prestamo_socio")).when(repo).delete(existente);

        assertThrows(ConflictoException.class, () -> service.eliminar(1));
    }

    @Test
    void eliminar_noExiste_lanzaNoEncontrado() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.eliminar(99));
    }

    // ---------- listar con filtros ----------

    @Test
    void listar_sinFiltros_regresaTodos() {
        List<Socio> todos = List.of(socio(1, "Ana", "a@c.com"), socio(2, "Luis", "l@c.com"));
        when(repo.findAll()).thenReturn(todos);

        assertEquals(2, service.listar(null, null).size());
    }

    @Test
    void listar_nombreEnBlanco_seIgnora() {
        when(repo.findAll()).thenReturn(List.of());

        service.listar("   ", null);

        verify(repo).findAll();
    }

    @Test
    void listar_soloNombre() {
        when(repo.findByNombreContainingIgnoreCase("ana")).thenReturn(List.of(socio(1, "Ana", "a@c.com")));

        assertEquals(1, service.listar("ana", null).size());
    }

    @Test
    void listar_soloEstado() {
        when(repo.findByEstado(EstadoSocio.SUSPENDIDO)).thenReturn(List.of());

        assertEquals(0, service.listar(null, EstadoSocio.SUSPENDIDO).size());
    }

    @Test
    void listar_nombreYEstado() {
        when(repo.findByNombreContainingIgnoreCaseAndEstado("ana", EstadoSocio.ACTIVO))
                .thenReturn(List.of(socio(1, "Ana", "a@c.com")));

        assertEquals(1, service.listar("ana", EstadoSocio.ACTIVO).size());
    }
}
