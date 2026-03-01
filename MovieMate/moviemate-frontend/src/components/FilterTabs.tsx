import { cn } from '@/lib/utils'
import type { ContentType } from '@/types'

type Filter = ContentType | 'ALL'

interface Props {
  value: Filter
  onChange: (filter: Filter) => void
}

const TABS: { label: string; value: Filter }[] = [
  { label: 'Todo',      value: 'ALL' },
  { label: 'Películas', value: 'MOVIE' },
  { label: 'Series',    value: 'TV' },
]

export default function FilterTabs({ value, onChange }: Props) {
  return (
    <div className="flex gap-1 bg-bg-2 rounded-xl p-1 w-fit">
      {TABS.map((tab) => (
        <button
          key={tab.value}
          onClick={() => onChange(tab.value)}
          className={cn(
            'px-4 py-1.5 rounded-lg text-sm font-medium transition-all',
            value === tab.value
              ? 'bg-accent text-bg-0 shadow-sm'
              : 'text-muted hover:text-white'
          )}
        >
          {tab.label}
        </button>
      ))}
    </div>
  )
}