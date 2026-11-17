package mx.uv.biblioteca.service;

import java.util.List;
import mx.uv.biblioteca.dto.LibroRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.repository.CategoriaRepository;
import mx.uv.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;

@Service
public class LibroService {

    private final LibroRepository libros;
    private final CategoriaRepository categorias;

    public LibroService(LibroRepository libros, CategoriaRepository categorias) {
        this.libros = libros;
        this.categorias = categorias;
    }

    public List<Libro> listar() {
        return libros.findAll();
    }

    public Libro obtener(Integer id) {
        return libros.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el libro " + id));
    }

    public Libro crear(LibroRequest req) {
        if (libros.existsByIsbn(req.isbn())) {
            throw new ConflictoException("Ya existe un libro con el ISBN " + req.isbn());
        }
        Categoria categoria = categorias.findById(req.categoriaId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la categoría " + req.categoriaId()));

        Libro l = new Libro();
        l.setTitulo(req.titulo());
        l.setAutor(req.autor());
        l.setIsbn(req.isbn());
        l.setAnioPublicacion(req.anioPublicacion());
        l.setEjemplaresTotales(req.ejemplaresTotales());
        l.setEjemplaresDisponibles(req.ejemplaresTotales()); // al inicio todos están disponibles
        l.setCategoria(categoria);
        return libros.save(l);
    }
}
