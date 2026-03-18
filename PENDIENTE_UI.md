# MovieMate — Mejoras y bugs pendientes de UI/UX

Registradas el 2026-03-12. Última actualización: 2026-03-17. Marcar con ✅ conforme se vayan completando.

---

## Bugs

| # | Estado | Descripción | Archivo(s) afectado(s) |
|---|--------|-------------|------------------------|
| B1 | ✅ | **Sidebar marca "Valoraciones" activo al visitar un perfil** — `NavLink` a `/profile/:username?tab=ratings` queda activo cuando estás en cualquier `/profile/...`. Debe desactivarse; "Valoraciones" solo debería estar activo si estás en TU propio perfil en la pestaña de ratings, o simplemente nunca marcar como activo. | `Sidebar.tsx` |
| B2 | ✅ | **Botón × desalineado en ListDetailPage** — el botón de eliminar contenido de una lista flota demasiado a la derecha de la card, fuera del borde. Debe estar pegado a la esquina superior derecha de la card (absolute dentro del contenedor del poster). | `ListDetailPage.tsx` |
| B3 | ✅ | **DetailPage no está centrada en la pantalla** — el contenido de la ficha de detalle ocupa todo el ancho. Debe tener `max-w` y estar centrado horizontalmente. Revisar también otras páginas con el mismo problema. | `DetailPage.tsx`, `DetailHero.tsx` |
| B4 | ✅ | **Sinopsis duplicada en DetailPage** — la sinopsis aparece dos veces (una cerca del título y otra más abajo en el aside). Eliminar la segunda aparición; quedarse solo con la que está junto al título. | `DetailPage.tsx` |
| B5 | ✅ | **Géneros no se muestran en DetailPage** — el campo `genres` llega del backend pero no se renderiza en la ficha. Añadirlo junto al título o en el aside. | `DetailPage.tsx` |
| B6 | ✅ | **Backend no arranca (socket hang up en todos los endpoints)** — al añadir campos `role VARCHAR NOT NULL` y `banned BOOLEAN NOT NULL` a la tabla `users` con `ddl-auto=update`, PostgreSQL rechazaba el ALTER TABLE porque las filas existentes quedarían NULL. Solucionado con `columnDefinition = "VARCHAR(20) DEFAULT 'USER'"` y `columnDefinition = "BOOLEAN DEFAULT FALSE"`. | `entity/User.java` |
| B7 | ✅ | **Valoraciones de contenido no visibles** — `getRatingsByContent` filtraba por usuario actual (`findAllByUserAndContent`) en lugar de devolver todas las valoraciones del contenido. Solo se veía la propia valoración. | `service/RatingService.java` |
| B8 | ✅ | **Notificaciones mostrando "usuario_null"** — `NotificationService.toDto()` intentaba buscar el remitente por `referenceId` (que era el ID de la valoración, no del like/follow), fallando siempre. Solucionado usando `notification.getSender()` directamente. | `service/NotificationService.java` |
| B9 | ✅ | **ActivityPage sin título ni poster del contenido** — el builder de actividades de valoración no incluía `.content(...)`. El frontend no podía mostrar a qué película/serie se refería la valoración. | `service/ActivityService.java` |
| B10 | ✅ | **Textos invisibles en páginas de auth** — "MovieMate", "Bienvenido" y "Crea tu cuenta" sin `text-white` heredaban el color del tema shadcn (oscuro). Botón de mostrar contraseña invisible por el mismo motivo. | `LoginPage.tsx`, `RegisterPage.tsx` |
| B11 | ✅ | **Botón nativo del navegador duplicado en campo de contraseña** — Edge/Chrome añaden su propio botón de revelar contraseña al escribir, apareciendo a la izquierda del botón SVG personalizado. Solucionado con `::-ms-reveal`, `::-ms-clear` y `::-webkit-credentials-auto-fill-button` en CSS global. | `index.css` |
| B12 | ✅ | **"Resolver" reporte no hacía nada visible** — solo cambiaba el estado del reporte a RESOLVED pero no borraba el contenido denunciado ni notificaba al usuario. | `service/ContentReportService.java` |

---

## Mejoras de UI / nuevas funcionalidades

| # | Estado | Descripción | Archivo(s) afectado(s) |
|---|--------|-------------|------------------------|
| M1 | ✅ | **ListsPage: separar "Mis listas" de "Explorar listas públicas"** — actualmente muestra todas las listas públicas mezcladas. El objetivo es: sección principal = solo listas del usuario autenticado; sección secundaria (misma página con tab/toggle, o página nueva `/lists/explore`) = listas públicas de otros usuarios. | `ListsPage.tsx` |
| M2 | ✅ | **Añadir contenido a una lista desde dentro de la lista** — en `ListDetailPage`, añadir un botón "+ Añadir contenido" que abra un buscador (similar al de `DiscoverPage`) y permita añadir el resultado directamente a esa lista. | `ListDetailPage.tsx`, posiblemente un nuevo componente `AddContentToListDialog.tsx` |
| M3 | ✅ | **Compactar espacio entre valoraciones en ReviewList** — actualmente hay demasiado `gap` entre las cards de reseña. Reducir el espaciado para que se parezca más al diseño de la sección "Última actividad" (cards más juntas, padding interno menor). | `ReviewList.tsx` |
| M4 | ✅ | **Ver seguidores y siguiendo de perfiles ajenos** — al pulsar en el contador de "Seguidores" o "Siguiendo" en el perfil de un usuario público o de alguien a quien sigues, debe abrirse un panel/modal/página con la lista de usuarios. Actualmente los contadores son solo texto. | `ProfilePage.tsx`, posiblemente un nuevo componente `FollowListDialog.tsx` o una ruta nueva |
| M5 | ✅ | **Fondo más transparente en DetailPage** — la imagen de backdrop detrás de los detalles del contenido debe ser más transparente / con mayor overlay oscuro para mejorar la legibilidad. | `DetailHero.tsx`, `DetailPage.tsx` |
| M6 | ✅ | **Fondo más transparente en el Hero del HomePage** — el backdrop de la sección "Tendencia de la semana" debe tener un overlay más oscuro/transparente, igual que la mejora en DetailPage. | `HomePage.tsx` |
| M7 | ✅ | **Logo "MovieMate" del sidebar clicable** — el icono 🎬 + texto "MovieMate" en la parte superior del sidebar debe ser un `<Link to="/">` para volver al inicio. | `Sidebar.tsx` |
| M8 | ✅ | **HomePage: más secciones de contenido** — añadir más carruseles además de "Populares ahora". Ideas: "Mejor valorados", "Tendencias esta semana", "Series populares", "Películas recientes". Cada sección es un carrusel horizontal independiente. | `HomePage.tsx`, `useDiscover.ts` o nuevo `useHome.ts` |
| M9 | ✅ | **Mezclar películas y series en carruseles y búsqueda** — actualmente los carruseles del home y la búsqueda muestran primero películas y luego series. Deben estar mezclados (por popularidad/relevancia sin separar por tipo). Afecta a: carrusel del home, resultados de búsqueda en `DiscoverPage`. | `HomePage.tsx`, `DiscoverPage.tsx`, `useDiscover.ts`, posiblemente ajuste en backend TMDB API |
| M10 | ✅ | **Logo MovieMate en Sidebar al mismo alto que la Topbar** — el icono 🎬 + texto "MovieMate" en la parte superior del sidebar no coincide en altura con la Topbar (`h-13` = 52px). Ajustar: bien ampliar la zona de marca en el sidebar para que tenga `h-13`, bien reducir el tamaño del icono para que encaje sin necesitar más espacio. | `Sidebar.tsx`, `Topbar.tsx` |
| M11 | ✅ | **Botones editar/borrar valoraciones en ProfilePage** — añadir botones ✏ y × en hover sobre cada poster de valoración propia. El × elimina la valoración directamente; el ✏ abre `QuickEditRatingDialog` con los campos pre-rellenados (puntuación, etiqueta emocional, estado, fecha, reseña). | `ProfilePage.tsx`, backend `DELETE /api/ratings/{id}` ya existía |
| M12 | ✅ | **Botones editar/borrar listas en ListsPage** — al pasar el ratón por una lista propia, mostrar ✏ (en todas las listas) y × (solo en listas CUSTOM). El ✏ abre `EditListDialog` con nombre/descripción/visibilidad; el nombre es readonly para FAVORITES/WATCHLIST/WATCHED. Backend: nuevos endpoints `DELETE /api/lists/{id}` y `PUT /api/lists/{id}`. | `ListsPage.tsx`, `ListService.java`, `ListController.java`, `listsApi.ts` |
| M13 | ✅ | **Responsive completo de la app** — en móvil la sidebar desaparece (`hidden lg:flex`) y se muestra una `BottomNavBar` fija con: Inicio, Descubrir, Actividad, Listas, Notificaciones (con badge). Padding adaptativo `px-4 lg:px-6` en todas las páginas. Hero del home reducido en móvil (`h-72 lg:h-115`), poster oculto en móvil. Topbar con padding reducido en móvil. | `Layout.tsx`, `Sidebar.tsx`, `Topbar.tsx`, `HomePage.tsx`, y todas las páginas de contenido |
| M14 | ✅ | **Grids de posters con separación excesiva** — los grids de columnas fijas (`grid-cols-N`) dejaban celdas más anchas que el `PosterCard` (w-36), creando espacios vacíos. Cambiados a `flex flex-wrap gap-3` en: `ContentGrid`, `SpecialListPage`, `ProfilePage` (tab valoraciones), `ListDetailPage`. | `ContentGrid.tsx`, `SpecialListPage.tsx`, `ProfilePage.tsx`, `ListDetailPage.tsx` |
| M15 | ✅ | **Centrado de páginas con contenido estrecho** — SettingsPage, DiscoverPage y ActivityPage no tenían `mx-auto`, por lo que el contenido se estiraba en pantallas anchas. Añadido `max-w-2xl/5xl/6xl mx-auto` según la página. | `SettingsPage.tsx`, `DiscoverPage.tsx`, `ActivityPage.tsx` |
| M16 | ✅ | **Auth pages — contraste y layout** — placeholders de inputs con `text-white/30` para distinguir del texto escrito; panel izquierdo de Login/Register centrado con `items-center` y textos con mejor contraste (`text-white/50`). | `LoginPage.tsx`, `RegisterPage.tsx` |
| M17 | ✅ | **Hover effect en contadores seguidores/siguiendo** — los contadores en ProfilePage cambian a color accent al hover para indicar que son clicables (usando el patrón `group`/`group-hover:`). | `ProfilePage.tsx` |
| M18 | ✅ | **Botón "+ Añadir" junto al título en ListDetailPage** — el botón estaba empujado al extremo derecho con `justify-between`; ahora está inline junto al icono y el nombre de la lista. | `ListDetailPage.tsx` |
| M19 | ✅ | **Sidebar "Mis listas" → "Listas"** — renombrado el enlace de navegación. | `Sidebar.tsx` |
| M20 | ✅ | **Botón de me gusta en valoraciones sin diferenciación visual** — el corazón no se distinguía entre activo e inactivo. Ahora: activo = rojo con fondo semitransparente; inactivo = gris claro; deshabilitado (no autenticado) = casi invisible. Reemplazado emoji por SVG. | `components/Detail/ReviewList.tsx` |
| M21 | ✅ | **Punto rojo de notificación no leída mal posicionado** — el indicador estaba al final de la fila, lejos del texto. Movido a la derecha inmediata del texto del mensaje. | `features/notifications/NotificationsPage.tsx` |
| M22 | ✅ | **Botón "Marcar todo como leído" no visible** — el botón usaba `text-muted` (muy oscuro) y sin borde. Actualizado a `text-white/70 hover:text-white border border-white/[0.15]`. | `features/notifications/NotificationsPage.tsx` |
| M23 | ✅ | **Admin: resolver reporte no borraba el contenido ni notificaba** — "Resolver" solo cambiaba el estado. Ahora obtiene el autor del contenido antes de borrarlo, lo elimina, y envía notificación `CONTENT_REMOVED` con el motivo en español. | `service/ContentReportService.java`, `service/NotificationService.java` |
| M24 | ✅ | **Admin: no se podía ver el contenido de un reporte** — el panel solo mostraba el ID del objeto denunciado. Ahora cada fila de reporte tiene un botón ▼ que despliega la valoración (estrellas, texto, título del contenido) o el comentario (autor, texto). Carga lazy al expandir. | `features/admin/AdminPage.tsx`, `controller/AdminController.java` |
| M25 | ✅ | **Botón de mostrar/ocultar contraseña mostraba emoji** — se usaba 👁️/🙈. Reemplazado por iconos SVG estilo Heroicons (ojo y ojo tachado) con `text-white/60 hover:text-white`. | `LoginPage.tsx`, `RegisterPage.tsx` |
| M26 | ✅ | **ActivityPage sin tipos RATING_UPDATED / LIST_UPDATED** — ActivityService no distinguía entre nueva valoración y edición. Ahora detecta `updatedAt > createdAt + 1min` y emite el tipo correspondiente con `updatedAt` como timestamp. | `service/ActivityService.java` |
| M27 | ✅ | **Usuarios sugeridos en HomePage** — sección "Cinéfilos que quizás conozcas" al final del home (solo autenticados). Muestra hasta 5 sugerencias con avatar, username y bio. | `features/home/HomePage.tsx` |
| M28 | ✅ | **Subida real de avatar (multipart)** — en Settings, el avatar ya no requiere pegar una URL: hay un botón de subir archivo que acepta JPG/PNG/WebP (≤5MB), muestra preview inmediato con `URL.createObjectURL` y sube al backend. Backend sirve los archivos desde `/uploads/avatars/**`. | `features/settings/SettingsPage.tsx`, `service/UserService.java`, `config/WebMvcConfig.java`, `config/SecurityConfig.java` |
| M29 | ✅ | **Recomendaciones personalizadas en HomePage** — carrusel "Para ti ✨" (solo autenticados). El backend toma el top género del usuario y lanza un discover TMDB filtrando por ese género con nota mínima 7.0, devolviendo hasta 6 películas + 6 series. | `features/home/HomePage.tsx`, `controller/UserController.java` |
| M30 | ✅ | **Insignias/gamificación** — sistema de 10 insignias: FIRST_REVIEW, CRITIC, CINEPHILE, FILM_BUFF, MOVIE_MARATHON, SERIES_BINGE, SOCIAL, POPULAR, LISTER, LIKED. Se otorgan automáticamente al actualizar stats. Se muestran como chips en el perfil (entre stats y películas favoritas). | `entity/UserBadge.java`, `service/BadgeService.java`, `features/profile/ProfilePage.tsx` |
| M31 | ✅ | **Comentarios en listas** — sección de comentarios en ListDetailPage: textarea para publicar, lista con avatar + timeAgo + botón eliminar propio. Backend: nueva entidad `ListComment`, servicio y controller en `/api/lists/{id}/comments`. | `entity/ListComment.java`, `service/ListCommentService.java`, `features/lists/ListDetailPage.tsx` |

---

## Bugs descubiertos en sesiones posteriores (B13–B18)

| # | Estado | Descripción | Archivo(s) afectado(s) |
|---|--------|-------------|------------------------|
| B13 | ✅ | **Sin botón volver en Login/Register** — añadido `BackButton` en ambas páginas. | `LoginPage.tsx`, `RegisterPage.tsx` |
| B14 | ✅ | **NPE al ver perfil de usuario (unauthenticated)** — `getUserProfile` llamaba a `userDetails.getUser()` sin null-check. Perfiles públicos accedidos sin JWT fallaban con NPE → 400. | `controller/UserController.java` |
| B15 | ✅ | **Películas favoritas se pisan en ProfilePage** — grids de posters migrados a `flex flex-wrap gap-3`. | `features/profile/ProfilePage.tsx` |
| B16 | ✅ | **Error 400 al añadir contenido a una lista** — `ContentService.getOrFetch` usaba `/search/multi?query={id}` (búsqueda de texto con un ID numérico) para detectar el tipo de contenido. El ID nunca aparecía en resultados → `orElseThrow` → 400. Arreglado: ahora prueba MOVIE primero y luego TV usando los endpoints directos `/movie/{id}` y `/tv/{id}`. | `service/ContentService.java` |
| B17 | ✅ | **Error 400 en recomendaciones y ratings** — dos fixes: (1) `UserStatsService.getFullStats` no era `@Transactional`, causando que `updateUserStats` via self-invocation se ejecutase sin transacción gestionada; (2) `fetchFromTmdb` podía NPE si TMDB devolvía `null`. | `service/UserStatsService.java`, `service/ContentService.java` |
| B18 | ✅ | **Toggle privacidad en SettingsPage mal renderizado** — el switch de perfil privado no reflejaba el estado inicial correctamente. | `features/settings/SettingsPage.tsx` |

## Mejoras (M32–M34)

| # | Estado | Descripción | Archivo(s) afectado(s) |
|---|--------|-------------|------------------------|
| M32 | ✅ | **Botón volver atrás en todas las páginas** — `BackButton` añadido a: NotificationsPage, ActivityPage, ListsPage, SpecialListPage, SettingsPage, PersonPage, ProfilePage (solo perfiles ajenos). | Múltiples páginas |
| M33 | ✅ | **Filtros en DiscoverPage para tipo ALL** — `useDiscover` ahora hace fetch de películas + series cuando `filter=ALL` y los interleaves. Filtros visibles para ALL cuando `hasActiveFilters`. | `hooks/useDiscover.ts`, `features/discover/DiscoverPage.tsx` |
| M34 | ✅ | **Episodios marcados como vistos visibles en perfil** — nueva sección "Progreso de series" en StatsTab. Backend: `GET /episodes/watched/summary` (JPQL GROUP BY serie). Frontend: `SeriesProgressSection` con cards clickables. | `controller/EpisodeWatchController.java`, `service/EpisodeWatchService.java`, `components/profile/StatsTab.tsx` |

---

## Estado final

**Todos los bugs (B1–B18) resueltos. Todas las mejoras (M1–M34) implementadas.**

---

## Notas técnicas

- El backend ya tiene `GET /api/users/{userId}/followers` y `GET /api/users/{userId}/following` — M4 es solo frontend.
- Para M9 (mezcla), el endpoint de TMDB `/trending/all/week` ya devuelve mix de movies y tv — se puede usar para M8 también.
- Para M2, el `ListController` ya tiene `POST /api/lists/{listId}/content` — solo falta la UI de búsqueda.
- Para M3, el diseño de referencia está en `ActivityPage.tsx` (sección `ActivityItem`).
- Avatares subidos: se guardan en `uploads/avatars/` en el directorio de trabajo del backend. En Docker esto es efímero; para producción habría que montar un volumen o migrar a S3.
- Insignias: se evalúan en `UserStatsService.updateUserStats()` — se otorgan de forma idempotente (no se duplican).
