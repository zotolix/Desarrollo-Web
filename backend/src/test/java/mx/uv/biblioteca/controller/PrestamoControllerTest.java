package mx.uv.biblioteca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import mx.uv.biblioteca.dto.PrestamoActualizarRequest;
import mx.uv.biblioteca.dto.PrestamoRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.DatoInvalidoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.EstadoPrestamo;
import mx.uv.biblioteca.model.Prestamo;
import mx.uv.biblioteca.service.PrestamoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PrestamoController.class)
class PrestamoControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private PrestamoService service;

    private Prestamo prestamo(EstadoPrestamo estado) {
        Prestamo p = new Prestamo();
        p.setId(10);
        p.setFechaPrestamo(LocalDate.of(2026, 11, 20));
        p.setFechaLimite(LocalDate.of(2026, 11, 27));
        p.setEstado(estado);
        return p;
    }

    @Test
    void post_valido_devuelve201() throws Exception {
        when(service.crear(any(PrestamoRequest.class))).thenReturn(prestamo(EstadoPrestamo.ACTIVO));

        mvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.fechaLimite").value("2026-11-27"));
    }

    @Test
    void post_sinLibro_devuelve400() throws Exception {
        mvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"socioId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_sinEjemplares_devuelve409() throws Exception {
        when(service.crear(any(PrestamoRequest.class)))
                .thenThrow(new ConflictoException("No hay ejemplares disponibles del libro 1"));

        mvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No hay ejemplares disponibles del libro 1"));
    }

    @Test
    void post_socioInexistente_devuelve404() throws Exception {
        when(service.crear(any(PrestamoRequest.class)))
                .thenThrow(new RecursoNoEncontradoException("No existe el socio 99"));

        mvc.perform(post("/api/prestamos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libroId\":1,\"socioId\":99}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void get_lista_conFiltros_devuelve200() throws Exception {
        when(service.listar(1, EstadoPrestamo.ACTIVO)).thenReturn(List.of(prestamo(EstadoPrestamo.ACTIVO)));

        mvc.perform(get("/api/prestamos").param("socioId", "1").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10));
    }

    @Test
    void get_estadoInvalido_devuelve400() throws Exception {
        mvc.perform(get("/api/prestamos").param("estado", "XYZ"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_devolucion_devuelve200() throws Exception {
        when(service.devolver(10)).thenReturn(prestamo(EstadoPrestamo.DEVUELTO));

        mvc.perform(put("/api/prestamos/10/devolucion"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"));
    }

    @Test
    void put_devolucion_yaDevuelto_devuelve409() throws Exception {
        when(service.devolver(10)).thenThrow(new ConflictoException("El préstamo 10 ya fue devuelto"));

        mvc.perform(put("/api/prestamos/10/devolucion"))
                .andExpect(status().isConflict());
    }

    @Test
    void put_fechaLimite_devuelve200() throws Exception {
        when(service.actualizar(eq(10), any(PrestamoActualizarRequest.class)))
                .thenReturn(prestamo(EstadoPrestamo.ACTIVO));

        mvc.perform(put("/api/prestamos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaLimite\":\"2026-12-04\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void put_fechaAnterior_devuelve400() throws Exception {
        when(service.actualizar(eq(10), any(PrestamoActualizarRequest.class)))
                .thenThrow(new DatoInvalidoException("La fecha límite no puede ser anterior a la fecha de préstamo"));

        mvc.perform(put("/api/prestamos/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fechaLimite\":\"2026-01-01\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_devuelto_devuelve204() throws Exception {
        mvc.perform(delete("/api/prestamos/10"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(10);
    }

    @Test
    void delete_activo_devuelve409() throws Exception {
        doThrow(new ConflictoException("Solo se pueden eliminar préstamos devueltos")).when(service).eliminar(10);

        mvc.perform(delete("/api/prestamos/10"))
                .andExpect(status().isConflict());
    }
}
