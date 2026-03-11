# MovieMate — Progreso del Frontend

Resumen de todo lo implementado en el frontend durante el desarrollo del TFG.
Actualizado: 2026-03-11

---

## Estado general

| Capa | Estado |
|------|--------|
| Backend (Spring Boot + PostgreSQL) | ✅ Completo |
| Frontend — Auth flow (login + register) | ✅ Completo |
| Frontend — Layout (Sidebar + Topbar) | ✅ Completo |
| Frontend — HomePage | ✅ Completo |
| Frontend — DiscoverPage | ✅ Completo |
| Frontend — DetailPage | ✅ Completo |
| Frontend — ProfilePage | ✅ Completo |
| Frontend — NotificationsPage | ✅ Completo |
| Frontend — ListsPage | ✅ Completo |
| Frontend — PosterCard interactivo | ✅ Completo |
| Frontend — Hero CTAs conectados | ✅ Completo |
| Frontend — SettingsPage | ✅ Completo |
| Frontend — ActivityPage/Feed | ✅ Completo |
| Frontend — WebSocket tiempo real | ❌ Pendiente |

---

## Rutas activas (App.tsx)

```
/login                              → LoginPage (pública)
/register                           → RegisterPage (pública)
/                                   → HomePage (pública)
/discover                           → DiscoverPage (pública)
/content/:contentType/:tmdbId/:slug → DetailPage (pública)
/profile/:username                  → ProfilePage (pública)
/lists                              → ListsPage (PrivateRoute)
/notifications                      → NotificationsPage (PrivateRoute)
```

Rutas en Sidebar que redirigen a `/` (sin página propia aún):
- `/ratings` — mis valoraciones (podría ir en ProfilePage tab)
- `/watchlist` — lista de "por ver" (podría ir en ListsPage filtrada)
- `/favorites` — lista de favoritos (podría ir en ListsPage filtrada)

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
- `src/components/layout/Topbar.tsx` — búsqueda + avatar + dropdown usuario

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

### ListsPage
- `src/features/lists/ListsPage.tsx`
  - Filtros: Todas/Públicas/Privadas/Películas/Series
  - Mosaico 2×2 de posters por lista
  - Dialog crear lista: nombre + descripción + toggle público/privado

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
  - `RATING_*`: card con poster mini, estrellas, tag emocional, extracto de reseña, enlaza a DetailPage
  - `LIST_*`: texto con nombre de lista
  - `FOLLOW`: enlaza al perfil del targetUser
  - Avatar con link al perfil; `key` compuesto (ActivityResponse no tiene id)

---

## Pendiente para próximas sesiones

1. **WebSocket tiempo real** — backend listo en `/ws` STOMP SockJS; notificaciones push sin polling
2. **Sidebar links** — `/ratings`, `/watchlist`, `/favorites` redirigen a `/`; se podría redirigir a ProfilePage o ListsPage con filtro
3. **Paginación en ReviewList** — actualmente sin paginar

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
