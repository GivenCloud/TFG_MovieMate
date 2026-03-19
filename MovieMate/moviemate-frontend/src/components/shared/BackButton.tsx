import { useNavigate } from 'react-router-dom'

interface Props {
  to?: string        // ruta fija; si no se pasa, usa navigate(-1)
  label?: string
  className?: string
}

export default function BackButton({ to, label = 'Atrás', className = '' }: Props) {
  const navigate = useNavigate()

  const handleClick = () => {
    if (to) {
      navigate(to)
    } else {
      navigate(-1)
    }
  }

  return (
    <button
      onClick={handleClick}
      className={`flex items-center gap-1.5 text-sm text-muted hover:text-white transition-colors ${className}`}
    >
      <svg
        xmlns="http://www.w3.org/2000/svg"
        className="w-4 h-4"
        fill="none"
        viewBox="0 0 24 24"
        stroke="currentColor"
        strokeWidth={2}
      >
        <path strokeLinecap="round" strokeLinejoin="round" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
      </svg>
      {label}
    </button>
  )
}
