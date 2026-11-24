# Persona 2: módulo de Socios

## Qué hace este módulo

Administra a las personas que piden libros prestados.

| Método | Ruta | Qué hace | Respuestas |
|--------|------|----------|------------|
| GET | `/api/socios` | Lista socios. Filtros opcionales `?nombre=` y `?estado=` (se pueden combinar) | 200 |
| GET | `/api/socios/{id}` | Consulta un socio | 200, 404 |
| POST | `/api/socios` | Crea un socio (queda ACTIVO, con la fecha de hoy) | 201, 400, 409 |
| PUT | `/api/socios/{id}` | Modifica nombre, correo, teléfono y estado | 200, 400, 404, 409 |
| DELETE | `/api/socios/{id}` | Elimina un socio | 204, 404, 409 |

## Reglas de negocio que le tocan

- RN-05: el correo es único. Se valida al crear y al modificar (un socio puede conservar su propio correo).
- RN-03: un socio SUSPENDIDO no recibe préstamos. El estado se cambia con el PUT; la regla la aplica el servicio de préstamos.
- RN-06: no se elimina un socio con préstamos. La llave foránea de MySQL lo impide y el servicio lo convierte en un 409 con mensaje claro.

## Archivos de esta persona

```
backend/src/main/java/mx/uv/biblioteca/
├── model/Socio.java, EstadoSocio.java
├── repository/SocioRepository.java
├── dto/SocioRequest.java, SocioActualizarRequest.java
├── service/SocioService.java
└── controller/SocioController.java
backend/src/test/java/mx/uv/biblioteca/
├── service/SocioServiceTest.java        (pruebas unitarias con Mockito, sin base de datos)
└── controller/SocioControllerTest.java  (pruebas HTTP con MockMvc, sin base de datos)
backend/pruebas-curl-socios.sh
```

## Cómo explicarlo en la defensa

- **Capas:** el controlador recibe la petición HTTP y valida el JSON (`@Valid`); el servicio aplica las reglas; el repositorio habla con MySQL a través de Hibernate/JPA. El controlador no toca la base de datos directamente.
- **Por qué hay dos DTO:** `SocioRequest` (POST) no pide estado porque todo socio nuevo es ACTIVO. `SocioActualizarRequest` (PUT) sí lo pide, porque ahí se suspende o reactiva.
- **Filtros:** los métodos `findByNombreContainingIgnoreCase` y `findByEstado` los genera Spring Data a partir del nombre del método. No se escribe SQL.
- **Por qué el DELETE depende de la llave foránea:** así la regla está protegida aunque alguien borre desde otra parte. Efecto secundario: tampoco se pueden borrar socios con préstamos ya devueltos. La alternativa para esos casos es suspender al socio.
- **Códigos HTTP:** 400 si el JSON no cumple las validaciones, 404 si no existe, 409 si choca con una regla (correo repetido o socio con préstamos).
- **Pruebas:** las del servicio usan Mockito para simular el repositorio, por eso no necesitan MySQL. Las del controlador usan `@WebMvcTest` y MockMvc para simular peticiones HTTP.

## Cómo ejecutar las pruebas automáticas

```bash
cd ~/desarrollo-web/backend
mvn test -Dtest='SocioServiceTest,SocioControllerTest'
```

## Plantilla para el registro de IA (`r-ia-dev-s01.md`)

| Concepto | Descripción |
|----------|-------------|
| Herramienta | Claude |
| Modelo/temperatura | _[modelo que aparezca en la app]_ / no aplica |
| Prompt original | _[pegar el texto que escribiste]_ |
| Respuesta de IA | _[pegar o resumir la respuesta; guardar el texto completo en respuesta.md]_ |
| Código anterior | Commit _[hash de git log --oneline]_ |
| Código posterior | Commit _[hash]_ |
| Diferencias entre versiones | _[salida de git diff --numstat ANTES DESPUES]_ |
| Prueba de funcionamiento | _[captura de mvn test y de pruebas-curl-socios.sh]_ |
| Código de uso | Generación / Pruebas |
| Reflexión del estudiante | _[2 o 3 líneas con tus palabras: qué entendiste, qué cambiaste, qué falló]_ |
