import { useState, useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useDebounce } from '@/hooks/useDebounce'
import { usePopular, useSearch } from '@/hooks/useDiscover'
import SearchBar from '@/components/SearchBar'
import FilterTabs from '@/components/FilterTabs'
import ContentGrid from '@/components/ContentGrid'
import type { ContentType } from '../../types'

type Filter = ContentType | 'ALL'

export default function DiscoverPage() {
  const [searchParams, setSearchParams] = useSearchParams()
  const [filter, setFilter] = useState<Filter>('ALL')

  // El query viene de la URL (?q=...) — así funciona desde la Topbar y es compartible
  const urlQuery = searchParams.get('q') ?? ''
  const [inputValue, setInputValue] = useState(urlQuery)

  // Sincroniza la URL cuando el usuario escribe
  // useEffect evita que cada tecla dispare una navegación
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

  // Debounce adicional para las queries al backend (evita peticiones en cada tecla)
  const debouncedQuery = useDebounce(inputValue.trim(), 400)
  const isSearching = debouncedQuery.length >= 2

  const popular = usePopular(filter)
  const search = useSearch(debouncedQuery, filter)

  const { data, isLoading } = isSearching ? search : popular

  const sectionTitle = isSearching
    ? `Resultados para "${debouncedQuery}"`
    : filter === 'MOVIE' ? 'Películas populares 🎬'
    : filter === 'TV'    ? 'Series populares 📺'
    : 'Tendencias ahora 🔥'

  return (
    <div className="px-6 py-8 max-w-350">
      {/* Cabecera */}
      <div className="mb-8">
        <h1 className="font-display font-bold italic text-3xl mb-1">Descubrir</h1>
        <p className="text-sm text-muted">Busca películas y series, o explora las tendencias</p>
      </div>

      {/* Buscador */}
      <div className="mb-5">
        <SearchBar
          value={inputValue}
          onChange={setInputValue}
          placeholder="Busca una película o serie…"
          autoFocus={!!urlQuery}
        />
      </div>

      {/* Filtros */}
      <div className="flex items-center justify-between mb-6 flex-wrap gap-3">
        <FilterTabs value={filter} onChange={setFilter} />
        {!isLoading && data.length > 0 && (
          <p className="text-xs text-muted font-mono">
            {data.length} resultado{data.length !== 1 ? 's' : ''}
          </p>
        )}
      </div>

      {/* Título de sección */}
      <h2 className="font-display font-bold italic text-xl mb-4">{sectionTitle}</h2>

      {/* Grid de resultados */}
      {!isLoading && data.length === 0 && isSearching ? (
        <div className="flex flex-col items-center justify-center py-24 text-center">
          <span className="text-5xl mb-4">🔍</span>
          <h3 className="text-lg font-semibold text-white/80 mb-2">Sin resultados</h3>
          <p className="text-sm text-muted max-w-xs">
            No encontramos nada para "{debouncedQuery}". Prueba con otro título.
          </p>
        </div>
      ) : (
        <ContentGrid items={data} isLoading={isLoading} />
      )}
    </div>
  )
}