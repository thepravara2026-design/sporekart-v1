import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { RegisterPage } from '../RegisterPage';
import { AuthProvider } from '../../context/AuthContext';
import { authApi } from '../../services/authApi';

vi.mock('../../services/authApi', () => ({
  authApi: {
    register: vi.fn(),
    login: vi.fn(),
    getCurrentUser: vi.fn(),
  },
}));

const renderRegisterPage = () => {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={['/register']}>
        <Routes>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<div data-testid="login-route">Login</div>} />
          <Route path="/" element={<div data-testid="home-route">Home</div>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
};

describe('RegisterPage (auth)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('renders the registration form with all required fields', () => {
    renderRegisterPage();
    expect(screen.getByRole('heading', { name: /Create your Sporekart account/ })).toBeInTheDocument();
    expect(screen.getByLabelText('Email Address')).toBeInTheDocument();
    expect(screen.getByLabelText('Password')).toBeInTheDocument();
    expect(screen.getByLabelText('Confirm Password')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Create Account' })).toBeInTheDocument();
  });

  it('validates that passwords match before submitting', async () => {
    renderRegisterPage();
    fireEvent.change(screen.getByLabelText('Email Address'), { target: { value: 'buyer@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'different123' } });

    fireEvent.click(screen.getByRole('button', { name: 'Create Account' }));

    expect(await screen.findByText('Passwords do not match.')).toBeInTheDocument();
    expect(authApi.register).not.toHaveBeenCalled();
  });

  it('submits the payload to the backend and redirects to login on success', async () => {
    vi.mocked(authApi.register).mockResolvedValue({
      id: 'cust-2',
      email: 'buyer@example.com',
      firstName: 'A.',
      lastName: 'Buyer',
      role: 'CUSTOMER',
      status: 'ACTIVE',
    });

    renderRegisterPage();
    fireEvent.change(screen.getByLabelText('First Name'), { target: { value: 'A.' } });
    fireEvent.change(screen.getByLabelText('Last Name'), { target: { value: 'Buyer' } });
    fireEvent.change(screen.getByLabelText('Email Address'), { target: { value: 'buyer@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'password123' } });

    fireEvent.click(screen.getByRole('button', { name: 'Create Account' }));

    await waitFor(() => {
      expect(authApi.register).toHaveBeenCalledWith({
        email: 'buyer@example.com',
        password: 'password123',
        firstName: 'A.',
        lastName: 'Buyer',
      });
    });
    expect(await screen.findByTestId('login-route')).toBeInTheDocument();
  });

  it('shows an error message when the backend rejects registration', async () => {
    vi.mocked(authApi.register).mockRejectedValue(new Error('Email already registered'));

    renderRegisterPage();
    fireEvent.change(screen.getByLabelText('Email Address'), { target: { value: 'taken@example.com' } });
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } });
    fireEvent.change(screen.getByLabelText('Confirm Password'), { target: { value: 'password123' } });

    fireEvent.click(screen.getByRole('button', { name: 'Create Account' }));

    expect(
      await screen.findByText('Unable to create your account. The email may already be registered.')
    ).toBeInTheDocument();
    expect(screen.queryByTestId('login-route')).not.toBeInTheDocument();
  });

  it('links to the sign-in page', () => {
    renderRegisterPage();
    expect(screen.getByRole('link', { name: 'Sign in' })).toHaveAttribute('href', '/login');
    expect(screen.getByRole('link', { name: '← Return to Storefront' })).toHaveAttribute('href', '/');
  });
});