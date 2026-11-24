package mx.uv.biblioteca.service;

import java.util.List;
import mx.uv.biblioteca.dto.LibroRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.repository.CategoriaRepository;
import mx.uv.biblioteca.repository.LibroRepository;
import mx.uv.biblioteca.repository.PrestamoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibroService {

    private final LibroRepository libros;
    private final CategoriaRepository categorias;
    private final PrestamoRepository prestamos;

    public LibroService(LibroRepository libros, CategoriaRepository categorias, PrestamoRepository prestamos) {
        this.libros = libros;
        this.categorias = categorias;
        this.prestamos = prestamos;
    }

    public List<Libro> listar() {
        return libros.findAll();
    }

    /**
     * Lista libros con filtros opcionales que se pueden combinar.
     * Como son cuatro filtros independientes, se filtra la lista en Java en lugar de
     * escribir un método del repositorio por cada combinación.
     */
    public List<Libro> buscar(String titulo, String autor, Integer categoriaId, Boolean disponible) {
        String t = normalizar(titulo);
        String a = normalizar(autor);
        return libros.findAll().stream()
                .filter(l -> t == null || l.getTitulo().toLowerCase().contains(t))
                .filter(l -> a == null || l.getAutor().toLowerCase().contains(a))
                .filter(l -> categoriaId == null || l.getCategoria().getId().equals(categoriaId))
                .filter(l -> !Boolean.TRUE.equals(disponible) || l.getEjemplaresDisponibles() > 0)
                .toList();
    }

    public Libro obtener(Integer id) {
        return libros.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + id));
    }

    public Libro crear(LibroRequest req) {
        if (libros.existsByIsbn(req.isbn())) { // RN-05
            throw new ConflictoException("Ya existe un libro con el ISBN " + req.isbn());
        }
        Libro l = new Libro();
        copiarDatos(req, l);
        l.setEjemplaresTotales(req.ejemplaresTotales());
        l.setEjemplaresDisponibles(req.ejemplaresTotales()); // al inicio todos están disponibles
        return libros.save(l);
    }

    /**
     * RN-08: si cambian los ejemplares totales, los prestados se conservan.
     * No se permite bajar los totales por debajo de los ejemplares que están prestados.
     */
    @Transactional
    public Libro actualizar(Integer id, LibroRequest req) {
        Libro l = obtener(id);
        if (libros.existsByIsbnAndIdNot(req.isbn(), id)) { // RN-05
            throw new ConflictoException("Otro libro ya usa el ISBN " + req.isbn());
        }
        int prestados = l.getEjemplaresTotales() - l.getEjemplaresDisponibles();
        if (req.ejemplaresTotales() < prestados) {
            throw new ConflictoException("El libro tiene " + prestados
                    + " ejemplares prestados; los totales no pueden ser menores");
        }
        copiarDatos(req, l);
        l.setEjemplaresTotales(req.ejemplaresTotales());
        l.setEjemplaresDisponibles(req.ejemplaresTotales() - prestados);
        return libros.save(l);
    }

    /** RN-06: no se elimina un libro que tiene préstamos registrados. */
    public void eliminar(Integer id) {
        Libro l = obtener(id);
        if (prestamos.existsByLibroId(id)) {
            throw new ConflictoException("No se puede eliminar el libro " + id + " porque tiene préstamos");
        }
        libros.delete(l);
    }

    private void copiarDatos(LibroRequest req, Libro l) {
        Categoria categoria = categorias.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la categoría " + req.categoriaId()));
        l.setTitulo(req.titulo());
        l.setAutor(req.autor());
        l.setIsbn(req.isbn());
        l.setAnioPublicacion(req.anioPublicacion());
        l.setCategoria(categoria);
    }

    private String normalizar(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim().toLowerCase();
    }
}
