import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { OrderDto } from '../../../services/orderApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';

export interface OrderSummaryProps {
  order: OrderDto;
}

const Row: FC<{ label: string; value: string; accent?: boolean }> = ({ label, value, accent = false }) => (
  <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem' }}>
    <dt style={{ color: 'var(--text-secondary)', fontWeight: accent ? 800 : 500 }}>{label}</dt>
    <dd
      style={{
        margin: 0,
        fontWeight: accent ? 800 : 600,
        fontSize: accent ? '1.05rem' : undefined,
        color: accent ? 'var(--accent-primary)' : 'var(--text-primary)',
      }}
    >
      {value}
    </dd>
  </div>
);

/** Backend-authoritative totals breakdown for an order. */
export const OrderSummary: FC<OrderSummaryProps> = ({ order }) => {
  const currency = order.currency || 'INR';
  const totalUnits = order.items.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <Card data-testid="order-summary">
      <h3 style={{ fontSize: '1.25rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Order Summary</h3>
      <dl style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.9rem' }}>
        <Row label="Items" value={String(totalUnits)} />
        <Row label="Subtotal" value={formatPrice(order.subtotal, currency)} />
        {order.discountTotal > 0 && <Row label="Discount" value={`−${formatPrice(order.discountTotal, currency)}`} />}
        <Row label="Tax" value={formatPrice(order.taxTotal, currency)} />
        <Row label="Shipping" value={formatPrice(order.shippingFee, currency)} />
        <div style={{ borderTop: '1px solid var(--border-color)', paddingTop: '0.75rem' }}>
          <Row label="Order Total" value={formatPrice(order.grandTotal, currency)} accent />
        </div>
      </dl>
    </Card>
  );
};