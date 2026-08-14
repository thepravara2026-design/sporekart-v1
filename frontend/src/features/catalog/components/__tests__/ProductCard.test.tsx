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
    currency: 'USD',
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

  it('renders product details correctly', () => {
    render(
      <MemoryRouter>
        <ProductCard product={mockProduct} />
      </MemoryRouter>
    );

    expect(screen.getByText('Red Reishi Spore Extract')).toBeInTheDocument();
    expect(screen.getByText('SKU: SKU-REISHI-001')).toBeInTheDocument();
    expect(screen.getByText('$34.50')).toBeInTheDocument();
    expect(screen.getByText('Extracts')).toBeInTheDocument();
    expect(screen.getByText('In Stock')).toBeInTheDocument();
    expect(screen.getByText('High potency dual-extracted liquid')).toBeInTheDocument();
    expect(screen.getByRole('link', { name: /view details/i })).toHaveAttribute(
      'href',
      '/products/p-100'
    );
  });
});
