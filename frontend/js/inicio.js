// Pantalla de inicio: muestra los totales consultando la API.
async function cargarResumen() {
  try {
    const [libros, socios, activos, vencidos] = await Promise.all([
      api('/libros'),
      api('/socios'),
      api('/prestamos?estado=ACTIVO'),
      api('/prestamos?estado=VENCIDO'),
    ]);
    document.getElementById('total-libros').textContent = libros.length;
    document.getElementById('total-socios').textContent = socios.length;
    document.getElementById('total-activos').textContent = activos.length + vencidos.length;
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

cargarResumen();
