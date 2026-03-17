# MovieMate — Progreso del Frontend

Resumen de todo lo implementado en el frontend durante el desarrollo del TFG.
Actualizado: 2026-03-17 (sesión final BAJA)

---

## Estado general

| Capa | Estado |
|------|--------|
| Backend (Spring Boot + PostgreSQL) | ✅ Completo |
| Frontend — Auth flow (login + register) | ✅ Completo |
| Frontend — Layout (Sidebar + Topbar) | ✅ Completo |
| Frontend — HomePage | ✅ Completo |
| Frontend — DiscoverPage + filtros avanzados | ✅ Completo |
| Frontend — DetailPage + ¿Dónde ver? + Cast | ✅ Completo |
| Frontend — ProfilePage + favoritas fijadas | ✅ Completo |
| Frontend — NotificationsPage | ✅ Completo |
| Frontend — ListsPage | ✅ Completo |
| Frontend — PosterCard interactivo | ✅ Completo |
| Frontend — Hero CTAs conectados | ✅ Completo |
| Frontend — SettingsPage | ✅ Completo |
| Frontend — ActivityPage/Feed | ✅ Completo |
| Frontend — Sidebar links + avatar en layout | ✅ Completo |
| Frontend — ListDetailPage + comentarios | ✅ Completo |
| Frontend — WatchlistPage / FavoritesPage | ✅ Completo |
| Frontend — ProfilePage deep-link por tab | ✅ Completo |
| Frontend — WebSocket tiempo real | ✅ Completo |
| Frontend — Paginación ReviewList | ✅ Completo |
| Frontend — PersonPage (actores/directores) | ✅ Completo |
| Frontend — Spoiler tags en reseñas | ✅ Completo |
| Frontend — Temporadas y episodios (SeasonAccordion) | ✅ Completo |
| Frontend — Estadísticas avanzadas (StatsTab en perfil) | ✅ Completo |
| Frontend — Subida de avatar (multipart) | ✅ Completo |
| Frontend — Recomendaciones personalizadas | ✅ Completo |
| Frontend — Insignias/gamificación | ✅ Completo |
| Backend — RATING_UPDATED / LIST_UPDATED en ActivityService | ✅ Completo |
| Backend — Usuarios sugeridos en HomePage | ✅ Completo |

---

## Rutas activas (App.tsx)

```
/login                              → LoginPage (pública)
/register                           → RegisterPage (pública)
/                                   → HomePage (pública)
/discover                           → DiscoverPage (pública) — filtros avanzados
/content/:contentType/:tmdbId/:slug → DetailPage (pública) — cast + ¿dónde ver?
/person/:personId/:slug?            → PersonPage (pública) — filmografía
/profile/:username                  → ProfilePage (pública) — favoritas fijadas
/lists                              → ListsPage (PrivateRoute)
/lists/:listId                      → ListDetailPage (pública)
/watchlist                          → SpecialListPage WATCHLIST (PrivateRoute)
/favorites                          → SpecialListPage FAVORITES (PrivateRoute)
/watched                            → SpecialListPage WATCHED (PrivateRoute)
/notifications                      → NotificationsPage (PrivateRoute)
/settings                           → SettingsPage (PrivateRoute)
/activity                           → ActivityPage (pública)
/admin                              → AdminPage (AdminRoute)
```

Sidebar — "Mi espacio" (todas las rutas resueltas):
- `⭐ Valoraciones` → `/profile/:username?tab=ratings` — abre la pestaña de valoraciones directamente
- `📋 Mis listas` → `/lists` — todas las listas
- `🕐 Por ver` → `/watchlist` — página dedicada con la lista WATCHLIST
- `❤️ Favoritos` → `/favorites` — página dedicada con la lista FAVORITES

---

## Archivos clave por feature

### Auth
- `src/features/auth/LoginPage.tsx` — split layout, JWT, Zustand
- `src/features/auth/RegisterPage.tsx` — validación cliente + errores de API por campo
- `src/hooks/useAuth.ts` — `useLogin`, `useRegister`, `useLogout`, `useMyProfile`
- `src/store/authStore.ts` — Zustand persist, `isInitialized` flag

### Layout
- `src/components/layout/Layout.tsx` — Outlet con Sidebar + Topbar
- `src/components/layout/Sidebar.tsx` — navegación + badge notificaciones no leídas
  - Links "Mi espacio" resueltos (ratings → perfil, watchlist/favorites → /lists)
  - Avatar del footer usa `useMyProfile().avatarUrl` — se actualiza al guardar en Settings
- `src/components/layout/Topbar.tsx` — búsqueda + avatar + dropdown usuario
  - Avatar del dropdown usa `useMyProfile().avatarUrl` — reactivo a cambios en Settings

### HomePage
- `src/features/home/HomePage.tsx`
  - Hero con backdrop del contenido más popular
  - Botones hero conectados: "Añadir a lista" (AddToListButton / login), "⭐ Valorar" → DetailPage, "Ver ficha →" → DetailPage
  - Carrusel horizontal "Populares ahora" con PosterCard

### DiscoverPage
- `src/features/discover/DiscoverPage.tsx` — búsqueda debounced + filtros MOVIE/TV/ALL + grid
- `src/hooks/useDiscover.ts` — `usePopular`, `useSearch`

### DetailPage ⭐ (completada en última sesión)
- `src/features/detail/DetailPage.tsx`
  - Lee `location.state.content` para evitar re-fetch cuando viene de PosterCard/hero
  - Fallback a `useSyncContent` si accede directo por URL
  - Guard auth para RatingWidget y AddToListButton (redirect a /login si no autenticado)
  - Aside: géneros, sinopsis completa, estadísticas (valoraciones, nota MovieMate, nota TMDB)
- `src/components/Detail/DetailHero.tsx` — backdrop + poster flotante + scores TMDB/MovieMate
- `src/components/Detail/RatingWidget.tsx` ⭐ MEJORADO
  - Carga valoración existente del usuario con `useMyRatingForContent`
  - Pre-rellena el formulario si ya existe valoración
  - Botón "Editar valoración" con estrellas visibles cuando ya valorado
  - Botón "Eliminar" (rojo) al editar
  - Botón "Actualizar" vs "Guardar valoración" según contexto
  - Guard: si no autenticado → redirige a /login
- `src/components/Detail/AddToListButton.tsx` — dropdown lazy de listas del usuario
- `src/components/Detail/ReviewList.tsx` ⭐ MEJORADO
  - Avatar del usuario (imagen si tiene avatarUrl, inicial si no)
  - Botón de like ❤️/🤍 por reseña con contador
  - Si no autenticado: like deshabilitado con tooltip
- `src/hooks/useDetail.ts` ⭐ MEJORADO
  - `useSyncContent` — fetch por tmdbId cuando no hay state
  - `useReviews(contentId)` — reseñas del contenido
  - `useMyRatingForContent(contentId, enabled)` — NEW: valoración propia filtrada de mis ratings
  - `useCreateRating(content)` — crear/actualizar
  - `useDeleteRating(contentId)` — NEW: eliminar + invalidar caché
  - `useToggleLike(ratingId, contentId)` — NEW: toggle like + invalidar reseñas

### ProfilePage
- `src/features/profile/ProfilePage.tsx`
  - Carga: `useUserByUsername` → userId → `useUserProfile` + `useUserStats` en paralelo
  - Perfil privado: 403 → componente `PrivateProfile`
  - `isOwnProfile`: carga además ratings y listas del usuario
  - Botón Seguir/Siguiendo (hover rojo → unfollow)
  - Dialog edición: bio (200 chars) + avatarUrl
  - 4 pestañas: Actividad · Valoraciones · Listas · Siguiendo (lazy)
- `src/hooks/useProfile.ts` — 8 hooks: byUsername, profile, stats, ratings, lists, following, follow/unfollow, updateProfile

### NotificationsPage
- `src/features/notifications/NotificationsPage.tsx`
  - 4 tipos: FOLLOWER, FOLLOW_REQUEST, FOLLOW_REQUEST_ACCEPTED, REVIEW_LIKE
  - Optimistic update en markAsRead
  - Acciones: Seguir/Siguiendo, Aceptar/Rechazar solicitud

### ListsPage + ListDetailPage + SpecialListPage
- `src/features/lists/ListsPage.tsx`
  - Filtros: Todas/Públicas/Privadas/Películas/Series
  - Mosaico 2×2 de posters por lista
  - Dialog crear lista: nombre + descripción + toggle público/privado
  - `ListCard` envuelta con `<Link to="/lists/:id" state={{ list }}>` — navegación a detalle
- `src/features/lists/ListDetailPage.tsx`
  - Ruta: `/lists/:listId` (pública)
  - `placeholderData` con `location.state.list` — carga instantánea al venir de ListCard
  - Siempre re-fetchea la versión fresca del servidor
  - Header: icono del tipo, nombre, descripción, enlace al autor, nº títulos, visibilidad
  - Grid de `PosterCard`; si es el dueño: botón `×` al hover para eliminar de la lista
  - `listsApi.getById(id)` + `queryKeys.lists.detail(id)`
- `src/features/lists/SpecialListPage.tsx`
  - Ruta: `/watchlist` y `/favorites` (ambas PrivateRoute)
  - Recibe `listType: 'WATCHLIST' | 'FAVORITES'` como prop
  - Reutiliza `queryKeys.users.lists()` (ya cacheado) y filtra por `listType`
  - Mismo grid con botón `×` para eliminar — siempre es el propio usuario el dueño
  - Título, subtítulo y estado vacío específicos por tipo

### Shared components
- `src/components/shared/PosterCard.tsx` — dropdown lazy "+ Lista", cierre fuera, toast
- `src/components/shared/StarRating.tsx`
- `src/components/shared/EmptyState.tsx`

---

## Tipos TypeScript relevantes (`src/types/index.ts`)

```ts
RatingResponse {
  id, rating, reviewText?, emotionalTag, status, watchedDate, createdAt,
  user: UserResponse, content: ContentResponse,
  likesCount?: number,          // añadido — puede venir del backend
  likedByCurrentUser?: boolean  // añadido — puede venir del backend
}
```

---

## Patrones técnicos establecidos

### Botones dentro de `<Link>` (PosterCard)
```tsx
e.preventDefault(); e.stopPropagation()
```

### Cierre dropdown al clicar fuera
```tsx
useEffect(() => {
  if (!open) return
  const handler = (e: MouseEvent) => {
    if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false)
  }
  document.addEventListener('mousedown', handler)
  return () => document.removeEventListener('mousedown', handler)
}, [open])
```

### Queries encadenadas (username → id → datos)
```tsx
const { data: user } = useUserByUsername(username)
const { data: profile } = useUserProfile(user?.id)   // enabled: !!userId
const { data: stats }   = useUserStats(user?.id)      // paralelo
```

### Actualización optimista (NotificationsPage)
```tsx
onMutate: async (id) => {
  await queryClient.cancelQueries({ queryKey })
  const prev = queryClient.getQueryData(queryKey)
  queryClient.setQueryData(queryKey, (old) => old.map(n => n.id === id ? { ...n, read: true } : n))
  return { prev }
},
onError: (_err, _id, ctx) => { if (ctx?.prev) queryClient.setQueryData(queryKey, ctx.prev) },
```

### Perfil privado (403)
```tsx
retry: (_count, error: any) => error?.response?.status !== 403
const isPrivate = (profileQuery.error as any)?.response?.status === 403
```

### Cargar valoración existente (RatingWidget)
```tsx
// useMyRatingForContent filtra del array de todos mis ratings
select: (ratings) => ratings.find((r) => r.content.id === contentId)
// useEffect pre-rellena el form cuando llega la data
useEffect(() => { if (existingRating) { setRating(existingRating.rating); ... } }, [existingRating])
```

---

## Dual cache key — listas

`AddToListButton` y `PosterCard` usan `queryKeys.lists.mine()`.
`ListsPage` y `ProfilePage` usan `queryKeys.users.lists()`.
Ambas apuntan a `/users/me/lists`. Al mutar, **invalidar las dos**:
```ts
queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
queryClient.invalidateQueries({ queryKey: queryKeys.users.lists() })
```

---

### WebSocket — notificaciones tiempo real ⭐ NUEVO
- `src/hooks/useWebSocket.ts`
  - Conecta al broker STOMP SockJS en `/ws` (URL derivada de `VITE_API_BASE_URL`)
  - Pasa JWT en `connectHeaders: { Authorization: Bearer <token> }` del CONNECT frame
  - `reconnectDelay: 5000` — reconexión automática en caso de caída
  - Suscripción a `/user/queue/notifications` (canal privado por usuario)
  - Al recibir notificación: prepende a `queryKeys.users.notifications()` + invalida `queryKeys.notifications.unreadCount()`
  - Solo se activa cuando `isAuthenticated && !!token`
- `src/components/layout/Layout.tsx` — llama `useWebSocket()` (se monta una sola vez con el layout)
- Backend — `WebSocketAuthInterceptor.java` (`@Component`) lee JWT del STOMP CONNECT frame y asigna el principal a la sesión WebSocket
- Backend — `WebSocketConfig.java` registra el interceptor en `configureClientInboundChannel`
- Backend — `SecurityConfig.java` permite `/ws/**` en `permitAll()` (SockJS necesita el handshake HTTP sin auth previa)

### ReviewList con paginación ⭐ NUEVO
- `src/components/Detail/ReviewList.tsx`
  - Muestra `PAGE_SIZE = 5` reseñas inicialmente
  - Estado `visibleCount` crecer de 5 en 5 al pulsar "Ver más"
  - El botón muestra cuántas quedan: "Ver N más (M restantes)"
  - Al resetear el contenido (nueva página), `visibleCount` vuelve a 5 automáticamente

### SettingsPage
- `src/features/settings/SettingsPage.tsx`
  - Sección "Perfil público": avatar URL con preview en tiempo real (maneja error de carga) + bio con contador
  - Sección "Privacidad": toggle público/privado (guarda inmediatamente con toast)
  - Sección "Cuenta": username, email, fecha de registro (readonly con badge)
  - Sección "Zona de peligro": cerrar sesión con confirmación en dos pasos
  - Usa `useMyProfile()` para cargar datos, `useUpdateProfile()` + `usersApi.updatePublicStatus()` para guardar

### ActivityPage
- `src/features/activity/ActivityPage.tsx`
  - Dos tabs: "Para ti" (feed personal, lazy) y "Global"; "Para ti" solo visible si autenticado
  - Paginación "Ver más": re-fetcha con `size` creciente (0..20..40...) sin infinite scroll
  - `ActivityItem`: renderizado diferente por tipo vía switch
  - `RATING_CREATED` / `RATING_UPDATED`: card con poster mini, estrellas, tag emocional, extracto de reseña
  - `LIST_CREATED` / `LIST_UPDATED`: texto con nombre de lista
  - `FOLLOW`: enlaza al perfil del targetUser
  - Avatar con link al perfil; `key` compuesto (ActivityResponse no tiene id)
- Backend `ActivityService.java` detecta edición comparando `updatedAt > createdAt + 1min` — no requiere entidad separada

### SettingsPage — Subida de avatar ⭐ NUEVO
- `src/features/settings/SettingsPage.tsx`
  - Botón de subir archivo + input file hidden (trigger con `useRef`)
  - Preview instantáneo con `URL.createObjectURL(file)` antes de que complete el upload
  - Mutation `uploadAvatar`: POST multipart a `/api/users/me/avatar`; en success actualiza cache y limpia preview
  - Mantiene opción de pegar URL manualmente como fallback
- Backend `service/UserService.java` — `uploadAvatar(User, MultipartFile)`:
  - Valida `contentType.startsWith("image/")` y tamaño ≤5MB
  - Guarda en `uploads/avatars/{userId}_{8charUUID}{ext}`
  - Actualiza `user.avatarUrl` a `/uploads/avatars/{filename}`
- Backend `config/WebMvcConfig.java` — sirve `/uploads/**` desde el sistema de ficheros
- Backend `config/SecurityConfig.java` — `/uploads/**` en `permitAll()` (imágenes públicas sin auth)

### HomePage — Recomendaciones + Usuarios sugeridos ⭐ NUEVO
- `src/features/home/HomePage.tsx`
  - Sección **"Para ti ✨"** (solo autenticados): carrusel con `recommendations` — top género del usuario → discover TMDB rating ≥7 → 6 películas + 6 series
  - Sección **"Cinéfilos que quizás conozcas 👥"** (solo autenticados): hasta 5 `SuggestedUserCard` con avatar, @username y bio
  - `queryKeys.users.recommendations()` / `queryKeys.users.suggestions()` con staleTime 30/10 min
- Backend `GET /api/users/me/recommendations`: extrae top-1 género de `FullStatsDto`, mapea nombre a ID TMDB, llama `discoverMovies` + `discoverTvShows`

### ProfilePage — Insignias ⭐ NUEVO
- `src/features/profile/ProfilePage.tsx`
  - Componente `BadgesSection`: chips `emoji + nombre` con tooltip de descripción, visibles en todos los perfiles (propio y ajeno), entre stats y películas favoritas
  - Query `badgesQuery`: usa `getMyBadges()` si es perfil propio, `getBadgesByUserId(id)` si es ajeno
- Backend `entity/UserBadge.java` — tabla `user_badges` con constraint unique (user_id, badge_type)
- Backend `service/BadgeService.java` — 10 insignias (enum `BadgeType`), evaluación idempotente en `checkAndAward(user, stats)`:
  - FIRST_REVIEW (≥1 rating) · CRITIC (≥10) · CINEPHILE (≥50) · FILM_BUFF (≥100)
  - MOVIE_MARATHON (≥10 películas) · SERIES_BINGE (≥10 series)
  - SOCIAL (≥1 seguidor) · POPULAR (≥10 seguidores)
  - LISTER (≥1 lista) · LIKED (≥1 like recibido)
- Integrado en `UserStatsService.updateUserStats()` → se evalúa en cada recalculación de stats
- `GET /api/users/me/badges` y `GET /api/users/{id}/badges`

### ListDetailPage — Comentarios ⭐ NUEVO
- `src/features/lists/ListDetailPage.tsx`
  - Sección de comentarios al final: textarea + botón Publicar (disabled si vacío o enviando)
  - Lista de comentarios: avatar (imagen o inicial), @username con link al perfil, `timeAgo`, texto del comentario
  - Botón `×` para eliminar propio (solo visible en hover del autor)
  - Skeleton de carga (2 filas animadas)
  - Estado vacío: "Sin comentarios todavía. ¡Sé el primero!"
  - `queryKeys.comments.byList(listId)` con staleTime 2 min
- Backend `entity/ListComment.java` — tabla `list_comments`, FK a `User` + `List`, soft delete (`deleted = false`)
- Backend `service/ListCommentService.java` — `getByList`, `create`, `delete` (verifica autoría)
- Backend `controller/ListCommentController.java` — `GET/POST /api/lists/{id}/comments`, `DELETE .../comments/{cid}`

---

## Estado del proyecto — COMPLETO

**Toda la funcionalidad está implementada (prioridades ALTA, MEDIA y BAJA).**

### Bugs resueltos (todos)
- B1–B12: todos ✅

### Funcionalidades completadas (todas)
- M1–M31: todas ✅

### Pendiente operacional
- **Avatar en producción/Docker**: los archivos se guardan en `uploads/` en el filesystem local. En un entorno productivo habría que montar un volumen Docker o migrar a un object storage (S3, Cloudflare R2). No afecta a la funcionalidad del TFG.
- **Memoria de la TFG**: la documentación escrita (memoria académica) es el siguiente paso natural.

---

## Rama y commits

```
feat/frontend-pages
```

| Commit | Contenido |
|--------|-----------|
| `@feat: añadir RegisterPage, ProfilePage y NotificationsPage al frontend` | Las tres páginas + hooks |
| `@feat: añadir ListsPage y conectar botones interactivos del frontend` | ListsPage, PosterCard dropdown, hero CTAs, rutas activas |
| `@feat: completar DetailPage con edicion de valoracion, likes y guards de auth` | RatingWidget edit/delete, ReviewList likes, useDetail hooks nuevos |
| `@feat: añadir SettingsPage con edicion de perfil, privacidad y cierre de sesion` | SettingsPage completa |
| `@feat: añadir ActivityPage con feed global y personal` | ActivityPage, ruta /activity |
| `@feat: arreglar sidebar links y avatar, añadir paginas de lista` | Sidebar/Topbar avatar reactivo, ListDetailPage, SpecialListPage watchlist/favorites, ProfilePage tab deep-link |
| `@fix: corregir toSlug con titulo undefined, tab deep-link en ProfilePage y mutacion segura en SpecialListPage` | 3 bugs corregidos tras revisión de código |
| `@feat: integrar WebSocket STOMP para notificaciones en tiempo real y paginacion en ReviewList` | useWebSocket hook, Layout integración, backend WebSocketAuthInterceptor, ReviewList "Ver más" |
| `@feat: subida de avatar, recomendaciones, insignias y comentarios en listas` | UserService.uploadAvatar, WebMvcConfig static, BadgeService+UserBadge, ListCommentService+entity, ProfilePage badges, ListDetailPage comments, HomePage Para Ti |
