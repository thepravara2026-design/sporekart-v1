import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ProductPrice } from '../ProductPrice';

describe('ProductPrice', () => {
  it('renders selling price when strikeOutPrice is not provided', () => {
    render(<ProductPrice price={999} currency="INR" />);
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*999\.00/);
    expect(screen.queryByTestId('strike-out-price')).not.toBeInTheDocument();
  });

  it('renders strike-out price when strikeOutPrice is greater than price', () => {
    render(<ProductPrice price={999} strikeOutPrice={1499} currency="INR" showDiscountBadge={true} />);
    expect(screen.getByTestId('strike-out-price')).toHaveTextContent(/₹\s*1,499\.00/);
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*999\.00/);
    expect(screen.getByTestId('discount-badge')).toHaveTextContent('33% OFF');
  });

  it('does not render strike-out price when strikeOutPrice is equal to or less than price', () => {
    render(<ProductPrice price={999} strikeOutPrice={999} currency="INR" />);
    expect(screen.getByTestId('selling-price')).toHaveTextContent(/₹\s*999\.00/);
    expect(screen.queryByTestId('strike-out-price')).not.toBeInTheDocument();
  });
});
