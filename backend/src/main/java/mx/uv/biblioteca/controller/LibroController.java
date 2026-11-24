package mx.uv.biblioteca.controller;

import jakarta.validation.Valid;
import java.util.List;
import mx.uv.biblioteca.dto.LibroRequest;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.service.LibroService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
@CrossOrigin(origins = "*")
public class LibroController {

    private final LibroService service;

    public LibroController(LibroService service) {
        this.service = service;
    }

    // GET /api/libros?titulo=&autor=&categoriaId=&disponible=true  (todos los filtros son opcionales)
    @GetMapping
    public List<Libro> listar(@RequestParam(required = false) String titulo,
                              @RequestParam(required = false) String autor,
                              @RequestParam(required = false) Integer categoriaId,
                              @RequestParam(required = false) Boolean disponible) {
        return service.buscar(titulo, autor, categoriaId, disponible);
    }

    @GetMapping("/{id}")
    public Libro obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Libro crear(@Valid @RequestBody LibroRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public Libro actualizar(@PathVariable Integer id, @Valid @RequestBody LibroRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
