// Pantalla de libros: buscar con filtros, crear, editar y eliminar.
const form = document.getElementById('form-libro');
const formFiltros = document.getElementById('form-filtros');
const tabla = document.getElementById('tabla-libros');
let libros = [];

async function cargarCategorias() {
  try {
    const categorias = await api('/categorias');
    const opciones = categorias.map(c => `<option value="${c.id}">${esc(c.nombre)}</option>`).join('');
    document.getElementById('libro-categoria').innerHTML = opciones;
    document.getElementById('f-categoria').innerHTML = '<option value="">Todas</option>' + opciones;
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

async function cargarLibros() {
  const filtros = {
    titulo: document.getElementById('f-titulo').value.trim(),
    autor: document.getElementById('f-autor').value.trim(),
    categoriaId: document.getElementById('f-categoria').value,
    disponible: document.getElementById('f-disponible').checked,
  };
  try {
    libros = await api('/libros' + queryString(filtros));
    pintarTabla();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
}

function pintarTabla() {
  if (libros.length === 0) {
    tabla.innerHTML = '<tr><td colspan="6" class="vacio">No hay libros que coincidan con la búsqueda.</td></tr>';
    return;
  }
  tabla.innerHTML = libros.map(l => `
    <tr>
      <td>${esc(l.titulo)}${l.anioPublicacion ? ` (${l.anioPublicacion})` : ''}</td>
      <td>${esc(l.autor)}</td>
      <td>${esc(l.isbn)}</td>
      <td>${esc(l.categoria.nombre)}</td>
      <td class="numero">${l.ejemplaresDisponibles} de ${l.ejemplaresTotales}</td>
      <td class="botones">
        <button class="chico secundario" data-accion="editar" data-id="${l.id}">Editar</button>
        <button class="chico peligro" data-accion="eliminar" data-id="${l.id}">Eliminar</button>
      </td>
    </tr>`).join('');
}

function modoEdicion(l) {
  document.getElementById('libro-id').value = l ? l.id : '';
  document.getElementById('libro-titulo').value = l ? l.titulo : '';
  document.getElementById('libro-autor').value = l ? l.autor : '';
  document.getElementById('libro-isbn').value = l ? l.isbn : '';
  document.getElementById('libro-anio').value = l && l.anioPublicacion ? l.anioPublicacion : '';
  document.getElementById('libro-totales').value = l ? l.ejemplaresTotales : 1;
  if (l) document.getElementById('libro-categoria').value = l.categoria.id;
  document.getElementById('titulo-form').textContent = l ? 'Editar libro' : 'Nuevo libro';
  document.getElementById('btn-guardar').textContent = l ? 'Guardar cambios' : 'Crear libro';
  document.getElementById('btn-cancelar').hidden = !l;
}

form.addEventListener('submit', async (evento) => {
  evento.preventDefault();
  const id = document.getElementById('libro-id').value;
  const anio = document.getElementById('libro-anio').value;
  const datos = {
    titulo: document.getElementById('libro-titulo').value.trim(),
    autor: document.getElementById('libro-autor').value.trim(),
    isbn: document.getElementById('libro-isbn').value.trim(),
    anioPublicacion: anio ? Number(anio) : null,
    ejemplaresTotales: Number(document.getElementById('libro-totales').value),
    categoriaId: Number(document.getElementById('libro-categoria').value),
  };
  try {
    if (id) {
      await api(`/libros/${id}`, { method: 'PUT', body: datos });
      mostrarMensaje(`Libro "${datos.titulo}" actualizado.`);
    } else {
      await api('/libros', { method: 'POST', body: datos });
      mostrarMensaje(`Libro "${datos.titulo}" creado.`);
    }
    modoEdicion(null);
    cargarLibros();
  } catch (e) {
    mostrarMensaje(e.message, 'error');
  }
});

document.getElementById('btn-cancelar').addEventListener('click', () => modoEdicion(null));

formFiltros.addEventListener('submit', (evento) => {
  evento.preventDefault();
  cargarLibros();
});

tabla.addEventListener('click', async (evento) => {
  const boton = evento.target.closest('button[data-accion]');
  if (!boton) return;
  const id = Number(boton.dataset.id);
  const libro = libros.find(l => l.id === id);

  if (boton.dataset.accion === 'editar') {
    modoEdicion(libro);
  } else if (boton.dataset.accion === 'eliminar') {
    if (!confirm(`¿Eliminar el libro "${libro.titulo}"?`)) return;
    try {
      await api(`/libros/${id}`, { method: 'DELETE' });
      mostrarMensaje(`Libro "${libro.titulo}" eliminado.`);
      cargarLibros();
    } catch (e) {
      mostrarMensaje(e.message, 'error');
    }
  }
});

cargarCategorias().then(cargarLibros);
