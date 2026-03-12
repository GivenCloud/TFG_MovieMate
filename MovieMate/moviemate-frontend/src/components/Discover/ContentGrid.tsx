import PosterCard from '../shared/PosterdCard'
import type { ContentResponse } from '@/types'

interface Props {
  items: ContentResponse[]
  isLoading?: boolean
  skeletonCount?: number
}

function GridSkeleton({ count }: { count: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="w-36 shrink-0 animate-pulse">
          <div className="aspect-[2/3] rounded-xl bg-bg-3 mb-2" />
          <div className="h-3 bg-bg-3 rounded w-3/4 mb-1.5" />
          <div className="h-2.5 bg-bg-3 rounded w-1/2" />
        </div>
      ))}
    </>
  )
}

export default function ContentGrid({ items, isLoading, skeletonCount = 12 }: Props) {
  return (
    <div className="flex flex-wrap gap-3.5">
      {isLoading
        ? <GridSkeleton count={skeletonCount} />
        : items.map((item) => (
            <PosterCard key={`${item.contentType}-${item.tmdbId}`} content={item} />
          ))
      }
    </div>
  )
}