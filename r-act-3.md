# Reporte de Avance del Proyecto
**Equipo:** _[nombre del equipo]_
**Fecha:** _[dd/mm/2026]_
**Número de reporte:** 3
**Repositorio GitHub:** _[https://github.com/usuario/desarrollo-web]_
---
# 1. Actividades realizadas
- Se completaron los archivos del módulo de socios que faltaban en el repositorio (`Socio.java`, `EstadoSocio.java`, `SocioRequest.java`); sin ellos el backend no compilaba.
- Se agregaron PUT y DELETE a categorías, libros y préstamos (socios ya los tenía).
- Se agregó el endpoint de devolución `PUT /api/prestamos/{id}/devolucion` (RN-02).
- Se agregaron filtros: libros por título, autor, categoría y disponibilidad; préstamos por socio y estado.
- Se aplicó RN-06 (no eliminar categorías con libros ni libros con préstamos) y RN-08 (ejemplares entre 0 y los totales al modificar un libro y al devolver).
- Se agregó el manejo de errores 400 para parámetros con tipo incorrecto y para fechas inválidas.
- Se escribieron pruebas automáticas con ayuda de IA: `CategoriaServiceTest`, `LibroServiceTest`, `PrestamoServiceTest`, `LibroControllerTest` y `PrestamoControllerTest`.
- Se amplió `pruebas-curl.sh` para recorrer toda la API e indicar `[OK]` o `[FALLA]` en cada caso.
- Se quitó la contraseña real de `esquema.sql` y se dejó `cambiar_clave`.
---
# 2. Evidencias de trabajo
| Actividad | Integrante responsable | Enlace a commit |
|------------|------------------------|-----------------|
| PUT, DELETE y filtros de categorías y libros, con pruebas | _[Integrante 1]_ | _[enlace]_ |
| Archivos faltantes de socios | _[Integrante 2]_ | _[enlace]_ |
| Devolución, PUT, DELETE y filtros de préstamos, con pruebas | _[Integrante 3]_ | _[enlace]_ |
| Script CURL completo y corrección de esquema.sql | _[Integrante 4]_ | _[enlace]_ |
---
# 3. Problemas encontrados
- **El backend no compilaba:** `SocioService`, `PrestamoService` y las pruebas usaban `Socio`, `EstadoSocio` y `SocioRequest`, pero esos archivos no se habían subido. Se detectó al revisar el repositorio y se resolvió agregándolos. _[Ajustar a lo que realmente pasó.]_
- **Contraseña real en el repositorio:** `esquema.sql` tenía la contraseña de un integrante. Se reemplazó por `cambiar_clave`; cada quien la cambia solo en su copia local.
- **RN-04 y préstamos vencidos:** contar solo los préstamos `ACTIVO` dejaba fuera a los `VENCIDO`. Se cambió a contar todos los que no están `DEVUELTO`.
- _[Agregar los problemas reales del equipo.]_
---
# 4. Avance funcional
- Operativo: CRUD completo de las cuatro entidades, filtros, devolución, reglas RN-01 a RN-08, manejo de errores 400/404/409, pruebas automáticas y script CURL.
- Pendiente: frontend integrado (Entregable 4).
---
# 5. Uso de IA generativa
- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Revisión del repositorio contra los requisitos; generación de los archivos faltantes de socios, de PUT/DELETE/filtros/devolución y de las pruebas automáticas.
- **Código o contenido generado:** _[listar archivos]_
- **Modificaciones realizadas por el equipo:** _[completar]_
- **Forma en que se verificó el resultado:** `mvn test` (BUILD SUCCESS) y `bash backend/pruebas-curl.sh` _[pegar el resultado]_.
> Registro detallado en `documentacion/IAG/`.
---
# 6. Participación individual
- **Integrante 1:** _[contribución]_
- **Integrante 2:** _[contribución]_
- **Integrante 3:** _[contribución]_
- **Integrante 4:** _[contribución]_
---
# 7. Próximas actividades
- Pantallas del frontend con Fetch (libros, categorías, socios, préstamos)
- Mostrar errores y confirmaciones de la API en la interfaz
- Página de resumen con plantilla Thymeleaf
- README final y prueba en un WSL limpio
- Reporte R4 y releases v0.4-integrado y v1.0-rc
---
## Observaciones adicionales
Release de este hito: `v0.3-api-completa`.
