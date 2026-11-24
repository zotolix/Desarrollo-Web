// Pantalla de socios: filtrar, registrar, editar (incluye suspender) y eliminar.
const form = document.getElementById('form-socio');
const formFiltros = document.getElementById('form-filtros');
const tabla = document.getElementById('tabla-socios');
let socios = [];

async function cargarSocios() {
  const filtros = {
    nombre: document.getElementById('f-nombre').value.trim(),
    estado: document.getElementById('f-estado').value,
  };
  try {
    socios = await api('/socios' + queryString(filtros));
    pintarTabla();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

function pintarTabla() {
  if (socios.length === 0) {
    tabla.innerHTML = '<tr><td colspan="6" class="vacio">No hay socios que coincidan con la búsqueda.</td></tr>';
    return;
  }
  tabla.innerHTML = socios.map(s => `
    <tr>
      <td>${esc(s.nombre)}</td>
      <td>${esc(s.correo)}</td>
      <td>${esc(s.telefono)}</td>
      <td><span class="estado ${s.estado}">${s.estado === 'ACTIVO' ? 'Activo' : 'Suspendido'}</span></td>
      <td class="fecha">${s.fechaRegistro}</td>
      <td class="botones">
        <button class="chico secundario" data-accion="editar" data-id="${s.id}">Editar</button>
        <button class="chico peligro" data-accion="eliminar" data-id="${s.id}">Eliminar</button>
      </td>
    </tr>`).join('');
}

function modoEdicion(s) {
  document.getElementById('socio-id').value = s ? s.id : '';
  document.getElementById('socio-nombre').value = s ? s.nombre : '';
  document.getElementById('socio-correo').value = s ? s.correo : '';
  document.getElementById('socio-telefono').value = s ? (s.telefono || '') : '';
  document.getElementById('socio-estado').value = s ? s.estado : 'ACTIVO';
  document.getElementById('campo-estado').hidden = !s; // el estado solo se elige al editar
  document.getElementById('titulo-form').textContent = s ? 'Editar socio' : 'Nuevo socio';
  document.getElementById('btn-guardar').textContent = s ? 'Guardar cambios' : 'Registrar socio';
  document.getElementById('btn-cancelar').hidden = !s;
}

form.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const id = document.getElementById('socio-id').value;
  const datos = {
    nombre: document.getElementById('socio-nombre').value.trim(),
    correo: document.getElementById('socio-correo').value.trim(),
    telefono: document.getElementById('socio-telefono').value.trim() || null,
  };
  try {
    if (id) {
      datos.estado = document.getElementById('socio-estado').value;
      await api(`/socios/${id}`, { method: 'PUT', body: datos });
      mostrarMensaje(`Socio "${datos.nombre}" actualizado.`);
    } else {
      await api('/socios', { method: 'POST', body: datos });
      mostrarMensaje(`Socio "${datos.nombre}" registrado.`);
    }
    modoEdicion(null);
    cargarSocios();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
});

document.getElementById('btn-cancelar').addEventListener('click', () => modoEdicion(null));

formFiltros.addEventListener('submit', (evento) => {
  evento.preventDefault();
  cargarSocios();
});

tabla.addEventListener('click', async (evento) => {
  const boton = evento.target.closest('button[data-accion]');
  if (!boton) return;
  const id = Number(boton.dataset.id);
  const socio = socios.find(s => s.id === id);

  if (boton.dataset.accion === 'editar') {
    modoEdicion(socio);
  } else if (boton.dataset.accion === 'eliminar') {
    if (!confirm(`¿Eliminar al socio "${socio.nombre}"?`)) return;
    try {
      await api(`/socios/${id}`, { method: 'DELETE' });
      mostrarMensaje(`Socio "${socio.nombre}" eliminado.`);
      cargarSocios();
    } catch (e) {
      mostrarMensaje(e.message, 'error');
    }
  }
});

cargarSocios();
