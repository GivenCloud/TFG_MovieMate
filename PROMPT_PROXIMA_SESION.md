# Prompt para la próxima sesión con Claude

Copia y pega esto al inicio de la siguiente conversación:

---

Continuamos el desarrollo del TFG MovieMate. Lee MEMORY.md para el contexto completo.

**Resumen rápido del estado actual:**

PROYECTO: Plataforma social de cine/series (IMDB + Letterboxd + Serializd).
Stack: Spring Boot 3.3.4 + PostgreSQL 16 (backend) / React 19 + TypeScript + TanStack Query 5 + Tailwind CSS 4 (frontend).
Rama activa: `feat/frontend-pages`

**HECHO en las últimas sesiones (resumen):**
- Todas las páginas implementadas (HomePage, DiscoverPage, DetailPage, ProfilePage, ListsPage, ListDetailPage, SpecialListPage, NotificationsPage, SettingsPage, ActivityPage)
- WebSocket STOMP: `useWebSocket` en Layout.tsx, notificaciones en tiempo real
- Paginación cliente en ReviewList (5 en 5)
- Bugs B1-B5 y mejoras M1-M9 completados (ver PENDIENTE_UI.md)
- M11: Borrar/editar valoraciones propias en ProfilePage (QuickEditRatingDialog, botones ✏ × en hover)
- M12: Borrar/editar listas en ListsPage (✏ en todas, × solo CUSTOM, EditListDialog) + backend DELETE/PUT /api/lists/{id}
- M13: Responsive completo — Sidebar oculto en móvil, BottomNavBar fija (Inicio/Descubrir/Actividad/Listas/Notif.), paddings adaptativos px-4 lg:px-6 en todas las páginas, hero del home adaptable
- M14: Grids de posters → flex flex-wrap gap-3 (ContentGrid, SpecialListPage, ProfilePage, ListDetailPage) — elimina celdas vacías enormes
- M15: Centrado de SettingsPage (max-w-2xl mx-auto), DiscoverPage (max-w-6xl mx-auto), ActivityPage (max-w-2xl mx-auto en feed)
- M16: Auth pages — placeholders más grises, panel izquierdo centrado, contraste mejorado
- M17: ProfilePage — hover effect en contadores seguidores/siguiendo (group/group-hover)
- M18: ListDetailPage — botón "+ Añadir" inline junto al título (no justify-between)
- M19: Sidebar "Mis listas" → "Listas"

**PENDIENTE (ver `PENDIENTE_UI.md` para detalle completo):**
- M10: Logo MovieMate en Sidebar al mismo alto que la Topbar (h-13 = 52px) — decidir si ampliar sidebar brand o reducir icono

**Ficheros clave:**
- `TFG_MovieMate/PENDIENTE_UI.md` — lista de bugs y mejoras con estado actual
- `TFG_MovieMate/PROGRESO_FRONTEND.md` — progreso detallado de todo lo implementado
- Backend: `MovieMate/moviemate-backend/src/main/java/com/moviemate/`
- Frontend: `MovieMate/moviemate-frontend/src/`

Empieza leyendo `PENDIENTE_UI.md` para ver el único punto pendiente (M10) y pregunta al usuario qué quiere hacer a continuación.
