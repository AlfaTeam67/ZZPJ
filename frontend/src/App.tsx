import { Navigate, Route, Routes } from 'react-router-dom'

import { AppLayout } from '@/components/layout/AppLayout'
import { RequireAuth } from '@/features/auth/components/RequireAuth'
import { AdvisorPage } from '@/pages/AdvisorPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { LoginPage } from '@/pages/LoginPage'
import { MarketPage } from '@/pages/MarketPage'
import { PortfolioPage } from '@/pages/PortfolioPage'
import { PortfolioDetailsPage } from '@/pages/PortfolioDetailsPage'
import { ProfilePage } from '@/pages/ProfilePage'
import { SettingsPage } from '@/pages/SettingsPage'
import { TransactionsPage } from '@/pages/TransactionsPage'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route index element={<DashboardPage />} />
          <Route path="portfolio" element={<PortfolioPage />} />
          <Route path="portfolio/:id" element={<PortfolioDetailsPage />} />
          <Route path="market" element={<MarketPage />} />
          <Route path="advisor" element={<AdvisorPage />} />
          <Route path="transactions" element={<TransactionsPage />} />
          <Route path="profile" element={<ProfilePage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
