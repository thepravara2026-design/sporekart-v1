import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { ProductCard } from '../ProductCard';
import { Product } from '../../../../types/catalog';

describe('ProductCard', () => {
  const mockProduct: Product = {
    id: 'p-100',
    sku: 'SKU-REISHI-001',
    name: 'Red Reishi Spore Extract',
    description: 'High potency dual-extracted liquid',
    price: 34.50,
    currency: 'INR',
    status: 'ACTIVE',
    category: {
      id: 'c-200',
      name: 'Extracts',
      slug: 'extracts',
      description: null,
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
    },
    createdAt: '2026-01-01T00:00:00Z',
    updatedAt: '2026-01-01T00:00:00Z',
  };

  it('renders product details correctly without strike-out price', () => {
    render(
      <MemoryRouter>
        <ProductCard product={mockProduct} />
      </MemoryRouter>
    );

    expect(screen.getByText('Red Reishi Spore Extract')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-REISHI-001')).toBeInTheDocument();
    expect(screen.getByText(/₹\s*34\.50/)).toBeInTheDocument();
    expect(screen.queryByTestId('strike-out-price')).not.toBeInTheDocument();
    expect(screen.getByText('Extracts')).toBeInTheDocument();
    expect(screen.getByText('In Stock')).toBeInTheDocument();
    expect(screen.getByText('High potency dual-extracted liquid')).toBeInTheDocument();
  });

  it('renders strike-out price and discount badge when strikeOutPrice is present', () => {
    const discountedProduct: Product = {
      ...mockProduct,
      price: 1999.00,
      strikeOutPrice: 2499.00,
    };

    render(
      <MemoryRouter>
        <ProductCard product={discountedProduct} />
      </MemoryRouter>
    );

    expect(screen.getByTestId('strike-out-price')).toBeInTheDocument();
    expect(screen.getByTestId('strike-out-price')).toHaveTextContent(/₹\s*2,499\.00/);
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*1,999\.00/);
    expect(screen.getByTestId('discount-badge')).toHaveTextContent('20% OFF');
  });
});
