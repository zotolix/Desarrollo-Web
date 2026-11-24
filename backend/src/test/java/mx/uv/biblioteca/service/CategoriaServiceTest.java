package mx.uv.biblioteca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import mx.uv.biblioteca.dto.CategoriaRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.repository.CategoriaRepository;
import mx.uv.biblioteca.repository.LibroRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository repo;

    @Mock
    private LibroRepository libros;

    @InjectMocks
    private CategoriaService service;

    private Categoria categoria(int id, String nombre) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre(nombre);
        return c;
    }

    @Test
    void crear_nombreNuevo_guarda() {
        when(repo.existsByNombre("Historia")).thenReturn(false);
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria c = service.crear(new CategoriaRequest("Historia", "Libros de historia"));

        assertEquals("Historia", c.getNombre());
        assertEquals("Libros de historia", c.getDescripcion());
    }

    @Test
    void crear_nombreRepetido_lanzaConflictoYNoGuarda() {
        when(repo.existsByNombre("Novela")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(new CategoriaRequest("Novela", null)));
        verify(repo, never()).save(any(Categoria.class));
    }

    @Test
    void obtener_noExiste_lanzaNoEncontrado() {
        when(repo.findById(99)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener(99));
    }

    @Test
    void actualizar_conservaSuPropioNombre() {
        when(repo.findById(1)).thenReturn(Optional.of(categoria(1, "Novela")));
        when(repo.existsByNombreAndIdNot("Novela", 1)).thenReturn(false);
        when(repo.save(any(Categoria.class))).thenAnswer(inv -> inv.getArgument(0));

        Categoria c = service.actualizar(1, new CategoriaRequest("Novela", "Nueva descripción"));

        assertEquals("Nueva descripción", c.getDescripcion());
    }

    @Test
    void actualizar_nombreDeOtraCategoria_lanzaConflicto() {
        when(repo.findById(1)).thenReturn(Optional.of(categoria(1, "Novela")));
        when(repo.existsByNombreAndIdNot("Ciencia", 1)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.actualizar(1, new CategoriaRequest("Ciencia", null)));
        verify(repo, never()).save(any(Categoria.class));
    }

    @Test
    void eliminar_sinLibros_borra() {
        Categoria c = categoria(4, "Poesía");
        when(repo.findById(4)).thenReturn(Optional.of(c));
        when(libros.existsByCategoriaId(4)).thenReturn(false);

        service.eliminar(4);

        verify(repo).delete(c);
    }

    @Test
    void eliminar_conLibros_lanzaConflicto_RN06() {
        Categoria c = categoria(1, "Novela");
        when(repo.findById(1)).thenReturn(Optional.of(c));
        when(libros.existsByCategoriaId(1)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.eliminar(1));
        verify(repo, never()).delete(c);
    }
}
