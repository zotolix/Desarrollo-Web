#!/bin/bash
# Pruebas manuales del módulo de socios. Uso: bash pruebas-curl-socios.sh
# Requiere el backend corriendo en localhost:8080
URL=http://localhost:8080/api/socios
H="Content-Type: application/json"
CORREO="prueba$RANDOM@correo.com"

echo "== POST socio nuevo (201) =="
RESP=$(curl -s -X POST $URL -H "$H" -d "{\"nombre\":\"Socio de Prueba\",\"correo\":\"$CORREO\",\"telefono\":\"2281112233\"}")
echo "$RESP"
ID=$(echo "$RESP" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
echo "ID creado: $ID"

echo; echo "== GET por id (200) =="
curl -s $URL/$ID

echo; echo "== GET filtro nombre=prueba (200) =="
curl -s "$URL?nombre=prueba"

echo; echo "== GET filtro estado=ACTIVO (200) =="
curl -s "$URL?estado=ACTIVO"

echo; echo "== PUT suspender al socio (200) =="
curl -s -i -X PUT $URL/$ID -H "$H" -d "{\"nombre\":\"Socio Editado\",\"correo\":\"$CORREO\",\"telefono\":\"2281112233\",\"estado\":\"SUSPENDIDO\"}"

echo; echo "== GET filtro estado=SUSPENDIDO: debe aparecer el socio =="
curl -s "$URL?estado=SUSPENDIDO"

echo; echo "== PUT con correo de otro socio (409) =="
curl -s -i -X PUT $URL/$ID -H "$H" -d "{\"nombre\":\"X\",\"correo\":\"ana@correo.com\",\"estado\":\"ACTIVO\"}"

echo; echo "== PUT con estado invalido (400) =="
curl -s -i -X PUT $URL/$ID -H "$H" -d "{\"nombre\":\"X\",\"correo\":\"$CORREO\",\"estado\":\"XYZ\"}"

echo; echo "== PUT socio inexistente (404) =="
curl -s -i -X PUT $URL/9999 -H "$H" -d "{\"nombre\":\"X\",\"correo\":\"x@correo.com\",\"estado\":\"ACTIVO\"}"

echo; echo "== DELETE socio sin prestamos (204) =="
curl -s -i -X DELETE $URL/$ID

echo; echo "== DELETE otra vez (404) =="
curl -s -i -X DELETE $URL/$ID
echo
