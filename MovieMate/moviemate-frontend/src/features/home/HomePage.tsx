import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { tmdbApi } from '../../api/tmdb'
import { queryKeys } from '../../lib/queryKeys'
import { toSlug } from '../../lib/utils'
import { useAuthStore } from '../../store/authStore'
import PosterCard from '@/components/shared/PosterdCard'
import AddToListButton from '@/components/Detail/AddToListButton'
import type { ContentResponse } from '../../types'

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

export default function HomePage() {
  const navigate = useNavigate()
  const { isAuthenticated } = useAuthStore()

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

  const isLoading = loadingMovies || loadingTv
  const featured: ContentResponse | undefined = popularMovies?.[0]
  const rest = [...(popularMovies?.slice(1) ?? []), ...(popularTv ?? [])]

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
      <div className="relative h-115 overflow-hidden">
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
          <div className="absolute right-20 top-8 -bottom-7.5 w-48 z-10">
            <img
              src={featured.posterUrl}
              alt={`Poster de ${featured.title}`}
              className="w-full h-full object-cover rounded-2xl shadow-[0_30px_80px_rgba(0,0,0,0.7)] border border-white/8"
            />
          </div>
        )}

        {/* CONTENIDO */}
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