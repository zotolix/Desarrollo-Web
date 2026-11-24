// Pantalla de categorías: listar, crear, editar y eliminar.
const form = document.getElementById('form-categoria');
const tabla = document.getElementById('tabla-categorias');
let categorias = [];

async function cargarCategorias() {
  try {
    categorias = await api('/categorias');
    pintarTabla();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

function pintarTabla() {
  if (categorias.length === 0) {
    tabla.innerHTML = '<tr><td colspan="4" class="vacio">Todavía no hay categorías. Crea la primera con el formulario.</td></tr>';
    return;
  }
  tabla.innerHTML = categorias.map(c => `
    <tr>
      <td class="numero">${c.id}</td>
      <td>${esc(c.nombre)}</td>
      <td>${esc(c.descripcion)}</td>
      <td class="botones">
        <button class="chico secundario" data-accion="editar" data-id="${c.id}">Editar</button>
        <button class="chico peligro" data-accion="eliminar" data-id="${c.id}">Eliminar</button>
      </td>
    </tr>`).join('');
}

function modoEdicion(c) {
  document.getElementById('cat-id').value = c ? c.id : '';
  document.getElementById('cat-nombre').value = c ? c.nombre : '';
  document.getElementById('cat-descripcion').value = c ? (c.descripcion || '') : '';
  document.getElementById('titulo-form').textContent = c ? 'Editar categoría' : 'Nueva categoría';
  document.getElementById('btn-guardar').textContent = c ? 'Guardar cambios' : 'Crear categoría';
  document.getElementById('btn-cancelar').hidden = !c;
}

form.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const id = document.getElementById('cat-id').value;
  const datos = {
    nombre: document.getElementById('cat-nombre').value.trim(),
    descripcion: document.getElementById('cat-descripcion').value.trim() || null,
  };
  try {
    if (id) {
      await api(`/categorias/${id}`, { method: 'PUT', body: datos });
      mostrarMensaje(`Categoría "${datos.nombre}" actualizada.`);
    } else {
      await api('/categorias', { method: 'POST', body: datos });
      mostrarMensaje(`Categoría "${datos.nombre}" creada.`);
    }
    modoEdicion(null);
    cargarCategorias();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
});

document.getElementById('btn-cancelar').addEventListener('click', () => modoEdicion(null));

tabla.addEventListener('click', async (evento) => {
  const boton = evento.target.closest('button[data-accion]');
  if (!boton) return;
  const id = Number(boton.dataset.id);
  const categoria = categorias.find(c => c.id === id);

  if (boton.dataset.accion === 'editar') {
    modoEdicion(categoria);
  } else if (boton.dataset.accion === 'eliminar') {
    if (!confirm(`¿Eliminar la categoría "${categoria.nombre}"?`)) return;
    try {
      await api(`/categorias/${id}`, { method: 'DELETE' });
      mostrarMensaje(`Categoría "${categoria.nombre}" eliminada.`);
      cargarCategorias();
    } catch (e) {
      mostrarMensaje(e.message, 'error');
    }
  }
});

cargarCategorias();
