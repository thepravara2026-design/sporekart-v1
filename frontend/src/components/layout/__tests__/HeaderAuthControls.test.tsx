import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Header } from '../Header';
import { AuthProvider } from '../../../context/AuthContext';
import { cartApi } from '../../../services/cartApi';

vi.mock('../../../services/cartApi', () => ({
  cartApi: {
    getCart: vi.fn(),
  },
}));

const renderHeader = (initialPath = '/') => {
  const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return render(
    <QueryClientProvider client={queryClient}>
      <AuthProvider>
        <MemoryRouter initialEntries={[initialPath]}>
          <Routes>
            <Route path="/" element={<Header />} />
            <Route path="/login" element={<div data-testid="login-route">Login</div>} />
            <Route path="/register" element={<div data-testid="register-route">Register</div>} />
          </Routes>
        </MemoryRouter>
      </AuthProvider>
    </QueryClientProvider>
  );
};

describe('Header auth controls', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
    vi.mocked(cartApi.getCart).mockResolvedValue({
      success: true,
      data: { itemCount: 0, items: [], subtotal: 0, currency: 'INR' },
    } as never);
  });

  it('shows Sign In and a link to registration when logged out', () => {
    renderHeader();
    expect(screen.getByRole('link', { name: /sign in/i })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('banner')).toBeInTheDocument();
  });

  it('shows the signed-in user name and Sign Out button when authenticated', () => {
    localStorage.setItem('accessToken', 'mock-jwt-customer-token');
    localStorage.setItem(
      'sporekart_user',
      JSON.stringify({
        id: 'usr-customer-01',
        name: 'Mushroom Cultivator',
        email: 'customer@sporekart.com',
        role: 'ROLE_CUSTOMER',
        roles: ['ROLE_CUSTOMER'],
      })
    );
    renderHeader();

    expect(screen.getByTestId('header-user-name')).toHaveTextContent('Mushroom Cultivator');
    expect(screen.getByRole('button', { name: 'Sign out' })).toBeInTheDocument();
    expect(screen.queryByRole('link', { name: /sign in/i })).not.toBeInTheDocument();
  });

  it('logs the user out and clears the session when Sign Out is clicked', () => {
    localStorage.setItem('accessToken', 'mock-jwt-customer-token');
    localStorage.setItem(
      'sporekart_user',
      JSON.stringify({
        id: 'usr-customer-01',
        name: 'Mushroom Cultivator',
        email: 'customer@sporekart.com',
        role: 'ROLE_CUSTOMER',
        roles: ['ROLE_CUSTOMER'],
      })
    );
    renderHeader();

    fireEvent.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(localStorage.getItem('accessToken')).toBeNull();
    expect(localStorage.getItem('sporekart_user')).toBeNull();
    expect(screen.getByRole('link', { name: /sign in/i })).toBeInTheDocument();
  });
});
