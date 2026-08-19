import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { OrderReturnHandoff, OrderSupportHandoff } from '../OrderReturnHandoff';
import { returnApi } from '../../../../services/returnApi';
import { makeReturnEligibility } from '../../__tests__/fixtures';
import { ApiError } from '../../../../services/apiError';

vi.mock('../../../../services/returnApi', () => ({
  returnApi: { checkEligibility: vi.fn() },
}));

const renderHandoff = (queryClient: QueryClient) =>
  render(
    <QueryClientProvider client={queryClient}>
      <MemoryRouter>
        <OrderReturnHandoff orderReference="ORD-2026-000001" />
      </MemoryRouter>
    </QueryClientProvider>
  );

const newClient = () => new QueryClient({ defaultOptions: { queries: { retry: false } } });

describe('OrderReturnHandoff (FD-13)', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('surfaces the Request Return action only when the backend says eligible', async () => {
    vi.mocked(returnApi.checkEligibility).mockResolvedValue(makeReturnEligibility());

    renderHandoff(newClient());

    const link = await screen.findByRole('link', { name: 'Request Return' });
    expect(link).toHaveAttribute('href', '/orders/ORD-2026-000001/return-request');
    expect(returnApi.checkEligibility).toHaveBeenCalledWith('ORD-2026-000001');
  });

  it('renders nothing when the order is not eligible', async () => {
    vi.mocked(returnApi.checkEligibility).mockResolvedValue(makeReturnEligibility({ eligible: false }));

    renderHandoff(newClient());

    await waitFor(() => expect(returnApi.checkEligibility).toHaveBeenCalled());
    await waitFor(() =>
      expect(screen.queryByTestId('order-return-handoff-loading')).not.toBeInTheDocument()
    );
    expect(screen.queryByTestId('order-return-handoff')).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Request Return' })).not.toBeInTheDocument();
  });

  it('renders nothing when eligibility cannot be determined', async () => {
    vi.mocked(returnApi.checkEligibility).mockRejectedValue(new ApiError('nope', 'SHIPMENT_NOT_FOUND', 404));

    renderHandoff(newClient());

    await waitFor(() => expect(returnApi.checkEligibility).toHaveBeenCalled());
    await waitFor(() =>
      expect(screen.queryByTestId('order-return-handoff-loading')).not.toBeInTheDocument()
    );
    expect(screen.queryByTestId('order-return-handoff')).not.toBeInTheDocument();
    expect(screen.queryByRole('link', { name: 'Request Return' })).not.toBeInTheDocument();
  });
});

describe('OrderSupportHandoff (FD-13)', () => {
  it('renders a navigate-only support handoff without fabricating a ticket', () => {
    render(
      <MemoryRouter>
        <OrderSupportHandoff orderReference="ORD-2026-000001" />
      </MemoryRouter>
    );

    expect(screen.getByTestId('order-support-handoff')).toBeInTheDocument();
    expect(screen.getByText(/Need help with this order\?/)).toBeInTheDocument();
    expect(screen.getByText(/support team can assist you/)).toBeInTheDocument();
  });
});
