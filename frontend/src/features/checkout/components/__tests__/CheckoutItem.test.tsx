import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CheckoutItem } from '../CheckoutItem';
import { CheckoutLineResponse } from '../../../../services/cartApi';

const makeLine = (overrides: Partial<CheckoutLineResponse> = {}): CheckoutLineResponse => ({
  cartItemId: 'item-1',
  productId: 'prod-1',
  sku: 'SKU-OYSTER-01',
  productName: 'Blue Oyster Spawn',
  quantity: 2,
  cartUnitPrice: 249,
  authoritativeUnitPrice: 249,
  priceChanged: false,
  lineSubtotal: 498,
  discountAmount: 0,
  taxAmount: 44.82,
  lineTotal: 542.82,
  ...overrides,
});

const renderItem = (line: CheckoutLineResponse) =>
  render(
    <MemoryRouter>
      <ul>
        <CheckoutItem line={line} currency="INR" />
      </ul>
    </MemoryRouter>
  );

describe('CheckoutItem (FD-12)', () => {
  it('renders the server-authoritative line with per-unit and line totals', () => {
    renderItem(makeLine());
    expect(screen.getByText('Blue Oyster Spawn')).toBeInTheDocument();
    expect(screen.getByText(/SKU-OYSTER-01 · Qty 2 × ₹249\.00/)).toBeInTheDocument();
    expect(screen.getByText('₹542.82')).toBeInTheDocument();
    expect(screen.queryByText(/Price updated since you added it/)).not.toBeInTheDocument();
  });

  it('surfaces a price-changed badge with the previous price', () => {
    renderItem(makeLine({ priceChanged: true, authoritativeUnitPrice: 279 }));
    expect(screen.getByText('Price updated since you added it')).toBeInTheDocument();
    expect(screen.getByText('Was ₹249.00')).toBeInTheDocument();
  });

  it('never renders a price-changed badge for unchanged prices', () => {
    const { container } = renderItem(makeLine({ priceChanged: false }));
    expect(container.querySelector('[role="status"]')).toBeNull();
  });
});