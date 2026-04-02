import { AuthStatusCard } from '@/features/auth/components/AuthStatusCard'

export function AuthPage() {
  return (
    <div className="mx-auto max-w-xl space-y-6 py-8">
      <h1 className="text-2xl font-semibold tracking-tight">Authentication</h1>
      <AuthStatusCard />
    </div>
  )
}
