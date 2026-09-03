import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import {
  ProductQuantity,
  ProductActions,
  ProductGallery,
  ProductMetadata,
  ProductDescription,
  ProductInfo,
  RelatedProducts,
  ProductDetailSkeleton,
} from '../../index';
import { Product } from '../../types/catalog';
import { cartApi } from '../../../../services/cartApi';
import { catalogApi } from '../../../../services/catalogApi';
import { ToastProvider } from '../../../../components/ui/Toast';

vi.mock('../../../../services/cartApi', () => ({
  cartApi: {
    addItem: vi.fn(),
  },
}));

vi.mock('../../../../services/catalogApi', () => ({
  catalogApi: {
    getProducts: vi.fn(),
  },
}));

const mockProduct: Product = {
  id: 'prod-707',
  sku: 'SPW-LION-001',
  name: 'Lions Mane Liquid Culture',
  description: 'Pure liquid culture syringe for lions mane cultivation.',
  price: 28.0,
  currency: 'INR',
  status: 'ACTIVE',
  category: {
    id: 'cat-70',
    name: 'Medicinal Cultures',
    slug: 'medicinal-cultures',
    description: null,
    status: 'ACTIVE',
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  },
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
};

describe('Product Detail & Discovery Components (Sprint FD-07)', () => {
  let queryClient: QueryClient;

  beforeEach(() => {
    vi.clearAllMocks();
    queryClient = new QueryClient({
      defaultOptions: {
        queries: {
          retry: false,
        },
      },
    });
  });

  it('renders ProductQuantity selector and handles increment/decrement', () => {
    const handleChange = vi.fn();
    render(<ProductQuantity quantity={2} onQuantityChange={handleChange} />);

    const decBtn = screen.getByRole('button', { name: 'Decrease quantity' });
    const incBtn = screen.getByRole('button', { name: 'Increase quantity' });
    const input = screen.getByRole('spinbutton', { name: 'Quantity' });

    expect(input).toHaveValue(2);

    fireEvent.click(decBtn);
    expect(handleChange).toHaveBeenCalledWith(1);

    fireEvent.click(incBtn);
    expect(handleChange).toHaveBeenCalledWith(3);
  });

  it('renders ProductActions and triggers cartApi.addItem on click', async () => {
    (cartApi.addItem as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: { id: 'cart-1', customerId: 'cust-1', items: [], totalQuantity: 1, subtotal: 28.0, status: 'ACTIVE', createdAt: '', updatedAt: '' },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <ProductActions product={mockProduct} />
        </ToastProvider>
      </QueryClientProvider>
    );

    const addToCartBtn = screen.getByRole('button', { name: 'Add to Cart' });
    fireEvent.click(addToCartBtn);

    await waitFor(() => {
      expect(cartApi.addItem).toHaveBeenCalledWith({
        productId: 'prod-707',
        quantity: 1,
      });
    });
  });

  it('renders ProductGallery with branded placeholder when no images are provided', () => {
    render(<ProductGallery productName="Lions Mane" />);
    expect(screen.getByText('Mushroom Spawn')).toBeInTheDocument();
    // No gallery controls should be rendered for a single image product.
    expect(screen.queryByRole('button', { name: 'Previous product image' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Next product image' })).not.toBeInTheDocument();
  });

  it('renders ProductMetadata definition list cleanly', () => {
    render(<ProductMetadata product={mockProduct} />);
    expect(screen.getByText('SPW-LION-001')).toBeInTheDocument();
    expect(screen.getByText('Medicinal Cultures')).toBeInTheDocument();
  });

  it('renders ProductDescription section', () => {
    render(<ProductDescription description="High potency lions mane protocol." />);
    expect(screen.getByText('High potency lions mane protocol.')).toBeInTheDocument();
  });

  it('renders ProductInfo composing product details and actions', () => {
    render(
      <QueryClientProvider client={queryClient}>
        <ToastProvider>
          <BrowserRouter>
            <ProductInfo product={mockProduct} />
          </BrowserRouter>
        </ToastProvider>
      </QueryClientProvider>
    );

    expect(screen.getByText('Lions Mane Liquid Culture')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*28\.00/)).toBeInTheDocument();
    expect(screen.getByText('SKU: SPW-LION-001')).toBeInTheDocument();
  });

  it('renders RelatedProducts and excludes current product', async () => {
    (catalogApi.getProducts as unknown as ReturnType<typeof vi.fn>).mockResolvedValueOnce({
      success: true,
      data: {
        content: [
          mockProduct,
          {
            ...mockProduct,
            id: 'prod-808',
            name: 'Reishi Culture',
            sku: 'SPW-REI-001',
          },
        ],
        page: 0,
        size: 12,
        totalElements: 2,
        totalPages: 1,
        first: true,
        last: true,
      },
    });

    render(
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <RelatedProducts categoryId="cat-70" currentProductId="prod-707" />
        </BrowserRouter>
      </QueryClientProvider>
    );

    expect(await screen.findByText('Reishi Culture')).toBeInTheDocument();
    expect(screen.queryByText('Lions Mane Liquid Culture')).not.toBeInTheDocument();
  });

  it('renders ProductDetailSkeleton loader', () => {
    render(<ProductDetailSkeleton />);
    expect(document.querySelector('.card')).toBeInTheDocument();
  });
});
