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

    @GetMapping
    public List<Libro> listar() {
        return service.listar();
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
}
