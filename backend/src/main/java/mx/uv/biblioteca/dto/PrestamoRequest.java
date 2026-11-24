package mx.uv.biblioteca.dto;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequest(
        @NotNull(message = "El libro es obligatorio") Integer libroId,
        @NotNull(message = "El socio es obligatorio") Integer socioId) {
}
