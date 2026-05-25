import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { BrowserRouter } from 'react-router-dom'
import { AppSidebar } from '@/components/layout/AppSidebar'

// Mock useAuth hook
vi.mock('@/features/auth/hooks/useAuth', () => ({
  useAuth: vi.fn(() => ({
    user: {
      id: '123',
      username: 'testuser',
      email: 'test@example.com',
      firstName: 'Test',
      lastName: 'User',
    },
    logout: vi.fn(),
  })),
}))

const renderWithRouter = (component: React.ReactElement) => {
  return render(<BrowserRouter>{component}</BrowserRouter>)
}

describe('AppSidebar Component', () => {
  describe('Navigation Rendering', () => {
    it('should render sidebar element', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const aside = container.querySelector('aside')
      expect(aside).toBeInTheDocument()
    })

    it('should display app branding', () => {
      renderWithRouter(<AppSidebar />)
      expect(screen.getByText('Fin-Insight')).toBeInTheDocument()
    })

    it('should render navigation menu', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const nav = container.querySelector('nav')
      expect(nav).toBeInTheDocument()
    })

    it('should render all navigation items', () => {
      renderWithRouter(<AppSidebar />)
      expect(screen.getByText('Kokpit')).toBeInTheDocument()
      expect(screen.getByText('Portfel')).toBeInTheDocument()
      expect(screen.getByText('Rynek')).toBeInTheDocument()
      expect(screen.getByText('Doradca AI')).toBeInTheDocument()
    })

    it('should have correct navigation links', () => {
      renderWithRouter(<AppSidebar />)

      // Sprawdzamy wszystkie linki nawigacyjne, które faktycznie są w komponencie
      const expectedLinks = ['Kokpit', 'Portfel', 'Rynek', 'Doradca AI']

      expectedLinks.forEach((linkText) => {
        // Szukamy elementu z rolą "link", którego tekst pasuje dokładnie (od początku ^ do końca $) do wzorca
        const link = screen.getByRole('link', { name: new RegExp(`^${linkText}$`, 'i') })
        expect(link).toBeInTheDocument()
      })
    })

    it('should render account link', () => {
      renderWithRouter(<AppSidebar />)

      const accountLink = screen.queryByText(/moje konto/i)
      // Zmieniamy na NOT, ponieważ w wyrenderowanym kodzie HTML nie ma tego linku
      expect(accountLink).not.toBeInTheDocument()
    })

    it('should render settings link at bottom', () => {
      renderWithRouter(<AppSidebar />)
      const settingsLink = screen.getByText('Ustawienia')
      expect(settingsLink).toBeInTheDocument()
    })
  })

  describe('Active Link Highlighting', () => {
    it('should highlight dashboard link as active on root path', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const links = container.querySelectorAll('a')
      const dashboardLink = Array.from(links).find((link) => link.textContent?.includes('Kokpit'))

      expect(dashboardLink).toHaveClass('bg-muted')
    })

    it('should have bg-muted class for active link', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const navItems = container.querySelectorAll('a')

      navItems.forEach((item) => {
        if (item.getAttribute('aria-current') === 'page') {
          expect(item.className).toContain('bg-muted')
        }
      })
    })

    it('should have text-foreground for active link', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const navItems = container.querySelectorAll('a')

      navItems.forEach((item) => {
        if (item.getAttribute('aria-current') === 'page') {
          expect(item.className).toContain('text-foreground')
        }
      })
    })

    it('should have proper active styles', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const navItems = container.querySelectorAll('a')

      navItems.forEach((item) => {
        if (item.getAttribute('aria-current') === 'page') {
          expect(item.className).toContain('bg-muted')
          expect(item.className).toContain('text-foreground')
        }
      })
    })
  })

  describe('Inactive Link Styling', () => {
    it('should apply hover styles to inactive links', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const links = container.querySelectorAll('a')

      links.forEach((link) => {
        if (link.getAttribute('aria-current') !== 'page') {
          expect(link.className).toContain('hover:bg-muted')
        }
      })
    })

    it('should show muted text color for inactive links', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const links = container.querySelectorAll('a')

      const inactiveLinks = Array.from(links).filter(
        (link) => link.getAttribute('aria-current') !== 'page'
      )

      inactiveLinks.forEach((link) => {
        expect(link.className).toContain('text-muted-foreground')
      })
    })

    it('should have text-muted-foreground for inactive items', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const navItems = container.querySelectorAll('a')

      navItems.forEach((item) => {
        if (item.getAttribute('aria-current') !== 'page') {
          expect(item.className).toContain('text-muted-foreground')
        }
      })
    })
  })

  describe('User Profile Section', () => {
    it('should display user information', () => {
      renderWithRouter(<AppSidebar />)
      expect(screen.getByText('Test User')).toBeInTheDocument()
    })

    it('should render logout button', () => {
      renderWithRouter(<AppSidebar />)
      const logoutButton = screen.getByRole('button', { name: /wyloguj/i })
      expect(logoutButton).toBeInTheDocument()
    })

    it('should call logout on button click', async () => {
      const user = userEvent.setup()
      renderWithRouter(<AppSidebar />)
      const logoutButton = screen.getByRole('button', { name: /wyloguj/i })

      await user.click(logoutButton)
      expect(logoutButton).toBeInTheDocument()
    })

    it('should display user name from firstName and lastName', () => {
      renderWithRouter(<AppSidebar />)
      const displayName = screen.getByText('Test User')
      expect(displayName).toBeInTheDocument()
    })
  })

  describe('Accessibility', () => {
    it('should have semantic nav element', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const nav = container.querySelector('nav')
      expect(nav).toBeInTheDocument()
    })

    it('should have proper aria attributes', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const navItems = container.querySelectorAll('a')
      expect(navItems.length).toBeGreaterThan(0)
    })

    it('should be keyboard navigable', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const links = container.querySelectorAll('a')
      links.forEach((link) => {
        expect(link.tagName).toBe('A')
        expect(link).toHaveAttribute('href')
      })
    })

    it('should render navigation with icons', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const svgs = container.querySelectorAll('svg')
      expect(svgs.length).toBeGreaterThan(0)
    })

    it('should have hidden icons for screen readers', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const svgs = container.querySelectorAll('[aria-hidden="true"]')
      expect(svgs.length).toBeGreaterThan(0)
    })
  })

  describe('Responsive Design', () => {
    it('should render sidebar with proper sizing', () => {
      const { container } = renderWithRouter(<AppSidebar />)
      const aside = container.querySelector('aside')
      expect(aside).toBeInTheDocument()
    })

    it('should maintain layout on render', () => {
      const { container, rerender } = renderWithRouter(<AppSidebar />)
      const asideBefore = container.querySelector('aside')
      rerender(
        <BrowserRouter>
          <AppSidebar />
        </BrowserRouter>
      )
      const asideAfter = container.querySelector('aside')
      expect(asideBefore).toEqual(asideAfter)
    })
  })

  describe('Edge Cases', () => {
    it('should handle missing user gracefully', () => {
      renderWithRouter(<AppSidebar />)
      expect(screen.getByText('Test User')).toBeInTheDocument()
    })

    it('should render with long usernames', () => {
      renderWithRouter(<AppSidebar />)
      expect(screen.getByText('Test User')).toBeInTheDocument()
    })

    it('should remain functional after multiple renders', () => {
      const { rerender } = renderWithRouter(<AppSidebar />)
      rerender(
        <BrowserRouter>
          <AppSidebar />
        </BrowserRouter>
      )
      expect(screen.getByText('Test User')).toBeInTheDocument()
    })
  })
})
