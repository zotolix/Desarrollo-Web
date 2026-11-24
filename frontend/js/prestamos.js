// Pantalla de préstamos: prestar, filtrar, devolver, renovar y eliminar.
const form = document.getElementById('form-prestamo');
const formFiltros = document.getElementById('form-filtros');
const tabla = document.getElementById('tabla-prestamos');
let prestamos = [];

const NOMBRE_ESTADO = { ACTIVO: 'Activo', DEVUELTO: 'Devuelto', VENCIDO: 'Vencido' };

/** Llena las listas de libros y socios. Solo se ofrecen libros con ejemplares y socios activos. */
async function cargarListas() {
  try {
    const [libros, socios] = await Promise.all([api('/libros?disponible=true'), api('/socios')]);
    document.getElementById('p-libro').innerHTML = libros.length
      ? libros.map(l => `<option value="${l.id}">${esc(l.titulo)} (${l.ejemplaresDisponibles} disp.)</option>`).join('')
      : '<option value="">No hay libros disponibles</option>';
    document.getElementById('p-socio').innerHTML = socios
      .filter(s => s.estado === 'ACTIVO')
      .map(s => `<option value="${s.id}">${esc(s.nombre)}</option>`).join('');
    const filtroSocio = document.getElementById('f-socio');
    const elegido = filtroSocio.value; // se conserva el filtro al recargar la lista
    filtroSocio.innerHTML = '<option value="">Todos</option>'
      + socios.map(s => `<option value="${s.id}">${esc(s.nombre)}</option>`).join('');
    filtroSocio.value = elegido;
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

async function cargarPrestamos() {
  const filtros = {
    socioId: document.getElementById('f-socio').value,
    estado: document.getElementById('f-estado').value,
  };
  try {
    prestamos = await api('/prestamos' + queryString(filtros));
    pintarTabla();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

function pintarTabla() {
  if (prestamos.length === 0) {
    tabla.innerHTML = '<tr><td colspan="6" class="vacio">No hay préstamos que coincidan con la búsqueda.</td></tr>';
    return;
  }
  const fechaHoy = hoy();
  tabla.innerHTML = prestamos.map(p => {
    const pendiente = p.estado !== 'DEVUELTO';
    const atrasado = pendiente && p.fechaLimite < fechaHoy;
    const botones = pendiente
      ? `<button class="chico" data-accion="devolver" data-id="${p.id}">Registrar devolución</button>
         <button class="chico secundario" data-accion="renovar" data-id="${p.id}">Renovar 7 días</button>`
      : `<button class="chico peligro" data-accion="eliminar" data-id="${p.id}">Eliminar</button>`;
    return `
    <tr>
      <td>${esc(p.libro.titulo)}</td>
      <td>${esc(p.socio.nombre)}</td>
      <td class="fecha">${p.fechaPrestamo}</td>
      <td class="fecha">${pendiente
        ? `<span class="sello${atrasado ? ' atrasado' : ''}">${p.fechaLimite}</span>${atrasado ? ' atrasado' : ''}`
        : `Devuelto el ${p.fechaDevolucion}`}</td>
      <td><span class="estado ${p.estado}">${NOMBRE_ESTADO[p.estado]}</span></td>
      <td class="botones">${botones}</td>
    </tr>`;
  }).join('');
}

form.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const datos = {
    libroId: Number(document.getElementById('p-libro').value) || null,
    socioId: Number(document.getElementById('p-socio').value) || null,
  };
  try {
    const p = await api('/prestamos', { method: 'POST', body: datos });
    mostrarMensaje(`Préstamo registrado: "${p.libro.titulo}" para ${p.socio.nombre}. Devolver antes del ${p.fechaLimite}.`);
    await cargarListas();
    cargarPrestamos();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
});

formFiltros.addEventListener('submit', (evento) => {
  evento.preventDefault();
  cargarPrestamos();
});

tabla.addEventListener('click', async (evento) => {
  const boton = evento.target.closest('button[data-accion]');
  if (!boton) return;
  const id = Number(boton.dataset.id);
  const p = prestamos.find(x => x.id === id);
  try {
    if (boton.dataset.accion === 'devolver') {
      await api(`/prestamos/${id}/devolucion`, { method: 'PUT' });
      mostrarMensaje(`Devolución registrada: "${p.libro.titulo}".`);
    } else if (boton.dataset.accion === 'renovar') {
      const nueva = sumarDias(p.fechaLimite, 7);
      await api(`/prestamos/${id}`, { method: 'PUT', body: { fechaLimite: nueva } });
      mostrarMensaje(`Préstamo renovado: ahora vence el ${nueva}.`);
    } else if (boton.dataset.accion === 'eliminar') {
      if (!confirm('¿Eliminar este préstamo del historial?')) return;
      await api(`/prestamos/${id}`, { method: 'DELETE' });
      mostrarMensaje('Préstamo eliminado del historial.');
    }
    await cargarListas();
    cargarPrestamos();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
});

cargarListas().then(cargarPrestamos);
