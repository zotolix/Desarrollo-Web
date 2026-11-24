package mx.uv.biblioteca.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import mx.uv.biblioteca.dto.LibroRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.repository.CategoriaRepository;
import mx.uv.biblioteca.repository.LibroRepository;
import mx.uv.biblioteca.repository.PrestamoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibroServiceTest {

    @Mock
    private LibroRepository libros;

    @Mock
    private CategoriaRepository categorias;

    @Mock
    private PrestamoRepository prestamos;

    @InjectMocks
    private LibroService service;

    private Categoria categoria(int id) {
        Categoria c = new Categoria();
        c.setId(id);
        c.setNombre("Categoría " + id);
        return c;
    }

    private Libro libro(int id, String titulo, String autor, int totales, int disponibles, int categoriaId) {
        Libro l = new Libro();
        l.setId(id);
        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setIsbn("ISBN-" + id);
        l.setEjemplaresTotales(totales);
        l.setEjemplaresDisponibles(disponibles);
        l.setCategoria(categoria(categoriaId));
        return l;
    }

    private LibroRequest request(String isbn, int totales) {
        return new LibroRequest("Clean Code", "Robert C. Martin", isbn, 2008, totales, 3);
    }

    // ---------- crear ----------

    @Test
    void crear_todosLosEjemplaresQuedanDisponibles() {
        when(libros.existsByIsbn("123")).thenReturn(false);
        when(categorias.findById(3)).thenReturn(Optional.of(categoria(3)));
        when(libros.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro l = service.crear(request("123", 4));

        assertEquals(4, l.getEjemplaresTotales());
        assertEquals(4, l.getEjemplaresDisponibles());
        assertEquals(3, l.getCategoria().getId());
    }

    @Test
    void crear_isbnRepetido_lanzaConflicto_RN05() {
        when(libros.existsByIsbn("123")).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.crear(request("123", 1)));
        verify(libros, never()).save(any(Libro.class));
    }

    @Test
    void crear_categoriaInexistente_lanzaNoEncontrado() {
        when(libros.existsByIsbn("123")).thenReturn(false);
        when(categorias.findById(3)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class, () -> service.crear(request("123", 1)));
    }

    // ---------- buscar ----------

    @Test
    void buscar_filtraPorTituloSinImportarMayusculas() {
        when(libros.findAll()).thenReturn(List.of(
                libro(1, "Cien años de soledad", "García Márquez", 3, 3, 1),
                libro(2, "Clean Code", "Martin", 2, 2, 3)));

        List<Libro> r = service.buscar("CIEN", null, null, null);

        assertEquals(1, r.size());
        assertEquals(1, r.get(0).getId());
    }

    @Test
    void buscar_soloDisponibles_excluyeLibrosSinEjemplares() {
        when(libros.findAll()).thenReturn(List.of(
                libro(1, "A", "X", 1, 0, 1),
                libro(2, "B", "X", 2, 1, 1)));

        List<Libro> r = service.buscar(null, null, null, true);

        assertEquals(1, r.size());
        assertEquals(2, r.get(0).getId());
    }

    @Test
    void buscar_combinaAutorYCategoria() {
        when(libros.findAll()).thenReturn(List.of(
                libro(1, "A", "Hawking", 1, 1, 2),
                libro(2, "B", "Hawking", 1, 1, 3),
                libro(3, "C", "Sagan", 1, 1, 2)));

        List<Libro> r = service.buscar(null, "hawk", 2, null);

        assertEquals(1, r.size());
        assertEquals(1, r.get(0).getId());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_conservaLosEjemplaresPrestados_RN08() {
        // 3 totales, 1 disponible: hay 2 prestados
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, "Clean Code", "Martin", 3, 1, 3)));
        when(libros.existsByIsbnAndIdNot("123", 1)).thenReturn(false);
        when(categorias.findById(3)).thenReturn(Optional.of(categoria(3)));
        when(libros.save(any(Libro.class))).thenAnswer(inv -> inv.getArgument(0));

        Libro l = service.actualizar(1, request("123", 5));

        assertEquals(5, l.getEjemplaresTotales());
        assertEquals(3, l.getEjemplaresDisponibles());
    }

    @Test
    void actualizar_totalesMenoresQuePrestados_lanzaConflicto() {
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, "Clean Code", "Martin", 3, 0, 3)));
        when(libros.existsByIsbnAndIdNot("123", 1)).thenReturn(false);

        assertThrows(ConflictoException.class, () -> service.actualizar(1, request("123", 2)));
        verify(libros, never()).save(any(Libro.class));
    }

    @Test
    void actualizar_isbnDeOtroLibro_lanzaConflicto() {
        when(libros.findById(1)).thenReturn(Optional.of(libro(1, "Clean Code", "Martin", 3, 3, 3)));
        when(libros.existsByIsbnAndIdNot("999", 1)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.actualizar(1, request("999", 3)));
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_sinPrestamos_borra() {
        Libro l = libro(1, "A", "X", 1, 1, 1);
        when(libros.findById(1)).thenReturn(Optional.of(l));
        when(prestamos.existsByLibroId(1)).thenReturn(false);

        service.eliminar(1);

        verify(libros).delete(l);
    }

    @Test
    void eliminar_conPrestamos_lanzaConflicto_RN06() {
        Libro l = libro(1, "A", "X", 1, 1, 1);
        when(libros.findById(1)).thenReturn(Optional.of(l));
        when(prestamos.existsByLibroId(1)).thenReturn(true);

        assertThrows(ConflictoException.class, () -> service.eliminar(1));
        verify(libros, never()).delete(l);
    }
}
