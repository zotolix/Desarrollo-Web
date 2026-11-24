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

## 1. Instalar las herramientas (WSL, Ubuntu)

```bash
sudo apt update
sudo apt install -y git openjdk-17-jdk maven mysql-server
java -version    # 17 o superior
mvn -version
```

## 2. Clonar el repositorio

```bash
cd ~
git clone https://github.com/<usuario>/desarrollo-web.git
cd desarrollo-web
```

Trabajen dentro de la carpeta de Ubuntu (`~`), no en `/mnt/c/...`: ahí todo es más lento.

## 3. Crear la base de datos

1. Abran `database/esquema.sql` y cambien `cambiar_clave` por una contraseña propia. **No suban su contraseña real a GitHub.**
2. Ejecuten el script. Crea la base `biblioteca`, el usuario `biblio`, las 4 tablas y datos de ejemplo (3 categorías, 3 libros y 3 socios; Pedro Gómez está SUSPENDIDO para probar la regla RN-03).

```bash
sudo service mysql start
sudo mysql < database/esquema.sql
sudo mysql -e 'USE biblioteca; SHOW TABLES;'   # deben salir categoria, libro, prestamo y socio
```

> El script **borra las tablas y las vuelve a crear**: se pierden los datos que hayan metido.
> Si el usuario `biblio` ya existía con otra contraseña, el script no la cambia. Cámbienla así:
> `sudo mysql -e "ALTER USER 'biblio'@'localhost' IDENTIFIED BY 'su_clave';"`

## 4. Encender el backend

```bash
cd backend
export DB_USER=biblio
export DB_PASSWORD=su_clave
mvn spring-boot:run
```

Listo cuando aparece `Started BibliotecaApplication`. La API queda en `http://localhost:8080/api`.
La primera vez Maven descarga dependencias y tarda varios minutos. Dejen esa terminal abierta; para apagar el backend, `Ctrl+C`.
Los `export` solo duran mientras la terminal esté abierta.

## 5. Abrir el frontend

En **otra** terminal de Ubuntu (el backend sigue encendido en la primera):

```bash
cd ~/desarrollo-web/frontend
python3 -m http.server 5500
```

Abran `http://localhost:5500` en el navegador de Windows. Pantallas: Inicio, Libros, Categorías, Socios y Préstamos.
También pueden abrir `http://localhost:8080/`, una página de resumen generada en el servidor con una plantilla Thymeleaf (`backend/src/main/resources/templates/resumen.html`).

## 6. Probar

**Pruebas automáticas** (no necesitan MySQL ni el backend encendido):

```bash
cd ~/desarrollo-web/backend
mvn test          # debe terminar con BUILD SUCCESS
```

**Pruebas manuales con CURL** (con el backend encendido):

```bash
bash ~/desarrollo-web/backend/pruebas-curl.sh          # toda la API, muestra [OK] o [FALLA] en cada caso
bash ~/desarrollo-web/backend/pruebas-curl-socios.sh   # solo el módulo de socios
```

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

## Problemas frecuentes

| Síntoma | Solución |
|---|---|
| `Access denied for user 'biblio'` | `DB_PASSWORD` no coincide con la contraseña del script. Repitan los `export` o usen el `ALTER USER` del paso 3. |
| `Communications link failure` | MySQL está apagado: `sudo service mysql start`. |
| `Port 8080 was already in use` | Hay otro backend encendido. Ciérrenlo con `Ctrl+C`. |
| `Schema-validation: missing table` | La base no se creó. Vuelvan a ejecutar `esquema.sql`. |
| El frontend dice que no se pudo conectar | El backend no está encendido o no terminó de arrancar. |
| `Permission denied (publickey)` | Falta agregar la llave SSH a GitHub, o clonen con HTTPS. |

## Integrantes

| Integrante | Usuario de GitHub | Responsabilidad |
|---|---|---|
| _[Integrante 1]_ | _[usuario]_ | Categorías y Libros, configuración base, esquema SQL |
| _[Integrante 2]_ | _[usuario]_ | Socios |
| _[Integrante 3]_ | _[usuario]_ | Préstamos |
| _[Integrante 4]_ | _[usuario]_ | Frontend, README y documentación |

Docente: Dr. José Rafael Rojano Cáceres.
