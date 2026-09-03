import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { OrderStatusBadge } from '../OrderStatusBadge';

describe('OrderStatusBadge (FD-13)', () => {
  it('renders the human label for a backend status', () => {
    render(<OrderStatusBadge status="PAYMENT_PENDING" />);
    expect(screen.getByText('Payment Pending')).toBeInTheDocument();
  });

  it('renders the raw value for unknown statuses', () => {
    render(<OrderStatusBadge status="MYSTERY" />);
    expect(screen.getByText('MYSTERY')).toBeInTheDocument();
  });
});
