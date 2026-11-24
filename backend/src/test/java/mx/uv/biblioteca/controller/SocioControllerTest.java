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
import mx.uv.biblioteca.dto.SocioActualizarRequest;
import mx.uv.biblioteca.dto.SocioRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.EstadoSocio;
import mx.uv.biblioteca.model.Socio;
import mx.uv.biblioteca.service.SocioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SocioController.class)
class SocioControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private SocioService service;

    private Socio socio(int id, String nombre, String correo, EstadoSocio estado) {
        Socio s = new Socio();
        s.setId(id);
        s.setNombre(nombre);
        s.setCorreo(correo);
        s.setEstado(estado);
        s.setFechaRegistro(LocalDate.of(2026, 11, 1));
        return s;
    }

    @Test
    void post_valido_devuelve201() throws Exception {
        when(service.crear(any(SocioRequest.class)))
                .thenReturn(socio(1, "Ana", "ana@correo.com", EstadoSocio.ACTIVO));

        mvc.perform(post("/api/socios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.correo").value("ana@correo.com"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    void post_correoInvalido_devuelve400() throws Exception {
        mvc.perform(post("/api/socios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_sinNombre_devuelve400() throws Exception {
        mvc.perform(post("/api/socios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"ana@correo.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_correoRepetido_devuelve409() throws Exception {
        when(service.crear(any(SocioRequest.class)))
                .thenThrow(new ConflictoException("Ya existe un socio con el correo ana@correo.com"));

        mvc.perform(post("/api/socios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void get_porId_noExiste_devuelve404() throws Exception {
        when(service.obtener(99)).thenThrow(new RecursoNoEncontradoException("No existe el socio 99"));

        mvc.perform(get("/api/socios/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe el socio 99"));
    }

    @Test
    void get_lista_conFiltros_devuelve200() throws Exception {
        when(service.listar("ana", EstadoSocio.ACTIVO))
                .thenReturn(List.of(socio(1, "Ana", "ana@correo.com", EstadoSocio.ACTIVO)));

        mvc.perform(get("/api/socios").param("nombre", "ana").param("estado", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    void put_valido_devuelve200() throws Exception {
        when(service.actualizar(eq(1), any(SocioActualizarRequest.class)))
                .thenReturn(socio(1, "Ana", "ana@correo.com", EstadoSocio.SUSPENDIDO));

        mvc.perform(put("/api/socios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\",\"estado\":\"SUSPENDIDO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("SUSPENDIDO"));
    }

    @Test
    void put_estadoInvalido_devuelve400() throws Exception {
        mvc.perform(put("/api/socios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\",\"estado\":\"XYZ\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void put_sinEstado_devuelve400() throws Exception {
        mvc.perform(put("/api/socios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Ana\",\"correo\":\"ana@correo.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void delete_valido_devuelve204() throws Exception {
        mvc.perform(delete("/api/socios/1"))
                .andExpect(status().isNoContent());

        verify(service).eliminar(1);
    }

    @Test
    void delete_conPrestamos_devuelve409() throws Exception {
        doThrow(new ConflictoException("No se puede eliminar el socio 1 porque tiene préstamos registrados"))
                .when(service).eliminar(1);

        mvc.perform(delete("/api/socios/1"))
                .andExpect(status().isConflict());
    }

    @Test
    void delete_noExiste_devuelve404() throws Exception {
        doThrow(new RecursoNoEncontradoException("No existe el socio 99")).when(service).eliminar(99);

        mvc.perform(delete("/api/socios/99"))
                .andExpect(status().isNotFound());
    }
}
