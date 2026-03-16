# MovieMate — Funcionalidades pendientes

Registrado 2026-03-13. Estado: ⬜ pendiente | 🔄 en progreso | ✅ hecho

---

## Índice

1. [Comentarios en valoraciones](#1-comentarios-en-valoraciones)
2. [Rol Admin y moderación](#2-rol-admin-y-moderación)
3. [Perfiles de actor y director](#3-perfiles-de-actor-y-director)
4. [Estadísticas personales avanzadas](#4-estadísticas-personales-avanzadas)
5. [Filtros avanzados en Discover](#5-filtros-avanzados-en-discover)
6. ["¿Dónde ver?" — disponibilidad en plataformas](#6-dónde-ver--disponibilidad-en-plataformas)
7. [Gamificación — insignias y logros](#7-gamificación--insignias-y-logros)
8. [Seguimiento por temporada y episodio](#8-seguimiento-por-temporada-y-episodio)
9. [Recomendaciones personalizadas](#9-recomendaciones-personalizadas)
10. [Búsqueda de usuarios](#10-búsqueda-de-usuarios)
11. [Perfil público: valoraciones y listas ajenas](#11-perfil-público-valoraciones-y-listas-ajenas)
12. [Lista "Vistos" (WATCHED)](#12-lista-vistos-watched)
13. [Cambio de contraseña](#13-cambio-de-contraseña)
14. [Subida real de avatar](#14-subida-real-de-avatar)
15. [Importar / exportar datos](#15-importar--exportar-datos)
16. [Películas favoritas fijadas en el perfil](#16-películas-favoritas-fijadas-en-el-perfil)
17. [Actividad: tipos RATING_UPDATED y LIST_UPDATED](#17-actividad-tipos-rating_updated-y-list_updated)
18. [Votar utilidad de reseñas](#18-votar-utilidad-de-reseñas)
19. [Etiquetas de spoiler en reseñas](#19-etiquetas-de-spoiler-en-reseñas)
20. [Comentarios en listas](#20-comentarios-en-listas)
21. [Usuarios sugeridos](#21-usuarios-sugeridos)
22. [Menciones @usuario en comentarios](#22-menciones-usuario-en-comentarios)
23. [Notificaciones push web (PWA)](#23-notificaciones-push-web-pwa)
24. [Modo oscuro / claro alternativo](#24-modo-oscuro--claro-alternativo)
25. [Explorar listas: propietario y buscador](#25-explorar-listas-propietario-y-buscador)
26. [Carruseles en HomePage con flechas de navegación](#26-carruseles-en-homepage-con-flechas-de-navegación)
27. [DetailPage: estadísticas de MovieMate siempre actualizadas](#27-detailpage-estadísticas-de-moviemate-siempre-actualizadas)
28. [DevOps — Contenerización, CI/CD y despliegue en Kubernetes](#28-devops--contenerización-cicd-completo-y-despliegue-en-kubernetes)

---

## 1. Comentarios en valoraciones ✅

**Prioridad: ALTA** | Referencia: Letterboxd, Serializd

Los usuarios pueden comentar las valoraciones de otros. Esto incluye hilo de comentarios, notificaciones al autor de la valoración y moderación por admins.

### Backend
- [x] Entidad `Comment` (id, content, author, rating, createdAt, updatedAt, deleted)
- [x] `CommentRepository` + `CommentService`
- [x] `CommentController` — `/api/ratings/:ratingId/comments`
  - `GET /` — listar comentarios
  - `POST /` — crear comentario (auth requerida)
  - `DELETE /:commentId` — borrar propio (o admin)
  - [ ] `PUT /:commentId` — editar propio (pendiente)
- [x] Notificación al autor de la valoración cuando recibe un comentario (`COMMENT_ON_RATING`)
- [ ] Actividad: `COMMENT_ADDED_TO_RATING` en el feed global/personal (pendiente)
- [ ] `ActivityType.COMMENT_ADDED_TO_RATING` — conectar al backend real (pendiente)

### Frontend
- [x] Sección de comentarios en `DetailPage` bajo cada `ReviewCard` (`CommentSection`)
- [x] Componente `CommentForm` (textarea + submit, con límite de 1000 chars)
- [x] Botón "💬 Comentarios" en `ReviewCard` para expandir/colapsar el hilo
- [x] Hooks: `useComments`, `useCreateComment`, `useDeleteComment`
- [x] Módulo API: `api/comments.ts`
- [ ] Editar comentario propio (pendiente)

---

## 2. Rol Admin y moderación ✅

**Prioridad: ALTA** | Necesario para moderar comentarios y reseñas

### Backend
- [x] Enum `Role` con `USER` y `ADMIN` en entidad `User`
- [x] Campo `role` en `User` (default `USER`) + campo `banned` (default `false`)
- [x] `@PreAuthorize("hasRole('ADMIN')")` en endpoints de moderación
- [x] Endpoints de moderación:
  - `DELETE /api/admin/comments/:id` — borrar cualquier comentario
  - `DELETE /api/admin/ratings/:id` — borrar cualquier valoración
  - `GET /api/admin/ratings/:id` — ver datos de una valoración concreta (para previsualizar en reporte)
  - `GET /api/admin/comments/:id` — ver datos de un comentario concreto (para previsualizar en reporte)
  - `GET /api/admin/users` — listar todos los usuarios con paginación y búsqueda
  - `PUT /api/admin/users/:id/role` — promover/degradar rol de un usuario
  - `PUT /api/admin/users/:id/ban` — bloquear cuenta
  - `GET /api/admin/reports` — listado de reportes de contenido
  - `PUT /api/admin/reports/:id/resolve` — resuelve el reporte, elimina el contenido y notifica al autor
  - `PUT /api/admin/reports/:id/dismiss` — desestima el reporte sin borrar contenido
- [x] Entidad `ContentReport` (id, reporter, targetType, targetId, reason, status, createdAt)
- [x] `POST /api/reports` — cualquier usuario puede reportar una valoración o comentario
- [x] `DataSeeder`: crear usuario admin inicial (`admin` / `Admin1234!`)
- [x] Notificación `CONTENT_REMOVED` al usuario cuando un admin elimina su contenido (incluye motivo de la denuncia)
- [x] Campo `message` (nullable) en entidad `Notification` para notificaciones de sistema sin remitente

### Frontend
- [x] Ruta `/admin` protegida por `AdminRoute` (requiere `role === ADMIN`)
- [x] Panel de administración (`AdminPage`):
  - [x] Sección "Usuarios" — tabla con búsqueda, opciones cambiar rol / banear
  - [x] Sección "Reportes" — lista de reportes con filtro por estado (PENDING / RESOLVED / DISMISSED)
  - [x] Click en fila de reporte despliega preview del contenido denunciado (estrellas, texto, autor)
  - [x] Resolver reporte elimina el contenido y envía notificación al autor con el motivo
  - [ ] Sección "Valoraciones" — borrar valoración directamente sin reporte previo (pendiente)
  - [ ] Sección "Comentarios" — borrar comentario directamente sin reporte previo (pendiente)
- [x] Botón "Reportar" (🚩) en `ReviewCard` para usuarios normales
- [x] `ReportDialog` — modal con selector de motivo (spam / inapropiado / spoiler / otro)
- [x] `NotificationsPage` maneja `CONTENT_REMOVED` — muestra el mensaje personalizado del backend sin enlace a usuario
- [ ] Indicador visual de cuenta baneada en UI (pendiente)
- [ ] Badge "Admin" en perfiles de admin (pendiente)

---

## 3. Perfiles de actor y director

**Prioridad: MEDIA** | Referencia: IMDB, Letterboxd

Páginas dedicadas a personas (actores, directores, etc.) con su filmografía completa.

### Backend
- [ ] Entidad `Person` (tmdbId, name, profilePath, biography, birthday, deathday, placeOfBirth, knownForDepartment)
- [ ] Entidad `ContentPerson` (content, person, role: ACTOR/DIRECTOR/WRITER/etc., character, order)
- [ ] `PersonController` — `/api/people`
  - `GET /:tmdbPersonId` — obtener persona (sync lazy desde TMDB si no existe)
  - `GET /:tmdbPersonId/credits` — obras en las que aparece (desde `ContentPerson`)
- [ ] `TmdbService`: `syncPerson(tmdbId)` — llamar a `/person/:id` y `/person/:id/combined_credits`
- [ ] Al sincronizar un `Content`, persistir también el cast y crew principales (top 10 actores + director)

### Frontend
- [ ] Ruta `/person/:tmdbId/:slug`
- [ ] `PersonPage` — foto, nombre, bio, fechas, filmografía ordenada por popularidad
- [ ] Links a PersonPage desde el cast/crew en `DetailPage` (el backend ya devuelve cast)
- [ ] Carrusel de créditos en `PersonPage` usando `PosterCard`

---

## 4. Estadísticas personales avanzadas

**Prioridad: MEDIA** | Referencia: Letterboxd (Pro), Serializd

Página dedicada de estadísticas del usuario autenticado.

### Backend
- [ ] Ampliar `UserStatsService` / nuevo `StatsService`:
  - Distribución de ratings (cuántos 1★, 2★, … 10★)
  - Géneros más vistos (top 5)
  - Directores más vistos (top 5)
  - Actores más vistos (top 5)
  - Películas/series vistas por año de estreno (histograma por década)
  - Actividad por mes (cuántos ítems registrados cada mes)
  - Total horas vistas (ya calculado en `UserStats.totalWatchTime`)
  - Racha actual (días consecutivos con alguna actividad)
- [ ] Endpoint `GET /api/users/me/stats/full` (o ampliar el existente)

### Frontend
- [ ] Ruta `/profile/:username/stats` o tab "Stats" en `ProfilePage`
- [ ] `StatsPage` / `StatsTab`:
  - Gráfico de distribución de notas (bar chart)
  - Gráfico géneros (donut / bar)
  - Heatmap de actividad mensual
  - Top directores / actores (lista con posters)
  - Resumen numérico: total vistas, horas, listas, seguidores, likes recibidos

---

## 5. Filtros avanzados en Discover

**Prioridad: MEDIA** | Referencia: IMDB, Letterboxd

### Backend
- [ ] Ampliar `TmdbController`:
  - `GET /tmdb/discover/movies?genre=&year=&minRating=&sortBy=` — proxy a TMDB Discover API
  - `GET /tmdb/discover/tv?genre=&year=&network=&sortBy=`
  - `GET /tmdb/genres/movies` — lista de géneros de películas
  - `GET /tmdb/genres/tv` — lista de géneros de series

### Frontend
- [ ] Panel de filtros en `DiscoverPage` (colapsable en móvil):
  - Tipo: Película / Serie / Todo
  - Género (multi-select con chips)
  - Año o rango de años (slider o select)
  - Puntuación mínima (slider 0-10)
  - Ordenar por: popularidad / nota / fecha estreno / título
- [ ] URL params sync para todos los filtros (`?genre=28&year=2023&sort=vote_average`)
- [ ] Indicador visual de filtros activos (badge con número)

---

## 6. "¿Dónde ver?" — disponibilidad en plataformas

**Prioridad: MEDIA** | Referencia: IMDB, JustWatch

TMDB tiene el endpoint `/movie/:id/watch/providers` que devuelve proveedores por país.

### Backend
- [ ] Campo `watchProviders` (JSON) en entidad `Content` (nullable)
- [ ] Al sincronizar un `Content`, llamar también a `/movie/:id/watch/providers` o `/tv/:id/watch/providers` y persistir resultado
- [ ] Endpoint `GET /api/content/:id/providers` — devuelve proveedores del contenido

### Frontend
- [ ] Sección "¿Dónde ver?" en `DetailPage` (logos de plataformas: Netflix, Prime, Disney+, etc.)
- [ ] Filtro por plataforma en `DiscoverPage` (requiere feat. 5)

---

## 7. Gamificación — insignias y logros

**Prioridad: BAJA-MEDIA** | Diferenciador del TFG

### Backend
- [ ] Entidad `Badge` (id, key, name, description, iconUrl, condition)
- [ ] Entidad `UserBadge` (user, badge, earnedAt)
- [ ] `BadgeService` — evaluar condiciones al crear rating, seguir usuario, etc.:
  - "Primera valoración" (1 rating)
  - "Crítico novato" (10 ratings)
  - "Crítico experto" (50 ratings)
  - "Cinéfilo" (100 ratings)
  - "Maestro del cine" (500 ratings)
  - "Social" (primer seguidor)
  - "Influencer" (50 seguidores)
  - "Listero" (5 listas creadas)
  - "Completista" (marcar toda una saga como vista)
- [ ] Endpoint `GET /api/users/:id/badges`
- [ ] Notificación al ganar un badge

### Frontend
- [ ] Sección "Insignias" en `ProfilePage`
- [ ] Toast especial al desbloquear una insignia

---

## 8. Seguimiento por temporada y episodio

**Prioridad: MEDIA** | Referencia: Serializd (feature core)

### Backend
- [ ] Entidades `Season` y `Episode` (sincronizadas desde TMDB al hacer sync de una serie)
- [ ] Entidad `EpisodeWatch` (user, episode, watchedAt, rating opcional)
- [ ] `EpisodeController` — `/api/tv/:tmdbId/seasons/:seasonNumber/episodes`
  - `POST /:episodeNumber/watch` — marcar episodio como visto
  - `DELETE /:episodeNumber/watch` — desmarcar
  - `POST /season/:seasonNumber/watch` — marcar temporada completa
- [ ] Calcular progreso de serie en `UserStats` (episodios vistos / total)

### Frontend
- [ ] Sección "Temporadas y episodios" en `DetailPage` (solo para series)
- [ ] `SeasonAccordion` — lista de temporadas desplegable con episodios
- [ ] Checkbox por episodio (marcado/desmarcado) + botón "Marcar temporada"
- [ ] Barra de progreso de la serie en `DetailPage`
- [ ] Estado "En progreso" visible en `ProfilePage` (series parcialmente vistas)

---

## 9. Recomendaciones personalizadas

**Prioridad: BAJA** | Referencia: IMDB, Letterboxd

### Backend
- [ ] Algoritmo simple basado en géneros más valorados positivamente por el usuario
- [ ] Endpoint `GET /api/users/me/recommendations` — devuelve lista de `Content` recomendados (proxy a TMDB Discover con géneros preferidos)

### Frontend
- [ ] Carrusel "Para ti" en `HomePage` (solo si autenticado)
- [ ] Sección "Puede que te guste" en `DetailPage` (basado en géneros del contenido actual)

---

## 10. Búsqueda de usuarios ✅

**Prioridad: MEDIA** | El backend y la API ya existen, solo falta UI

### Frontend
- [x] Modo "Usuarios" en `DiscoverPage` — selector 🎬 Contenido / 👥 Usuarios
- [x] `UserCard` — card con avatar, username, bio, enlace al perfil
- [x] Mostrar usuarios sugeridos cuando no hay búsqueda activa

---

## 11. Perfil público: valoraciones y listas ajenas ✅

**Prioridad: ALTA** | Bug actual: las tabs de Ratings y Lists en perfiles ajenos no cargan

### Backend
- [x] Nuevo endpoint `GET /api/users/:userId/ratings` — ratings públicos de ese usuario (`@RequirePublicProfile`)
- [x] Nuevo endpoint `GET /api/users/:userId/lists` — listas públicas de ese usuario (`@RequirePublicProfile`)

### Frontend
- [x] En `ProfilePage`, cuando `isOwnProfile === false`, usar los nuevos endpoints en lugar de los `/me/...`
- [x] Ocultar botones de edición/borrado cuando es perfil ajeno

---

## 12. Lista "Vistos" (WATCHED) ✅

**Prioridad: BAJA** | El enum `LIST_TYPE_CONFIG.WATCHED` ya existe en frontend

- [x] Ruta `/watched` en `App.tsx`
- [x] Link "👁️ Ya vistas" en `Sidebar`

---

## 13. Cambio de contraseña ✅

**Prioridad: MEDIA** | Funcionalidad básica de cualquier plataforma

### Backend
- [x] Endpoint `PUT /api/users/me/password` — valida BCrypt, re-hashea y guarda
- [x] DTO `ChangePasswordRequest` (currentPassword, newPassword con @Size min=8)
- [x] `UserService.changePassword()` — lanza `IllegalArgumentException` si la contraseña actual no coincide (capturada por GlobalExceptionHandler → 400)

### Frontend
- [x] Sección "Seguridad" en `SettingsPage` con formulario de cambio (contraseña actual, nueva, confirmar)
- [x] Validación en cliente: coincidencia y longitud mínima antes de llamar al backend
- [x] `usersApi.changePassword()` en `api/users.ts`

---

## 14. Subida real de avatar

**Prioridad: BAJA** | Actualmente solo se acepta URL

### Backend
- [ ] Endpoint `POST /api/users/me/avatar` — `multipart/form-data`, guarda en disco o S3/Cloudinary
- [ ] Devuelve URL pública del avatar subido

### Frontend
- [ ] Input `type="file"` en `SettingsPage` (y/o `EditProfileDialog`) con preview antes de subir

---

## 15. Importar / exportar datos

**Prioridad: BAJA** | Referencia: Letterboxd

### Backend
- [ ] `GET /api/users/me/export` — devuelve CSV con todo el historial de ratings
- [ ] `POST /api/users/me/import` — acepta CSV de Letterboxd o formato propio, importa ratings masivamente

### Frontend
- [ ] Sección "Datos" en `SettingsPage` con botones Exportar / Importar

---

## 16. Películas favoritas fijadas en el perfil

**Prioridad: BAJA** | Referencia: Letterboxd (4 favoritas en portada)

La lista `FAVORITES` ya existe; solo falta la sección visual destacada en el perfil.

### Frontend
- [ ] En `ProfilePage`, mostrar los primeros 4 ítems de FAVORITES con posters grandes y destacados, por encima de las tabs
- [ ] Estilo similar al de Letterboxd (4 posters en fila, prominentes)

---

## 17. Actividad: tipos RATING_UPDATED y LIST_UPDATED

**Prioridad: BAJA** | Bug interno — los tipos existen en el frontend pero el backend no los emite

### Backend
- [ ] En `RatingService.updateRating()`: emitir `ActivityType.RATING_UPDATED` si la valoración ya existía
- [ ] En `ListService.updateList()`: emitir `ActivityType.LIST_UPDATED`

---

## 18. Votar utilidad de reseñas

**Prioridad: BAJA** | Referencia: IMDB ("Was this review helpful?")

El like en valoraciones ya existe. Esto añadiría un voto explícito de utilidad.

- [ ] Campo `helpfulVotes` / `notHelpfulVotes` en `Rating`
- [ ] Endpoint `POST /api/ratings/:id/helpful` y `POST /api/ratings/:id/not-helpful`
- [ ] UI en `ReviewCard`: "¿Útil? 👍 N / 👎 N"

---

## 19. Etiquetas de spoiler en reseñas

**Prioridad: BAJA** | Referencia: Letterboxd, Serializd

### Backend
- [ ] Campo `containsSpoiler` (boolean) en `Rating`

### Frontend
- [ ] Checkbox "Contiene spoilers" en `RatingWidget`
- [ ] En `ReviewCard`, si `containsSpoiler === true`: ocultar el texto de la reseña con un overlay "Contiene spoilers — haz clic para leer"

---

## 20. Comentarios en listas

**Prioridad: BAJA** | Referencia: Letterboxd

Igual que comentarios en valoraciones pero para listas. Usar la misma entidad `Comment` con campo `targetType` (RATING / LIST).

- [ ] Ampliar `Comment` con `targetType` enum y `listId` nullable
- [ ] `GET /api/lists/:listId/comments`, `POST /`, `DELETE /:commentId`
- [ ] Sección de comentarios al final de `ListDetailPage`

---

## 21. Usuarios sugeridos

**Prioridad: BAJA** | El backend ya tiene `GET /api/users/suggestions`

### Frontend
- [ ] Widget "Usuarios que quizás conozcas" en `HomePage` (sidebar derecho o sección)
- [ ] Widget o sección en `DiscoverPage` cuando no hay búsqueda activa

---

## 22. Menciones @usuario en comentarios

**Prioridad: MUY BAJA** | Referencia: Letterboxd

- [ ] Detectar `@username` en texto de comentario al crear/editar
- [ ] Resolver menciones a usuarios reales, generar notificación `MENTION`
- [ ] Renderizar menciones como links al perfil del usuario

---

## 23. Notificaciones push web (PWA)

**Prioridad: MUY BAJA** | Diferenciador

- [ ] Manifest + Service Worker para PWA
- [ ] Suscripción push (Web Push API)
- [ ] Backend: enviar push al recibir notificación importante

---

## 24. Modo oscuro / claro alternativo

**Prioridad: MUY BAJA**

- [ ] Token CSS para tema claro
- [ ] Toggle en `SettingsPage`
- [ ] Persistir preferencia en `localStorage`

---

## 25. Explorar listas: propietario y buscador ✅

**Prioridad: MEDIA** | UX básica de la sección de exploración

### Frontend
- [x] Mostrar `@username` del propietario en cada `ListCard` del tab "Explorar" (`showOwner` prop)
- [x] Buscador por nombre de lista o por usuario en el tab "Explorar" — filtrado local sobre los resultados cargados
- [x] Botón × para limpiar la búsqueda, mensaje "Sin resultados" cuando no hay coincidencias

---

## 26. Carruseles en HomePage con flechas de navegación ✅

**Prioridad: MEDIA** | UX básica — el contenido era inaccesible sin scroll táctil o rueda del ratón

### Frontend
- [x] Componente `ContentCarousel` con `useRef` para el contenedor de scroll
- [x] Flecha izquierda y derecha siempre visibles, posicionadas absolutas a los lados del carrusel
- [x] Flecha deshabilitada (opacidad 25%) cuando no hay más contenido en esa dirección, detectado via `onScroll`
- [x] Aplicado a los tres carruseles: Tendencias, Películas populares, Series populares

---

## 27. DetailPage: estadísticas de MovieMate siempre actualizadas ✅

**Prioridad: ALTA** | Bug — `appRating` y `appVoteCount` mostraban 0 aunque existieran valoraciones

**Causa raíz**: al navegar desde un `PosterCard` o el hero, el contenido viajaba en `location.state` con datos cacheados del carrusel (potencialmente con `appVoteCount=0`). `useSyncContent` quedaba desactivado cuando existía `stateContent`, así que nunca se obtenían datos frescos del backend.

### Frontend
- [x] `useSyncContent` siempre habilitado (`enabled: true`) en `DetailPage`
- [x] El `stateContent` se usa como fallback mientras carga la query (evita flash de esqueleto)
- [x] `content = syncedContent ?? stateContent` — en cuanto llega la respuesta fresca, se muestran stats actualizadas

---

## Resumen de prioridades

| Prioridad | Items |
|-----------|-------|
| **ALTA** | ~~1 (Comentarios en valoraciones)~~ ✅, ~~2 (Admin/moderación)~~ ✅, ~~11 (Perfil público)~~ ✅, ~~27 (Stats DetailPage)~~ ✅, ~~28 (DevOps)~~ ✅ |
| **MEDIA** | 3 (Actores/directores), 4 (Stats avanzadas), 5 (Filtros Discover), 6 (¿Dónde ver?), 8 (Temporadas/episodios), ~~10 (Búsqueda usuarios)~~ ✅, ~~13 (Cambio contraseña)~~ ✅, ~~25 (Explorar listas)~~ ✅, ~~26 (Carruseles flechas)~~ ✅ |
| **BAJA** | 7 (Insignias), 9 (Recomendaciones), ~~12 (Lista Vistos)~~ ✅, 14 (Avatar upload), 15 (Import/export), 16 (Favoritas en perfil), 17 (Activity updates), 18 (Votar reseñas), 19 (Spoilers), 20 (Comentarios listas), 21 (Usuarios sugeridos) |
| **MUY BAJA** | 22 (Menciones), 23 (Push PWA), 24 (Tema claro) |

---

## 28. DevOps — Contenerización, CI/CD completo y despliegue en Kubernetes ✅

**Prioridad: ALTA (para prácticas DevOps)**

### Completado

- [x] `Dockerfile` backend (multi-stage Maven → eclipse-temurin JRE)
- [x] `Dockerfile` frontend (multi-stage node:22-alpine → nginx:1.27-alpine)
- [x] `nginx.conf.template` con `envsubst` para `BACKEND_HOST` en runtime (proxy `/api/` y `/ws`)
- [x] `docker-compose.yml` completo (postgres + backend + frontend)
- [x] GitHub Actions `ci.yml` — tests + build en push a `feat/*` y PRs a `main`
- [x] GitHub Actions `cd.yml` — build multi-arch (amd64+arm64) + push a GHCR en merge a `main`
- [x] Helm chart completo en `k8s/moviemate/` (backend + frontend + postgres + ingress + secrets)
- [x] Despliegue verificado en Minikube con namespace `moviemate`
- [x] Acceso desde WSL2+Windows via port-forward
- [x] CORS configurable via env var `CORS_ALLOWED_ORIGINS`
- [x] Self-hosted runner registrado en WSL2 — job `deploy` en `cd.yml` activo
- [x] imagePullSecrets en Helm chart para autenticación con GHCR privado
- [x] Pipeline completo verificado: push a `main` → tests → build+push GHCR → deploy Minikube automático
- [x] Fix: imagen tag lowercase (`givencloud` en lugar de `GivenCloud`) en `cd.yml`

### Decisión: sin despliegue en cloud real

Todos los proveedores gratuitos evaluados (Oracle Cloud, DigitalOcean vía GitHub Student Pack, Azure for Students) requieren tarjeta de crédito o email universitario activo. Se decide que el despliegue en Minikube con pipeline automático es suficiente para el TFG.

Ver `DEVOPS.md` sección 6.5 para la documentación completa del self-hosted runner.
