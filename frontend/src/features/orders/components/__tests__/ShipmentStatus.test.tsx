import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ShipmentStatus } from '../ShipmentStatus';
import { makeShipment } from '../../__tests__/fixtures';

describe('ShipmentStatus (FD-13)', () => {
  it('renders shipment fields: reference, courier, tracking number, AWB', () => {
    render(<ShipmentStatus shipment={makeShipment()} />);

    expect(screen.getByText('Shipment')).toBeInTheDocument();
    expect(screen.getByText('In Transit')).toBeInTheDocument();
    expect(screen.getByText('SHP-2026-0001')).toBeInTheDocument();
    expect(screen.getByText('Delhivery')).toBeInTheDocument();
    expect(screen.getByText('TRK-001')).toBeInTheDocument();
    expect(screen.getByText('AWB-123456')).toBeInTheDocument();
  });

  it('falls back to the AWB when no tracking number exists', () => {
    render(<ShipmentStatus shipment={makeShipment({ trackingNumber: undefined })} />);
    expect(screen.getAllByText('AWB-123456').length).toBeGreaterThanOrEqual(2);
  });

  it('renders the delivered timestamp when present', () => {
    render(
      <ShipmentStatus shipment={makeShipment({ status: 'DELIVERED', deliveredAt: '2026-08-20T10:00:00Z' })} />
    );
    // "Delivered" appears as the status label and as the delivered-at field label.
    expect(screen.getAllByText('Delivered').length).toBeGreaterThanOrEqual(2);
  });

  it('never renders an external tracking link (backend has no tracking URL)', () => {
    const { container } = render(<ShipmentStatus shipment={makeShipment()} />);
    expect(container.querySelector('a')).toBeNull();
  });
});
