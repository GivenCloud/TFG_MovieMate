import { useState } from 'react'
import { toast } from 'sonner'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '../../components/ui/dialog'
import { useCreateReport } from '../../hooks/useAdmin'
import type { TargetType, ReportReason } from '../../api/admin'

interface Props {
  open: boolean
  onClose: () => void
  targetType: TargetType
  targetId: number
}

const REASONS: { value: ReportReason; label: string }[] = [
  { value: 'SPAM',          label: '🚫 Spam' },
  { value: 'INAPPROPRIATE', label: '⚠️ Contenido inapropiado' },
  { value: 'SPOILER',       label: '🎭 Spoiler sin marcar' },
  { value: 'OTHER',         label: '📝 Otro' },
]

export default function ReportDialog({ open, onClose, targetType, targetId }: Props) {
  const [reason, setReason] = useState<ReportReason>('SPAM')
  const { mutate: createReport, isPending } = useCreateReport()

  const handleSubmit = () => {
    createReport(
      { targetType, targetId, reason },
      {
        onSuccess: () => {
          toast.success('Reporte enviado. Lo revisaremos pronto.')
          onClose()
        },
        onError: () => toast.error('No se pudo enviar el reporte.'),
      }
    )
  }

  return (
    <Dialog open={open} onOpenChange={(v) => !v && onClose()}>
      <DialogContent className="bg-bg-1 border-white/[0.1] text-white max-w-sm">
        <DialogHeader>
          <DialogTitle className="font-display font-bold italic text-xl">
            Reportar contenido
          </DialogTitle>
        </DialogHeader>

        <p className="text-sm text-muted mb-3">
          Selecciona el motivo del reporte. Lo revisaremos y tomaremos las medidas necesarias.
        </p>

        <div className="space-y-2">
          {REASONS.map((r) => (
            <label
              key={r.value}
              className={`flex items-center gap-3 p-3 rounded-xl border cursor-pointer transition-colors ${
                reason === r.value
                  ? 'border-accent bg-accent/[0.06] text-white'
                  : 'border-white/[0.08] hover:border-white/20 text-muted'
              }`}
            >
              <input
                type="radio"
                name="reason"
                value={r.value}
                checked={reason === r.value}
                onChange={() => setReason(r.value)}
                className="hidden"
              />
              <span className="text-sm">{r.label}</span>
            </label>
          ))}
        </div>

        <DialogFooter className="mt-4 gap-2">
          <button
            onClick={onClose}
            className="px-4 py-2 text-sm text-muted hover:text-white border border-white/[0.1] rounded-xl transition-colors"
          >
            Cancelar
          </button>
          <button
            onClick={handleSubmit}
            disabled={isPending}
            className="px-4 py-2 text-sm font-medium bg-red-500 text-white rounded-xl hover:bg-red-600 disabled:opacity-40 transition-colors"
          >
            {isPending ? 'Enviando…' : 'Reportar'}
          </button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
