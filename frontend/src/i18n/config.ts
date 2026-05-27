import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import LanguageDetector from 'i18next-browser-languagedetector'

import commonPl from './locales/pl/common.json'
import navPl from './locales/pl/nav.json'
import authPl from './locales/pl/auth.json'
import portfolioPl from './locales/pl/portfolio.json'
import dashboardPl from './locales/pl/dashboard.json'
import marketPl from './locales/pl/market.json'
import advisorPl from './locales/pl/advisor.json'

import commonEn from './locales/en/common.json'
import navEn from './locales/en/nav.json'
import authEn from './locales/en/auth.json'
import portfolioEn from './locales/en/portfolio.json'
import dashboardEn from './locales/en/dashboard.json'
import marketEn from './locales/en/market.json'
import advisorEn from './locales/en/advisor.json'

export const LANGUAGES = ['pl', 'en'] as const
export type Language = (typeof LANGUAGES)[number]

export const resources = {
  pl: {
    common: commonPl,
    nav: navPl,
    auth: authPl,
    portfolio: portfolioPl,
    dashboard: dashboardPl,
    market: marketPl,
    advisor: advisorPl,
  },
  en: {
    common: commonEn,
    nav: navEn,
    auth: authEn,
    portfolio: portfolioEn,
    dashboard: dashboardEn,
    market: marketEn,
    advisor: advisorEn,
  },
} as const

i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources,
    fallbackLng: 'pl',
    detection: {
      order: ['localStorage', 'navigator'],
      lookupLocalStorage: 'fin-insight.lang',
      caches: ['localStorage'],
    },
    interpolation: { escapeValue: false },
    react: { useSuspense: false },
  })

export default i18n
