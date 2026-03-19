#!/usr/bin/env bash
# ================================================================
#  MovieMate — Smoke Test completo (con datos de prueba)
#  Crea datos temporales, prueba todos los endpoints y los elimina.
#
#  Uso:     ./smoke-test.sh [BASE_URL] [USUARIO] [PASSWORD]
#  Ejemplo: ./smoke-test.sh http://localhost:8080 alice Test1234!
#
#  Requisito: backend corriendo con DataSeeder ejecutado.
# ================================================================

BASE_URL="${1:-http://localhost:8080}"
LOGIN_USER="${2:-alice}"
LOGIN_PASS="${3:-Test1234!}"

# ── Colores ──────────────────────────────────────────────────────
GREEN='\033[0;32m'; RED='\033[0;31m'; YELLOW='\033[1;33m'
CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

PASS=0; FAIL=0; SKIP=0; TOKEN=""; LAST_ID=""

# IDs de datos de prueba creados (para limpiar al finalizar)
TEST_LIST_ID=""
TEST_RATING_COMMENT_ID=""
TEST_LIST_COMMENT_ID=""
BOB_ID=""

# ── Helpers de salida ─────────────────────────────────────────────
section()  { echo ""; echo -e "${CYAN}${BOLD}── $1 ──────────────────────────────────────────────${NC}"; }
ok()       { echo -e "  ${GREEN}[✓]${NC} $1"; ((PASS++)); }
fail()     { echo -e "  ${RED}[✗]${NC} $1"; ((FAIL++)); }
skip_msg() { echo -e "  ${YELLOW}[-]${NC} $(printf '%-8s %-50s' "$1" "$2") ${YELLOW}(skip: $3)${NC}"; ((SKIP++)); }
info()     { echo -e "  ${YELLOW}→${NC} $1"; }

# ── Llamada HTTP ──────────────────────────────────────────────────
# Devuelve: <body>\n<status_code>
# Uso: resp=$(api "METHOD" "/path" '{"json":"body"}')
api() {
  local method="$1" path="$2" body="${3:-}"
  local args=(-s -w "\n%{http_code}" -X "$method" "$BASE_URL$path"
              -H "Content-Type: application/json")
  [[ -n "$TOKEN" ]] && args+=(-H "Authorization: Bearer $TOKEN")
  [[ -n "$body"  ]] && args+=(-d "$body")
  curl "${args[@]}" 2>/dev/null
}

body_of()   { echo "$1" | sed '$d'; }   # todo menos la última línea
status_of() { echo "$1" | tail -1; }    # última línea = HTTP status
first_id()  { echo "$1" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*'; }

# ── check ─────────────────────────────────────────────────────────
# check MÉTODO PATH [EXPECTED] [BODY]
# Guarda el ID de la respuesta en $LAST_ID
check() {
  local method="$1" path="$2" expected="${3:-200}" body="${4:-}"
  local resp; resp=$(api "$method" "$path" "$body")
  local status; status=$(status_of "$resp")
  local resp_body; resp_body=$(body_of "$resp")
  LAST_ID=$(first_id "$resp_body")
  local line; line="$(printf '%-8s %-50s → %s' "$method" "$path" "$status")"
  if [[ "$status" == "$expected" ]]; then
    ok "$line"
  else
    fail "$line ${RED}(esperado: $expected)${NC}"
    local err_msg; err_msg=$(echo "$resp_body" | grep -o '"message":"[^"]*"' | head -1 | cut -d'"' -f4)
    [[ -n "$err_msg" ]] && echo -e "     ${RED}└─ $err_msg${NC}"
  fi
}

# ── Limpieza al salir ─────────────────────────────────────────────
cleanup() {
  if [[ -z "$TOKEN" ]]; then return; fi
  echo ""
  echo -e "${YELLOW}${BOLD}── Limpiando datos de prueba...${NC}"

  local EXISTING_RATING_ID
  EXISTING_RATING_ID=$(first_id "$(body_of "$(api GET /api/users/me/ratings)")")

  [[ -n "$TEST_RATING_COMMENT_ID" && -n "$EXISTING_RATING_ID" ]] && {
    api DELETE "/api/ratings/$EXISTING_RATING_ID/comments/$TEST_RATING_COMMENT_ID" > /dev/null
    echo -e "  ${YELLOW}→${NC} Comentario de valoración eliminado ($TEST_RATING_COMMENT_ID)"
  }
  [[ -n "$TEST_LIST_COMMENT_ID" && -n "$TEST_LIST_ID" ]] && {
    api DELETE "/api/lists/$TEST_LIST_ID/comments/$TEST_LIST_COMMENT_ID" > /dev/null
    echo -e "  ${YELLOW}→${NC} Comentario de lista eliminado ($TEST_LIST_COMMENT_ID)"
  }
  [[ -n "$TEST_LIST_ID" ]] && {
    api DELETE "/api/lists/$TEST_LIST_ID" > /dev/null
    echo -e "  ${YELLOW}→${NC} Lista de prueba eliminada ($TEST_LIST_ID)"
  }
  [[ -n "$BOB_ID" ]] && {
    api DELETE "/api/users/$BOB_ID/followers" > /dev/null
    echo -e "  ${YELLOW}→${NC} Unfollow de bob ($BOB_ID)"
  }
  echo -e "${YELLOW}Limpieza completada.${NC}"
}
trap cleanup EXIT

# ════════════════════════════════════════════════════════════════════
# 1. LOGIN
# ════════════════════════════════════════════════════════════════════
section "AUTH"

LOGIN_RESP=$(api POST /api/auth/login \
  "{\"usernameOrEmail\":\"$LOGIN_USER\",\"password\":\"$LOGIN_PASS\"}")
TOKEN=$(body_of "$LOGIN_RESP" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [[ -z "$TOKEN" ]]; then
  echo -e "${RED}${BOLD}✗ Login fallido. ¿Está el backend corriendo en $BASE_URL?${NC}"
  echo "  Respuesta: $(body_of "$LOGIN_RESP")"
  exit 1
fi
ok "$(printf '%-8s %-50s → %s' "POST" "/api/auth/login" "200") (token OK)"

# Registro (usuario nuevo efímero)
check POST /api/auth/register 200 \
  "{\"username\":\"smoketest_$$\",\"email\":\"smoke$$@test.com\",\"password\":\"Smoke1234!\"}"

# ════════════════════════════════════════════════════════════════════
# 2. RECOPILAR IDs DINÁMICOS
# ════════════════════════════════════════════════════════════════════
section "Recopilando IDs..."

ME_RESP=$(body_of "$(api GET /api/users/me)")
USER_ID=$(first_id "$ME_RESP")
info "userId        = $USER_ID"

TRENDING_RESP=$(body_of "$(api GET /api/tmdb/trending)")
TMDB_ID=$(echo "$TRENDING_RESP" | grep -o '"tmdbId":[0-9]*' | head -1 | grep -o '[0-9]*')
CONTENT_TYPE=$(echo "$TRENDING_RESP" | grep -o '"contentType":"[^"]*"' | head -1 | cut -d'"' -f4)
info "tmdbId        = ${TMDB_ID:-none}  ($CONTENT_TYPE)"

RATINGS_RESP=$(body_of "$(api GET /api/users/me/ratings)")
EXISTING_RATING_ID=$(first_id "$RATINGS_RESP")
EXISTING_CONTENT_DB_ID=$(echo "$RATINGS_RESP" | grep -o '"content":{[^}]*"id":[0-9]*' | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
info "ratingId      = ${EXISTING_RATING_ID:-none}"
info "contentDbId   = ${EXISTING_CONTENT_DB_ID:-none}"

LISTS_RESP=$(body_of "$(api GET /api/users/me/lists)")
EXISTING_LIST_ID=$(first_id "$LISTS_RESP")
info "listId        = ${EXISTING_LIST_ID:-none}"

BOB_RESP=$(body_of "$(api GET "/api/users?q=bob")")
BOB_ID=$(first_id "$BOB_RESP")
info "bobId         = ${BOB_ID:-none}"

NOTIF_RESP=$(body_of "$(api GET /api/users/me/notifications)")
NOTIF_ID=$(first_id "$NOTIF_RESP")
info "notifId       = ${NOTIF_ID:-none}"

# ════════════════════════════════════════════════════════════════════
# 3. CREAR DATOS DE PRUEBA
# ════════════════════════════════════════════════════════════════════
section "Creando datos de prueba"

# Lista custom con nombre único
TEST_LIST_NAME="SmokeTest_$$"
check POST /api/lists 200 \
  "{\"name\":\"$TEST_LIST_NAME\",\"description\":\"Lista de prueba smoke test\",\"isPublic\":true,\"listType\":\"CUSTOM\"}"
TEST_LIST_ID="$LAST_ID"
info "TEST_LIST_ID  = ${TEST_LIST_ID:-ERROR}"

# Añadir contenido a la lista (usando tmdbId trending o fallback 550)
TMDB_FOR_LIST="${TMDB_ID:-550}"
[[ -n "$TEST_LIST_ID" ]] && check POST "/api/lists/$TEST_LIST_ID/content" 200 \
  "{\"tmdbId\":$TMDB_FOR_LIST}" || skip_msg POST "/api/lists/{id}/content" "no TEST_LIST_ID"

# Comentario en valoración existente
[[ -n "$EXISTING_RATING_ID" ]] && {
  check POST "/api/ratings/$EXISTING_RATING_ID/comments" 200 \
    "{\"content\":\"Comentario de smoke test\"}"
  TEST_RATING_COMMENT_ID="$LAST_ID"
  info "TEST_RATING_COMMENT_ID = ${TEST_RATING_COMMENT_ID:-ERROR}"
} || skip_msg POST "/api/ratings/{id}/comments" "no ratingId existente"

# Comentario en lista de prueba
[[ -n "$TEST_LIST_ID" ]] && {
  check POST "/api/lists/$TEST_LIST_ID/comments" 200 \
    "{\"content\":\"Comentario smoke test en lista\"}"
  TEST_LIST_COMMENT_ID="$LAST_ID"
  info "TEST_LIST_COMMENT_ID   = ${TEST_LIST_COMMENT_ID:-ERROR}"
} || skip_msg POST "/api/lists/{id}/comments" "no TEST_LIST_ID"

# Like en valoración existente (toggle)
[[ -n "$EXISTING_RATING_ID" ]] && check POST "/api/ratings/$EXISTING_RATING_ID/likes" 200 \
  || skip_msg POST "/api/ratings/{id}/likes" "no ratingId"

# Follow a bob (primero deseguir por si ya lo seguía del DataSeeder)
[[ -n "$BOB_ID" ]] && {
  api DELETE "/api/users/$BOB_ID/followers" > /dev/null 2>&1
  check POST "/api/users/$BOB_ID/follow-requests" 200
} || skip_msg POST "/api/users/{id}/follow-requests" "no bobId"

# ════════════════════════════════════════════════════════════════════
# 4. USUARIOS
# ════════════════════════════════════════════════════════════════════
section "USUARIOS — GET"

check GET /api/users/me
check GET /api/users/me/ratings
check GET /api/users/me/lists
check GET /api/users/me/badges
check GET /api/users/me/recommendations
check GET /api/users/me/stats/full
check GET /api/users/me/notifications
check GET /api/users/me/follow-requests
check GET /api/users/suggestions
check GET "/api/users?q=alice"
check GET "/api/users/username/$LOGIN_USER"

[[ -n "$USER_ID" ]] && {
  check GET "/api/users/$USER_ID"
  check GET "/api/users/$USER_ID/stats"
  check GET "/api/users/$USER_ID/ratings"
  check GET "/api/users/$USER_ID/lists"
  check GET "/api/users/$USER_ID/badges"
  check GET "/api/users/$USER_ID/followers"
  check GET "/api/users/$USER_ID/following"
} || {
  for ep in "" /stats /ratings /lists /badges /followers /following; do
    skip_msg GET "/api/users/{id}$ep" "no userId"
  done
}

[[ -n "$BOB_ID" ]] && check GET "/api/users/$BOB_ID/following-status" \
  || skip_msg GET "/api/users/{id}/following-status" "no bobId"

section "USUARIOS — PUT/PATCH"

check PUT /api/users/me/profile 200 \
  "{\"bio\":\"Bio de prueba smoke test\",\"avatarUrl\":null}"
check PUT /api/users/me/public-status 200 \
  "{\"isPublic\":false}"
# Restaurar estado público
check PUT /api/users/me/public-status 200 \
  "{\"isPublic\":true}"

# ════════════════════════════════════════════════════════════════════
# 5. TMDB
# ════════════════════════════════════════════════════════════════════
section "TMDB"

check GET /api/tmdb/trending
check GET /api/tmdb/movies/popular
check GET /api/tmdb/tv/popular
check GET /api/tmdb/discover/movies
check GET /api/tmdb/discover/tv
check GET /api/tmdb/genres/movies
check GET /api/tmdb/genres/tv
check GET "/api/tmdb/movies?query=batman"
check GET "/api/tmdb/tv?query=breaking+bad"

[[ -n "$TMDB_ID" && -n "$CONTENT_TYPE" ]] && {
  check GET "/api/tmdb/$CONTENT_TYPE/$TMDB_ID/credits"
  check GET "/api/tmdb/$CONTENT_TYPE/$TMDB_ID/providers"
  [[ "$CONTENT_TYPE" == "TV" ]] && check GET "/api/tmdb/tv/$TMDB_ID/seasons"
  # Person (usa ID de persona conocida: Tom Hanks = 31)
  check GET "/api/tmdb/people/31"
  check GET "/api/tmdb/people/31/credits"
} || {
  skip_msg GET "/api/tmdb/{type}/{id}/credits"  "no tmdbId"
  skip_msg GET "/api/tmdb/{type}/{id}/providers" "no tmdbId"
}

# ════════════════════════════════════════════════════════════════════
# 6. LISTAS
# ════════════════════════════════════════════════════════════════════
section "LISTAS"

check GET /api/lists/public

[[ -n "$EXISTING_LIST_ID" ]] && {
  check GET "/api/lists/$EXISTING_LIST_ID"
  check GET "/api/lists/$EXISTING_LIST_ID/comments"
} || {
  skip_msg GET "/api/lists/{id}"          "no listId"
  skip_msg GET "/api/lists/{id}/comments" "no listId"
}

[[ -n "$TEST_LIST_ID" ]] && {
  check GET "/api/lists/$TEST_LIST_ID"
  check PUT "/api/lists/$TEST_LIST_ID" 200 \
    "{\"name\":\"${TEST_LIST_NAME}_edited\",\"description\":\"Editada\",\"isPublic\":false,\"listType\":\"CUSTOM\"}"
  # Eliminar contenido de la lista
  check DELETE "/api/lists/$TEST_LIST_ID/content/$TMDB_FOR_LIST" 200
} || skip_msg PUT "/api/lists/{id}" "no TEST_LIST_ID"

# ════════════════════════════════════════════════════════════════════
# 7. VALORACIONES
# ════════════════════════════════════════════════════════════════════
section "VALORACIONES"

[[ -n "$EXISTING_CONTENT_DB_ID" ]] && check GET "/api/ratings/$EXISTING_CONTENT_DB_ID" \
  || skip_msg GET "/api/ratings/{contentId}" "no contentDbId"

[[ -n "$EXISTING_RATING_ID" ]] && {
  check GET "/api/ratings/$EXISTING_RATING_ID/likes"
  check GET "/api/ratings/$EXISTING_RATING_ID/like-status"
  check GET "/api/ratings/$EXISTING_RATING_ID/comments"
  # Toggle like off (segunda llamada = unlike)
  check POST "/api/ratings/$EXISTING_RATING_ID/likes" 200
} || {
  skip_msg GET "/api/ratings/{id}/likes"    "no ratingId"
  skip_msg GET "/api/ratings/{id}/comments" "no ratingId"
}

# Crear una valoración nueva (upsert seguro: actualiza si ya existe)
[[ -n "$TMDB_ID" && -n "$CONTENT_TYPE" ]] && check POST /api/ratings 200 \
  "{\"tmdbId\":$TMDB_ID,\"contentType\":\"$CONTENT_TYPE\",\"rating\":4,\"status\":\"VISTA\",\"reviewText\":\"Review smoke test\"}" \
  || skip_msg POST "/api/ratings" "no tmdbId"

# ════════════════════════════════════════════════════════════════════
# 8. FEED
# ════════════════════════════════════════════════════════════════════
section "FEED / ACTIVIDAD"

check GET /api/feed/global
check GET /api/feed/personal

# ════════════════════════════════════════════════════════════════════
# 9. NOTIFICACIONES
# ════════════════════════════════════════════════════════════════════
section "NOTIFICACIONES"

check GET /api/notifications/unread-count

[[ -n "$NOTIF_ID" ]] && {
  check PATCH "/api/notifications/$NOTIF_ID/read" 204
} || skip_msg PATCH "/api/notifications/{id}/read" "sin notificaciones"

check PATCH /api/notifications/read-all 204

# ════════════════════════════════════════════════════════════════════
# 10. EPISODIOS
# ════════════════════════════════════════════════════════════════════
section "EPISODIOS"

check GET /api/episodes/watched/summary

# Usa una serie conocida (Breaking Bad = 1396) para test toggle episodio
SERIES_ID=1396
check GET    "/api/episodes/watched/$SERIES_ID"
check POST   "/api/episodes/watched/$SERIES_ID/1/1"    200   # toggle ep 1x01
check POST   "/api/episodes/watched/$SERIES_ID/1/1"    200   # toggle de nuevo (desmarcar)

# ════════════════════════════════════════════════════════════════════
# 11. CONTENIDO (BD interna)
# ════════════════════════════════════════════════════════════════════
section "CONTENT DB"

check GET /api/content

[[ -n "$EXISTING_CONTENT_DB_ID" ]] && check GET "/api/content/$EXISTING_CONTENT_DB_ID" \
  || skip_msg GET "/api/content/{id}" "no contentDbId"

# ════════════════════════════════════════════════════════════════════
# 12. ADMIN (debe devolver 403 para usuario normal)
# ════════════════════════════════════════════════════════════════════
section "ADMIN (esperado 403 — usuario normal)"

check GET  /api/admin/users   403
check GET  /api/admin/reports 403

# ════════════════════════════════════════════════════════════════════
# 13. COMENTARIOS — DELETE (limpieza manual de comentario de rating)
# ════════════════════════════════════════════════════════════════════
section "CLEANUP COMMENTS"

[[ -n "$TEST_RATING_COMMENT_ID" && -n "$EXISTING_RATING_ID" ]] && {
  check DELETE "/api/ratings/$EXISTING_RATING_ID/comments/$TEST_RATING_COMMENT_ID" 200
  TEST_RATING_COMMENT_ID=""  # ya limpiado, no repetir en trap
} || skip_msg DELETE "/api/ratings/{id}/comments/{id}" "no hay comentario de test"

[[ -n "$TEST_LIST_COMMENT_ID" && -n "$TEST_LIST_ID" ]] && {
  check DELETE "/api/lists/$TEST_LIST_ID/comments/$TEST_LIST_COMMENT_ID" 200
  TEST_LIST_COMMENT_ID=""
} || skip_msg DELETE "/api/lists/{id}/comments/{id}" "no hay comentario de test"

# ════════════════════════════════════════════════════════════════════
# RESUMEN FINAL
# ════════════════════════════════════════════════════════════════════
TOTAL=$((PASS + FAIL + SKIP))
echo ""
echo -e "${BOLD}══════════════════════════════════════════════════════════${NC}"
echo -e " Total: $TOTAL   ${GREEN}${BOLD}✓ Passed: $PASS${NC}   ${RED}${BOLD}✗ Failed: $FAIL${NC}   ${YELLOW}– Skipped: $SKIP${NC}"
echo -e "${BOLD}══════════════════════════════════════════════════════════${NC}"
if [[ $FAIL -eq 0 ]]; then
  echo -e " ${GREEN}${BOLD}Todos los endpoints responden correctamente.${NC}"
else
  echo -e " ${RED}${BOLD}$FAIL endpoint(s) con errores — revisa los marcados con [✗].${NC}"
fi
echo ""

exit $FAIL
