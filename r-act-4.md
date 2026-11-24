# Reporte de Avance del Proyecto
**Equipo:** _[nombre del equipo]_
**Fecha:** _[dd/mm/2026]_
**Número de reporte:** 4
**Repositorio GitHub:** _[https://github.com/usuario/desarrollo-web]_
---
# 1. Actividades realizadas
- Se construyó el frontend en `frontend/` con HTML, CSS y JavaScript sin frameworks: Inicio, Libros, Categorías, Socios y Préstamos.
- Cada pantalla consume la API con Fetch en JSON (`js/api.js`), muestra el mensaje de error que devuelve la API (400, 404, 409) y una confirmación cuando la operación sale bien.
- La pantalla de préstamos permite prestar, registrar devoluciones, renovar 7 días y eliminar préstamos devueltos; resalta los préstamos atrasados.
- Se agregó una página de resumen generada en el servidor con Thymeleaf (`GET /`, plantilla `templates/resumen.html`).
- Se completó el README con las instrucciones para reproducir el proyecto en otro WSL.
- Se probó el proyecto clonándolo en un WSL limpio siguiendo solo el README. _[Confirmar.]_
---
# 2. Evidencias de trabajo
| Actividad | Integrante responsable | Enlace a commit |
|------------|------------------------|-----------------|
| Pantallas de libros y categorías | _[Integrante]_ | _[enlace]_ |
| Pantalla de socios | _[Integrante]_ | _[enlace]_ |
| Pantalla de préstamos y plantilla Thymeleaf | _[Integrante]_ | _[enlace]_ |
| README final y documentación | _[Integrante 4]_ | _[enlace]_ |
---
# 3. Problemas encontrados
- _[Por ejemplo: errores de CORS, el backend no encendido al abrir el frontend, rutas de archivos en WSL. Describir los reales.]_
---
# 4. Avance funcional
- Operativo: sistema completo de 3 capas desde WSL: frontend que consume la API, backend con reglas de negocio y MySQL.
- Pendiente (trabajo futuro): autenticación, multas y marcado automático de préstamos vencidos, notificaciones.
---
# 5. Uso de IA generativa
- **Herramienta utilizada:** Claude (Anthropic)
- **Actividad para la que se utilizó:** Generación del frontend, de la plantilla Thymeleaf y del README.
- **Código o contenido generado:** _[listar archivos]_
- **Modificaciones realizadas por el equipo:** _[completar]_
- **Forma en que se verificó el resultado:** Uso del frontend con el backend encendido; `mvn test`; `pruebas-curl.sh`. _[Adjuntar capturas.]_
> Registro detallado en `documentacion/IAG/`.
---
# 6. Participación individual
- **Integrante 1:** _[contribución]_
- **Integrante 2:** _[contribución]_
- **Integrante 3:** _[contribución]_
- **Integrante 4:** _[contribución]_
---
# 7. Próximas actividades
- Grabar el video de 15 a 20 minutos (todos los integrantes, cámara abierta) y subirlo a YouTube como no listado
- Completar el PDF de documentación con enlaces, capturas y reflexiones
- Firmar la declaración de uso de IA (`documentacion/declaracion-ia.md`)
- Preparar la defensa técnica individual
---
## Observaciones adicionales
Releases de este periodo: `v0.4-integrado` y `v1.0-rc`.
