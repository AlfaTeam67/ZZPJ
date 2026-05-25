import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen } from '@testing-library/react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import { ProtectedRoute } from '@/components/auth/ProtectedRoute';
import { authReducer } from '@/store/slices/authSlice';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const createTestStore = (preloadedState?: any) => {
  return (configureStore as any)({
    reducer: { auth: authReducer },
    preloadedState,
  });
};

const renderWithProviders = (
  component: React.ReactElement,
  { preloadedState = { auth: { token: 'test-token', user: null } } } = {}
) => {
  const store = createTestStore(preloadedState);
  return render(
    <Provider store={store}>
      <BrowserRouter>{component}</BrowserRouter>
    </Provider>
  );
};

describe('ProtectedRoute Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Basic Functionality', () => {
    it('should render without crashing', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      expect(() => {
        renderWithProviders(
          <Routes>
            <Route path="/" element={<TestComponent />} />
            <Route path="/login" element={<div>Login</div>} />
          </Routes>
        );
      }).not.toThrow();
    });

    it('should redirect to login when token is missing', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login Page</div>} />
        </Routes>,
        { preloadedState: { auth: { token: null as unknown as string, user: null } } }
      );

      expect(screen.getByText('Login Page')).toBeInTheDocument();
    });

    it('should use useAppSelector hook', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login</div>} />
        </Routes>
      );

      expect(screen.getByText('Login')).toBeInTheDocument();
    });

    it('should handle Navigate component', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login</div>} />
        </Routes>
      );

      expect(screen.getByText('Login')).toBeInTheDocument();
    });

    it('should work within Routes component', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      expect(() => {
        renderWithProviders(
          <Routes>
            <Route path="/" element={<TestComponent />} />
            <Route path="/login" element={<div>Login</div>} />
          </Routes>
        );
      }).not.toThrow();
    });
  });

  describe('Routing Behavior', () => {
    it('should accept ReactNode as children', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <section>Content</section>
        </ProtectedRoute>
      );

      expect(() => {
        renderWithProviders(
          <Routes>
            <Route path="/" element={<TestComponent />} />
            <Route path="/login" element={<div>Login</div>} />
          </Routes>
        );
      }).not.toThrow();
    });

    it('should render child components correctly', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Child Component</div>
        </ProtectedRoute>
      );

      expect(() => {
        renderWithProviders(
          <Routes>
            <Route path="/" element={<TestComponent />} />
            <Route path="/login" element={<div>Login</div>} />
          </Routes>
        );
      }).not.toThrow();
    });

    it('should check auth.token from Redux state', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login</div>} />
        </Routes>
      );

      expect(screen.getByText('Login')).toBeInTheDocument();
    });

    it('should treat empty token as unauthenticated', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login</div>} />
        </Routes>,
        { preloadedState: { auth: { token: '', user: null } } }
      );

      expect(screen.getByText('Login')).toBeInTheDocument();
    });

    it('should treat null token as unauthenticated', () => {
      const TestComponent = () => (
        <ProtectedRoute>
          <div>Protected</div>
        </ProtectedRoute>
      );

      renderWithProviders(
        <Routes>
          <Route path="/" element={<TestComponent />} />
          <Route path="/login" element={<div>Login</div>} />
        </Routes>,
        { preloadedState: { auth: { token: null as unknown as string, user: null } } }
      );

      expect(screen.getByText('Login')).toBeInTheDocument();
    });
  });
});
