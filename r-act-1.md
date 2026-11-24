# Reporte de Avance del Proyecto
**Equipo:** _[nombre del equipo]_
**Fecha:** _[dd/mm/2026]_
**Número de reporte:** 1
**Repositorio GitHub:** _[https://github.com/usuario/desarrollo-web]_
---
# 1. Actividades realizadas
- Se definió el sistema: Biblioteca Web (catálogo de libros, socios y préstamos).
- Se creó el repositorio `desarrollo-web` con la estructura `backend/`, `frontend/`, `database/` y `documentacion/`, y se configuró el webhook.
- Se definieron los requerimientos funcionales, no funcionales y las reglas de negocio.
- Se diseñó el modelo de datos (Categoría, Libro, Socio, Préstamo) y el script `database/esquema.sql`.
- Se definieron los endpoints REST de las cuatro entidades.
- Se distribuyeron las responsabilidades entre los integrantes.
- Documento completo: `documentacion/entregable-1-diseno.md`.
---
# 2. Evidencias de trabajo
| Actividad | Integrante responsable | Enlace a commit |
|------------|------------------------|-----------------|
| Creación del repositorio y estructura de carpetas | _[Integrante 1]_ | _[enlace]_ |
| Requerimientos y reglas de negocio | _[Integrante 2]_ | _[enlace]_ |
| Modelo de datos y `esquema.sql` | _[Integrante 1]_ | _[enlace]_ |
| Definición de endpoints REST | _[Integrante 3]_ | _[enlace]_ |
| Distribución de tareas y README inicial | _[Integrante 4]_ | _[enlace]_ |
---
# 3. Problemas encontrados
_[Completar con los problemas reales, por ejemplo: configuración de SSH/token de GitHub desde WSL, instalación de MySQL en WSL, activación del webhook. Si no hubo, indicarlo.]_
---
# 4. Avance funcional
- Operativo: diseño del sistema, modelo de datos y esquema de la base de datos.
- Pendiente: proyecto Spring Boot, conexión a MySQL, entidades, repositorios, controladores y frontend.
---
# 5. Uso de IA generativa
Documentar el uso de herramientas de inteligencia artificial durante el desarrollo del proyecto.
- **Herramienta utilizada:** Claude
- **Actividad para la que se utilizó:** Propuesta inicial de requerimientos, modelo de datos, endpoints y reparto de tareas
- **Código o contenido generado:** Borrador de `entregable-1-diseno.md`, `esquema.sql` y este reporte
- **Modificaciones realizadas por el equipo:** _[completar: qué cambiaron, quitaron o agregaron]_
- **Forma en que se verificó el resultado:** _[completar: por ejemplo, se ejecutó `esquema.sql` en MySQL desde WSL y se revisaron los endpoints entre todos]_
> Registro detallado en `documentacion/IAG/` (formatos r-ia-doc.md y r-ia-dev).
---
# 6. Participación individual
Describir brevemente la contribución de cada integrante.
- **Integrante 1:** _[nombre y contribución]_
- **Integrante 2:** _[nombre y contribución]_
- **Integrante 3:** _[nombre y contribución]_
- **Integrante 4:** _[nombre y contribución]_
---
# 7. Próximas actividades
- Crear el proyecto Spring Boot en `backend/` (Web, JPA, MySQL Driver)
- Configurar la conexión a MySQL en `application.properties`
- Crear entidades y repositorios de las cuatro tablas
- Implementar POST y GET de cada entidad
- Probar con CURL y preparar el reporte R2
---
## Observaciones adicionales
Cada hito se publica con su release en GitHub (este: `v0.1-diseno`).
