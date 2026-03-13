import { useState, useEffect } from 'react'
import { useSearchParams, useNavigate, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useDebounce } from '@/hooks/useDebounce'
import { usePopular, useSearch } from '@/hooks/useDiscover'
import SearchBar from '@/components/Discover/SearchBar'
import FilterTabs from '@/components/Discover/FilterTabs'
import ContentGrid from '@/components/Discover/ContentGrid'
import { usersApi } from '@/api/users'
import { queryKeys } from '@/lib/queryKeys'
import { cn } from '@/lib/utils'
import type { ContentType, UserResponse } from '../../types'

type Filter = ContentType | 'ALL'
type Mode = 'content' | 'users'

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
  const [searchParams, setSearchParams] = useSearchParams()
  const [filter, setFilter] = useState<Filter>('ALL')
  const [mode, setMode] = useState<Mode>('content')

  const urlQuery = searchParams.get('q') ?? ''
  const [inputValue, setInputValue] = useState(urlQuery)

  useEffect(() => {
    const timeout = setTimeout(() => {
      if (inputValue.trim()) {
        setSearchParams({ q: inputValue.trim() }, { replace: true })
      } else {
        setSearchParams({}, { replace: true })
      }
    }, 400)
    return () => clearTimeout(timeout)
  }, [inputValue])

  const debouncedQuery = useDebounce(inputValue.trim(), 400)
  const isSearching = debouncedQuery.length >= 2

  // ── Contenido ──────────────────────────────────────────────
  const popular = usePopular(filter)
  const search  = useSearch(debouncedQuery, filter)
  const { data: contentData = [], isLoading: contentLoading } = isSearching ? search : popular

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
    enabled: mode === 'users' && !isSearching,
    staleTime: 1000 * 60 * 5,
  })

  const users = isSearching ? userResults : suggestions
  const usersLoading = isSearching ? usersSearchLoading : suggestionsLoading

  // ── Título de sección ──────────────────────────────────────
  const contentTitle = isSearching
    ? `Resultados para "${debouncedQuery}"`
    : filter === 'MOVIE' ? 'Películas populares 🎬'
    : filter === 'TV'    ? 'Series populares 📺'
    : 'Tendencias ahora 🔥'

  const usersTitle = isSearching
    ? `Usuarios para "${debouncedQuery}"`
    : 'Sugerencias de usuarios 👥'

  return (
    <div className="px-4 lg:px-8 py-6 lg:py-8 max-w-6xl mx-auto">
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
          <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
            <FilterTabs value={filter} onChange={setFilter} />
            {!contentLoading && contentData.length > 0 && (
              <p className="text-xs text-muted font-mono">
                {contentData.length} resultado{contentData.length !== 1 ? 's' : ''}
              </p>
            )}
          </div>

          <h2 className="font-display font-bold italic text-xl mb-4">{contentTitle}</h2>

          {!contentLoading && contentData.length === 0 && isSearching ? (
            <div className="flex flex-col items-center justify-center py-24 text-center">
              <span className="text-5xl mb-4">🔍</span>
              <h3 className="text-lg font-semibold text-white/80 mb-2">Sin resultados</h3>
              <p className="text-sm text-muted max-w-xs">
                No encontramos nada para "{debouncedQuery}". Prueba con otro título.
              </p>
            </div>
          ) : (
            <ContentGrid items={contentData} isLoading={contentLoading} />
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
