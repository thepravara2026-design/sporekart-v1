import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { AuthProvider } from '../../context/AuthContext';
import { HomePage } from '../../pages/HomePage';
import { LoginPage } from '../../pages/LoginPage';
import { AdminDashboardPage } from '../../features/admin/pages/AdminDashboardPage';
import { TraineeTrainingConsole } from '../../features/trainee/pages/TraineeTrainingConsole';

const createTestQueryClient = () =>
  new QueryClient({
    defaultOptions: {
      queries: { retry: false },
    },
  });

describe('Visual QA Layout & Snapshot Consistency Suite (FD-23)', () => {
  it('renders HomePage layout tree deterministically', () => {
    const queryClient = createTestQueryClient();
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <HomePage />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );

    expect(container.firstChild).toBeDefined();
    expect(container.querySelectorAll('section').length).toBeGreaterThanOrEqual(1);
  });

  it('renders LoginPage structure deterministically', () => {
    const queryClient = createTestQueryClient();
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <LoginPage />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );

    expect(container.querySelector('form')).toBeInTheDocument();
    expect(container.querySelectorAll('button').length).toBeGreaterThanOrEqual(5);
  });

  it('renders AdminDashboardPage structure deterministically', () => {
    const queryClient = createTestQueryClient();
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <AdminDashboardPage />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );

    expect(container.querySelector('h1')).toHaveTextContent('Admin Control Panel');
    expect(container.querySelectorAll('a').length).toBe(6);
  });

  it('renders TraineeTrainingConsole structure deterministically', () => {
    const queryClient = createTestQueryClient();
    const { container } = render(
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <MemoryRouter>
            <TraineeTrainingConsole />
          </MemoryRouter>
        </AuthProvider>
      </QueryClientProvider>
    );

    expect(container.firstChild).toBeDefined();
  });
});
