import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
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

  it('renders single card with interactive multi-unit variant pills and updates price when clicked', () => {
    const variantProduct: Product = {
      ...mockProduct,
      variants: [
        {
          id: 'v-1',
          productId: 'p-100',
          sku: 'SKU-REISHI-100G',
          quantityValue: 100,
          quantityUnit: 'G',
          formattedQuantity: '100 g',
          sellingPrice: 799.00,
          strikeOutPrice: 999.00,
          status: 'ACTIVE',
        },
        {
          id: 'v-2',
          productId: 'p-100',
          sku: 'SKU-REISHI-250G',
          quantityValue: 250,
          quantityUnit: 'G',
          formattedQuantity: '250 g',
          sellingPrice: 1499.00,
          strikeOutPrice: 1799.00,
          status: 'ACTIVE',
        },
        {
          id: 'v-3',
          productId: 'p-100',
          sku: 'SKU-REISHI-1KG',
          quantityValue: 1,
          quantityUnit: 'KG',
          formattedQuantity: '1 kg',
          sellingPrice: 3999.00,
          strikeOutPrice: null,
          status: 'ACTIVE',
        },
      ],
    };

    const handleAddToCart = vi.fn();

    render(
      <MemoryRouter>
        <ProductCard product={variantProduct} onAddToCart={handleAddToCart} />
      </MemoryRouter>
    );

    // Initial variant is 100 g
    expect(screen.getByText('100 g')).toBeInTheDocument();
    expect(screen.getByText('250 g')).toBeInTheDocument();
    expect(screen.getByText('1 kg')).toBeInTheDocument();
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*799\.00/);
    expect(screen.getByText('SKU: SKU-REISHI-100G')).toBeInTheDocument();

    // Click on 250 g variant pill
    fireEvent.click(screen.getByText('250 g'));
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*1,499\.00/);
    expect(screen.getByText('SKU: SKU-REISHI-250G')).toBeInTheDocument();

    // Click on 1 kg variant pill
    fireEvent.click(screen.getByText('1 kg'));
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*3,999\.00/);
    expect(screen.getByText('SKU: SKU-REISHI-1KG')).toBeInTheDocument();

    // Click Add to Cart
    fireEvent.click(screen.getByRole('button', { name: /Add to Cart/i }));
    expect(handleAddToCart).toHaveBeenCalledTimes(1);
    expect(handleAddToCart).toHaveBeenCalledWith(variantProduct, expect.objectContaining({ id: 'v-3', formattedQuantity: '1 kg' }));
  });

  it('updates stock indicator dynamically when switching variants (In Stock -> Order Now -> Out of Stock)', () => {
    const stockVariantProduct: Product = {
      ...mockProduct,
      variants: [
        {
          id: 'v-normal',
          productId: 'p-100',
          sku: 'SKU-REISHI-100G',
          quantityValue: 100,
          quantityUnit: 'G',
          formattedQuantity: '100 g',
          sellingPrice: 799.00,
          status: 'ACTIVE',
          availableQuantity: 25,
        },
        {
          id: 'v-limited',
          productId: 'p-100',
          sku: 'SKU-REISHI-250G',
          quantityValue: 250,
          quantityUnit: 'G',
          formattedQuantity: '250 g',
          sellingPrice: 1499.00,
          status: 'ACTIVE',
          availableQuantity: 8,
        },
        {
          id: 'v-order-now',
          productId: 'p-100',
          sku: 'SKU-REISHI-500G',
          quantityValue: 500,
          quantityUnit: 'G',
          formattedQuantity: '500 g',
          sellingPrice: 2499.00,
          status: 'ACTIVE',
          availableQuantity: 3,
        },
        {
          id: 'v-out',
          productId: 'p-100',
          sku: 'SKU-REISHI-1KG',
          quantityValue: 1,
          quantityUnit: 'KG',
          formattedQuantity: '1 kg',
          sellingPrice: 3999.00,
          status: 'OUT_OF_STOCK',
          availableQuantity: 0,
        },
      ],
    };

    render(
      <MemoryRouter>
        <ProductCard product={stockVariantProduct} />
      </MemoryRouter>
    );

    // Initial variant 100 g has stock 25 -> "In Stock"
    expect(screen.getByText('In Stock')).toBeInTheDocument();

    // Select 250 g variant (stock = 8) -> "Limited Stock"
    fireEvent.click(screen.getByText('250 g'));
    expect(screen.getByText('Limited Stock')).toBeInTheDocument();

    // Select 500 g variant (stock = 3) -> "Order Now"
    fireEvent.click(screen.getByText('500 g'));
    expect(screen.getByText('Order Now')).toBeInTheDocument();

    // Select 1 kg variant (stock = 0) -> "Out of Stock"
    fireEvent.click(screen.getByText('1 kg'));
    expect(screen.getAllByText('Out of Stock').length).toBeGreaterThanOrEqual(1);
    expect(screen.getByRole('button', { name: /Out of Stock/i })).toBeDisabled();
  });
});
