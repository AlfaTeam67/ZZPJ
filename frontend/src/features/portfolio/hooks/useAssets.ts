import { usePortfolio } from '@/features/portfolio/hooks/usePortfolio'

export function useAssets(portfolioId: string | null) {
  const portfolioQuery = usePortfolio()

  if (!portfolioId) {
    return {
      ...portfolioQuery,
      data: [],
    }
  }

  // Derive assets from the current portfolio
  return {
    ...portfolioQuery,
    data: portfolioQuery.data?.assets || [],
  }
}
