# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**HECHO en las últimas sesiones:**
- Todas las páginas implementadas y funcionando (HomePage, DiscoverPage, DetailPage, ProfilePage, ListsPage, ListDetailPage, SpecialListPage, NotificationsPage, SettingsPage, ActivityPage)
- WebSocket STOMP integrado: `useWebSocket` hook en `Layout.tsx`, conecta a `/ws` con JWT, suscribe a `/user/queue/notifications`, actualiza badge en tiempo real
- Paginación cliente en ReviewList (5 en 5, botón "Ver más")
- Varios bugs corregidos: `@ElementCollection(fetch = FetchType.EAGER)` en `Content.java`, endpoint `GET /api/lists/{listId}` añadido al backend con `ListPrivateException`, proxy `/ws` en Vite config, `global: 'globalThis'` en Vite para sockjs

**PENDIENTE (ver `PENDIENTE_UI.md` para detalle completo):**

Bugs:
- B1: Sidebar marca "Valoraciones" activo al visitar cualquier perfil (debería no marcarse)
- B2: Botón × de eliminar en ListDetailPage desalineado (flota a la derecha de la card)
- B3: DetailPage no está centrada en pantalla (falta max-w y centrado horizontal)
- B4: Sinopsis duplicada en DetailPage (aparece dos veces, eliminar la segunda)
- B5: Géneros no se muestran en DetailPage

Mejoras:
- M1: ListsPage — separar "Mis listas" de "Explorar listas públicas" (sección/tab/página separada)
- M2: Añadir contenido a una lista desde dentro de ListDetailPage (buscador + añadir)
- M3: Reducir espaciado entre reseñas en ReviewList (como en ActivityPage)
- M4: Seguidores/siguiendo clicables en perfiles ajenos (abrir lista de usuarios)
- M5: Backdrop más transparente en DetailPage
- M6: Backdrop más transparente en Hero del HomePage
- M7: Logo "MovieMate" del sidebar debe ser Link al inicio (trivial)
- M8: Más secciones en HomePage (Tendencias, Mejor valorados, etc.)
- M9: Mezclar películas y series en carruseles y búsqueda (sin separar por tipo)

**Ficheros clave:**
- `TFG_MovieMate/PROGRESO_FRONTEND.md` — progreso detallado de todo lo implementado
- `TFG_MovieMate/PENDIENTE_UI.md` — lista de bugs y mejoras pendientes con prioridad
- Backend: `moviemate-backend/src/main/java/com/moviemate/`
- Frontend: `moviemate-frontend/src/`

Empieza leyendo `PENDIENTE_UI.md` y `PROGRESO_FRONTEND.md`, y luego implementa las tareas en el orden de prioridad del fichero.
