package mx.uv.biblioteca.exception;

/** Datos que pasan la validación del JSON pero no tienen sentido para el negocio (se responde 400). */
public class DatoInvalidoException extends RuntimeException {
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
