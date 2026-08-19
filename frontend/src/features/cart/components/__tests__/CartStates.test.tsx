import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CartEmptyState } from '../CartEmptyState';
import { CartSkeleton } from '../CartSkeleton';
import { CartErrorState } from '../CartErrorState';
import { ApiError } from '../../../../services/apiError';

describe('CartEmptyState (FD-11)', () => {
  it('shows the empty message and a Continue Shopping CTA to the catalog', () => {
    render(
      <MemoryRouter>
        <CartEmptyState />
      </MemoryRouter>
    );
    expect(screen.getByRole('heading', { name: 'Your cart is empty' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toHaveAttribute('href', '/products');
  });
});

describe('CartSkeleton (FD-11)', () => {
  it('renders the loading marker with aria-busy and decorative aria-hidden skeletons', () => {
    const { container } = render(<CartSkeleton />);
    expect(screen.getByTestId('cart-loading')).toHaveAttribute('aria-busy', 'true');
    const skeletons = container.querySelectorAll('.skeleton-loader');
    expect(skeletons.length).toBeGreaterThan(0);
    skeletons.forEach((s) => expect(s).toHaveAttribute('aria-hidden', 'true'));
  });
});

describe('CartErrorState (FD-11)', () => {
  it('shows a safe message with Retry and Continue Shopping', () => {
    const onRetry = vi.fn();
    render(
      <MemoryRouter>
        <CartErrorState error={new Error('raw internal detail')} onRetry={onRetry} />
      </MemoryRouter>
    );
    expect(screen.getByRole('alert')).toHaveTextContent('Unable to load your cart');
    expect(screen.getByRole('button', { name: 'Retry' })).toBeInTheDocument();
    expect(screen.getByRole('link', { name: 'Continue Shopping' })).toHaveAttribute('href', '/products');
  });

  it('preserves the backend message and requestId for diagnostics', () => {
    const error = new ApiError('Backend server is unavailable.', 'NETWORK_ERROR', undefined, '/api/v1/cart', '2026-08-18T00:00:00Z', 'req-cart-123');
    render(
      <MemoryRouter>
        <CartErrorState error={error} onRetry={() => {}} />
      </MemoryRouter>
    );
    expect(screen.getByText('Backend server is unavailable.')).toBeInTheDocument();
    expect(screen.getByText('Reference: req-cart-123')).toBeInTheDocument();
  });

  it('invokes the retry callback', () => {
    const onRetry = vi.fn();
    render(
      <MemoryRouter>
        <CartErrorState error={new ApiError('Failed', 'NETWORK_ERROR')} onRetry={onRetry} />
      </MemoryRouter>
    );
    screen.getByRole('button', { name: 'Retry' }).click();
    expect(onRetry).toHaveBeenCalledTimes(1);
  });
});
