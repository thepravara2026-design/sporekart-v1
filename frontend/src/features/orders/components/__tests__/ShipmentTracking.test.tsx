import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ShipmentTracking } from '../ShipmentTracking';
import { makeShipmentTracking } from '../../__tests__/fixtures';

describe('ShipmentTracking (FD-13)', () => {
  it('renders tracking events with description, location, and timestamp', () => {
    render(<ShipmentTracking tracking={makeShipmentTracking()} />);

    expect(screen.getByText('Tracking')).toBeInTheDocument();
    expect(screen.getByTestId('shipment-tracking-events')).toBeInTheDocument();
    expect(screen.getByText('Shipment picked up')).toBeInTheDocument();
    expect(screen.getByText(/Bengaluru/)).toBeInTheDocument();
    expect(screen.getByText(/19 Aug 2026/)).toBeInTheDocument();
  });

  it('renders an empty state when no events exist', () => {
    render(<ShipmentTracking tracking={makeShipmentTracking({ timeline: [] })} />);
    expect(screen.getByText('No tracking events have been reported yet. Check back shortly.')).toBeInTheDocument();
  });
});
