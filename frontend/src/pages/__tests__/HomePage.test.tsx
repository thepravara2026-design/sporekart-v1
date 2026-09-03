import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { vi, describe, it, expect } from 'vitest';
import { HomePage } from '../HomePage';
import { ToastProvider } from '../../components/ui/Toast';

// Mock the catalog API — HomePage now contains API-backed FeaturedProducts / CategorySection
vi.mock('../../services/catalogApi', () => ({
  catalogApi: {
    getProducts: vi.fn().mockReturnValue(new Promise(() => {})),
    getCategories: vi.fn().mockReturnValue(new Promise(() => {})),
  },
}));

function Wrapper({ children }: { children: React.ReactNode }) {
  const qc = new QueryClient({ defaultOptions: { queries: { retry: false } } });
  return (
    <QueryClientProvider client={qc}>
      <ToastProvider>
        <BrowserRouter>{children}</BrowserRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

describe('HomePage Component', () => {
  it('renders the marketing home page without crashing', () => {
    render(<HomePage />, { wrapper: Wrapper });
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  it('renders the primary H1 hero heading', () => {
    render(<HomePage />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument();
  });

  it('renders FAQ section heading', () => {
    render(<HomePage />, { wrapper: Wrapper });
    expect(
      screen.getByRole('heading', { name: /frequently asked questions/i }),
    ).toBeInTheDocument();
  });
});
