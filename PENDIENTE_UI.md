# MovieMate — Mejoras y bugs pendientes de UI/UX

Registradas el 2026-03-12. Marcar con ✅ conforme se vayan completando.

---

## Bugs

| # | Estado | Descripción | Archivo(s) afectado(s) |
|---|--------|-------------|------------------------|
| B1 | ✅ | **Sidebar marca "Valoraciones" activo al visitar un perfil** — `NavLink` a `/profile/:username?tab=ratings` queda activo cuando estás en cualquier `/profile/...`. Debe desactivarse; "Valoraciones" solo debería estar activo si estás en TU propio perfil en la pestaña de ratings, o simplemente nunca marcar como activo. | `Sidebar.tsx` |
| B2 | ✅ | **Botón × desalineado en ListDetailPage** — el botón de eliminar contenido de una lista flota demasiado a la derecha de la card, fuera del borde. Debe estar pegado a la esquina superior derecha de la card (absolute dentro del contenedor del poster). | `ListDetailPage.tsx` |
| B3 | ✅ | **DetailPage no está centrada en la pantalla** — el contenido de la ficha de detalle ocupa todo el ancho. Debe tener `max-w` y estar centrado horizontalmente. Revisar también otras páginas con el mismo problema. | `DetailPage.tsx`, `DetailHero.tsx` |
| B4 | ✅ | **Sinopsis duplicada en DetailPage** — la sinopsis aparece dos veces (una cerca del título y otra más abajo en el aside). Eliminar la segunda aparición; quedarse solo con la que está junto al título. | `DetailPage.tsx` |
| B5 | ✅ | **Géneros no se muestran en DetailPage** — el campo `genres` llega del backend pero no se renderiza en la ficha. Añadirlo junto al título o en el aside. | `DetailPage.tsx` |

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
| M10 | ⬜ | **Logo MovieMate en Sidebar al mismo alto que la Topbar** — el icono 🎬 + texto "MovieMate" en la parte superior del sidebar no coincide en altura con la Topbar (`h-13` = 52px). Ajustar: bien ampliar la zona de marca en el sidebar para que tenga `h-13`, bien reducir el tamaño del icono para que encaje sin necesitar más espacio. | `Sidebar.tsx`, `Topbar.tsx` |
| M11 | ✅ | **Botones editar/borrar valoraciones en ProfilePage** — añadir botones ✏ y × en hover sobre cada poster de valoración propia. El × elimina la valoración directamente; el ✏ abre `QuickEditRatingDialog` con los campos pre-rellenados (puntuación, etiqueta emocional, estado, fecha, reseña). | `ProfilePage.tsx`, backend `DELETE /api/ratings/{id}` ya existía |
| M12 | ✅ | **Botones editar/borrar listas en ListsPage** — al pasar el ratón por una lista propia, mostrar ✏ (en todas las listas) y × (solo en listas CUSTOM). El ✏ abre `EditListDialog` con nombre/descripción/visibilidad; el nombre es readonly para FAVORITES/WATCHLIST/WATCHED. Backend: nuevos endpoints `DELETE /api/lists/{id}` y `PUT /api/lists/{id}`. | `ListsPage.tsx`, `ListService.java`, `ListController.java`, `listsApi.ts` |
| M13 | ✅ | **Responsive completo de la app** — en móvil la sidebar desaparece (`hidden lg:flex`) y se muestra una `BottomNavBar` fija con: Inicio, Descubrir, Actividad, Listas, Notificaciones (con badge). Padding adaptativo `px-4 lg:px-6` en todas las páginas. Hero del home reducido en móvil (`h-72 lg:h-115`), poster oculto en móvil. Topbar con padding reducido en móvil. | `Layout.tsx`, `Sidebar.tsx`, `Topbar.tsx`, `HomePage.tsx`, y todas las páginas de contenido |
| M14 | ✅ | **Grids de posters con separación excesiva** — los grids de columnas fijas (`grid-cols-N`) dejaban celdas más anchas que el `PosterCard` (w-36), creando espacios vacíos. Cambiados a `flex flex-wrap gap-3` en: `ContentGrid`, `SpecialListPage`, `ProfilePage` (tab valoraciones), `ListDetailPage`. | `ContentGrid.tsx`, `SpecialListPage.tsx`, `ProfilePage.tsx`, `ListDetailPage.tsx` |
| M15 | ✅ | **Centrado de páginas con contenido estrecho** — SettingsPage, DiscoverPage y ActivityPage no tenían `mx-auto`, por lo que el contenido se estiraba en pantallas anchas. Añadido `max-w-2xl/5xl/6xl mx-auto` según la página. | `SettingsPage.tsx`, `DiscoverPage.tsx`, `ActivityPage.tsx` |
| M16 | ✅ | **Auth pages — contraste y layout** — placeholders de inputs con `text-white/30` para distinguir del texto escrito; panel izquierdo de Login/Register centrado con `items-center` y textos con mejor contraste (`text-white/50`). | `LoginPage.tsx`, `RegisterPage.tsx` |
| M17 | ✅ | **Hover effect en contadores seguidores/siguiendo** — los contadores en ProfilePage cambian a color accent al hover para indicar que son clicables (usando el patrón `group`/`group-hover:`). | `ProfilePage.tsx` |
| M18 | ✅ | **Botón "+ Añadir" junto al título en ListDetailPage** — el botón estaba empujado al extremo derecho con `justify-between`; ahora está inline junto al icono y el nombre de la lista. | `ListDetailPage.tsx` |
| M19 | ✅ | **Sidebar "Mis listas" → "Listas"** — renombrado el enlace de navegación. | `Sidebar.tsx` |

---

## Orden de prioridad sugerido

1. B1, B2, B3 — bugs visuales rápidos
2. M7 — logo clicable (trivial)
3. M3, M5, M6 — polish visual
4. B4, B5 — DetailPage cleanup
5. M1 — separar listas propias de exploración
6. M2 — añadir contenido desde dentro de la lista
7. M4 — seguidores/siguiendo clicables
8. M8, M9 — más contenido en home y búsqueda mixta

---

## Notas técnicas

- El backend ya tiene `GET /api/users/{userId}/followers` y `GET /api/users/{userId}/following` — M4 es solo frontend.
- Para M9 (mezcla), el endpoint de TMDB `/trending/all/week` ya devuelve mix de movies y tv — se puede usar para M8 también.
- Para M2, el `ListController` ya tiene `POST /api/lists/{listId}/content` — solo falta la UI de búsqueda.
- Para M3, el diseño de referencia está en `ActivityPage.tsx` (sección `ActivityItem`).
