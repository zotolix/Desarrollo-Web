package mx.uv.biblioteca.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Cuerpo del PUT /api/prestamos/{id}: permite cambiar la fecha límite (por ejemplo, renovar). */
public record PrestamoActualizarRequest(
        @NotNull(message = "La fecha límite es obligatoria") LocalDate fechaLimite) {
}
