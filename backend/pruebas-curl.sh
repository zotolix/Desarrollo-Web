#!/bin/bash
# Pruebas manuales de la API (Entregable 2). Uso: bash pruebas-curl.sh
# Requiere el backend corriendo en localhost:8080 y la BD creada con database/esquema.sql
URL=http://localhost:8080/api
H="Content-Type: application/json"

echo "== POST categoria (201) =="
curl -s -i -X POST $URL/categorias -H "$H" -d '{"nombre":"Historia","descripcion":"Libros de historia"}'
echo; echo "== POST categoria repetida (409) =="
curl -s -i -X POST $URL/categorias -H "$H" -d '{"nombre":"Historia"}'
echo; echo "== POST categoria sin nombre (400) =="
curl -s -i -X POST $URL/categorias -H "$H" -d '{"descripcion":"sin nombre"}'

echo; echo "== GET categorias (200) =="
curl -s $URL/categorias

echo; echo "== POST libro (201) =="
curl -s -i -X POST $URL/libros -H "$H" -d '{"titulo":"El laberinto de la soledad","autor":"Octavio Paz","isbn":"9789681608828","anioPublicacion":1950,"ejemplaresTotales":1,"categoriaId":1}'
echo; echo "== POST libro con categoria inexistente (404) =="
curl -s -i -X POST $URL/libros -H "$H" -d '{"titulo":"X","autor":"Y","isbn":"111","ejemplaresTotales":1,"categoriaId":999}'
echo; echo "== GET libros (200) =="
curl -s $URL/libros
echo; echo "== GET libro inexistente (404) =="
curl -s -i $URL/libros/999

echo; echo "== POST socio (201) =="
curl -s -i -X POST $URL/socios -H "$H" -d '{"nombre":"María López","correo":"maria@correo.com","telefono":"2281112233"}'
echo; echo "== POST socio con correo invalido (400) =="
curl -s -i -X POST $URL/socios -H "$H" -d '{"nombre":"Test","correo":"no-es-correo"}'
echo; echo "== GET socios (200) =="
curl -s $URL/socios

echo; echo "== POST prestamo (201) =="
curl -s -i -X POST $URL/prestamos -H "$H" -d '{"libroId":1,"socioId":1}'
echo; echo "== GET prestamos (200) =="
curl -s $URL/prestamos
echo; echo "== GET libro 1: ejemplaresDisponibles debe haber bajado =="
curl -s $URL/libros/1
echo
