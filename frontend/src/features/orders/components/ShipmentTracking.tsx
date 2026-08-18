import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { ShipmentTrackingResponseDto } from '../../../services/shippingApi';
import { formatOrderDate } from '../utils/orderUtils';

export interface ShipmentTrackingProps {
  tracking: ShipmentTrackingResponseDto;
}

/** Courier scan event timeline for an order's shipment. */
export const ShipmentTracking: FC<ShipmentTrackingProps> = ({ tracking }) => {
  const events = tracking.timeline ?? [];

  return (
    <Card data-testid="shipment-tracking">
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Tracking</h3>

      {events.length === 0 ? (
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: 0 }}>
          No tracking events have been reported yet. Check back shortly.
        </p>
      ) : (
        <ol
          data-testid="shipment-tracking-events"
          style={{
            listStyle: 'none',
            margin: 0,
            padding: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem',
            borderLeft: '2px solid rgba(59, 130, 246, 0.25)',
            marginLeft: '0.6rem',
            paddingLeft: '1.4rem',
          }}
        >
          {events.map((event, index) => (
            <li key={event.id ?? event.providerEventId ?? index} data-testid="shipment-tracking-event" style={{ position: 'relative' }}>
              <span
                aria-hidden="true"
                style={{
                  position: 'absolute',
                  left: '-1.72rem',
                  top: '0.35rem',
                  width: '0.65rem',
                  height: '0.65rem',
                  borderRadius: '50%',
                  backgroundColor: index === 0 ? '#3b82f6' : 'var(--border-color)',
                  border: '2px solid var(--bg-surface)',
                }}
              />
              <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-primary)' }}>
                {event.description || event.providerStatus}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
                {formatOrderDate(event.occurredAt)}
                {event.location ? ` · ${event.location}` : ''}
              </div>
            </li>
          ))}
        </ol>
      )}
    </Card>
  );
};