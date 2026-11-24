# Biblioteca Web

Sistema web para administrar libros, socios y préstamos de una biblioteca.
Proyecto final de Desarrollo Web, LISTI (agosto 2026 - enero 2027).

**Arquitectura:** Frontend (HTML/CSS/JS + Fetch) → Backend (Java + Spring Boot) → MySQL (Hibernate/JPA).

## Estado

| Entregable | Estado |
|---|---|
| 1. Diseño inicial (`documentacion/entregable-1-diseno.md`) | Listo |
| 2. API inicial: POST y GET de las 4 entidades | Listo |
| 3. API completa (PUT, DELETE, filtros, devolución) | Pendiente |
| 4. Frontend integrado | Pendiente |
| 5. Versión candidata | Pendiente |

## Requisitos (WSL, Ubuntu)

```bash
sudo apt update
sudo apt install -y git openjdk-17-jdk maven mysql-server
java -version    # 17 o superior
mvn -version
```

## 1. Base de datos

```bash
sudo service mysql start
sudo mysql < database/esquema.sql
```

El script crea la base `biblioteca`, el usuario `biblio` y datos de ejemplo.
Antes de ejecutarlo, cambien `cambiar_clave` por una contraseña propia en `esquema.sql`
(y úsenla en el paso siguiente).

## 2. Backend

```bash
cd backend
export DB_USER=biblio
export DB_PASSWORD=su_clave
mvn spring-boot:run
```

La API queda en `http://localhost:8080/api`.
Si no se definen las variables, se usan `biblio` / `cambiar_clave` (ver `application.properties`).

## 3. Probar con CURL

Con el backend corriendo, en otra terminal:

```bash
bash backend/pruebas-curl.sh
```

O de forma manual:

```bash
curl http://localhost:8080/api/libros
curl -X POST http://localhost:8080/api/socios -H "Content-Type: application/json" \
  -d '{"nombre":"Ana","correo":"ana2@correo.com"}'
```

## Endpoints disponibles (Entregable 2)

| Recurso | GET lista | GET por id | POST |
|---|---|---|---|
| Categorías | `/api/categorias` | `/api/categorias/{id}` | `/api/categorias` |
| Libros | `/api/libros` | `/api/libros/{id}` | `/api/libros` |
| Socios | `/api/socios` | `/api/socios/{id}` | `/api/socios` |
| Préstamos | `/api/prestamos` | `/api/prestamos/{id}` | `/api/prestamos` |

Códigos: 200, 201, 400 (validación), 404 (no existe), 409 (conflicto o regla de negocio).
El detalle completo está en `documentacion/entregable-1-diseno.md`.

## Estructura

```
backend/      Spring Boot (controller, service, repository, model, dto, exception)
frontend/     HTML, CSS y JS (pendiente)
database/     esquema.sql
documentacion/
```

## Clonar y ejecutar en otro WSL

```bash
git clone https://github.com/<usuario>/desarrollo-web.git
cd desarrollo-web
# seguir los pasos 1, 2 y 3
```

## Integrantes

_[Completar con nombres y responsabilidades]_
