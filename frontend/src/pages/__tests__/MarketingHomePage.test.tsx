import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';

import { HeroSection } from '../../features/marketing/components/HeroSection';
import { FeaturedProducts } from '../../features/marketing/components/FeaturedProducts';
import { CategorySection } from '../../features/marketing/components/CategorySection';
import { TrainingSection } from '../../features/marketing/components/TrainingSection';
import { TrustSection } from '../../features/marketing/components/TrustSection';
import { GrowerStories } from '../../features/marketing/components/GrowerStories';
import { FAQSection } from '../../features/marketing/components/FAQSection';
import { HomePage } from '../HomePage';

import { catalogApi } from '../../services/catalogApi';
import { ToastProvider } from '../../components/ui/Toast';

vi.mock('../../services/catalogApi', () => ({
  catalogApi: {
    getProducts: vi.fn(),
    getCategories: vi.fn(),
    getProduct: vi.fn(),
    getCategory: vi.fn(),
  },
}));

const mockProductPage = {
  success: true,
  data: {
    content: [
      {
        id: 'prod-1',
        sku: 'SPW-OYS-001',
        name: 'Oyster Spawn Block',
        description: 'High-yield oyster mushroom spawn.',
        price: 149.0,
        currency: 'INR',
        status: 'ACTIVE',
        category: {
          id: 'cat-1',
          name: 'Oyster',
          slug: 'oyster',
          description: null,
          status: 'ACTIVE',
          createdAt: '',
          updatedAt: '',
        },
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z',
      },
    ],
    page: 0,
    size: 6,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
  },
};

const mockCategoryPage = {
  success: true,
  data: {
    content: [
      {
        id: 'cat-1',
        name: 'Oyster Spawn',
        slug: 'oyster-spawn',
        description: 'Oyster varieties',
        status: 'ACTIVE',
        createdAt: '',
        updatedAt: '',
      },
      {
        id: 'cat-2',
        name: 'Medicinal Cultures',
        slug: 'medicinal-cultures',
        description: null,
        status: 'ACTIVE',
        createdAt: '',
        updatedAt: '',
      },
    ],
    page: 0,
    size: 8,
    totalElements: 2,
    totalPages: 1,
    first: true,
    last: true,
  },
};

function createQueryClient() {
  return new QueryClient({ defaultOptions: { queries: { retry: false } } });
}

function Wrapper({ children }: { children: React.ReactNode }) {
  return (
    <QueryClientProvider client={createQueryClient()}>
      <ToastProvider>
        <BrowserRouter>{children}</BrowserRouter>
      </ToastProvider>
    </QueryClientProvider>
  );
}

// ─────────────────────────────────────────────────────────────────────────────
// HeroSection Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('HeroSection', () => {
  it('renders the primary H1 heading', () => {
    render(<HeroSection />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument();
  });

  it('renders Shop Spawn CTA linking to /products', () => {
    render(<HeroSection />, { wrapper: Wrapper });
    const link = screen.getByRole('link', { name: /shop spawn/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', '/products');
  });

  it('renders Browse Categories CTA linking to /categories', () => {
    render(<HeroSection />, { wrapper: Wrapper });
    const link = screen.getByRole('link', { name: /browse categories/i });
    expect(link).toHaveAttribute('href', '/categories');
  });

  it('renders trust micro-signals', () => {
    render(<HeroSection />, { wrapper: Wrapper });
    expect(screen.getByText('Cold-chain shipping')).toBeInTheDocument();
    expect(screen.getByText('Expert grower support')).toBeInTheDocument();
  });

  it('has accessible section landmark', () => {
    render(<HeroSection />, { wrapper: Wrapper });
    expect(screen.getByRole('region', { name: /hero/i })).toBeInTheDocument();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// FeaturedProducts Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('FeaturedProducts', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders loading skeletons initially', () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    render(<FeaturedProducts />, { wrapper: Wrapper });
    expect(screen.getByTestId('featured-products-loading')).toBeInTheDocument();
  });

  it('renders product cards on success', async () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockResolvedValue(mockProductPage);
    render(<FeaturedProducts />, { wrapper: Wrapper });
    expect(await screen.findByTestId('featured-products-grid')).toBeInTheDocument();
    expect(screen.getByText('Oyster Spawn Block')).toBeInTheDocument();
  });

  it('renders View All Products CTA', async () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockResolvedValue(mockProductPage);
    render(<FeaturedProducts />, { wrapper: Wrapper });
    await screen.findByTestId('featured-products-grid');
    expect(screen.getAllByRole('link', { name: /view all products/i }).length).toBeGreaterThan(0);
  });

  it('renders error state when API fails', async () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Network error'));
    render(<FeaturedProducts />, { wrapper: Wrapper });
    expect(await screen.findByText(/unable to load products/i)).toBeInTheDocument();
  });

  it('renders empty state when no products returned', async () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockResolvedValue({
      success: true,
      data: {
        content: [],
        page: 0,
        size: 6,
        totalElements: 0,
        totalPages: 0,
        first: true,
        last: true,
      },
    });
    render(<FeaturedProducts />, { wrapper: Wrapper });
    expect(await screen.findByText(/no products available/i)).toBeInTheDocument();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// CategorySection Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('CategorySection', () => {
  beforeEach(() => vi.clearAllMocks());

  it('renders loading skeletons initially', () => {
    (catalogApi.getCategories as ReturnType<typeof vi.fn>).mockReturnValue(new Promise(() => {}));
    render(<CategorySection />, { wrapper: Wrapper });
    expect(screen.getByTestId('category-section-loading')).toBeInTheDocument();
  });

  it('renders category cards on success', async () => {
    (catalogApi.getCategories as ReturnType<typeof vi.fn>).mockResolvedValue(mockCategoryPage);
    render(<CategorySection />, { wrapper: Wrapper });
    expect(await screen.findByTestId('category-section-grid')).toBeInTheDocument();
    expect(screen.getByText('Oyster Spawn')).toBeInTheDocument();
    expect(screen.getByText('Medicinal Cultures')).toBeInTheDocument();
  });

  it('renders error state when API fails', async () => {
    (catalogApi.getCategories as ReturnType<typeof vi.fn>).mockRejectedValue(new Error('Timeout'));
    render(<CategorySection />, { wrapper: Wrapper });
    expect(await screen.findByText(/unable to load categories/i)).toBeInTheDocument();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// TrainingSection Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('TrainingSection', () => {
  it('renders section heading', () => {
    render(<TrainingSection />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { name: /learn to grow/i })).toBeInTheDocument();
  });

  it('renders all training highlight cards', () => {
    render(<TrainingSection />, { wrapper: Wrapper });
    expect(screen.getAllByTestId('training-highlight').length).toBeGreaterThan(0);
  });

  it('renders Explore Training CTA', () => {
    render(<TrainingSection />, { wrapper: Wrapper });
    expect(screen.getByRole('link', { name: /explore training programs/i })).toBeInTheDocument();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// TrustSection Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('TrustSection', () => {
  it('renders section heading', () => {
    render(<TrustSection />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { name: /built for serious growers/i })).toBeInTheDocument();
  });

  it('renders all trust signal cards', () => {
    render(<TrustSection />, { wrapper: Wrapper });
    expect(screen.getAllByTestId('trust-signal-item').length).toBeGreaterThanOrEqual(4);
  });

  it('does not render fake metrics or star ratings', () => {
    render(<TrustSection />, { wrapper: Wrapper });
    expect(screen.queryByText(/★/)).not.toBeInTheDocument();
    expect(screen.queryByText(/10,000\+ customers/i)).not.toBeInTheDocument();
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// GrowerStories Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('GrowerStories', () => {
  it('renders section heading', () => {
    render(<GrowerStories />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { name: /grower stories/i })).toBeInTheDocument();
  });

  it('renders story cards', () => {
    render(<GrowerStories />, { wrapper: Wrapper });
    expect(screen.getAllByTestId('grower-story-card').length).toBeGreaterThan(0);
  });

  it('story CTA links navigate to real routes', () => {
    render(<GrowerStories />, { wrapper: Wrapper });
    const links = screen.getAllByRole('link');
    links.forEach((link) => {
      const href = link.getAttribute('href');
      expect(href).toBeTruthy();
      expect(href).not.toContain('#placeholder');
    });
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// FAQSection Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('FAQSection', () => {
  it('renders section heading', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    expect(
      screen.getByRole('heading', { name: /frequently asked questions/i }),
    ).toBeInTheDocument();
  });

  it('all FAQ items start collapsed', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    const buttons = screen.getAllByRole('button');
    buttons.forEach((btn) => {
      expect(btn).toHaveAttribute('aria-expanded', 'false');
    });
  });

  it('expands an FAQ item on click and sets aria-expanded=true', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    const firstBtn = screen.getAllByRole('button')[0];
    fireEvent.click(firstBtn);
    expect(firstBtn).toHaveAttribute('aria-expanded', 'true');
  });

  it('collapses an expanded FAQ when clicked again', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    const firstBtn = screen.getAllByRole('button')[0];
    fireEvent.click(firstBtn);
    expect(firstBtn).toHaveAttribute('aria-expanded', 'true');
    fireEvent.click(firstBtn);
    expect(firstBtn).toHaveAttribute('aria-expanded', 'false');
  });

  it('closes first FAQ when second opens (single open pattern)', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    const buttons = screen.getAllByRole('button');
    fireEvent.click(buttons[0]);
    expect(buttons[0]).toHaveAttribute('aria-expanded', 'true');
    fireEvent.click(buttons[1]);
    expect(buttons[0]).toHaveAttribute('aria-expanded', 'false');
    expect(buttons[1]).toHaveAttribute('aria-expanded', 'true');
  });

  it('FAQ buttons have aria-controls referencing existing panel elements', () => {
    render(<FAQSection />, { wrapper: Wrapper });
    const buttons = screen.getAllByRole('button');
    buttons.forEach((btn) => {
      const controlsId = btn.getAttribute('aria-controls');
      expect(controlsId).toBeTruthy();
      expect(document.getElementById(controlsId!)).toBeTruthy();
    });
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// HomePage Integration Tests
// ─────────────────────────────────────────────────────────────────────────────
describe('HomePage integration', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockResolvedValue(mockProductPage);
    (catalogApi.getCategories as ReturnType<typeof vi.fn>).mockResolvedValue(mockCategoryPage);
  });

  it('renders the full homepage without crashing', () => {
    render(<HomePage />, { wrapper: Wrapper });
    expect(screen.getByTestId('home-page')).toBeInTheDocument();
  });

  it('renders H1 heading in hero', () => {
    render(<HomePage />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument();
  });

  it('renders FAQ section even when products API fails', () => {
    (catalogApi.getProducts as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error('Products down'),
    );
    render(<HomePage />, { wrapper: Wrapper });
    expect(
      screen.getByRole('heading', { name: /frequently asked questions/i }),
    ).toBeInTheDocument();
  });

  it('renders Training section even when categories API fails', () => {
    (catalogApi.getCategories as ReturnType<typeof vi.fn>).mockRejectedValue(
      new Error('Categories down'),
    );
    render(<HomePage />, { wrapper: Wrapper });
    expect(screen.getByRole('heading', { name: /learn to grow/i })).toBeInTheDocument();
  });

  it('renders all major section headings on mount', () => {
    render(<HomePage />, { wrapper: Wrapper });
    // Hero H1
    expect(screen.getByRole('heading', { level: 1 })).toBeInTheDocument();
    // Section H2s
    expect(screen.getByRole('heading', { name: /shop the catalog/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /shop by category/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /learn to grow/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /built for serious growers/i })).toBeInTheDocument();
    expect(screen.getByRole('heading', { name: /grower stories/i })).toBeInTheDocument();
    expect(
      screen.getByRole('heading', { name: /frequently asked questions/i }),
    ).toBeInTheDocument();
  });
});
