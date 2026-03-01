# MovieMate — Guía de Frontend Definitiva

Stack: **React 18 + TypeScript + Vite + Tailwind CSS + shadcn/ui + React Query + Zustand + Axios**

---

## Índice

1. [Creación del proyecto](#1-creación-del-proyecto)
2. [Instalación de dependencias](#2-instalación-de-dependencias)
3. [Variables de entorno](#3-variables-de-entorno)
4. [Configuración de Tailwind](#4-configuración-de-tailwind)
5. [Inicialización de shadcn/ui](#5-inicialización-de-shadcnui)
6. [Estructura de carpetas](#6-estructura-de-carpetas)
7. [Tipos TypeScript (DTOs)](#7-tipos-typescript-dtos)
8. [Cliente HTTP (Axios)](#8-cliente-http-axios)
9. [Funciones de API](#9-funciones-de-api)
10. [Estado global (Zustand)](#10-estado-global-zustand)
11. [Configuración raíz — main.tsx](#11-configuración-raíz--maintsx)
12. [Rutas — App.tsx](#12-rutas--apptsx)
13. [Layout — Sidebar y Topbar](#13-layout--sidebar-y-topbar)
14. [Componentes base reutilizables](#14-componentes-base-reutilizables)
15. [Páginas](#15-páginas)
16. [CORS en Spring Boot](#16-cors-en-spring-boot)
17. [Arrancar y verificar](#17-arrancar-y-verificar)
18. [Orden de implementación recomendado](#18-orden-de-implementación-recomendado)

---

## 1. Creación del proyecto

```bash
npm create vite@latest moviemate-frontend -- --template react-ts
cd moviemate-frontend
npm install
code .
```

Verifica que funciona antes de continuar:

```bash
npm run dev
# Abre http://localhost:5173 — deberías ver la página de bienvenida de Vite
```

---

## 2. Instalación de dependencias

> **Nota sobre Tailwind v4:** La versión 4 (instalada por defecto desde finales de 2024) eliminó el comando `npx tailwindcss init` y cambió completamente la integración. Ya no se usa `tailwind.config.js` ni `postcss`. En su lugar se integra directamente como plugin de Vite. Los pasos de abajo ya reflejan esto.

```bash
# Dependencias de producción
npm install \
  react-router-dom \
  @tanstack/react-query \
  @tanstack/react-query-devtools \
  axios \
  zustand \
  clsx \
  tailwind-merge

# Dependencias de desarrollo — Tailwind v4 + plugin de Vite (sin postcss ni autoprefixer)
npm install -D \
  tailwindcss \
  @tailwindcss/vite \
  @types/node
```

**Por qué cada una:**
- `react-router-dom` — navegación entre páginas
- `@tanstack/react-query` — gestión de datos del servidor (caché, loading, error)
- `@tanstack/react-query-devtools` — herramienta visual para depurar queries en desarrollo
- `axios` — cliente HTTP con interceptores para JWT
- `zustand` — estado global ligero (sesión del usuario)
- `clsx` + `tailwind-merge` — combinar clases CSS de forma segura (necesario para shadcn)
- `@tailwindcss/vite` — integración directa de Tailwind v4 con Vite (reemplaza a postcss)

---

## 3. Variables de entorno

Crea **tres archivos** en la raíz del proyecto (al mismo nivel que `package.json`):

**`.env.development`** — se usa automáticamente con `npm run dev`
```env
VITE_API_BASE_URL=http://localhost:8080/api
VITE_TMDB_IMAGE_BASE=https://image.tmdb.org/t/p
```

**`.env.production`** — se usa automáticamente con `npm run build`
```env
VITE_API_BASE_URL=https://tu-dominio.com/api
VITE_TMDB_IMAGE_BASE=https://image.tmdb.org/t/p
```

**`.env.example`** — sin valores reales, para que otros sepan qué variables existen
```env
VITE_API_BASE_URL=
VITE_TMDB_IMAGE_BASE=
```

Añade `.env.development` y `.env.production` al `.gitignore` (el `.env.example` sí se sube a Git).

> **Importante:** En Vite, las variables de entorno deben empezar por `VITE_` para ser accesibles en el código del frontend. Se accede con `import.meta.env.VITE_NOMBRE`.

---

## 4. Configuración de Tailwind

Con Tailwind v4, **no existe `tailwind.config.js`**. La configuración se hace en dos sitios: el plugin de Vite y el CSS.

### 4.1 — Registrar el plugin en `vite.config.ts`

Reemplaza el contenido de `vite.config.ts`:

```ts
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(), // Tailwind v4 se integra aquí, sin postcss
  ],
})
```

### 4.2 — Configurar `src/index.css`

En v4 toda la configuración de tema (colores, fuentes, etc.) vive en el CSS con `@theme`. Reemplaza todo el contenido de `src/index.css`:

```css
/* Fuentes de Google */
@import url('https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,700;0,900;1,700&family=DM+Sans:wght@300;400;500;600&family=DM+Mono:wght@400;500&display=swap');

/* Importa Tailwind v4 — una sola línea, sin @tailwind base/components/utilities */
@import "tailwindcss";

/* ── Tema personalizado MovieMate ──────────────────────────────
   @theme define variables CSS que Tailwind expone como utilidades.
   Ej: --color-bg-0 → bg-bg-0, text-bg-0, border-bg-0, etc.
   ──────────────────────────────────────────────────────────── */
@theme {
  /* Colores de fondo */
  --color-bg-0: #0a0a0f;
  --color-bg-1: #12121a;
  --color-bg-2: #1a1a26;
  --color-bg-3: #22223a;

  /* Color de acento dorado */
  --color-accent:       #e8c97a;
  --color-accent-light: #f5d98a;

  /* Color de texto atenuado */
  --color-muted: #7a7a9a;

  /* Familias tipográficas */
  --font-display: 'Playfair Display', Georgia, serif;
  --font-body:    'DM Sans', sans-serif;
  --font-mono:    'DM Mono', monospace;
}

/* ── Estilos base globales ─────────────────────────────────── */
@layer base {
  body {
    background-color: var(--color-bg-0);
    color: white;
    font-family: var(--font-body);
    -webkit-font-smoothing: antialiased;
  }

  /* Scrollbar personalizada */
  ::-webkit-scrollbar       { width: 5px; height: 5px; }
  ::-webkit-scrollbar-track { background: transparent; }
  ::-webkit-scrollbar-thumb { background: var(--color-bg-3); border-radius: 2px; }
  ::-webkit-scrollbar-thumb:hover { background: var(--color-accent); }
}

/* ── Utilidades personalizadas ─────────────────────────────── */
@layer utilities {
  .scrollbar-none {
    scrollbar-width: none;
  }
  .scrollbar-none::-webkit-scrollbar {
    display: none;
  }
}
```

> **¿Cómo se usan estos colores en el código?** Exactamente igual que antes: `bg-bg-0`, `text-accent`, `border-muted`, etc. Tailwind v4 genera las utilidades automáticamente a partir de las variables `--color-*` definidas en `@theme`.

---

## 5. Inicialización de shadcn/ui

> **Tailwind v4 + shadcn:** shadcn/ui tiene soporte oficial para Tailwind v4 desde finales de 2024. Usa el flag `--css-variables` al inicializar para asegurarte de que usa el nuevo sistema de temas CSS en vez del `tailwind.config.js` que ya no existe.

```bash
npx shadcn@latest init
```

Responde así a las preguntas:
- Which style? → **Default**
- Which color? → **Slate**
- Use CSS variables? → **Yes**

Si el init detecta Tailwind v4, lo configurará automáticamente sin tocar ningún `tailwind.config.js`. Si algo falla, ejecuta:

```bash
npx shadcn@latest init --force
```

Instala los componentes que usarás:

```bash
npx shadcn@latest add button dialog dropdown-menu sonner tabs badge avatar
```

> **Nota:** `toast` está deprecado en shadcn/ui. Se usa `sonner` en su lugar, que es más ligero y moderno.

Estos componentes aparecerán en `src/components/ui/` y los puedes customizar libremente.

Para que las notificaciones toast funcionen en toda la app, añade `<Toaster />` en tu `main.tsx`, justo dentro del `QueryClientProvider` y fuera del `BrowserRouter`:

```tsx
import { Toaster } from './components/ui/sonner'

// Dentro del JSX de main.tsx:
<QueryClientProvider client={queryClient}>
  <BrowserRouter>
    <App />
  </BrowserRouter>
  <Toaster position="bottom-right" richColors />
</QueryClientProvider>
```

Y para usarlo desde cualquier componente:

```tsx
import { toast } from 'sonner'

toast.success('Lista creada correctamente')
toast.error('No se pudo guardar la valoración')
toast.info('Sesión iniciada')
```

---

## 6. Estructura de carpetas

Ejecuta esto desde la raíz del proyecto para crear toda la estructura de una vez:

```bash
mkdir -p src/{api,components/{ui,layout,shared},features/{home,discover,detail,profile,lists,auth,notifications},hooks,lib,store,types}
```

La estructura resultante:

```
src/
├── api/                        ← una función por dominio
│   ├── auth.ts
│   ├── content.ts
│   ├── ratings.ts
│   ├── lists.ts
│   ├── users.ts
│   └── notifications.ts
│
├── components/
│   ├── ui/                     ← generados por shadcn, no tocar
│   ├── layout/                 ← Sidebar, Topbar, Layout
│   │   ├── Layout.tsx
│   │   ├── Sidebar.tsx
│   │   └── Topbar.tsx
│   └── shared/                 ← componentes usados en más de una feature
│       ├── PosterCard.tsx
│       ├── StarRating.tsx
│       ├── ContentBadge.tsx
│       └── EmptyState.tsx
│
├── features/                   ← cada feature tiene su propia carpeta
│   ├── home/
│   │   ├── HomePage.tsx
│   │   ├── HeroFeatured.tsx
│   │   └── ActivityFeed.tsx
│   ├── discover/
│   │   ├── DiscoverPage.tsx
│   │   └── SearchFilters.tsx
│   ├── detail/
│   │   ├── DetailPage.tsx
│   │   ├── ReviewList.tsx
│   │   └── RatingWidget.tsx
│   ├── profile/
│   │   ├── ProfilePage.tsx
│   │   └── ProfileStats.tsx
│   ├── lists/
│   │   ├── ListsPage.tsx
│   │   ├── ListCard.tsx
│   │   └── CreateListDialog.tsx
│   ├── auth/
│   │   ├── LoginPage.tsx
│   │   └── RegisterPage.tsx
│   └── notifications/
│       └── NotificationsPage.tsx
│
├── hooks/                      ← hooks reutilizables
│   ├── useAuth.ts
│   └── useDebounce.ts
│
├── lib/
│   ├── apiClient.ts            ← instancia de Axios configurada
│   ├── queryKeys.ts            ← claves de React Query centralizadas
│   └── utils.ts                ← helpers (formatDate, getTmdbImage, etc.)
│
├── store/
│   └── authStore.ts            ← estado global de sesión
│
└── types/
    └── index.ts                ← interfaces TypeScript
```

> **Por qué `features/` en vez de `pages/`:** Agrupar por feature (en vez de por tipo de archivo) hace que todo lo relacionado con, por ejemplo, "listas" esté junto. Cuando crezcas el proyecto o necesites buscar algo, sabes exactamente dónde está.

---

## 7. Tipos TypeScript (DTOs)

Crea `src/types/index.ts`. Es una copia exacta de los DTOs del backend. **No añadas ni quites campos** — si el backend cambia, este fichero cambia también.

```typescript
// ─── Paginación ────────────────────────────────────────────────
export interface Page<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number          // página actual (0-indexed)
  size: number
  first: boolean
  last: boolean
}

// ─── Auth ──────────────────────────────────────────────────────
export interface LoginRequest {
  usernameOrEmail: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  email: string
  message: string
}

// ─── Contenido ─────────────────────────────────────────────────
export type ContentType = 'MOVIE' | 'TV'

// posterUrl y backdropUrl son URLs completas — úsalas directamente en <img src>
export interface ContentResponse {
  id: number
  tmdbId: number
  contentType: ContentType
  title: string
  releaseDate: string
  synopsis: string
  posterUrl: string
  backdropUrl: string
  genres: string[]
  tmdbRating: number
  tmdbVoteCount: number
  appRating: number
  appVoteCount: number
}

// ─── Usuario ───────────────────────────────────────────────────
// Versión compacta — embebida en ratings, listas, notificaciones, etc.
export interface UserResponse {
  id: number
  username: string
  email: string
  avatarUrl?: string
  bio?: string
  isPublic: boolean
  createdAt: string
}

// Versión completa — página de perfil
export interface UserProfileResponse {
  id: number
  username: string
  email: string
  avatarUrl?: string
  bio?: string
  isPublic: boolean
  createdAt: string
  followersCount: number
  followingCount: number
  isFollowing: boolean
}

export interface UserStatsResponse {
  totalRatings: number
  averageRating: number
  moviesWatched: number
  seriesWatched: number
  totalWatchTime: number
  listsCreated: number
  followersCount: number
  followingCount: number
  likesReceived: number
}

export interface UpdateProfileRequest {
  bio?: string
  avatarUrl?: string
}

export interface UpdateProfilePublicStatusRequest {
  isPrivate: boolean
}

// ─── Valoraciones ──────────────────────────────────────────────
export type EmotionalTag =
  | 'INCREIBLE'
  | 'RECOMENDADA'
  | 'ENTRETENIDA'
  | 'REGULAR'
  | 'DECEPCIONANTE'

export type Status =
  | 'POR_VER'
  | 'EN_PROGRESO'
  | 'VISTA'
  | 'ABANDONADA'
  | 'PAUSADA'

export interface RatingRequest {
  tmdbId: number
  contentType: ContentType
  rating: number
  reviewText?: string
  emotionaltag: EmotionalTag  // typo del backend — respetar tal cual
  status: Status
  wathchedDate: string        // typo del backend — respetar tal cual
}

export interface RatingResponse {
  id: number
  rating: number
  reviewText?: string
  emotionaltag: EmotionalTag  // typo del backend — respetar tal cual
  status: Status
  wathchedDate: string        // typo del backend — respetar tal cual
  createdAt: string
  user: UserResponse
  content: ContentResponse
}

// ─── Listas ────────────────────────────────────────────────────
export type ListType = 'CUSTOM' | 'FAVORITES' | 'WATCHLIST' | 'WATCHED'

export interface ListRequest {
  name: string
  description?: string
  isPublic: boolean
  listType: ListType
}

export interface ListResponse {
  id: number
  name: string
  description?: string
  isPublic: boolean
  listType: ListType
  itemCount: number
  createdAt: string
  user: UserResponse
  contents: ContentResponse[]
}

export interface AddToListRequest {
  tmdbId: number
  contentType: ContentType
}

// ─── Seguimiento ───────────────────────────────────────────────
export type FollowRequestStatus = 'ACCEPTED' | 'REJECTED'

export interface FollowRequestDto {
  id: number
  senderId: number
  senderUsername: string
  senderAvatarUrl: string
  createdAt: string
}

export interface FollowRequestActionResponse {
  requestId: number
  senderId: number
  receiverId: number
  status: FollowRequestStatus
  actionAt: string
}

// ─── Notificaciones ────────────────────────────────────────────
export type NotificationType =
  | 'FOLLOW_REQUEST'
  | 'FOLLOW_REQUEST_ACCEPTED'
  | 'FOLLOWER'
  | 'REVIEW_LIKE'

export interface NotificationDto {
  id: number
  type: NotificationType
  referenceId: number
  read: boolean
  createdAt: string
  senderId: number
  senderUsername?: string
  senderAvatarUrl?: string
}

// ─── Feed de actividad ─────────────────────────────────────────
export type ActivityType =
  | 'RATING_CREATED'
  | 'RATING_UPDATED'
  | 'LIST_CREATED'
  | 'LIST_UPDATED'
  | 'FOLLOW'
  | 'COMMENT_ADDED_TO_LIST'

export interface ActivityResponse {
  type: ActivityType
  user: UserResponse
  createdAt: string
  rating?: RatingResponse
  list?: ListResponse
  targetUser?: UserResponse
  content?: ContentResponse
}

// ─── Errores ───────────────────────────────────────────────────
export interface ErrorResponse {
  error: string
  message: string
  status: number
  timestamp: string
  details: { [key: string]: string }
}
```

---

## 8. Cliente HTTP (Axios)

Crea `src/lib/apiClient.ts`:

```typescript
import axios, { AxiosError } from 'axios'

// Lee la URL base del fichero .env correspondiente al entorno
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000, // 15 segundos máximo por petición
})

// ── Interceptor de REQUEST ──────────────────────────────────────
// Añade el token JWT automáticamente en cada petición
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('mm_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// ── Interceptor de RESPONSE ─────────────────────────────────────
// Maneja errores globales sin romper el router de React
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // Token expirado o inválido: limpia la sesión
      localStorage.removeItem('mm_token')
      // Emite un evento personalizado que el componente raíz escucha
      // (No usamos window.location.href para no romper el historial del router)
      window.dispatchEvent(new CustomEvent('mm:unauthorized'))
    }
    // Rechaza con el error original para que React Query lo propague
    return Promise.reject(error)
  }
)

export default apiClient
```

Crea `src/lib/utils.ts`:

```typescript
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

// Combina clases de Tailwind de forma segura (requerido por shadcn)
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Construye la URL completa de una imagen de TMDB
export function getTmdbImage(
  path: string | null | undefined,
  size: 'w92' | 'w185' | 'w342' | 'w500' | 'w780' | 'w1280' | 'original' = 'w342'
): string | null {
  if (!path) return null
  return `${import.meta.env.VITE_TMDB_IMAGE_BASE}/${size}${path}`
}

// Formatea una fecha ISO a texto legible en español
export function formatDate(isoString: string): string {
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(isoString))
}

// Formatea "hace X tiempo" relativo
export function timeAgo(isoString: string): string {
  const rtf = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })
  const diff = (new Date(isoString).getTime() - Date.now()) / 1000
  const units: [Intl.RelativeTimeFormatUnit, number][] = [
    ['year', 31536000],
    ['month', 2592000],
    ['week', 604800],
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60],
    ['second', 1],
  ]
  for (const [unit, seconds] of units) {
    if (Math.abs(diff) >= seconds) {
      return rtf.format(Math.round(diff / seconds), unit)
    }
  }
  return 'ahora mismo'
}
```

Crea `src/lib/queryKeys.ts`. Centralizar las query keys evita errores de tipado y facilita la invalidación de caché:

```typescript
// Todas las claves de React Query en un único lugar
// Patrón: de más general a más específico
export const queryKeys = {
  // Contenido
  content: {
    all: ['content'] as const,
    popular: (type?: string) => ['content', 'popular', type] as const,
    search: (query: string, page: number) => ['content', 'search', query, page] as const,
    detail: (id: number) => ['content', id] as const,
    ratings: (id: number) => ['content', id, 'ratings'] as const,
  },
  // Usuario
  users: {
    all: ['users'] as const,
    me: () => ['users', 'me'] as const,
    profile: (username: string) => ['users', username] as const,
    stats: (username: string) => ['users', username, 'stats'] as const,
    ratings: (username: string) => ['users', username, 'ratings'] as const,
    lists: (username: string) => ['users', username, 'lists'] as const,
  },
  // Listas
  lists: {
    all: ['lists'] as const,
    mine: () => ['lists', 'mine'] as const,
    detail: (id: number) => ['lists', id] as const,
    items: (id: number) => ['lists', id, 'items'] as const,
  },
  // Notificaciones
  notifications: {
    all: ['notifications'] as const,
    unreadCount: () => ['notifications', 'unread-count'] as const,
  },
  // Feed de actividad
  feed: {
    activity: (page: number) => ['feed', 'activity', page] as const,
  },
} as const
```

---

## 9. Funciones de API

> **Importante:** El backend devuelve `posterUrl` y `backdropUrl` como URLs completas de TMDB. No tienes que construirlas. La función `getTmdbImage()` de `utils.ts` ya no hace falta para el contenido — úsala solo si en algún sitio manejas rutas relativas manualmente.

**`src/api/auth.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { AuthResponse, LoginRequest, RegisterRequest, UserProfileResponse } from '../types'

export const authApi = {
  login: (data: LoginRequest) =>
    apiClient.post<AuthResponse>('/auth/login', data),

  register: (data: RegisterRequest) =>
    apiClient.post<AuthResponse>('/auth/register', data),

  // Obtiene el perfil completo del usuario autenticado
  getMe: () =>
    apiClient.get<UserProfileResponse>('/users/me'),
}
```

**`src/api/content.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { ContentResponse, ContentType, Page } from '../types'

export const contentApi = {
  getPopular: (type?: ContentType, page = 0) =>
    apiClient.get<Page<ContentResponse>>('/content/popular', { params: { type, page } }),

  search: (query: string, page = 0) =>
    apiClient.get<Page<ContentResponse>>('/content/search', { params: { q: query, page } }),

  getById: (id: number) =>
    apiClient.get<ContentResponse>(`/content/${id}`),
}
```

**`src/api/ratings.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { RatingRequest, RatingResponse, Page } from '../types'

export const ratingsApi = {
  // Ratings de un contenido concreto
  getByContent: (contentId: number, page = 0) =>
    apiClient.get<Page<RatingResponse>>(`/content/${contentId}/ratings`, { params: { page } }),

  // Rating del usuario autenticado para un contenido
  getMyRatingForContent: (contentId: number) =>
    apiClient.get<RatingResponse>(`/content/${contentId}/ratings/me`),

  // Ratings de un usuario
  getByUser: (username: string, page = 0) =>
    apiClient.get<Page<RatingResponse>>(`/users/${username}/ratings`, { params: { page } }),

  create: (data: RatingRequest) =>
    apiClient.post<RatingResponse>('/ratings', data),

  update: (ratingId: number, data: RatingRequest) =>
    apiClient.put<RatingResponse>(`/ratings/${ratingId}`, data),

  delete: (ratingId: number) =>
    apiClient.delete(`/ratings/${ratingId}`),

  like: (ratingId: number) =>
    apiClient.post(`/ratings/${ratingId}/like`),

  unlike: (ratingId: number) =>
    apiClient.delete(`/ratings/${ratingId}/like`),
}
```

**`src/api/lists.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { ListRequest, ListResponse, AddToListRequest } from '../types'

export const listsApi = {
  // Listas del usuario autenticado
  getMine: () =>
    apiClient.get<ListResponse[]>('/lists/me'),

  // Listas de un usuario concreto
  getByUser: (username: string) =>
    apiClient.get<ListResponse[]>(`/users/${username}/lists`),

  getById: (id: number) =>
    apiClient.get<ListResponse>(`/lists/${id}`),

  create: (data: ListRequest) =>
    apiClient.post<ListResponse>('/lists', data),

  update: (id: number, data: Partial<ListRequest>) =>
    apiClient.put<ListResponse>(`/lists/${id}`, data),

  delete: (id: number) =>
    apiClient.delete(`/lists/${id}`),

  // Añadir contenido a una lista (por tmdbId + tipo, no por id interno)
  addContent: (listId: number, data: AddToListRequest) =>
    apiClient.post(`/lists/${listId}/items`, data),

  removeContent: (listId: number, contentId: number) =>
    apiClient.delete(`/lists/${listId}/items/${contentId}`),
}
```

**`src/api/users.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type {
  UserProfileResponse,
  UserStatsResponse,
  UpdateProfileRequest,
  UpdateProfilePublicStatusRequest,
  FollowRequestDto,
  FollowRequestActionResponse,
} from '../types'

export const usersApi = {
  getProfile: (username: string) =>
    apiClient.get<UserProfileResponse>(`/users/${username}`),

  getStats: (username: string) =>
    apiClient.get<UserStatsResponse>(`/users/${username}/stats`),

  updateProfile: (data: UpdateProfileRequest) =>
    apiClient.put<UserProfileResponse>('/users/me', data),

  updatePrivacy: (data: UpdateProfilePublicStatusRequest) =>
    apiClient.patch<UserProfileResponse>('/users/me/privacy', data),

  follow: (username: string) =>
    apiClient.post(`/users/${username}/follow`),

  unfollow: (username: string) =>
    apiClient.delete(`/users/${username}/follow`),

  // Solicitudes de seguimiento pendientes (para perfiles privados)
  getPendingFollowRequests: () =>
    apiClient.get<FollowRequestDto[]>('/users/me/follow-requests'),

  respondToFollowRequest: (requestId: number, action: 'ACCEPTED' | 'REJECTED') =>
    apiClient.post<FollowRequestActionResponse>(`/users/me/follow-requests/${requestId}`, { status: action }),
}
```

**`src/api/notifications.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { NotificationDto, Page } from '../types'

export const notificationsApi = {
  getAll: (page = 0) =>
    apiClient.get<Page<NotificationDto>>('/notifications', { params: { page } }),

  getUnreadCount: () =>
    apiClient.get<{ count: number }>('/notifications/unread-count'),

  markAsRead: (id: number) =>
    apiClient.patch(`/notifications/${id}/read`),

  markAllAsRead: () =>
    apiClient.patch('/notifications/read-all'),
}
```

**`src/api/activity.ts`**

```typescript
import apiClient from '../lib/apiClient'
import type { ActivityResponse, Page } from '../types'

export const activityApi = {
  // Feed de actividad de usuarios que sigues
  getFeed: (page = 0) =>
    apiClient.get<Page<ActivityResponse>>('/activity/feed', { params: { page } }),

  // Actividad de un usuario concreto
  getByUser: (username: string, page = 0) =>
    apiClient.get<Page<ActivityResponse>>(`/users/${username}/activity`, { params: { page } }),
}
```

---

## 10. Estado global (Zustand)

Crea `src/store/authStore.ts`:

> **Atención:** El backend devuelve `{ token, username, email, message }` al hacer login/register — no un objeto `User` completo. El perfil completo se carga aparte con `getMe()` cuando sea necesario.

```typescript
import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

// Lo que guardamos de la sesión tras el login
interface SessionUser {
  username: string
  email: string
}

interface AuthState {
  sessionUser: SessionUser | null
  token: string | null
  isAuthenticated: boolean
  isInitialized: boolean    // evita flash de login al recargar
}

interface AuthActions {
  setAuth: (username: string, email: string, token: string) => void
  logout: () => void
  setInitialized: () => void
}

type AuthStore = AuthState & AuthActions

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      // Estado inicial
      sessionUser: null,
      token: null,
      isAuthenticated: false,
      isInitialized: false,

      // Acciones
      setAuth: (username, email, token) => {
        localStorage.setItem('mm_token', token)
        set({ sessionUser: { username, email }, token, isAuthenticated: true })
      },

      logout: () => {
        localStorage.removeItem('mm_token')
        set({ sessionUser: null, token: null, isAuthenticated: false })
      },

      setInitialized: () => set({ isInitialized: true }),
    }),
    {
      name: 'mm-auth',
      storage: createJSONStorage(() => localStorage),
      // Solo persiste estos campos (no isInitialized)
      partialize: (state) => ({
        sessionUser: state.sessionUser,
        token: state.token,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
```

Crea `src/hooks/useAuth.ts`:

```typescript
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/auth'
import { useAuthStore } from '../store/authStore'
import { queryKeys } from '../lib/queryKeys'
import type { LoginRequest, RegisterRequest } from '../types'

export function useLogin() {
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: LoginRequest) => authApi.login(data),
    onSuccess: ({ data }) => {
      setAuth(data.username, data.email, data.token)
      // Limpia la caché anterior (por si había otro usuario)
      queryClient.clear()
      navigate('/')
    },
  })
}

export function useRegister() {
  const { setAuth } = useAuthStore()
  const navigate = useNavigate()

  return useMutation({
    mutationFn: (data: RegisterRequest) => authApi.register(data),
    onSuccess: ({ data }) => {
      setAuth(data.username, data.email, data.token)
      navigate('/')
    },
  })
}

export function useLogout() {
  const { logout } = useAuthStore()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  return () => {
    logout()
    queryClient.clear()
    navigate('/login')
  }
}

// Hook para obtener el perfil completo del usuario autenticado
// Solo fetcha si hay sesión activa
export function useMyProfile() {
  const { isAuthenticated } = useAuthStore()

  return useQuery({
    queryKey: queryKeys.users.me(),
    queryFn: () => authApi.getMe(),
    enabled: isAuthenticated,
    select: (res) => res.data,
    // El perfil no cambia frecuentemente — 10 min de stale time
    staleTime: 1000 * 60 * 10,
  })
}
```

---

## 11. Configuración raíz — main.tsx

Reemplaza `src/main.tsx`:

```tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ReactQueryDevtools } from '@tanstack/react-query-devtools'
import { Toaster } from './components/ui/sonner'
import App from './App'
import './index.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,       // datos frescos 5 minutos
      retry: 1,                         // reintenta una vez si falla
      refetchOnWindowFocus: false,      // no re-fetcha al volver a la pestaña
    },
    mutations: {
      retry: 0,
    },
  },
})

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <App />
      </BrowserRouter>
      <Toaster position="bottom-right" richColors />
      <ReactQueryDevtools initialIsOpen={false} />
    </QueryClientProvider>
  </React.StrictMode>
)
```

---

## 12. Rutas — App.tsx

Reemplaza `src/App.tsx`:

```tsx
import { useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import HomePage from './features/home/HomePage'
import DiscoverPage from './features/discover/DiscoverPage'
import DetailPage from './features/detail/DetailPage'
import ProfilePage from './features/profile/ProfilePage'
import ListsPage from './features/lists/ListsPage'
import NotificationsPage from './features/notifications/NotificationsPage'
import LoginPage from './features/auth/LoginPage'
import RegisterPage from './features/auth/RegisterPage'

// Protege rutas que requieren autenticación.
// Muestra un spinner mientras Zustand se inicializa desde localStorage
// para evitar el flash de redirección al login.
function PrivateRoute({ children }: { children: React.ReactNode }) {
  const { isAuthenticated, isInitialized } = useAuthStore()

  if (!isInitialized) {
    return (
      <div className="flex items-center justify-center h-screen bg-bg-0">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin" />
      </div>
    )
  }

  return isAuthenticated ? children : <Navigate to="/login" replace />
}

export default function App() {
  const { logout, isInitialized, setInitialized } = useAuthStore()
  const navigate = useNavigate()

  // Marca la sesión como inicializada en el primer render
  useEffect(() => {
    if (!isInitialized) setInitialized()
  }, [])

  // Escucha el evento de sesión expirada que emite el interceptor de Axios.
  // Usar un evento personalizado (en vez de window.location.href)
  // preserva el historial del router de React.
  useEffect(() => {
    const handleUnauthorized = () => {
      logout()
      navigate('/login', { replace: true })
    }
    window.addEventListener('mm:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('mm:unauthorized', handleUnauthorized)
  }, [logout, navigate])

  return (
    <Routes>
      {/* Rutas públicas — sin sidebar ni topbar */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Rutas con layout (sidebar + topbar) */}
      <Route element={<Layout />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/discover" element={<DiscoverPage />} />
        <Route path="/content/:id" element={<DetailPage />} />
        <Route path="/profile/:username" element={<ProfilePage />} />

        {/* Requieren login */}
        <Route
          path="/lists"
          element={<PrivateRoute><ListsPage /></PrivateRoute>}
        />
        <Route
          path="/notifications"
          element={<PrivateRoute><NotificationsPage /></PrivateRoute>}
        />
      </Route>

      {/* Ruta desconocida → inicio */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
```

---

## 13. Layout — Sidebar y Topbar

**`src/components/layout/Layout.tsx`**

```tsx
import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function Layout() {
  return (
    <div className="flex h-screen bg-bg-0 text-white overflow-hidden">
      <Sidebar />
      <div className="flex flex-col flex-1 min-w-0">
        <Topbar />
        {/* El scroll ocurre aquí dentro, no en el layout entero */}
        <main className="flex-1 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
```

**`src/components/layout/Sidebar.tsx`**

```tsx
import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { useQuery } from '@tanstack/react-query'
import { notificationsApi } from '../../api/notifications'
import { queryKeys } from '../../lib/queryKeys'
import { cn } from '../../lib/utils'

const NAV_MAIN = [
  { to: '/',          icon: '🏠', label: 'Inicio' },
  { to: '/discover',  icon: '🔍', label: 'Descubrir' },
  { to: '/activity',  icon: '📡', label: 'Actividad' },
]

const NAV_PERSONAL = [
  { to: '/ratings',   icon: '⭐', label: 'Valoraciones' },
  { to: '/lists',     icon: '📋', label: 'Mis listas' },
  { to: '/watchlist', icon: '🕐', label: 'Por ver' },
  { to: '/favorites', icon: '❤️',  label: 'Favoritos' },
]

function SidebarSection({ label, items }: { label: string; items: typeof NAV_MAIN }) {
  return (
    <div className="mb-2">
      <p className="px-4 py-1.5 text-[0.6rem] font-bold tracking-[1.2px] uppercase text-muted/60">
        {label}
      </p>
      <div className="px-3">
        {items.map(({ to, icon, label: itemLabel }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => cn(
              'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2 mb-0.5',
              isActive
                ? 'text-accent bg-accent/[0.06] border-accent font-medium'
                : 'text-muted hover:text-white hover:bg-bg-2 border-transparent'
            )}
          >
            <span className="w-5 text-center text-[0.9rem] opacity-80">{icon}</span>
            {itemLabel}
          </NavLink>
        ))}
      </div>
    </div>
  )
}

export default function Sidebar() {
  const { isAuthenticated, sessionUser } = useAuthStore()
  const navigate = useNavigate()

  const { data: unreadCount } = useQuery({
    queryKey: queryKeys.notifications.unreadCount(),
    queryFn: () => notificationsApi.getUnreadCount(),
    enabled: isAuthenticated,
    refetchInterval: 60_000,
    select: (res) => res.data.count,
  })

  return (
    <aside className="w-56 shrink-0 bg-bg-1 border-r border-white/[0.06] flex flex-col h-full">
      {/* Brand */}
      <div className="flex items-center gap-2.5 px-4 py-5 border-b border-white/[0.06]">
        <div className="w-8 h-8 bg-accent rounded-lg flex items-center justify-center text-bg-0 font-bold text-sm shrink-0">
          🎬
        </div>
        <span className="font-display font-bold italic text-[1.1rem] tracking-tight">
          MovieMate
        </span>
      </div>

      <nav className="flex-1 overflow-y-auto py-4 scrollbar-none">
        <SidebarSection label="Principal" items={NAV_MAIN} />
        <SidebarSection label="Mi espacio" items={NAV_PERSONAL} />

        {/* Notificaciones con badge de no leídas */}
        <div className="px-3 mt-1">
          <NavLink
            to="/notifications"
            className={({ isActive }) => cn(
              'flex items-center gap-2.5 px-3 py-2 rounded-lg text-sm transition-all border-l-2',
              isActive
                ? 'text-accent bg-accent/[0.06] border-accent font-medium'
                : 'text-muted hover:text-white hover:bg-bg-2 border-transparent'
            )}
          >
            <span className="w-5 text-center text-[0.9rem]">🔔</span>
            <span className="flex-1">Notificaciones</span>
            {unreadCount && unreadCount > 0 && (
              <span className="bg-red-500 text-white text-[0.6rem] font-bold px-1.5 py-0.5 rounded-full min-w-[18px] text-center">
                {unreadCount > 99 ? '99+' : unreadCount}
              </span>
            )}
          </NavLink>
        </div>
      </nav>

      {/* Usuario en el pie — sessionUser se guarda al hacer login */}
      {sessionUser && (
        <div
          className="px-4 py-3 border-t border-white/[0.06] flex items-center gap-2.5 cursor-pointer hover:bg-bg-2 transition-colors"
          onClick={() => navigate(`/profile/${sessionUser.username}`)}
        >
          <div className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 shrink-0">
            {sessionUser.username.charAt(0).toUpperCase()}
          </div>
          <div className="min-w-0">
            <p className="text-sm text-white/90 truncate font-medium">{sessionUser.username}</p>
            <p className="text-xs text-muted font-mono truncate">{sessionUser.email}</p>
          </div>
        </div>
      )}
    </aside>
  )
}
```

**`src/components/layout/Topbar.tsx`**

```tsx
import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useLogout } from '../../hooks/useAuth'
import { useAuthStore } from '../../store/authStore'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu'

export default function Topbar() {
  const [query, setQuery] = useState('')
  const navigate = useNavigate()
  const logout = useLogout()
  const { isAuthenticated, sessionUser } = useAuthStore()

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (query.trim()) {
      navigate(`/discover?q=${encodeURIComponent(query.trim())}`)
    }
  }

  return (
    <header className="h-13 flex items-center gap-3 px-6 bg-bg-1 border-b border-white/[0.06] sticky top-0 z-10">
      <form onSubmit={handleSearch} className="flex-1 max-w-md">
        <div className="flex items-center gap-2 bg-bg-2 border border-white/[0.06] rounded-xl px-3.5 py-2 text-sm text-muted hover:border-white/[0.12] transition-colors focus-within:border-accent/50">
          <span>🔍</span>
          <input
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Buscar películas, series, usuarios…"
            className="flex-1 bg-transparent outline-none text-white placeholder:text-muted text-sm"
          />
        </div>
      </form>

      <div className="ml-auto flex items-center gap-2">
        {isAuthenticated && sessionUser ? (
          <>
            <button
              onClick={() => navigate('/notifications')}
              className="w-8 h-8 rounded-full bg-bg-2 border border-white/[0.06] flex items-center justify-center hover:border-white/20 transition-colors"
              aria-label="Notificaciones"
            >
              🔔
            </button>

            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <button
                  className="w-8 h-8 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-xs font-bold text-bg-0 hover:ring-2 hover:ring-accent/40 transition-all"
                  aria-label="Menú de usuario"
                >
                  {sessionUser.username.charAt(0).toUpperCase()}
                </button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end" className="w-44 bg-bg-2 border-white/10 text-white">
                <DropdownMenuItem onClick={() => navigate(`/profile/${sessionUser.username}`)}>
                  👤 Mi perfil
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => navigate('/settings')}>
                  ⚙️ Ajustes
                </DropdownMenuItem>
                <DropdownMenuSeparator className="bg-white/10" />
                <DropdownMenuItem onClick={logout} className="text-red-400 focus:text-red-400">
                  🚪 Cerrar sesión
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </>
        ) : (
          <button
            onClick={() => navigate('/login')}
            className="text-sm font-medium text-accent hover:text-accent-light transition-colors"
          >
            Iniciar sesión
          </button>
        )}
      </div>
    </header>
  )
}
```

---

## 14. Componentes base reutilizables

**`src/components/shared/PosterCard.tsx`**

El backend devuelve `posterUrl` como URL completa — no hay que construirla.

```tsx
import { Link } from 'react-router-dom'
import type { ContentResponse } from '../../types'

interface Props {
  content: ContentResponse
  userRating?: number
}

export default function PosterCard({ content, userRating }: Props) {
  return (
    <Link
      to={`/content/${content.id}`}
      className="group shrink-0 w-36 cursor-pointer focus:outline-none focus-visible:ring-2 focus-visible:ring-accent rounded-xl"
    >
      <div className="relative aspect-[2/3] rounded-xl overflow-hidden bg-bg-3 border border-white/[0.06] mb-2">
        {content.posterUrl ? (
          <img
            src={content.posterUrl}
            alt={`Poster de ${content.title}`}
            loading="lazy"
            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center text-4xl bg-bg-3">
            🎬
          </div>
        )}

        <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent" />

        {/* Puntuación */}
        <div className="absolute bottom-1.5 left-2 flex items-center gap-1 text-yellow-400 text-[0.65rem] font-mono font-semibold">
          ⭐ {content.appRating > 0
            ? content.appRating.toFixed(1)
            : content.tmdbRating.toFixed(1)}
        </div>

        {/* Tipo */}
        <div className="absolute top-1.5 right-1.5 text-[10px] font-semibold bg-black/60 backdrop-blur-sm text-white/70 px-1.5 py-0.5 rounded">
          {content.contentType === 'MOVIE' ? 'Film' : 'Serie'}
        </div>

        {/* Hover overlay */}
        <div className="absolute inset-0 bg-black/60 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col items-center justify-center gap-2">
          <span className="text-xs font-semibold bg-accent text-bg-0 px-3 py-1.5 rounded-lg">
            + Lista
          </span>
          <span className="text-xs font-medium text-white/80">Ver ficha →</span>
        </div>
      </div>

      <p className="text-sm font-semibold text-white/90 leading-tight mb-1 line-clamp-2">
        {content.title}
      </p>
      <p className="text-[0.65rem] text-muted font-mono">
        {content.releaseDate ? new Date(content.releaseDate).getFullYear() : '—'}
        {content.genres[0] ? ` · ${content.genres[0]}` : ''}
      </p>

      {userRating != null && (
        <div className="flex gap-0.5 mt-1">
          {[1, 2, 3, 4, 5].map((n) => (
            <span
              key={n}
              className={`text-[0.6rem] ${n <= userRating ? 'text-yellow-400' : 'text-white/20'}`}
            >
              ★
            </span>
          ))}
        </div>
      )}
    </Link>
  )
}
```

**`src/components/shared/StarRating.tsx`**

```tsx
import { useState } from 'react'

interface Props {
  value: number           // 1-5, 0 = sin valorar
  onChange?: (value: number) => void
  readonly?: boolean
  size?: 'sm' | 'md' | 'lg'
}

const sizes = { sm: 'text-xs', md: 'text-lg', lg: 'text-3xl' }

export default function StarRating({ value, onChange, readonly = false, size = 'md' }: Props) {
  const [hovered, setHovered] = useState(0)
  const displayed = hovered || value

  return (
    <div
      className="flex gap-0.5"
      onMouseLeave={() => !readonly && setHovered(0)}
      role={readonly ? undefined : 'radiogroup'}
      aria-label="Valoración"
    >
      {[1, 2, 3, 4, 5].map((n) => (
        <span
          key={n}
          className={[
            sizes[size],
            'transition-all duration-75 select-none',
            n <= displayed ? 'text-yellow-400' : 'text-white/20',
            !readonly ? 'cursor-pointer hover:scale-125' : '',
          ].join(' ')}
          onMouseEnter={() => !readonly && setHovered(n)}
          onClick={() => !readonly && onChange?.(n)}
          role={readonly ? undefined : 'radio'}
          aria-label={`${n} estrella${n > 1 ? 's' : ''}`}
          aria-checked={n === value}
        >
          ★
        </span>
      ))}
    </div>
  )
}
```

**`src/components/shared/EmptyState.tsx`**

```tsx
interface Props {
  icon?: string
  title: string
  description?: string
  action?: React.ReactNode
}

export default function EmptyState({ icon = '🎬', title, description, action }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-20 px-4 text-center">
      <span className="text-5xl mb-4">{icon}</span>
      <h3 className="text-lg font-semibold text-white/80 mb-2">{title}</h3>
      {description && (
        <p className="text-sm text-muted max-w-sm mb-6">{description}</p>
      )}
      {action}
    </div>
  )
}
```

---

## 15. Páginas

### LoginPage

**`src/features/auth/LoginPage.tsx`**

El campo del formulario se llama `usernameOrEmail` para coincidir con `LoginRequest`.

```tsx
import { useState } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { useLogin } from '../../hooks/useAuth'
import { useAuthStore } from '../../store/authStore'

export default function LoginPage() {
  const { isAuthenticated } = useAuthStore()
  const login = useLogin()
  const [form, setForm] = useState({ usernameOrEmail: '', password: '' })
  const [error, setError] = useState('')

  if (isAuthenticated) return <Navigate to="/" replace />

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    try {
      await login.mutateAsync(form)
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Credenciales incorrectas.')
    }
  }

  return (
    <div className="min-h-screen grid grid-cols-1 lg:grid-cols-2 bg-bg-0">
      {/* Panel izquierdo — presentación (solo desktop) */}
      <div className="hidden lg:flex flex-col justify-center px-16 bg-bg-1 border-r border-white/[0.06] relative overflow-hidden">
        <div className="absolute w-[500px] h-[500px] rounded-full bg-accent/[0.06] blur-3xl top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 pointer-events-none" />
        <div className="relative">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-11 h-11 bg-accent rounded-xl flex items-center justify-center text-xl">
              🎬
            </div>
            <span className="font-display font-bold italic text-2xl tracking-tight">MovieMate</span>
          </div>
          <p className="font-mono text-xs text-muted tracking-wider uppercase mb-10">
            Tu diario cinematográfico social
          </p>
          {[
            { icon: '🎬', title: 'Películas y series juntas', desc: 'Un solo lugar para todo tu contenido audiovisual.' },
            { icon: '⭐', title: 'Valora y reseña',          desc: 'Comparte tu opinión y descubre la de la comunidad.' },
            { icon: '📋', title: 'Listas personalizadas',    desc: 'Organiza tu colección como quieras.' },
            { icon: '👥', title: 'Red social cinéfila',      desc: 'Sigue a personas y descubre nuevo contenido.' },
          ].map(({ icon, title, desc }) => (
            <div key={title} className="flex gap-3 mb-5">
              <div className="w-8 h-8 rounded-lg bg-bg-3 border border-white/[0.06] flex items-center justify-center text-sm shrink-0 mt-0.5">
                {icon}
              </div>
              <div>
                <p className="text-sm font-semibold text-white/90">{title}</p>
                <p className="text-xs text-muted mt-0.5">{desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Panel derecho — formulario */}
      <div className="flex items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="flex items-center gap-2 mb-8 lg:hidden">
            <div className="w-9 h-9 bg-accent rounded-xl flex items-center justify-center">🎬</div>
            <span className="font-display font-bold italic text-xl">MovieMate</span>
          </div>

          <h1 className="font-display font-bold italic text-3xl mb-1">Bienvenido</h1>
          <p className="text-sm text-muted mb-8">Inicia sesión para continuar</p>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Email o usuario
              </label>
              <input
                type="text"
                required
                autoComplete="username"
                value={form.usernameOrEmail}
                onChange={(e) => setForm((f) => ({ ...f, usernameOrEmail: e.target.value }))}
                placeholder="usuario@email.com"
                className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
              />
            </div>
            <div>
              <label className="block font-mono text-xs text-muted tracking-wider uppercase mb-1.5">
                Contraseña
              </label>
              <input
                type="password"
                required
                autoComplete="current-password"
                value={form.password}
                onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                placeholder="••••••••"
                className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3.5 py-2.5 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 focus:ring-2 focus:ring-accent/10 transition-all"
              />
            </div>

            {error && (
              <p className="text-sm text-red-400 bg-red-400/10 border border-red-400/20 rounded-lg px-3 py-2">
                {error}
              </p>
            )}

            <button
              type="submit"
              disabled={login.isPending}
              className="w-full bg-accent hover:bg-accent-light disabled:opacity-60 disabled:cursor-not-allowed text-bg-0 font-semibold rounded-xl py-2.5 text-sm transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-accent/20"
            >
              {login.isPending ? 'Iniciando sesión…' : 'Iniciar sesión'}
            </button>
          </form>

          <p className="text-center text-sm text-muted mt-6">
            ¿No tienes cuenta?{' '}
            <Link to="/register" className="text-accent hover:text-accent-light transition-colors">
              Regístrate gratis
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
```

### HomePage

**`src/features/home/HomePage.tsx`**

Usa los campos reales del DTO: `posterUrl`, `backdropUrl`, `contentType`, `tmdbRating`, `appRating`.

```tsx
import { useQuery } from '@tanstack/react-query'
import { contentApi } from '../../api/content'
import { queryKeys } from '../../lib/queryKeys'
import PosterCard from '../../components/shared/PosterCard'
import type { ContentResponse } from '../../types'

function PosterSkeleton() {
  return (
    <div className="shrink-0 w-36 animate-pulse">
      <div className="aspect-[2/3] rounded-xl bg-bg-3 mb-2" />
      <div className="h-3 bg-bg-3 rounded w-3/4 mb-1.5" />
      <div className="h-2.5 bg-bg-3 rounded w-1/2" />
    </div>
  )
}

function HeroSkeleton() {
  return (
    <div className="space-y-3 animate-pulse">
      <div className="h-10 bg-bg-3 rounded w-3/4" />
      <div className="h-4 bg-bg-3 rounded w-1/2" />
    </div>
  )
}

export default function HomePage() {
  const { data: popular, isLoading } = useQuery({
    queryKey: queryKeys.content.popular(),
    queryFn: () => contentApi.getPopular(),
    select: (res) => res.data.content,
  })

  const featured: ContentResponse | undefined = popular?.[0]
  const rest = popular?.slice(1) ?? []

  return (
    <div className="pb-10">
      {/* ── HERO ── */}
      <div className="relative h-[460px] overflow-hidden">
        {featured?.backdropUrl ? (
          <img
            src={featured.backdropUrl}
            alt=""
            aria-hidden="true"
            className="absolute inset-0 w-full h-full object-cover object-top scale-105 brightness-[0.35] saturate-150"
          />
        ) : (
          <div className="absolute inset-0 bg-gradient-to-br from-[#1a0a2e] to-[#0a1628]" />
        )}

        <div className="absolute inset-0 bg-gradient-to-r from-bg-0 via-bg-0/70 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-t from-bg-0 via-transparent to-transparent" />
        <div
          className="absolute bottom-[-1px] left-0 right-0 h-20 bg-bg-0"
          style={{ clipPath: 'polygon(0 60%, 100% 0%, 100% 100%, 0% 100%)' }}
        />

        {featured?.posterUrl && (
          <div className="absolute right-20 top-8 bottom-[-30px] w-48 z-10">
            <img
              src={featured.posterUrl}
              alt={`Poster de ${featured.title}`}
              className="w-full h-full object-cover rounded-2xl shadow-[0_30px_80px_rgba(0,0,0,0.7)] border border-white/[0.08]"
            />
          </div>
        )}

        <div className="relative z-10 h-full flex flex-col justify-end px-9 pb-10 max-w-lg">
          <div className="inline-flex items-center gap-1.5 bg-accent/10 border border-accent/30 text-accent text-xs font-semibold px-2.5 py-1 rounded-full mb-3 w-fit">
            ✨ Tendencia esta semana
          </div>

          {featured ? (
            <>
              <h1 className="font-display font-bold text-[2.8rem] leading-[1.05] tracking-tight mb-2.5">
                {featured.title}
              </h1>
              <div className="flex items-center gap-2.5 text-sm text-white/70 mb-5 flex-wrap">
                <span className="flex items-center gap-1 bg-yellow-400/15 text-yellow-400 font-bold text-xs px-2 py-0.5 rounded">
                  ⭐ {featured.tmdbRating.toFixed(1)}
                </span>
                <span className="text-white/30">·</span>
                <span>{new Date(featured.releaseDate).getFullYear()}</span>
                <span className="text-white/30">·</span>
                <span>{featured.contentType === 'MOVIE' ? 'Película' : 'Serie'}</span>
              </div>
            </>
          ) : (
            <HeroSkeleton />
          )}

          <div className="flex gap-2.5 flex-wrap">
            <button className="bg-accent hover:bg-accent-light text-bg-0 font-semibold text-sm px-4 py-2 rounded-xl transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-accent/20">
              + Añadir a lista
            </button>
            <button className="bg-white/[0.08] hover:bg-white/[0.14] border border-white/[0.1] text-white font-medium text-sm px-4 py-2 rounded-xl transition-colors">
              ⭐ Valorar
            </button>
            <button className="bg-transparent hover:bg-white/[0.06] border border-white/[0.1] text-white/70 font-medium text-sm px-4 py-2 rounded-xl transition-colors">
              Ver ficha →
            </button>
          </div>
        </div>
      </div>

      {/* ── POPULARES ── */}
      <section className="px-6 pt-8">
        <div className="flex items-baseline justify-between mb-4">
          <h2 className="font-display font-bold italic text-xl">Populares ahora 🔥</h2>
          <button className="text-xs text-accent hover:text-accent-light font-medium transition-colors">
            Ver todo →
          </button>
        </div>
        <div className="flex gap-3.5 overflow-x-auto scrollbar-none pb-1">
          {isLoading
            ? Array.from({ length: 7 }).map((_, i) => <PosterSkeleton key={i} />)
            : rest.map((item) => <PosterCard key={item.id} content={item} />)
          }
        </div>
      </section>
    </div>
  )
}
```

### Patrón useMutation — operaciones de escritura

Cuando necesites **crear, editar o borrar** algo, usa `useMutation` con invalidación de caché. Ejemplo para crear una valoración:

```tsx
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ratingsApi } from '../../api/ratings'
import { queryKeys } from '../../lib/queryKeys'
import type { RatingRequest } from '../../types'

function useCreateRating() {
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (data: RatingRequest) => ratingsApi.create(data),

    onSuccess: (_, variables) => {
      // Invalida las queries afectadas — se re-fetchan automáticamente
      queryClient.invalidateQueries({ queryKey: queryKeys.content.ratings(variables.tmdbId) })
      queryClient.invalidateQueries({ queryKey: queryKeys.users.me() })
      toast.success('Valoración guardada')
    },

    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Error al guardar la valoración')
    },
  })
}
```

Aplica el mismo patrón para listas (`listsApi.create`), seguir usuarios (`usersApi.follow`), dar like a reseñas (`ratingsApi.like`), etc.

---

## 16. CORS en Spring Boot

Asegúrate de que tu `SecurityConfig.java` permite el origen `http://localhost:5173`:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
        "http://localhost:5173",   // Vite en desarrollo
        "https://tu-dominio.com"   // producción
    ));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

---

## 17. Arrancar y verificar

```bash
# Terminal 1 — backend
cd moviemate-backend
./mvnw spring-boot:run

# Terminal 2 — frontend
cd moviemate-frontend
npm run dev
```

Abre `http://localhost:5173` y comprueba en DevTools → Network:

| Síntoma | Causa probable | Solución |
|---|---|---|
| Error CORS en rojo | Spring no permite el origen | Revisa el capítulo 16 |
| 404 en `/api/...` | La URL base no coincide | Ajusta `VITE_API_BASE_URL` en `.env.development` |
| 401 inmediatamente | El endpoint requiere auth | Implementa el login primero |
| Imagen rota | La URL de TMDB está mal formada | Imprime `content.posterUrl` en consola y verifica |
| Token no se envía | El interceptor no lo encuentra | Comprueba que `mm_token` existe en localStorage |

---

## 18. Orden de implementación recomendado

Sigue este orden — cada paso desbloquea el siguiente:

```
1. LoginPage + RegisterPage
   Prueba: haces login, el token se guarda en localStorage,
           te redirige a / y ves tu username en el sidebar.

2. Sidebar + Topbar (Layout)
   Prueba: la navegación entre rutas funciona sin recargar.

3. HomePage — hero con datos reales + carrusel de populares
   Prueba: ves posters de TMDB, el backdrop se carga en el hero.

4. DiscoverPage — buscador con useQuery y parámetro ?q=
   Prueba: escribes algo, los resultados aparecen.

5. DetailPage — fetch de /content/:id, backdrop, valoración
   Prueba: ves la ficha completa y puedes dar estrellas.

6. ProfilePage — fetch de /users/:username + stats
   Prueba: ves tu propio perfil con datos reales.

7. ListsPage — lista de listas + CreateListDialog (useMutation)
   Prueba: creas una lista y aparece sin recargar la página.

8. NotificationsPage — lista + badge en tiempo real
   Prueba: el contador del sidebar se actualiza.

9. Conectar el resto de botones:
   seguir usuarios, añadir a lista, dar like a reseñas
   (todos con useMutation + invalidación de caché).
```
