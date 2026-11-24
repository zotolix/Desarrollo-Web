package mx.uv.biblioteca.controller;

import jakarta.validation.Valid;
import java.util.List;
import mx.uv.biblioteca.dto.PrestamoRequest;
import mx.uv.biblioteca.model.Prestamo;
import mx.uv.biblioteca.service.PrestamoService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/prestamos")
@CrossOrigin(origins = "*")
public class PrestamoController {

    private final PrestamoService service;

    public PrestamoController(PrestamoService service) {
        this.service = service;
    }

    @GetMapping
    public List<Prestamo> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public Prestamo obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Prestamo crear(@Valid @RequestBody PrestamoRequest req) {
        return service.crear(req);
    }
}
