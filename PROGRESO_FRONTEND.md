# MovieMate — Progreso del Frontend

Resumen de todo lo implementado en el frontend durante el desarrollo del TFG.

---

## Estado general

| Capa | Estado |
|------|--------|
| Backend (Spring Boot + PostgreSQL) | ✅ Completo |
| Frontend — Auth flow | ✅ Completo |
| Frontend — Layout (Sidebar + Topbar) | ✅ Completo |
| Frontend — HomePage | ✅ Completo |
| Frontend — DiscoverPage | ✅ Completo |
| Frontend — DetailPage | ✅ Completo |
| Frontend — RegisterPage | ✅ Completo |
| Frontend — ProfilePage | ✅ Completo |
| Frontend — NotificationsPage | ✅ Completo |
| Frontend — ListsPage | ✅ Completo |
| Frontend — PosterCard interactivo | ✅ Completo |
| Frontend — Hero CTAs conectados | ✅ Completo |

---

## Rutas activas (App.tsx)

```
/login                              → LoginPage (pública)
/register                           → RegisterPage (pública)
/                                   → HomePage (pública)
/discover                           → DiscoverPage (pública)
/content/:contentType/:tmdbId/:slug → DetailPage (pública)
/profile/:username                  → ProfilePage (pública)
/lists                              → ListsPage (requiere login)
/notifications                      → NotificationsPage (requiere login)
```

---

## Archivos creados / modificados

### Nuevas páginas

#### `src/features/auth/RegisterPage.tsx`
- Layout en dos paneles igual que LoginPage (panel izquierdo con brand + 4 pills de características)
- Campos: username, email, password, confirmPassword
- Validación cliente: formato email, mínimo 8 chars contraseña, mínimo 3 chars username, contraseñas coinciden
- Errores de API mapeados al campo correspondiente (conflicto de username/email)
- Al registrarse con éxito llama a `setAuth` y navega a `/`

#### `src/features/profile/ProfilePage.tsx`
- Carga encadenada: `useUserByUsername(username)` → `userId` → `useUserProfile` + `useUserStats` en paralelo
- Perfil privado detectado por `403` → muestra componente `PrivateProfile`
- Perfil propio (`isOwnProfile`): carga también ratings y listas del usuario
- Botón "Seguir" / "Siguiendo" (se vuelve rojo al hover para deseguir)
- Dialog de edición: bio con contador de 200 chars + campo avatarUrl
- 4 pestañas: Actividad · Valoraciones · Listas · Siguiendo
- Pestaña "Siguiendo" carga lazy con `enabled: activeTab === 'following'`

#### `src/features/notifications/NotificationsPage.tsx`
- 4 tipos: `FOLLOWER`, `FOLLOW_REQUEST`, `FOLLOW_REQUEST_ACCEPTED`, `REVIEW_LIKE`
- Filtros: Todas · No leídas (con contador) · Seguimientos · Me gustas
- Actualización optimista al marcar como leído (flip en caché, rollback en error)
- "Marcar todo como leído" actualiza caché + invalida badge del Sidebar
- Acciones por tipo: FOLLOWER → botón "Seguir"/"Siguiendo"; FOLLOW_REQUEST → "Aceptar"/"Rechazar"
- Estado local con `Set<number>` para no repetir acciones en la misma sesión

#### `src/features/lists/ListsPage.tsx`
- Filtros: Todas · Públicas · Privadas · Películas · Series
- Tarjeta con mosaico 2×2 de posters de los primeros 4 contenidos de la lista
- Icono por tipo: ❤️ Favoritos · 🕐 Por ver · 👁️ Ya vistas · 📋 Custom
- Tarjeta "Crear nueva lista" con borde punteado
- Dialog de creación: nombre (req., min 2 chars), descripción (opcional, 300 chars), toggle público/privado

### Hooks

#### `src/hooks/useProfile.ts`
Hooks exportados:
- `useUserByUsername(username)` — obtiene el ID a partir del username
- `useUserProfile(userId)` — perfil completo; retry desactivado en 403
- `useUserStats(userId)` — estadísticas (ratings, listas, seguidores...)
- `useMyProfileRatings(enabled)` — valoraciones propias (lazy)
- `useMyProfileLists(enabled)` — listas propias (lazy)
- `useUserFollowing(userId, enabled)` — lista de seguidos (lazy)
- `useFollowUser(userId)` / `useUnfollowUser(userId)` — invalidan perfil al mutar
- `useUpdateProfile(username, onSuccess?)` — actualiza caché instantáneamente + invalida query

### Componentes modificados

#### `src/components/shared/PosterCard.tsx`
- Botón "+ Lista" visible en el overlay de hover (solo si `isAuthenticated`)
- Lista cargada lazy (`enabled: listOpen && isAuthenticated`)
- Mutación `addToList` invalida **dos** claves de caché: `queryKeys.lists.mine()` y `queryKeys.users.lists()`
- Toast: `"Título" añadido a NombreDeLista`
- Cierre del dropdown al clicar fuera con `document.addEventListener('mousedown')` + `ref.contains()`
- Dropdown posicionado `absolute top-full` fuera del `overflow-hidden` del poster, `z-30`
- Items del dropdown: icono de tipo, nombre truncado, contador de items

#### `src/features/home/HomePage.tsx`
- Botón **"Añadir a lista"**: usa `<AddToListButton>` si autenticado; si no, redirige a `/login`
- Botón **"⭐ Valorar"**: navega a `featuredUrl` con `state: { content: featured }`
- Botón **"Ver ficha →"**: navega a `featuredUrl` con `state: { content: featured }`
- Ambos botones de navegación tienen `disabled` mientras los datos cargan

#### `src/App.tsx`
- Todas las rutas activas (antes algunas estaban comentadas)
- `/lists` y `/notifications` envueltas en `<PrivateRoute>`

---

## Patrones técnicos usados

### Botones dentro de `<Link>`
```tsx
const handleClick = (e: React.MouseEvent) => {
  e.preventDefault()
  e.stopPropagation()
  // lógica...
}
```

### Cierre al clicar fuera (sin stopPropagation en React)
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
const { data: user } = useUserByUsername(username)         // paso 1
const userId = user?.id
const { data: profile } = useUserProfile(userId)           // paso 2, enabled: !!userId
const { data: stats }   = useUserStats(userId)             // paso 2 paralelo
```

### Actualización optimista
```tsx
onMutate: async (id) => {
  await queryClient.cancelQueries({ queryKey })
  const prev = queryClient.getQueryData(queryKey)
  queryClient.setQueryData(queryKey, (old) => old.map(n => n.id === id ? { ...n, read: true } : n))
  return { prev }
},
onError: (_err, _id, ctx) => {
  if (ctx?.prev) queryClient.setQueryData(queryKey, ctx.prev)
},
```

### Perfil privado (403)
```tsx
retry: (_count, error: any) => error?.response?.status !== 403
// ...
const isPrivate = (profileQuery.error as any)?.response?.status === 403
```

---

## Pendiente

- **DetailPage**: ya existe pero revisar integración completa con el nuevo estado de las otras páginas
- **Sidebar links**: `/ratings`, `/watchlist`, `/favorites` apuntan a rutas no implementadas (redirigen a `/`)
- **WebSocket / tiempo real**: infraestructura lista en backend (`/ws` STOMP), no conectado en frontend
- **SettingsPage**: no implementada
- **ActivityPage**: no implementada

---

## Rama de trabajo

```
feat/frontend-pages
```

Commits principales:
- `@feat: añadir RegisterPage, ProfilePage y NotificationsPage al frontend`
- `@feat: añadir ListsPage y conectar botones interactivos del frontend`
