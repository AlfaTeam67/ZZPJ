import { useTranslation } from 'react-i18next'
import { useCallback } from 'react'

import { LANGUAGES, type Language } from '../config'

export function useLanguage() {
  const { i18n } = useTranslation()

  const language = (
    LANGUAGES.includes(i18n.language as Language) ? i18n.language : 'pl'
  ) as Language

  const setLanguage = useCallback(
    (lng: Language) => {
      void i18n.changeLanguage(lng)
    },
    [i18n]
  )

  const locale = language === 'pl' ? 'pl-PL' : 'en-US'

  return { language, setLanguage, locale } as const
}
