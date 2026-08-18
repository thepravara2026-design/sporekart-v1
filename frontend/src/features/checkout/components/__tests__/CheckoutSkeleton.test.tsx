import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { CheckoutSkeleton } from '../CheckoutSkeleton';

describe('CheckoutSkeleton (FD-12)', () => {
  it('renders a loading placeholder that mirrors the checkout layout', () => {
    const { container } = render(<CheckoutSkeleton />);
    expect(screen.getByTestId('checkout-skeleton')).toBeInTheDocument();
    expect(container.querySelectorAll('.skeleton-loader').length).toBeGreaterThan(0);
  });
});