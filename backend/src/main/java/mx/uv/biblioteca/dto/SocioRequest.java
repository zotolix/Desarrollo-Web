package mx.uv.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo del POST /api/socios. No pide estado: todo socio nuevo queda ACTIVO. */
public record SocioRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 120, message = "El nombre admite máximo 120 caracteres") String nombre,
        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 120, message = "El correo admite máximo 120 caracteres") String correo,
        @Size(max = 20, message = "El teléfono admite máximo 20 caracteres") String telefono) {
}
