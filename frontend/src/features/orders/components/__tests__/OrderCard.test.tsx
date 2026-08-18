import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { OrderCard } from '../OrderCard';
import { makeOrderSummary } from '../../__tests__/fixtures';

const renderCard = (order = makeOrderSummary()) =>
  render(
    <MemoryRouter>
      <OrderCard order={order} />
    </MemoryRouter>
  );

describe('OrderCard (FD-13)', () => {
  it('renders order number, status, item count, total, and date', () => {
    renderCard(makeOrderSummary({ status: 'DELIVERED', grandTotal: 567.82, itemCount: 2 }));

    expect(screen.getByText('ORD-2026-000001')).toBeInTheDocument();
    expect(screen.getByText('Delivered')).toBeInTheDocument();
    expect(screen.getByText('2 items')).toBeInTheDocument();
    expect(screen.getByTestId('order-card-total')).toHaveTextContent('₹567.82');
    expect(screen.getByText(/18 Aug 2026/)).toBeInTheDocument();
  });

  it('links to the order detail page using the order number', () => {
    renderCard();
    const link = screen.getByTestId('order-card-link');
    expect(link).toHaveAttribute('href', '/orders/ORD-2026-000001');
  });

  it('singularizes the item count', () => {
    renderCard(makeOrderSummary({ itemCount: 1 }));
    expect(screen.getByText('1 item')).toBeInTheDocument();
  });
});