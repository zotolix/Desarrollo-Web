# Biblioteca Web

Sistema web para administrar los libros, socios y préstamos de una biblioteca pequeña.
Proyecto final de Desarrollo Web, LISTI, Universidad Veracruzana (agosto 2026 - enero 2027).

**Arquitectura de 3 capas:** Frontend (HTML, CSS, JavaScript con Fetch) → Backend (Java 17 + Spring Boot 3.3) → MySQL 8 (Hibernate/JPA).

## Estado

| Entregable | Release | Estado |
|---|---|---|
| 1. Diseño inicial (`documentacion/entregable-1-diseno.md`) | `v0.1-diseno` | Listo |
| 2. API inicial: POST y GET de las 4 entidades | `v0.2-api-inicial` | Listo |
| 3. API completa: PUT, DELETE, filtros, devolución y pruebas automáticas | `v0.3-api-completa` | Listo |
| 4. Frontend integrado con la API | `v0.4-integrado` | Listo |
| 5. Versión candidata (pruebas, README, documentación) | `v1.0-rc` | Listo |

## API REST

URL base: `http://localhost:8080/api`. Respuestas en JSON. Errores con el formato `{ "status": 409, "mensaje": "..." }`.

| Recurso | Método y ruta | Descripción |
|---|---|---|
| Categorías | `GET /categorias`, `GET /categorias/{id}` | Listar y consultar |
| | `POST /categorias`, `PUT /categorias/{id}` | Crear y modificar (nombre único) |
| | `DELETE /categorias/{id}` | Eliminar (409 si tiene libros) |
| Libros | `GET /libros?titulo=&autor=&categoriaId=&disponible=true` | Listar con filtros opcionales |
| | `GET /libros/{id}`, `POST /libros`, `PUT /libros/{id}` | Consultar, crear y modificar (ISBN único) |
| | `DELETE /libros/{id}` | Eliminar (409 si tiene préstamos) |
| Socios | `GET /socios?nombre=&estado=ACTIVO\|SUSPENDIDO` | Listar con filtros opcionales |
| | `GET /socios/{id}`, `POST /socios`, `PUT /socios/{id}` | Consultar, registrar y modificar (correo único; el PUT suspende o reactiva) |
| | `DELETE /socios/{id}` | Eliminar (409 si tiene préstamos) |
| Préstamos | `GET /prestamos?socioId=&estado=ACTIVO\|DEVUELTO\|VENCIDO` | Listar con filtros opcionales |
| | `GET /prestamos/{id}`, `POST /prestamos` | Consultar y prestar (`{"libroId":1,"socioId":1}`) |
| | `PUT /prestamos/{id}/devolucion` | Registrar la devolución |
| | `PUT /prestamos/{id}` | Cambiar la fecha límite (`{"fechaLimite":"2026-12-04"}`) |
| | `DELETE /prestamos/{id}` | Eliminar un préstamo ya devuelto |

Códigos: 200 (correcto), 201 (creado), 204 (eliminado), 400 (datos inválidos), 404 (no existe), 409 (choca con una regla de negocio).

### Reglas de negocio

| ID | Regla | Dónde se aplica |
|---|---|---|
| RN-01 | No se presta un libro sin ejemplares disponibles | `PrestamoService.crear` |
| RN-02 | Prestar resta 1 ejemplar; devolver suma 1 | `PrestamoService.crear` y `devolver` |
| RN-03 | Un socio SUSPENDIDO no recibe préstamos | `PrestamoService.crear` |
| RN-04 | Máximo 3 préstamos sin devolver por socio | `PrestamoService.crear` |
| RN-05 | ISBN, correo y nombre de categoría únicos | Servicios de cada entidad y `UNIQUE` en MySQL |
| RN-06 | No se elimina una categoría con libros, ni un libro o socio con préstamos | `CategoriaService`, `LibroService`, `SocioService` |
| RN-07 | La fecha límite es 7 días después del préstamo | `PrestamoService.crear` |
| RN-08 | Disponibles entre 0 y los totales | `LibroService.actualizar`, `PrestamoService.devolver` y `CHECK` en MySQL |

## Estructura

```
backend/        Spring Boot: controller, service, repository, model, dto, exception
  src/test/     Pruebas automáticas (JUnit 5, Mockito, MockMvc)
  src/main/resources/templates/   Plantilla Thymeleaf
frontend/       index.html y una página por módulo, css/ y js/
database/       esquema.sql
documentacion/  Diseño, guía de defensa, registro de IA (IAG/)
r-act-1.md ... r-act-4.md   Reportes de avance
```


## Integrantes

| Integrante | Usuario de GitHub | Responsabilidad |
|---|---|---|
| _[Integrante 1]_ | _[usuario]_ | Categorías y Libros, configuración base, esquema SQL |
| _[Integrante 2]_ | _[usuario]_ | Socios |
| _[Integrante 3]_ | _[usuario]_ | Préstamos |
| _[Integrante 4]_ | _[usuario]_ | Frontend, README y documentación |

Docente: Dr. José Rafael Rojano Cáceres.
