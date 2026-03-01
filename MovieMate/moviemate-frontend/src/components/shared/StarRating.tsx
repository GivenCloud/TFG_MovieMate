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