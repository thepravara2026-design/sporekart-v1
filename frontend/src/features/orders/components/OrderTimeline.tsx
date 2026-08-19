import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { OrderTimelineDto } from '../../../services/orderApi';
import { OrderStatusBadge } from './OrderStatusBadge';
import { formatOrderDate } from '../utils/orderUtils';
import { getOrderStatusLabel } from '../utils/orderStatus';

export interface OrderTimelineProps {
  timeline: OrderTimelineDto;
}

/** Chronological status-event timeline for an order. */
export const OrderTimeline: FC<OrderTimelineProps> = ({ timeline }) => {
  const events = [...timeline.history].reverse();

  return (
    <Card data-testid="order-timeline">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '1rem', flexWrap: 'wrap' }}>
        <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Order Timeline</h3>
        <OrderStatusBadge status={timeline.currentStatus} />
      </div>

      {events.length === 0 ? (
        <p style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', margin: 0 }}>
          No status events recorded for this order yet.
        </p>
      ) : (
        <ol
          data-testid="order-timeline-events"
          style={{
            listStyle: 'none',
            margin: 0,
            padding: 0,
            display: 'flex',
            flexDirection: 'column',
            gap: '1rem',
            borderLeft: '2px solid rgba(16, 185, 129, 0.25)',
            marginLeft: '0.6rem',
            paddingLeft: '1.4rem',
          }}
        >
          {events.map((event, index) => (
            <li key={event.id ?? index} data-testid="order-timeline-event" style={{ position: 'relative' }}>
              <span
                aria-hidden="true"
                style={{
                  position: 'absolute',
                  left: '-1.72rem',
                  top: '0.35rem',
                  width: '0.65rem',
                  height: '0.65rem',
                  borderRadius: '50%',
                  backgroundColor: index === 0 ? 'var(--accent-primary)' : 'var(--border-color)',
                  border: '2px solid var(--bg-surface)',
                  boxShadow: index === 0 ? '0 0 8px 0 rgba(16, 185, 129, 0.6)' : 'none',
                }}
              />
              <div style={{ fontWeight: 700, fontSize: '0.9rem', color: 'var(--text-primary)' }}>
                {getOrderStatusLabel(event.newStatus)}
              </div>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '0.15rem' }}>
                {formatOrderDate(event.createdAt)}
              </div>
              {event.reason && (
                <div style={{ fontSize: '0.82rem', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>{event.reason}</div>
              )}
              {event.actorType && event.actorType !== 'SYSTEM' && (
                <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '0.15rem' }}>
                  Updated by {event.actorType.toLowerCase()}
                  {event.actorId ? ` (${event.actorId})` : ''}
                </div>
              )}
            </li>
          ))}
        </ol>
      )}
    </Card>
  );
};