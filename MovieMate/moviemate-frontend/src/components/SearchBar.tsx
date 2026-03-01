import { useRef } from 'react'
import { cn } from '@/lib/utils'

interface Props {
  value: string
  onChange: (value: string) => void
  placeholder?: string
  autoFocus?: boolean
}

export default function SearchBar({ value, onChange, placeholder = 'Buscar...', autoFocus }: Props) {
  const inputRef = useRef<HTMLInputElement>(null)

  return (
    <div className={cn(
      'flex items-center gap-3 bg-bg-2 border rounded-2xl px-4 py-3 transition-all',
      value
        ? 'border-accent/40 shadow-[0_0_0_3px_rgba(232,201,122,0.08)]'
        : 'border-white/8 hover:border-white/20'
    )}>
      <span className="text-muted text-lg shrink-0">🔍</span>
      <input
        ref={inputRef}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        autoFocus={autoFocus}
        className="flex-1 bg-transparent outline-none text-white placeholder:text-muted text-base"
        aria-label="Buscar contenido"
      />
      {value && (
        <button
          onClick={() => { onChange(''); inputRef.current?.focus() }}
          className="text-muted hover:text-white transition-colors text-lg shrink-0"
          aria-label="Limpiar búsqueda"
        >
          ✕
        </button>
      )}
    </div>
  )
}