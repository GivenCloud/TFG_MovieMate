import { useState, useEffect, useRef, useCallback } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import BackButton from '@/components/shared/BackButton'
import { useQuery } from '@tanstack/react-query'
import { useDebounce } from '@/hooks/useDebounce'
import { usePopular, useSearch, useDiscover, useGenres } from '@/hooks/useDiscover'
import SearchBar from '@/components/Discover/SearchBar'
import FilterTabs from '@/components/Discover/FilterTabs'
import ContentGrid from '@/components/Discover/ContentGrid'
import { usersApi } from '@/api/users'
import { queryKeys } from '@/lib/queryKeys'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/store/authStore'
import type { ContentType, UserResponse, ContentResponse } from '../../types'
import type { DiscoverParams } from '@/api/tmdb'

type Filter = ContentType | 'ALL'
type Mode = 'content' | 'users'

const SORT_OPTIONS = [
  { value: 'popularity.desc',     label: 'Popularidad ↓' },
  { value: 'vote_average.desc',   label: 'Mejor valoradas' },
  { value: 'release_date.desc',   label: 'Más recientes' },
  { value: 'revenue.desc',        label: 'Mayor taquilla' },
]

const RATING_OPTIONS = [
  { value: '', label: 'Cualquier nota' },
  { value: '5', label: '5+ ★★★' },
  { value: '6', label: '6+ ★★★½' },
  { value: '7', label: '7+ ★★★★' },
  { value: '8', label: '8+ ★★★★½' },
]

const currentYear = new Date().getFullYear()

// ── Tarjeta de usuario ──────────────────────────────────────────
function UserCard({ user }: { user: UserResponse }) {
  return (
    <Link
      to={`/profile/${user.username}`}
      className="flex items-center gap-3 p-3.5 bg-bg-1 hover:bg-bg-2 border border-white/[0.06] rounded-xl transition-colors"
    >
      <div className="w-11 h-11 rounded-full bg-gradient-to-br from-accent to-pink-500 flex items-center justify-center text-base font-bold text-bg-0 shrink-0 overflow-hidden border border-white/10">
        {user.avatarUrl ? (
          <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
        ) : (
          user.username.charAt(0).toUpperCase()
        )}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-semibold text-white truncate">@{user.username}</p>
        {user.bio ? (
          <p className="text-xs text-muted truncate mt-0.5">{user.bio}</p>
        ) : (
          <p className="text-xs text-muted/50 italic mt-0.5">Sin bio</p>
        )}
      </div>
      <span className="text-xs text-muted shrink-0">Ver perfil →</span>
    </Link>
  )
}

function UserCardSkeleton() {
  return (
    <div className="flex items-center gap-3 p-3.5 bg-bg-1 border border-white/[0.06] rounded-xl animate-pulse">
      <div className="w-11 h-11 rounded-full bg-bg-3 shrink-0" />
      <div className="flex-1 space-y-2">
        <div className="h-3.5 bg-bg-3 rounded w-1/3" />
        <div className="h-2.5 bg-bg-3 rounded w-2/3" />
      </div>
    </div>
  )
}

// ── Página ──────────────────────────────────────────────────────
export default function DiscoverPage() {
  const { isAuthenticated } = useAuthStore()
  const [searchParams, setSearchParams] = useSearchParams()
  const [filter, setFilter] = useState<Filter>((searchParams.get('type') as Filter) ?? 'ALL')
  const [mode, setMode] = useState<Mode>('content')
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [page, setPage] = useState(1)
  const [accumulatedItems, setAccumulatedItems] = useState<ContentResponse[]>([])
  const observerRef = useRef<IntersectionObserver | null>(null)

  // Filtros avanzados (desde URL params)
  const [genreId, setGenreId] = useState<number | undefined>(
    searchParams.get('genre') ? Number(searchParams.get('genre')) : undefined
  )
  const [year, setYear] = useState<string>(searchParams.get('year') ?? '')
  const [minRating, setMinRating] = useState<string>(searchParams.get('minRating') ?? '')
  const [sortBy, setSortBy] = useState<string>(searchParams.get('sort') ?? 'popularity.desc')

  const urlQuery = searchParams.get('q') ?? ''
  const [inputValue, setInputValue] = useState(urlQuery)

  const debouncedQuery = useDebounce(inputValue.trim(), 400)
  const isSearching = debouncedQuery.length >= 2

  // Resetear paginación cuando cambia la búsqueda efectiva o los filtros.
  // Usamos debouncedQuery (no inputValue) para que el reset solo ocurra cuando la query real
  // cambia, no en cada pulsación de teclado (evita bucles de reset → acumulación → sentinel).
  const prevSearchKey = useRef('')
  const currentSearchKey = `${debouncedQuery}|${filter}|${genreId}|${year}|${minRating}|${sortBy}`
  useEffect(() => {
    if (prevSearchKey.current !== currentSearchKey) {
      setPage(1)
      setAccumulatedItems([])
      prevSearchKey.current = currentSearchKey
    }
  }, [currentSearchKey])

  // Sincronizar todos los filtros a URL params
  useEffect(() => {
    const timeout = setTimeout(() => {
      const params: Record<string, string> = {}
      if (inputValue.trim()) params.q = inputValue.trim()
      if (filter !== 'ALL') params.type = filter
      if (genreId) params.genre = String(genreId)
      if (year) params.year = year
      if (minRating) params.minRating = minRating
      if (sortBy && sortBy !== 'popularity.desc') params.sort = sortBy
      setSearchParams(params, { replace: true })
    }, 400)
    return () => clearTimeout(timeout)
  }, [inputValue, filter, genreId, year, minRating, sortBy])

  // Parámetros de discover (solo cuando no buscando)
  const discoverParams: DiscoverParams = {
    genre: genreId,
    year: year ? Number(year) : undefined,
    minRating: minRating ? Number(minRating) : undefined,
    sortBy,
  }

  const hasActiveFilters =
    genreId !== undefined || !!year || !!minRating || (sortBy && sortBy !== 'popularity.desc')

  const activeFilterCount = [
    genreId !== undefined,
    !!year,
    !!minRating,
    sortBy && sortBy !== 'popularity.desc',
  ].filter(Boolean).length

  // ── Contenido ──────────────────────────────────────────────
  // MOVIE/TV: siempre discover; ALL: discover solo si hay filtros activos; si no → popular/trending
  const useDiscoverMode = !isSearching && (filter !== 'ALL' || !!hasActiveFilters)
  const discoverParamsWithPage = { ...discoverParams, page }
  const popular = usePopular(filter, page)
  const discover = useDiscover(filter, discoverParamsWithPage, useDiscoverMode)
  const search  = useSearch(debouncedQuery, filter, page)

  let contentQuery: { data: typeof popular.data; isLoading: boolean }
  if (isSearching) {
    contentQuery = search
  } else if (useDiscoverMode) {
    contentQuery = discover
  } else {
    contentQuery = popular
  }
  const { data: pageData = [], isLoading: contentLoading } = contentQuery

  // Acumular resultados de páginas anteriores
  useEffect(() => {
    if (pageData.length === 0) return
    if (page === 1) {
      setAccumulatedItems(pageData)
    } else {
      setAccumulatedItems((prev) => {
        const existingIds = new Set(prev.map((i) => `${i.tmdbId}-${i.contentType}`))
        const newItems = pageData.filter((i) => !existingIds.has(`${i.tmdbId}-${i.contentType}`))
        return [...prev, ...newItems]
      })
    }
  }, [pageData, page])

  const contentData = accumulatedItems
  const hasMorePages = pageData.length >= 20

  // Callback ref: el observer se crea al montar el sentinel y se destruye al desmontarlo.
  // El sentinel solo se renderiza cuando !contentLoading && hasMorePages && items > 0,
  // lo que garantiza que no hay loop: al disparar setPage, contentLoading pasa a true,
  // el sentinel desaparece del DOM, el observer se desconecta, y no puede volver a disparar
  // hasta que la siguiente página termine de cargar.
  const sentinelCallbackRef = useCallback((node: HTMLDivElement | null) => {
    if (observerRef.current) {
      observerRef.current.disconnect()
      observerRef.current = null
    }
    if (!node) return
    observerRef.current = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setPage((p) => p + 1)
        }
      },
      { rootMargin: '200px' }
    )
    observerRef.current.observe(node)
  }, [])

  // ── Géneros (cargados según el filtro activo) ──────────────
  const genres = useGenres(filter)

  // ── Usuarios ───────────────────────────────────────────────
  const { data: userResults = [], isLoading: usersSearchLoading } = useQuery({
    queryKey: queryKeys.users.search(debouncedQuery),
    queryFn: () => usersApi.search(debouncedQuery).then((r) => r.data),
    enabled: mode === 'users' && isSearching,
    staleTime: 1000 * 60 * 2,
  })

  const { data: suggestions = [], isLoading: suggestionsLoading } = useQuery({
    queryKey: queryKeys.users.suggestions(),
    queryFn: () => usersApi.getSuggestions().then((r) => r.data),
    enabled: isAuthenticated && mode === 'users' && !isSearching,
    staleTime: 1000 * 60 * 5,
  })

  const users = isSearching ? userResults : suggestions
  const usersLoading = isSearching ? usersSearchLoading : suggestionsLoading

  // ── Título de sección ──────────────────────────────────────
  const contentTitle = isSearching
    ? `Resultados para "${debouncedQuery}"`
    : filter === 'MOVIE' ? (hasActiveFilters ? 'Películas filtradas 🎬' : 'Películas populares 🎬')
    : filter === 'TV'    ? (hasActiveFilters ? 'Series filtradas 📺' : 'Series populares 📺')
    : hasActiveFilters   ? 'Resultados filtrados 🎬📺'
    : 'Tendencias ahora 🔥'

  const usersTitle = isSearching
    ? `Usuarios para "${debouncedQuery}"`
    : 'Sugerencias de usuarios 👥'

  const resetFilters = () => {
    setGenreId(undefined)
    setYear('')
    setMinRating('')
    setSortBy('popularity.desc')
    setPage(1)
    setAccumulatedItems([])
  }

  return (
    <div className="px-4 lg:px-8 py-6 lg:py-8 max-w-6xl mx-auto">
      <BackButton className="mb-5" />

      {/* Cabecera */}
      <div className="mb-8">
        <h1 className="font-display font-bold italic text-3xl mb-1">Descubrir</h1>
        <p className="text-sm text-muted">Busca películas, series o usuarios</p>
      </div>

      {/* Selector de modo */}
      <div className="flex gap-1 bg-bg-2 rounded-xl p-1 w-fit mb-5">
        {(['content', 'users'] as Mode[]).map((m) => (
          <button
            key={m}
            onClick={() => setMode(m)}
            className={cn(
              'px-4 py-1.5 rounded-lg text-sm font-medium transition-all',
              mode === m ? 'bg-accent text-bg-0 shadow-sm' : 'text-muted hover:text-white'
            )}
          >
            {m === 'content' ? '🎬 Contenido' : '👥 Usuarios'}
          </button>
        ))}
      </div>

      {/* Buscador */}
      <div className="mb-5">
        <SearchBar
          value={inputValue}
          onChange={setInputValue}
          placeholder={mode === 'content' ? 'Busca una película o serie…' : 'Busca un usuario…'}
          autoFocus={!!urlQuery}
        />
      </div>

      {/* Modo Contenido */}
      {mode === 'content' && (
        <>
          <div className="flex items-center justify-between mb-4 flex-wrap gap-3">
            <FilterTabs value={filter} onChange={(f) => { setFilter(f); setGenreId(undefined) }} />

            <div className="flex items-center gap-2">
              {/* Botón filtros avanzados */}
              {!isSearching && (
                <button
                  onClick={() => setFiltersOpen((p) => !p)}
                  className={cn(
                    'flex items-center gap-2 px-3 py-1.5 rounded-xl text-xs font-medium border transition-all',
                    filtersOpen || activeFilterCount > 0
                      ? 'bg-accent/15 border-accent/40 text-accent'
                      : 'border-white/[0.1] text-muted hover:text-white hover:border-white/[0.2]'
                  )}
                >
                  <span>⚙️ Filtros</span>
                  {activeFilterCount > 0 && (
                    <span className="bg-accent text-bg-0 text-[0.6rem] font-bold w-4 h-4 rounded-full flex items-center justify-center">
                      {activeFilterCount}
                    </span>
                  )}
                </button>
              )}

              {!contentLoading && accumulatedItems.length > 0 && (
                <p className="text-xs text-muted font-mono">
                  {accumulatedItems.length} resultado{accumulatedItems.length !== 1 ? 's' : ''}
                </p>
              )}
            </div>
          </div>

          {/* Panel de filtros avanzados */}
          {filtersOpen && !isSearching && (
            <div className="bg-bg-1 border border-white/[0.08] rounded-2xl p-4 mb-5 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
              {/* Género */}
              <div>
                <label className="block text-xs text-muted font-mono uppercase tracking-wider mb-1.5">
                  Género
                </label>
                <select
                  value={genreId ?? ''}
                  onChange={(e) => setGenreId(e.target.value ? Number(e.target.value) : undefined)}
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3 py-2 text-sm text-white outline-none focus:border-accent/50 transition-colors"
                >
                  <option value="">Todos</option>
                  {genres.map((g) => (
                    <option key={g.id} value={g.id}>{g.name}</option>
                  ))}
                </select>
              </div>

              {/* Año */}
              <div>
                <label className="block text-xs text-muted font-mono uppercase tracking-wider mb-1.5">
                  Año
                </label>
                <input
                  type="number"
                  value={year}
                  onChange={(e) => setYear(e.target.value)}
                  placeholder={`1950–${currentYear}`}
                  min={1950}
                  max={currentYear}
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3 py-2 text-sm text-white placeholder:text-muted outline-none focus:border-accent/50 transition-colors"
                />
              </div>

              {/* Puntuación mínima */}
              <div>
                <label className="block text-xs text-muted font-mono uppercase tracking-wider mb-1.5">
                  Nota mínima
                </label>
                <select
                  value={minRating}
                  onChange={(e) => setMinRating(e.target.value)}
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3 py-2 text-sm text-white outline-none focus:border-accent/50 transition-colors"
                >
                  {RATING_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>

              {/* Ordenar por */}
              <div>
                <label className="block text-xs text-muted font-mono uppercase tracking-wider mb-1.5">
                  Ordenar por
                </label>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="w-full bg-bg-2 border border-white/[0.1] rounded-xl px-3 py-2 text-sm text-white outline-none focus:border-accent/50 transition-colors"
                >
                  {SORT_OPTIONS.map((o) => (
                    <option key={o.value} value={o.value}>{o.label}</option>
                  ))}
                </select>
              </div>

              {/* Limpiar filtros */}
              {activeFilterCount > 0 && (
                <div className="sm:col-span-2 lg:col-span-4 flex justify-end">
                  <button
                    onClick={resetFilters}
                    className="text-xs text-muted hover:text-red-400 transition-colors"
                  >
                    × Limpiar filtros
                  </button>
                </div>
              )}
            </div>
          )}

          <h2 className="font-display font-bold italic text-xl mb-4">{contentTitle}</h2>

          {!contentLoading && contentData.length === 0 && (isSearching || hasActiveFilters) ? (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <span className="text-5xl mb-4">🔍</span>
              <h3 className="text-lg font-semibold text-white/80 mb-2">Sin resultados</h3>
              <p className="text-sm text-muted max-w-xs">
                {isSearching
                  ? `No encontramos nada para "${debouncedQuery}". Prueba con otro título.`
                  : 'No hay resultados con estos filtros. Prueba a ajustarlos.'}
              </p>
              {!isSearching && hasActiveFilters && (
                <button
                  onClick={resetFilters}
                  className="mt-4 text-sm text-accent hover:text-accent-light transition-colors"
                >
                  Limpiar filtros
                </button>
              )}
            </div>
          ) : (
            <>
              <ContentGrid items={contentData} isLoading={contentLoading && page === 1} />
              {/* Sentinel: solo existe cuando hay items, no estamos cargando y hay más páginas.
                  Al disparar setPage, contentLoading=true → desmonta → observer desconectado → sin loop */}
              {!contentLoading && accumulatedItems.length > 0 && hasMorePages && (
                <div ref={sentinelCallbackRef} className="h-1" />
              )}
            </>
          )}
        </>
      )}

      {/* Modo Usuarios */}
      {mode === 'users' && (
        <>
          <h2 className="font-display font-bold italic text-xl mb-4">{usersTitle}</h2>

          {usersLoading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {Array.from({ length: 6 }).map((_, i) => <UserCardSkeleton key={i} />)}
            </div>
          ) : !isAuthenticated && !isSearching ? (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <span className="text-5xl mb-4">👥</span>
              <h3 className="text-lg font-semibold text-white/80 mb-2">Inicia sesión para ver sugerencias</h3>
              <p className="text-sm text-muted max-w-xs">
                Puedes buscar usuarios por nombre sin estar registrado.
              </p>
            </div>
          ) : users.length === 0 && isSearching ? (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <span className="text-5xl mb-4">👤</span>
              <h3 className="text-lg font-semibold text-white/80 mb-2">Sin resultados</h3>
              <p className="text-sm text-muted max-w-xs">
                No encontramos usuarios con "{debouncedQuery}".
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {users.map((user) => <UserCard key={user.id} user={user} />)}
            </div>
          )}
        </>
      )}
    </div>
  )
}
