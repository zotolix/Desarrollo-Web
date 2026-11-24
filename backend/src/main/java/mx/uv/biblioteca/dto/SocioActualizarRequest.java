package mx.uv.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.uv.biblioteca.model.EstadoSocio;

public record SocioActualizarRequest(
        @NotBlank(message = "El nombre es obligatorio") String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido") String correo,
        String telefono,
        @NotNull(message = "El estado es obligatorio") EstadoSocio estado) {
}
