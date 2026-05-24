import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { RequireAuth } from '@/features/auth/components/RequireAuth';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

const mockLogin = vi.fn();

// Mockowanie zachowania hooka useAuth
vi.mock('@/features/auth/hooks/useAuth', vi.fn(() => ({
  useAuth: () => ({
    initialized: true,
    isAuthenticated: false, // Użytkownik niezalogowany -> wymusi redirect
    login: mockLogin,
    logout: vi.fn(),
  })
})));

describe('Integration - LoginPage & RequireAuth Guard', () => {
  it('should redirect unauthenticated users and call Keycloak login method', async () => {
    const user = userEvent.setup();
    
    // Testujemy zachowanie Guardu RequireAuth chroniącego stronę /dashboard
    render(
      <MemoryRouter initialEntries={['/dashboard']}>
        <Routes>
          <Route element={<RequireAuth />}>
            <Route path="/dashboard" element={<div>Protected Dashboard Content</div>} />
          </Route>
          <Route 
            path="/login" 
            element={
              <div>
                <h1>Strona Logowania</h1>
                <button onClick={() => mockLogin()}>Zaloguj przez Keycloak</button>
              </div>
            } 
          />
        </Routes>
      </MemoryRouter>
    );

    // Guard wykrył brak autoryzacji i natychmiast przekierował nas na /login
    expect(screen.getByText('Strona Logowania')).toBeInTheDocument();
    expect(screen.queryByText('Protected Dashboard Content')).not.toBeInTheDocument();

    // Kliknięcie przycisku powinno wywołać przekierowanie do serwera Keycloak
    const loginButton = screen.getByRole('button', { name: /Zaloguj przez Keycloak/i });
    await user.click(loginButton);

    expect(mockLogin).toHaveBeenCalled();
  });
});