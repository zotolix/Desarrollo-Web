package mx.uv.biblioteca.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LibroRequest(
        @NotBlank(message = "El título es obligatorio") String titulo,
        @NotBlank(message = "El autor es obligatorio") String autor,
        @NotBlank(message = "El ISBN es obligatorio") String isbn,
        Integer anioPublicacion,
        @NotNull(message = "Los ejemplares totales son obligatorios")
        @Min(value = 1, message = "Debe haber al menos 1 ejemplar") Integer ejemplaresTotales,
        @NotNull(message = "La categoría es obligatoria") Integer categoriaId) {
}
