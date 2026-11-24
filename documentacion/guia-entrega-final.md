# Guía para subir la versión final (Entregables 3, 4 y 5)

Esta guía continúa la "Guía paso a paso: Biblioteca Web". Supone que ya tienen el repositorio clonado en `~/desarrollo-web` y la base de datos creada.

## 0. Corrección a la guía anterior

En la Parte 8.2 de la guía anterior, la **Persona 2** no copiaba todos sus archivos. Le faltaban `dto/SocioActualizarRequest.java`, las pruebas `SocioServiceTest.java` y `SocioControllerTest.java`, `backend/pruebas-curl-socios.sh` y `documentacion/persona2-socios.md`. Además, en el repositorio nunca llegaron `Socio.java`, `EstadoSocio.java` y `SocioRequest.java`, y por eso el backend no compilaba. Esta guía lo corrige.

## 1. Descomprimir la versión final

```bash
cd ~
rm -rf ~/temp/desarrollo-web
mkdir -p temp
unzip /mnt/c/Users/TU_USUARIO/Downloads/desarrollo-web-final.zip -d ~/temp
ls ~/temp/desarrollo-web
```

## 2. Orden de subida

Cada quien sube **solo sus archivos**, con **su propia cuenta**, en **su rama**, y hace un Pull Request. El orden importa porque unas piezas dependen de otras: **Persona 2 → Persona 3 → Persona 1 → Persona 4**. Antes de empezar, cada quien actualiza su copia:

```bash
cd ~/desarrollo-web
git checkout main
git pull origin main
git checkout -b feature/NOMBRE-DE-TU-RAMA
```

### Persona 2 (socios), rama `feature/socios-final`

```bash
cd ~/temp/desarrollo-web
B=backend/src/main/java/mx/uv/biblioteca
T=backend/src/test/java/mx/uv/biblioteca
cp --parents $B/model/Socio.java $B/model/EstadoSocio.java $B/dto/SocioRequest.java \
  $B/dto/SocioActualizarRequest.java $B/repository/SocioRepository.java \
  $B/service/SocioService.java $B/controller/SocioController.java \
  $T/service/SocioServiceTest.java $T/controller/SocioControllerTest.java \
  backend/pruebas-curl-socios.sh documentacion/persona2-socios.md ~/desarrollo-web/
cd ~/desarrollo-web
git add .
git commit -m "Agrega entidad Socio, enum EstadoSocio y DTO SocioRequest que faltaban"
git push origin feature/socios-final
```

### Persona 3 (préstamos), rama `feature/prestamos-final`

```bash
cd ~/temp/desarrollo-web
B=backend/src/main/java/mx/uv/biblioteca
T=backend/src/test/java/mx/uv/biblioteca
cp --parents backend/pom.xml $B/model/Prestamo.java $B/model/EstadoPrestamo.java \
  $B/repository/PrestamoRepository.java $B/dto/PrestamoRequest.java \
  $B/dto/PrestamoActualizarRequest.java $B/service/PrestamoService.java \
  $B/controller/PrestamoController.java $B/controller/VistaController.java \
  $B/exception/DatoInvalidoException.java $B/exception/ManejadorErrores.java \
  backend/src/main/resources/templates/resumen.html \
  $T/service/PrestamoServiceTest.java $T/controller/PrestamoControllerTest.java ~/desarrollo-web/
cd ~/desarrollo-web
git add .
git commit -m "Agrega devolucion, PUT, DELETE y filtros de prestamos, pagina Thymeleaf y pruebas"
git push origin feature/prestamos-final
```

### Persona 1 (catálogo), rama `feature/catalogo-final`

```bash
cd ~/temp/desarrollo-web
B=backend/src/main/java/mx/uv/biblioteca
T=backend/src/test/java/mx/uv/biblioteca
cp --parents database/esquema.sql $B/repository/CategoriaRepository.java \
  $B/repository/LibroRepository.java $B/service/CategoriaService.java \
  $B/service/LibroService.java $B/controller/CategoriaController.java \
  $B/controller/LibroController.java $T/service/CategoriaServiceTest.java \
  $T/service/LibroServiceTest.java $T/controller/LibroControllerTest.java ~/desarrollo-web/
cd ~/desarrollo-web
git add .
git commit -m "Agrega PUT, DELETE y filtros de categorias y libros con pruebas; quita clave real del esquema"
git push origin feature/catalogo-final
```

### Persona 4 (frontend y documentación), rama `feature/frontend`

Conviene hacer dos commits para que se vea el avance por partes:

```bash
cd ~/temp/desarrollo-web
cp -r --parents frontend ~/desarrollo-web/
cd ~/desarrollo-web
git add frontend
git commit -m "Agrega frontend con Fetch: libros, categorias, socios y prestamos"

cd ~/temp/desarrollo-web
cp -r --parents README.md r-act-3.md r-act-4.md backend/pruebas-curl.sh \
  documentacion/IAG documentacion/declaracion-ia.md documentacion/guia-entrega-final.md ~/desarrollo-web/
cd ~/desarrollo-web
git add .
git commit -m "Completa README, pruebas CURL, reportes 3 y 4 y estructura del registro de IA"
git push origin feature/frontend
```

Si son tres integrantes, la Persona 2 hace también lo de la Persona 4.

## 3. Comprobar que todo junto funciona

Cuando los cuatro Pull Requests estén unidos a `main`:

```bash
cd ~/desarrollo-web
git checkout main
git pull origin main
cd backend
mvn test                       # debe decir BUILD SUCCESS
sudo service mysql start
sudo mysql < ../database/esquema.sql   # antes, cambien cambiar_clave por su contraseña (sin subirla)
export DB_USER=biblio
export DB_PASSWORD=su_clave
mvn spring-boot:run
```

En otra terminal:

```bash
bash ~/desarrollo-web/backend/pruebas-curl.sh      # al final: "N correctas, 0 con falla"
cd ~/desarrollo-web/frontend && python3 -m http.server 5500
```

Abran `http://localhost:5500` y `http://localhost:8080/` en el navegador. Tomen capturas de `mvn test`, del script CURL y de cada pantalla: son evidencias para los reportes y el PDF.

## 4. Releases

En GitHub: Releases, Draft a new release. Creen los que falten:

| Etiqueta | Apunta a |
|---|---|
| `v0.3-api-completa` | El merge de la Persona 1 (backend completo) |
| `v0.4-integrado` | El merge de la Persona 4 (frontend) |
| `v1.0-rc` | El último commit de `main`, con README y documentación completos |

## 5. Antes de entregar

- [ ] `mvn test` da BUILD SUCCESS en `main`.
- [ ] `pruebas-curl.sh` termina con 0 fallas.
- [ ] El frontend funciona con el backend encendido.
- [ ] `esquema.sql` en GitHub tiene `cambiar_clave`, no una contraseña real.
- [ ] Reportes r-act-1 a r-act-4 sin corchetes.
- [ ] Cada integrante tiene su carpeta en `documentacion/IAG/` con al menos el uso de IA de sus pruebas.
- [ ] Declaración de IA firmada.
- [ ] Video en YouTube (no listado, no privado) y PDF con los enlaces.
