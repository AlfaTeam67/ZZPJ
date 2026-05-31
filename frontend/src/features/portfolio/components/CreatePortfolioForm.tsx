import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { createPortfolio } from '../api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'

interface CreatePortfolioFormProps {
  onSuccess?: () => void
}

export function CreatePortfolioForm({ onSuccess }: CreatePortfolioFormProps = {}) {
  const { t } = useTranslation('portfolio')
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => createPortfolio({ name, description }),
    onSuccess: () => {
      setName('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
      onSuccess?.()
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    mutation.mutate()
  }

  return (
    <section className="rounded-2xl border border-border/40 bg-card/60 p-6">
      <h3 className="text-base font-semibold">{t('create-title')}</h3>
      <p className="mt-1 text-sm text-muted-foreground">{t('create-subtitle')}</p>

      <form onSubmit={handleSubmit} className="mt-4 space-y-4">
        <div className="space-y-2">
          <Label htmlFor="portfolio-name">{t('create-name-label')}</Label>
          <Input
            id="portfolio-name"
            placeholder={t('create-name-placeholder')}
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="portfolio-description">{t('create-description-label')}</Label>
          <Textarea
            id="portfolio-description"
            placeholder={t('create-description-placeholder')}
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </div>
        <Button type="submit" className="w-full" disabled={mutation.isPending}>
          {mutation.isPending ? t('create-pending') : t('create-button')}
        </Button>
        {mutation.isError && (
          <p className="text-sm text-destructive">
            {mutation.error instanceof Error ? mutation.error.message : t('create-error')}
          </p>
        )}
      </form>
    </section>
  )
}
