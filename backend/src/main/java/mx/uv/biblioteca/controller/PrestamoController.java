package mx.uv.biblioteca.controller;

import jakarta.validation.Valid;
import java.util.List;
import mx.uv.biblioteca.dto.PrestamoActualizarRequest;
import mx.uv.biblioteca.dto.PrestamoRequest;
import mx.uv.biblioteca.model.EstadoPrestamo;
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

    // GET /api/prestamos?socioId=1&estado=ACTIVO  (ambos filtros son opcionales)
    @GetMapping
    public List<Prestamo> listar(@RequestParam(required = false) Integer socioId,
                                 @RequestParam(required = false) EstadoPrestamo estado) {
        return service.listar(socioId, estado);
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

    @PutMapping("/{id}/devolucion")
    public Prestamo devolver(@PathVariable Integer id) {
        return service.devolver(id);
    }

    @PutMapping("/{id}")
    public Prestamo actualizar(@PathVariable Integer id, @Valid @RequestBody PrestamoActualizarRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
