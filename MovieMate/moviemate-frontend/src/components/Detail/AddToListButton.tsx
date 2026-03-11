import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { listsApi } from '@/api/lists'
import { queryKeys } from '@/lib/queryKeys'
import { cn } from '@/lib/utils'
import type { ContentResponse } from '@/types'

interface Props {
  content: ContentResponse
}

export default function AddToListButton({ content }: Props) {
  const [open, setOpen] = useState(false)
  const queryClient = useQueryClient()

  const { data: lists } = useQuery({
    queryKey: queryKeys.lists.mine(),
    queryFn: () => listsApi.getMine(),
    select: (res) => res.data,
    enabled: open,       // solo carga cuando abre el dropdown
  })

  const { mutate, isPending } = useMutation({
    mutationFn: (listId: number) =>
      listsApi.addContent(listId, {
        tmdbId: content.tmdbId,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.lists.mine() })
      toast.success('Añadido a la lista')
      setOpen(false)
    },
    onError: (err: any) => {
      toast.error(err?.response?.data?.message || 'Error al añadir a la lista')
    },
  })

  return (
    <div className="relative">
      <button
        onClick={() => setOpen((v) => !v)}
        className="flex items-center gap-2 bg-white/[0.08] hover:bg-white/[0.14] border border-white/[0.1] text-white font-medium text-sm px-5 py-2.5 rounded-xl transition-colors"
      >
        📋 Añadir a lista
      </button>

      {open && (
        <>
          {/* Overlay para cerrar al hacer clic fuera */}
          <div
            className="fixed inset-0 z-10"
            onClick={() => setOpen(false)}
          />
          <div className="absolute left-0 top-full mt-2 w-56 bg-bg-2 border border-white/10 rounded-xl shadow-xl z-20 overflow-hidden">
            {!lists ? (
              <p className="px-4 py-3 text-sm text-muted">Cargando listas…</p>
            ) : lists.length === 0 ? (
              <p className="px-4 py-3 text-sm text-muted">No tienes listas creadas</p>
            ) : (
              lists.map((list) => (
                <button
                  key={list.id}
                  onClick={() => mutate(list.id)}
                  disabled={isPending}
                  className={cn(
                    'w-full text-left px-4 py-3 text-sm transition-colors flex items-center gap-2',
                    'hover:bg-bg-3 text-white/80 hover:text-white'
                  )}
                >
                  <span className="text-accent">📋</span>
                  <span className="truncate">{list.name}</span>
                  <span className="ml-auto text-xs text-muted font-mono">
                    {list.itemCount}
                  </span>
                </button>
              ))
            )}
          </div>
        </>
      )}
    </div>
  )
}