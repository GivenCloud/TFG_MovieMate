import { useQuery } from '@tanstack/react-query'
import { useNavigate, Link } from 'react-router-dom'
import { useRef, useState, useCallback } from 'react'
import { tmdbApi } from '../../api/tmdb'
import { usersApi } from '../../api/users'
import { queryKeys } from '../../lib/queryKeys'
import { toSlug } from '../../lib/utils'
import { useAuthStore } from '../../store/authStore'
import PosterCard from '@/components/shared/PosterdCard'
import AddToListButton from '@/components/Detail/AddToListButton'
import type { ContentResponse, UserResponse } from '../../types'

function PosterSkeleton() {
  return (
    <div className="shrink-0 w-36 animate-pulse">
      <div className="aspect-2/3 rounded-xl bg-bg-3 mb-2" />
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

function ContentCarousel({ items, loading }: { items: ContentResponse[]; loading: boolean }) {
  const scrollRef = useRef<HTMLDivElement>(null)
  const [canScrollLeft, setCanScrollLeft] = useState(false)
  const [canScrollRight, setCanScrollRight] = useState(true)

  const updateScrollState = useCallback(() => {
    const el = scrollRef.current
    if (!el) return
    setCanScrollLeft(el.scrollLeft > 4)
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 4)
  }, [])

  const scroll = (dir: 'left' | 'right') => {
    const el = scrollRef.current
    if (!el) return
    el.scrollBy({ left: dir === 'right' ? 320 : -320, behavior: 'smooth' })
  }

  return (
    <div className="relative group/carousel">
      {/* Flecha izquierda */}
      <button
        onClick={() => scroll('left')}
        disabled={!canScrollLeft}
        aria-label="Desplazar a la izquierda"
        className="absolute left-0 top-1/2 -translate-y-1/2 z-10 w-9 h-9 flex items-center justify-center rounded-full bg-bg-0/90 border border-white/[0.1] text-white shadow-lg transition-all
          disabled:opacity-25 disabled:cursor-not-allowed
          enabled:hover:bg-bg-2 enabled:hover:border-white/20 enabled:hover:scale-105"
      >
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M15 19l-7-7 7-7" />
        </svg>
      </button>

      {/* Carrusel */}
      <div
        ref={scrollRef}
        onScroll={updateScrollState}
        className="flex gap-3.5 overflow-x-auto scrollbar-none pb-1 px-10"
      >
        {loading
          ? Array.from({ length: 7 }).map((_, i) => <PosterSkeleton key={i} />)
          : items.map((item) => (
              <PosterCard key={`${item.tmdbId}-${item.contentType}`} content={item} />
            ))
        }
      </div>

      {/* Flecha derecha */}
      <button
        onClick={() => scroll('right')}
        disabled={!canScrollRight}
        aria-label="Desplazar a la derecha"
        className="absolute right-0 top-1/2 -translate-y-1/2 z-10 w-9 h-9 flex items-center justify-center rounded-full bg-bg-0/90 border border-white/[0.1] text-white shadow-lg transition-all
          disabled:opacity-25 disabled:cursor-not-allowed
          enabled:hover:bg-bg-2 enabled:hover:border-white/20 enabled:hover:scale-105"
      >
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
          <path strokeLinecap="round" strokeLinejoin="round" d="M9 5l7 7-7 7" />
        </svg>
      </button>
    </div>
  )
}

function SuggestedUserCard({ user }: { user: UserResponse }) {
  return (
    <Link
      to={`/profile/${user.username}`}
      className="flex items-center gap-3 px-4 py-2.5 hover:bg-white/[0.04] rounded-xl transition-colors group"
    >
      <div className="w-9 h-9 rounded-full shrink-0 overflow-hidden border border-white/[0.08]">
        {user.avatarUrl ? (
          <img src={user.avatarUrl} alt={user.username} className="w-full h-full object-cover" />
        ) : (
          <div className="w-full h-full bg-gradient-to-br from-accent/70 to-purple-500/70 flex items-center justify-center text-xs font-bold text-bg-0">
            {user.username[0].toUpperCase()}
          </div>
        )}
      </div>
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-white/85 truncate group-hover:text-accent transition-colors">
          @{user.username}
        </p>
        {user.bio && (
          <p className="text-[0.65rem] text-muted truncate">{user.bio}</p>
        )}
      </div>
    </Link>
  )
}

export default function HomePage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()

  const { data: trending, isLoading: loadingTrending } = useQuery({
    queryKey: queryKeys.tmdb.trending(),
    queryFn: () => tmdbApi.getTrending(),
    select: (res) => res.data,
    staleTime: 1000 * 60 * 10,
  })

  const { data: popularMovies, isLoading: loadingMovies } = useQuery({
    queryKey: queryKeys.tmdb.popularMovies(),
    queryFn: () => tmdbApi.getPopularMovies(),
    select: (res) => res.data,
    staleTime: 1000 * 60 * 10,
  })

  const { data: popularTv, isLoading: loadingTv } = useQuery({
    queryKey: queryKeys.tmdb.popularTv(),
    queryFn: () => tmdbApi.getPopularTvShows(),
    select: (res) => res.data,
    staleTime: 1000 * 60 * 10,
  })

  const { data: suggestions = [] } = useQuery({
    queryKey: queryKeys.users.suggestions(),
    queryFn: () => usersApi.getSuggestions().then((r) => r.data),
    enabled: isAuthenticated,
    staleTime: 1000 * 60 * 10,
  })

  const { data: recommendations = [], isLoading: loadingRecs } = useQuery({
    queryKey: queryKeys.users.recommendations(),
    queryFn: () => usersApi.getMyRecommendations().then((r) => r.data),
    enabled: isAuthenticated,
    staleTime: 1000 * 60 * 30,
  })

  const featured: ContentResponse | undefined = trending?.[0] ?? popularMovies?.[0]
  const trendingRest = (trending ?? []).slice(1).filter((item) => item.tmdbId && item.contentType)
  const popularMoviesFiltered = (popularMovies ?? []).filter((item) => item.tmdbId && item.contentType)
  const popularTvFiltered = (popularTv ?? []).filter((item) => item.tmdbId && item.contentType)

  const featuredUrl = featured
    ? (() => {
        const slug = toSlug(featured.title)
        return slug
          ? `/content/${featured.contentType}/${featured.tmdbId}/${slug}`
          : `/content/${featured.contentType}/${featured.tmdbId}`
      })()
    : null

  return (
    <div className="pb-10">
      {/* ── HERO  ── */}
      <div className="relative h-72 lg:h-115 overflow-hidden">
        {featured?.backdropUrl ? (
          // backgroundImage + filter CSS 
          <div 
            className="absolute inset-0 w-full h-full object-cover object-top scale-105"
            style={{
              backgroundImage: `url(${featured.backdropUrl})`,
              filter: 'brightness(0.28) saturate(1.3)',
            }}
          />
        ) : (
          <div className="absolute inset-0 bg-linear-to-br from-[#1a0a2e] to-[#0a1628]" />
        )}

        
        <div className="absolute inset-0 bg-linear-to-r from-bg-0 via-bg-0/85 to-transparent" />
        <div className="absolute inset-0 bg-linear-to-t from-bg-0 via-transparent to-transparent" />
        
        {/* Diagonal cut */}
        <div
          className="absolute -bottom-px left-0 right-0 h-20 bg-bg-0"
          style={{ clipPath: 'polygon(0 60%, 100% 0%, 100% 100%, 0% 100%)' }}
        />

        {/* Poster */}
        {featured?.posterUrl && (
          <div className="hidden lg:block absolute right-20 top-8 -bottom-7.5 w-48 z-10">
            <img
              src={featured.posterUrl}
              alt={`Poster de ${featured.title}`}
              className="w-full h-full object-cover rounded-2xl shadow-[0_30px_80px_rgba(0,0,0,0.7)] border border-white/8"
            />
          </div>
        )}

        {/* CONTENIDO */}
        <div className="relative z-10 h-full flex flex-col justify-end px-4 lg:px-9 pb-6 lg:pb-10 max-w-lg">
          <div className="inline-flex items-center gap-1.5 bg-accent/10 border border-accent/30 text-accent text-xs font-semibold px-2.5 py-1 rounded-full mb-3 w-fit">
            ✨ Tendencia esta semana
          </div>

          {featured ? (
            <>
              <h1 className="font-display font-bold text-2xl lg:text-[2.8rem] leading-[1.05] tracking-tight mb-2.5">
                {featured.title}
              </h1>
              <div className="flex items-center gap-2.5 text-sm text-white/70 mb-5 flex-wrap">
                <span className="flex items-center gap-1 bg-yellow-400/15 text-yellow-400 font-bold text-xs px-2 py-0.5 rounded">
                  ⭐ {featured.tmdbRating?.toFixed(1) ?? '—'}
                </span>
                <span className="text-white/30">·</span>
                <span>{featured.releaseDate ? new Date(featured.releaseDate).getFullYear() : '—'}</span>
                <span className="text-white/30">·</span>
                <span>{featured.contentType === 'MOVIE' ? 'Película' : 'Serie'}</span>
              </div>
            </>
          ) : (
            <HeroSkeleton />
          )}

          <div className="flex gap-2.5 flex-wrap items-center">
            {featured && (
              isAuthenticated
                ? <AddToListButton content={featured} />
                : (
                  <button
                    onClick={() => navigate('/login')}
                    className="flex items-center gap-2 bg-white/[0.08] hover:bg-white/[0.14] border border-white/[0.1] text-white font-medium text-sm px-5 py-2.5 rounded-xl transition-colors"
                  >
                    📋 Añadir a lista
                  </button>
                )
            )}
            <button
              onClick={() => featuredUrl && navigate(featuredUrl, { state: { content: featured } })}
              disabled={!featuredUrl}
              className="bg-white/8 hover:bg-white/[0.14] border border-white/10 text-white font-medium text-sm px-4 py-2 rounded-xl transition-colors disabled:opacity-50"
            >
              ⭐ Valorar
            </button>
            <button
              onClick={() => featuredUrl && navigate(featuredUrl, { state: { content: featured } })}
              disabled={!featuredUrl}
              className="bg-transparent hover:bg-white/6 border border-white/10 text-white/70 font-medium text-sm px-4 py-2 rounded-xl transition-colors disabled:opacity-50"
            >
              Ver ficha →
            </button>
          </div>
        </div>
      </div>

      {/* ── TENDENCIAS ── */}
      <section className="px-4 lg:px-6 pt-8">
        <div className="flex items-baseline justify-between mb-4">
          <h2 className="font-display font-bold italic text-xl">Tendencias esta semana 🔥</h2>
        </div>
        <ContentCarousel items={trendingRest} loading={loadingTrending} />
      </section>

      {/* ── PELÍCULAS POPULARES ── */}
      <section className="px-4 lg:px-6 pt-8">
        <div className="flex items-baseline justify-between mb-4">
          <h2 className="font-display font-bold italic text-xl">Películas populares 🎬</h2>
        </div>
        <ContentCarousel items={popularMoviesFiltered} loading={loadingMovies} />
      </section>

      {/* ── SERIES POPULARES ── */}
      <section className="px-4 lg:px-6 pt-8">
        <div className="flex items-baseline justify-between mb-4">
          <h2 className="font-display font-bold italic text-xl">Series populares 📺</h2>
        </div>
        <ContentCarousel items={popularTvFiltered} loading={loadingTv} />
      </section>

      {/* ── PARA TI ── */}
      {isAuthenticated && (recommendations.length > 0 || loadingRecs) && (
        <section className="px-4 lg:px-6 pt-8">
          <div className="flex items-baseline justify-between mb-4">
            <h2 className="font-display font-bold italic text-xl">Para ti ✨</h2>
          </div>
          <ContentCarousel items={recommendations} loading={loadingRecs} />
        </section>
      )}

      {/* ── USUARIOS SUGERIDOS ── */}
      {isAuthenticated && suggestions.length > 0 && (
        <section className="px-4 lg:px-6 pt-8">
          <div className="flex items-baseline justify-between mb-3">
            <h2 className="font-display font-bold italic text-xl">Cinéfilos que quizás conozcas 👥</h2>
            <Link to="/discover?mode=users" className="text-sm text-accent hover:text-accent-light transition-colors">
              Ver más →
            </Link>
          </div>
          <div className="bg-bg-1 border border-white/[0.06] rounded-2xl overflow-hidden divide-y divide-white/[0.04]">
            {suggestions.slice(0, 5).map((u) => (
              <SuggestedUserCard key={u.id} user={u} />
            ))}
          </div>
        </section>
      )}
    </div>
  )
}