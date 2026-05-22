import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { createPortfolio } from '../api'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

export function CreatePortfolioForm() {
  const [name, setName] = useState('')
  const [description, setDescription] = useState('')
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => createPortfolio({ name, description }),
    onSuccess: () => {
      setName('')
      setDescription('')
      queryClient.invalidateQueries({ queryKey: ['portfolios'] })
    },
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!name.trim()) return
    mutation.mutate()
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>Create New Portfolio</CardTitle>
        <CardDescription>Add a new portfolio to track your assets.</CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name">Portfolio Name</Label>
            <Input
              id="name"
              placeholder="e.g. Retirement Fund"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="description">Description (Optional)</Label>
            <Textarea
              id="description"
              placeholder="Describe your goals..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <Button type="submit" className="w-full" disabled={mutation.isPending}>
            {mutation.isPending ? 'Creating...' : 'Create Portfolio'}
          </Button>
          {mutation.isError && (
            <p className="text-sm text-destructive mt-2">
              Error:{' '}
              {mutation.error instanceof Error
                ? mutation.error.message
                : 'Failed to create portfolio'}
            </p>
          )}
        </form>
      </CardContent>
    </Card>
  )
}
