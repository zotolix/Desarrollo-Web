#!/bin/bash
# Pruebas manuales de toda la API con CURL. Uso: bash backend/pruebas-curl.sh
# Requiere el backend corriendo en localhost:8080 y la base creada con database/esquema.sql.
# Cada prueba muestra el código esperado y el obtenido. Se puede ejecutar varias veces:
# los datos nuevos usan un sufijo aleatorio para no chocar con los de la vez anterior.

URL=http://localhost:8080/api
H="Content-Type: application/json"
N=$RANDOM
BIEN=0
MAL=0

# prueba "descripción" código_esperado método ruta [json]
prueba() {
  local desc=$1 esperado=$2 metodo=$3 ruta=$4 datos=$5
  if [ -n "$datos" ]; then
    RESP=$(curl -s -w '\n%{http_code}' -X "$metodo" "$URL$ruta" -H "$H" -d "$datos")
  else
    RESP=$(curl -s -w '\n%{http_code}' -X "$metodo" "$URL$ruta")
  fi
  CODIGO=$(echo "$RESP" | tail -n1)
  CUERPO=$(echo "$RESP" | sed '$d')
  if [ "$CODIGO" = "$esperado" ]; then
    echo "[OK]    $desc -> HTTP $CODIGO"
    BIEN=$((BIEN + 1))
  else
    echo "[FALLA] $desc -> esperado $esperado, obtenido $CODIGO"
    echo "        $CUERPO"
    MAL=$((MAL + 1))
  fi
}

id_de() { echo "$CUERPO" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2; }

if ! curl -s -o /dev/null "$URL/categorias"; then
  echo "El backend no responde en $URL. Enciéndelo con: cd backend && mvn spring-boot:run"
  exit 1
fi

echo "== Categorías =="
prueba "Crear categoría" 201 POST /categorias "{\"nombre\":\"Historia $N\",\"descripcion\":\"Libros de historia\"}"
CAT=$(id_de)
prueba "Crear categoría repetida (RN-05)" 409 POST /categorias "{\"nombre\":\"Historia $N\"}"
prueba "Crear categoría sin nombre" 400 POST /categorias '{"descripcion":"sin nombre"}'
prueba "Listar categorías" 200 GET /categorias
prueba "Consultar categoría inexistente" 404 GET /categorias/99999
prueba "Modificar categoría" 200 PUT /categorias/$CAT "{\"nombre\":\"Historia $N\",\"descripcion\":\"Editada\"}"
prueba "Eliminar categoría con libros (RN-06)" 409 DELETE /categorias/1

echo; echo "== Libros =="
prueba "Crear libro" 201 POST /libros "{\"titulo\":\"Libro de prueba $N\",\"autor\":\"Autor Prueba\",\"isbn\":\"PR-$N\",\"anioPublicacion\":2020,\"ejemplaresTotales\":1,\"categoriaId\":$CAT}"
LIBRO=$(id_de)
prueba "Crear libro con ISBN repetido (RN-05)" 409 POST /libros "{\"titulo\":\"X\",\"autor\":\"Y\",\"isbn\":\"PR-$N\",\"ejemplaresTotales\":1,\"categoriaId\":$CAT}"
prueba "Crear libro con categoría inexistente" 404 POST /libros '{"titulo":"X","autor":"Y","isbn":"ZZ-1","ejemplaresTotales":1,"categoriaId":99999}'
prueba "Crear libro con 0 ejemplares" 400 POST /libros '{"titulo":"X","autor":"Y","isbn":"ZZ-2","ejemplaresTotales":0,"categoriaId":1}'
prueba "Buscar libros por título" 200 GET "/libros?titulo=prueba"
prueba "Buscar libros por autor y categoría" 200 GET "/libros?autor=autor&categoriaId=$CAT"
prueba "Buscar solo libros disponibles" 200 GET "/libros?disponible=true"
prueba "Consultar libro con id no numérico" 400 GET /libros/abc
prueba "Modificar libro (2 ejemplares)" 200 PUT /libros/$LIBRO "{\"titulo\":\"Libro de prueba $N\",\"autor\":\"Autor Prueba\",\"isbn\":\"PR-$N\",\"anioPublicacion\":2021,\"ejemplaresTotales\":2,\"categoriaId\":$CAT}"

echo; echo "== Socios =="
prueba "Registrar socio" 201 POST /socios "{\"nombre\":\"Socio Prueba $N\",\"correo\":\"socio$N@correo.com\",\"telefono\":\"2281112233\"}"
SOCIO=$(id_de)
prueba "Registrar socio con correo repetido (RN-05)" 409 POST /socios "{\"nombre\":\"Otro\",\"correo\":\"socio$N@correo.com\"}"
prueba "Registrar socio con correo inválido" 400 POST /socios '{"nombre":"Test","correo":"no-es-correo"}'
prueba "Filtrar socios por nombre y estado" 200 GET "/socios?nombre=prueba&estado=ACTIVO"
prueba "Filtrar socios con estado inválido" 400 GET "/socios?estado=XYZ"

echo; echo "== Préstamos =="
prueba "Prestar libro" 201 POST /prestamos "{\"libroId\":$LIBRO,\"socioId\":$SOCIO}"
P1=$(id_de)
prueba "Prestar segundo ejemplar" 201 POST /prestamos "{\"libroId\":$LIBRO,\"socioId\":$SOCIO}"
P2=$(id_de)
prueba "Prestar sin ejemplares (RN-01)" 409 POST /prestamos "{\"libroId\":$LIBRO,\"socioId\":$SOCIO}"
prueba "Bajar ejemplares por debajo de los prestados (RN-08)" 409 PUT /libros/$LIBRO "{\"titulo\":\"Libro de prueba $N\",\"autor\":\"Autor Prueba\",\"isbn\":\"PR-$N\",\"ejemplaresTotales\":1,\"categoriaId\":$CAT}"
prueba "Prestar a socio suspendido (RN-03, socio 3 de ejemplo)" 409 POST /prestamos '{"libroId":1,"socioId":3}'
prueba "Prestar con socio inexistente" 404 POST /prestamos "{\"libroId\":1,\"socioId\":99999}"
prueba "Prestar sin libro" 400 POST /prestamos "{\"socioId\":$SOCIO}"
prueba "Prestar tercer libro al socio" 201 POST /prestamos "{\"libroId\":1,\"socioId\":$SOCIO}"
P3=$(id_de)
prueba "Cuarto préstamo al mismo socio (RN-04)" 409 POST /prestamos "{\"libroId\":2,\"socioId\":$SOCIO}"
prueba "Filtrar préstamos por socio y estado" 200 GET "/prestamos?socioId=$SOCIO&estado=ACTIVO"
prueba "Renovar préstamo (cambiar fecha límite)" 200 PUT /prestamos/$P1 '{"fechaLimite":"2030-01-15"}'
prueba "Fecha límite anterior al préstamo" 400 PUT /prestamos/$P1 '{"fechaLimite":"2000-01-01"}'
prueba "Eliminar préstamo activo" 409 DELETE /prestamos/$P1
prueba "Eliminar libro con préstamos (RN-06)" 409 DELETE /libros/$LIBRO
prueba "Eliminar socio con préstamos (RN-06)" 409 DELETE /socios/$SOCIO
prueba "Registrar devolución (RN-02)" 200 PUT /prestamos/$P1/devolucion
prueba "Devolver otra vez el mismo préstamo" 409 PUT /prestamos/$P1/devolucion
prueba "Devolver los demás préstamos" 200 PUT /prestamos/$P2/devolucion
prueba "Devolver el tercer préstamo" 200 PUT /prestamos/$P3/devolucion
prueba "Eliminar préstamo devuelto" 204 DELETE /prestamos/$P1
prueba "Consultar préstamo eliminado" 404 GET /prestamos/$P1

echo; echo "== Ejemplares del libro de prueba (deben ser 2 de 2) =="
curl -s $URL/libros/$LIBRO; echo

echo; echo "Resultado: $BIEN correctas, $MAL con falla."
