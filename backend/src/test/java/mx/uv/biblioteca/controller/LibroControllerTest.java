package mx.uv.biblioteca.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import mx.uv.biblioteca.dto.LibroRequest;
import mx.uv.biblioteca.exception.ConflictoException;
import mx.uv.biblioteca.exception.RecursoNoEncontradoException;
import mx.uv.biblioteca.model.Categoria;
import mx.uv.biblioteca.model.Libro;
import mx.uv.biblioteca.service.LibroService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LibroController.class)
class LibroControllerTest {

    private static final String LIBRO_JSON = "{\"titulo\":\"Clean Code\",\"autor\":\"Robert C. Martin\","
            + "\"isbn\":\"9780132350884\",\"anioPublicacion\":2008,\"ejemplaresTotales\":2,\"categoriaId\":3}";

    @Autowired
    private MockMvc mvc;

    @MockBean
    private LibroService service;

    private Libro libro() {
        Categoria c = new Categoria();
        c.setId(3);
        c.setNombre("Tecnología");
        Libro l = new Libro();
        l.setId(1);
        l.setTitulo("Clean Code");
        l.setAutor("Robert C. Martin");
        l.setIsbn("9780132350884");
        l.setEjemplaresTotales(2);
        l.setEjemplaresDisponibles(2);
        l.setCategoria(c);
        return l;
    }

    @Test
    void post_valido_devuelve201() throws Exception {
        when(service.crear(any(LibroRequest.class))).thenReturn(libro());

        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(2))
                .andExpect(jsonPath("$.categoria.nombre").value("Tecnología"));
    }

    @Test
    void post_sinTitulo_devuelve400() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"autor\":\"X\",\"isbn\":\"1\",\"ejemplaresTotales\":1,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_ceroEjemplares_devuelve400() throws Exception {
        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titulo\":\"T\",\"autor\":\"X\",\"isbn\":\"1\",\"ejemplaresTotales\":0,\"categoriaId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_isbnRepetido_devuelve409() throws Exception {
        when(service.crear(any(LibroRequest.class)))
                .thenThrow(new ConflictoException("Ya existe un libro con el ISBN 9780132350884"));

        mvc.perform(post("/api/libros").contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void get_conFiltros_devuelve200() throws Exception {
        when(service.buscar("clean", null, 3, true)).thenReturn(List.of(libro()));

        mvc.perform(get("/api/libros").param("titulo", "clean").param("categoriaId", "3").param("disponible", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titulo").value("Clean Code"));
    }

    @Test
    void get_idNoNumerico_devuelve400() throws Exception {
        mvc.perform(get("/api/libros/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_noExiste_devuelve404() throws Exception {
        when(service.obtener(99)).thenThrow(new RecursoNoEncontradoException("No existe el libro 99"));

        mvc.perform(get("/api/libros/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_valido_devuelve200() throws Exception {
        when(service.actualizar(eq(1), any(LibroRequest.class))).thenReturn(libro());

        mvc.perform(put("/api/libros/1").contentType(MediaType.APPLICATION_JSON).content(LIBRO_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void delete_valido_devuelve204() throws Exception {
        mvc.perform(delete("/api/libros/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_conPrestamos_devuelve409() throws Exception {
        doThrow(new ConflictoException("No se puede eliminar el libro 1 porque tiene préstamos"))
                .when(service).eliminar(1);

        mvc.perform(delete("/api/libros/1"))
                .andExpect(status().isConflict());
    }
}
