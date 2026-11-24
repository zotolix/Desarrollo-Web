package mx.uv.biblioteca.controller;

import jakarta.validation.Valid;
import java.util.List;
import mx.uv.biblioteca.dto.SocioActualizarRequest;
import mx.uv.biblioteca.dto.SocioRequest;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Socio;
import mx.uv.biblioteca.service.SocioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/socios")
@CrossOrigin(origins = "*")
public class SocioController {

    private final SocioService service;

    public SocioController(SocioService service) {
        this.service = service;
    }

    // GET /api/socios?nombre=ana&estado=ACTIVO  (ambos filtros son opcionales)
    @GetMapping
    public List<Socio> listar(@RequestParam(required = false) String nombre,
                              @RequestParam(required = false) EstadoSocio estado) {
        return service.listar(nombre, estado);
    }

    @GetMapping("/{id}")
    public Socio obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Socio crear(@Valid @RequestBody SocioRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{id}")
    public Socio actualizar(@PathVariable Integer id, @Valid @RequestBody SocioActualizarRequest req) {
        return service.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Integer id) {
        service.eliminar(id);
    }
}
