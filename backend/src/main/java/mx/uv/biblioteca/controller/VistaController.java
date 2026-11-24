package mx.uv.biblioteca.controller;

import mx.uv.biblioteca.model.EstadoPrestamo;
import mx.uv.biblioteca.repository.LibroRepository;
import mx.uv.biblioteca.repository.PrestamoRepository;
import mx.uv.biblioteca.repository.SocioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Página generada en el servidor con una plantilla Thymeleaf (src/main/resources/templates).
 * A diferencia de los controladores REST, este devuelve HTML y no JSON.
 */
@Controller
public class VistaController {

    private final LibroRepository libros;
    private final SocioRepository socios;
    private final PrestamoRepository prestamos;

    public VistaController(LibroRepository libros, SocioRepository socios, PrestamoRepository prestamos) {
        this.libros = libros;
        this.socios = socios;
        this.prestamos = prestamos;
    }

    @GetMapping("/")
    public String resumen(Model model) {
        model.addAttribute("totalLibros", libros.count());
        model.addAttribute("totalSocios", socios.count());
        model.addAttribute("activos", prestamos.findByEstado(EstadoPrestamo.ACTIVO));
        return "resumen";
    }
}
