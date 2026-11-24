# Reporte de Avance del Proyecto
**Equipo:** _[nombre del equipo]_
**Fecha:** _[dd/mm/2026]_
**Número de reporte:** 2
**Repositorio GitHub:** _[https://github.com/usuario/desarrollo-web]_
---
# 1. Actividades realizadas
- Se creó el proyecto Spring Boot en `backend/` (Web, Data JPA, Validation, MySQL Driver).
- Se configuró la conexión a MySQL en `application.properties` (usuario y clave por variables de entorno).
- Se crearon las entidades JPA: Categoría, Libro, Socio y Préstamo.
- Se crearon los repositorios, servicios y controladores con GET (lista y por id) y POST de las cuatro entidades.
- Se implementaron las reglas de negocio del préstamo (RN-01 a RN-04, RN-07) y el manejo de errores (400, 404, 409).
- Se probó la API con CURL (`backend/pruebas-curl.sh`).
---
# 2. Evidencias de trabajo
| Actividad | Integrante responsable | Enlace a commit |
|------------|------------------------|-----------------|
| Proyecto Spring Boot y conexión a MySQL | _[Integrante]_ | _[enlace]_ |
| Categoría y Libro (entidad, repositorio, servicio, controlador) | _[Integrante 1]_ | _[enlace]_ |
| Socio (entidad, repositorio, servicio, controlador) | _[Integrante 2]_ | _[enlace]_ |
| Préstamo y reglas de negocio | _[Integrante 3]_ | _[enlace]_ |
| README y pruebas CURL | _[Integrante 4]_ | _[enlace]_ |
---
# 3. Problemas encontrados
_[Completar con los problemas reales y cómo se resolvieron. Ejemplos de lo que suele pasar: error de conexión o autenticación con MySQL en WSL, `ddl-auto=validate` quejándose de un tipo de columna, versión de Java distinta a la del pom.]_
---
# 4. Avance funcional
- Operativo: POST y GET de categorías, libros, socios y préstamos; validaciones; descuento de ejemplares al prestar.
- Pendiente: PUT, DELETE, filtros, devolución de préstamos, frontend, pruebas automatizadas.
---
# 5. Uso de IA generativa
- **Herramienta utilizada:** Claude
- **Actividad para la que se utilizó:** Generación del esqueleto del backend (entidades, repositorios, servicios, controladores, manejo de errores) y del README
- **Código o contenido generado:** Proyecto Spring Boot inicial, `pruebas-curl.sh`, `README.md`
- **Modificaciones realizadas por el equipo:** _[completar]_
- **Forma en que se verificó el resultado:** _[completar: por ejemplo, ejecución con `mvn spring-boot:run` y pruebas con CURL]_
> Registro detallado en `documentacion/IAG/` (formato r-ia-dev-sxx.md).
---
# 6. Participación individual
- **Integrante 1:** _[contribución]_
- **Integrante 2:** _[contribución]_
- **Integrante 3:** _[contribución]_
- **Integrante 4:** _[contribución]_
---
# 7. Próximas actividades
- PUT y DELETE de las cuatro entidades
- Endpoint de devolución de préstamos
- Filtros (libros, socios, préstamos)
- Pruebas automatizadas de cada módulo
- Reporte R3
---
## Observaciones adicionales
Release de este hito: `v0.2-api-inicial`.
