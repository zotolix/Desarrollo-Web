package mx.uv.biblioteca.service;

import java.util.List;
import mx.uv.biblioteca.dto.CategoriaRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.repository.CategoriaRepository;
import mx.uv.biblioteca.repository.LibroRepository;
import org.springframework.stereotype.Service;

@Service
public class CategoriaService {

    private final CategoriaRepository repo;
    private final LibroRepository libros;

    public CategoriaService(CategoriaRepository repo, LibroRepository libros) {
        this.repo = repo;
        this.libros = libros;
    }

    public List<Categoria> listar() {
        return repo.findAll();
    }

    public Categoria obtener(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la categoría " + id));
    }

    public Categoria crear(CategoriaRequest req) {
        if (repo.existsByNombre(req.nombre())) {
            throw new ConflictoException("Ya existe una categoría llamada " + req.nombre());
        }
        Categoria c = new Categoria();
        c.setNombre(req.nombre());
        c.setDescripcion(req.descripcion());
        return repo.save(c);
    }

    public Categoria actualizar(Integer id, CategoriaRequest req) {
        Categoria c = obtener(id);
        if (repo.existsByNombreAndIdNot(req.nombre(), id)) {
            throw new ConflictoException("Ya existe otra categoría llamada " + req.nombre());
        }
        c.setNombre(req.nombre());
        c.setDescripcion(req.descripcion());
        return repo.save(c);
    }

    /** RN-06: no se elimina una categoría que tiene libros. */
    public void eliminar(Integer id) {
        Categoria c = obtener(id);
        if (libros.existsByCategoriaId(id)) {
            throw new ConflictoException("No se puede eliminar la categoría " + id + " porque tiene libros");
        }
        repo.delete(c);
    }
}
