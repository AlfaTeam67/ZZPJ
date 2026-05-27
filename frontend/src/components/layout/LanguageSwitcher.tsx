import { LANGUAGES } from '@/i18n/config'
import { useLanguage } from '@/i18n/hooks/useLanguage'
import { cn } from '@/lib/utils'

export function LanguageSwitcher({ className }: { className?: string }) {
  const { language, setLanguage } = useLanguage()

  return (
    <div
      className={cn(
        'flex items-center gap-0.5 rounded-full border border-border/40 bg-muted/30 p-0.5',
        className
      )}
      role="radiogroup"
      aria-label="Language"
    >
      {LANGUAGES.map((lng) => (
        <button
          key={lng}
          type="button"
          role="radio"
          aria-checked={language === lng}
          onClick={() => setLanguage(lng)}
          className={cn(
            'rounded-full px-2.5 py-1 text-[11px] font-medium uppercase tracking-wide transition-colors',
            language === lng
              ? 'bg-background text-foreground shadow-sm'
              : 'text-muted-foreground hover:text-foreground'
          )}
        >
          {lng}
        </button>
      ))}
    </div>
  )
}
