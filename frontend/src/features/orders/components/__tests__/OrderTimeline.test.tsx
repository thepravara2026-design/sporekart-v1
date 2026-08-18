import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { OrderTimeline } from '../OrderTimeline';
import { makeOrderTimeline } from '../../__tests__/fixtures';

describe('OrderTimeline (FD-13)', () => {
  it('renders the current status and chronological events', () => {
    render(<OrderTimeline timeline={makeOrderTimeline()} />);

    expect(screen.getByText('Order Timeline')).toBeInTheDocument();
    // "Confirmed" appears as the current-status badge and as the latest event.
    expect(screen.getAllByText('Confirmed').length).toBeGreaterThanOrEqual(2);

    const events = screen.getAllByTestId('order-timeline-event');
    expect(events).toHaveLength(2);
    expect(screen.getByText('Order Received')).toBeInTheDocument();
  });

  it('shows event reason and actor metadata when provided', () => {
    render(
      <OrderTimeline
        timeline={makeOrderTimeline({
          history: [
            {
              id: 'h-1',
              orderId: 'order-1',
              previousStatus: 'CONFIRMED',
              newStatus: 'CANCELLED',
              reason: 'Customer requested',
              actorType: 'CUSTOMER',
              actorId: 'cust-1',
              createdAt: '2026-08-18T12:00:00Z',
            },
          ],
        })}
      />
    );

    expect(screen.getByText('Customer requested')).toBeInTheDocument();
    expect(screen.getByText(/Updated by customer/)).toBeInTheDocument();
  });

  it('does not render actor metadata for SYSTEM actors', () => {
    render(<OrderTimeline timeline={makeOrderTimeline()} />);
    expect(screen.queryByText(/Updated by system/)).not.toBeInTheDocument();
  });

  it('renders an empty state when no events exist', () => {
    render(<OrderTimeline timeline={makeOrderTimeline({ history: [] })} />);
    expect(screen.getByText('No status events recorded for this order yet.')).toBeInTheDocument();
  });
});