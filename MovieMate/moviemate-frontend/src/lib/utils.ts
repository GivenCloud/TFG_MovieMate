import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

// Combina clases de Tailwind de forma segura (requerido por shadcn)
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Construye la URL completa de una imagen de TMDB
export function getTmdbImage(
  path: string | null | undefined,
  size: 'w92' | 'w185' | 'w342' | 'w500' | 'w780' | 'w1280' | 'original' = 'w342'
): string | null {
  if (!path) return null
  return `${import.meta.env.VITE_TMDB_IMAGE_BASE}/${size}${path}`
}

// Formatea una fecha ISO a texto legible en español
export function formatDate(isoString: string): string {
  return new Intl.DateTimeFormat('es-ES', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  }).format(new Date(isoString))
}

// Formatea "hace X tiempo" relativo
export function timeAgo(isoString: string): string {
  const rtf = new Intl.RelativeTimeFormat('es', { numeric: 'auto' })
  const diff = (new Date(isoString).getTime() - Date.now()) / 1000
  const units: [Intl.RelativeTimeFormatUnit, number][] = [
    ['year', 31536000],
    ['month', 2592000],
    ['week', 604800],
    ['day', 86400],
    ['hour', 3600],
    ['minute', 60],
    ['second', 1],
  ]
  for (const [unit, seconds] of units) {
    if (Math.abs(diff) >= seconds) {
      return rtf.format(Math.round(diff / seconds), unit)
    }
  }
  return 'ahora mismo'
}

// Genera un slug URL-friendly a partir del título
// "The Dark Knight" → "the-dark-knight"
export function toSlug(title: string | null | undefined): string | null {
  if (!title) return null
  const slug = title
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/[^a-z0-9\s-]/g, ' ')
    .trim()
    .replace(/\s+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')

  // Mínimo 2 caracteres para considerar el slug válido
  return slug.length >= 2 ? slug : null
}

// Extrae el año de una fecha ISO
export function getYear(dateStr: string): string {
  return dateStr ? new Date(dateStr).getFullYear().toString() : '—'
}