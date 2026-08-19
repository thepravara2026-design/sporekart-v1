import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CheckoutErrorState } from '../CheckoutErrorState';
import { ApiError } from '../../../../services/apiError';

const renderError = (error: Error | null) =>
  render(
    <MemoryRouter>
      <CheckoutErrorState error={error} onRetry={vi.fn()} />
    </MemoryRouter>
  );

describe('CheckoutErrorState (FD-12)', () => {
  it('renders the API error message with a diagnostic reference', () => {
    renderError(new ApiError('Could not load your cart.', 'CART_NOT_FOUND', 404, '/api/v1/cart', 't', 'req-123'));
    expect(screen.getByText('Could not load your cart.')).toBeInTheDocument();
    expect(screen.getByText('Reference: req-123')).toBeInTheDocument();
  });

  it('renders a safe network message for non-API failures', () => {
    renderError(new Error('boom'));
    expect(screen.getByText(/network error occurred/)).toBeInTheDocument();
  });

  it('provides retry and return-to-cart actions', () => {
    const onRetry = vi.fn();
    render(
      <MemoryRouter>
        <CheckoutErrorState error={new Error('x')} onRetry={onRetry} />
      </MemoryRouter>
    );
    screen.getByRole('button', { name: 'Retry' }).click();
    expect(onRetry).toHaveBeenCalledTimes(1);
    expect(screen.getByRole('link', { name: 'Return to Cart' })).toBeInTheDocument();
  });
});
