// Funciones comunes para hablar con el backend mediante la API Fetch.
const API = 'http://localhost:8080/api';

/**
 * Hace una petición a la API y devuelve el JSON de la respuesta.
 * Si la API responde con error (400, 404, 409...), lanza un Error con el mensaje
 * que viene en el JSON ({ "status": 409, "mensaje": "..." }).
 */
async function api(ruta, opciones = {}) {
  const config = { method: opciones.method || 'GET', headers: {} };
  if (opciones.body !== undefined) {
    config.headers['Content-Type'] = 'application/json';
    config.body = JSON.stringify(opciones.body);
  }

  let respuesta;
  try {
    respuesta = await fetch(API + ruta, config);
  } catch (e) {
    throw new Error('No se pudo conectar con el backend. Revisa que esté corriendo en http://localhost:8080.');
  }

  if (respuesta.status === 204) {
    return null; // DELETE correcto: no hay cuerpo
  }
  const datos = await respuesta.json().catch(() => null);
  if (!respuesta.ok) {
    const mensaje = datos && (datos.mensaje || datos.message);
    throw new Error(mensaje || `Error HTTP ${respuesta.status}`);
  }
  return datos;
}

/** Convierte un objeto en query string, ignorando los valores vacíos. */
function queryString(filtros) {
  const params = new URLSearchParams();
  Object.entries(filtros).forEach(([clave, valor]) => {
    if (valor !== '' && valor !== null && valor !== undefined && valor !== false) {
      params.append(clave, valor);
    }
  });
  const texto = params.toString();
  return texto ? '?' + texto : '';
}

/** Muestra una confirmación (tipo 'ok') o un error (tipo 'error') arriba de la página. */
function mostrarMensaje(texto, tipo = 'ok') {
  const caja = document.getElementById('mensaje');
  caja.textContent = texto;
  caja.className = 'mensaje ' + tipo;
  caja.scrollIntoView({ block: 'nearest' });
}

/** Evita que un texto escrito por el usuario se interprete como HTML. */
function esc(texto) {
  const div = document.createElement('div');
  div.textContent = texto ?? '';
  return div.innerHTML;
}

/** Fecha en formato AAAA-MM-DD (el mismo que usa la API). */
function formatoFecha(d) {
  const mes = String(d.getMonth() + 1).padStart(2, '0');
  const dia = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${mes}-${dia}`;
}

function hoy() {
  return formatoFecha(new Date());
}

/** Suma días a una fecha AAAA-MM-DD. */
function sumarDias(fecha, dias) {
  const d = new Date(fecha + 'T12:00:00');
  d.setDate(d.getDate() + dias);
  return formatoFecha(d);
}
