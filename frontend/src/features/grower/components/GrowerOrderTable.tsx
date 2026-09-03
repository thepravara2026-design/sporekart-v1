import { FC } from 'react';
import { GrowerOrder, OrderTransitionPayload } from '../types/growerOrder';
import { GrowerOrderStatus } from './GrowerOrderStatus';
import { formatCurrency } from '../utils/growerUtils';
import { Button } from '../../../components/ui/Button';

export interface GrowerOrderTableProps {
  orders: GrowerOrder[];
  onTransitionOrder?: (payload: OrderTransitionPayload) => Promise<void>;
  isTransitioning?: boolean;
}

export const GrowerOrderTable: FC<GrowerOrderTableProps> = ({
  orders,
  onTransitionOrder,
  isTransitioning = false,
}) => {
  return (
    <div style={{ overflowX: 'auto', borderRadius: '0.5rem', border: '1px solid rgba(255, 255, 255, 0.08)' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', backgroundColor: '#0d231a', fontSize: '0.875rem' }}>
        <thead>
          <tr style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.1)', color: '#9ca3af', backgroundColor: 'rgba(0, 0, 0, 0.2)' }}>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Order Ref</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Customer</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Date</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Total Amount</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>Status</th>
            <th style={{ padding: '0.75rem 1rem', fontWeight: 600, textAlign: 'right' }}>Actions</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order) => (
            <tr key={order.id} style={{ borderBottom: '1px solid rgba(255, 255, 255, 0.05)', color: '#e5e7eb' }}>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 600, color: '#10b981' }}>{order.orderNumber}</td>
              <td style={{ padding: '0.75rem 1rem', color: '#d1d5db' }}>{order.customerEmail}</td>
              <td style={{ padding: '0.75rem 1rem', color: '#9ca3af' }}>{new Date(order.createdAt).toLocaleDateString()}</td>
              <td style={{ padding: '0.75rem 1rem', fontWeight: 600 }}>{formatCurrency(order.totalAmount)}</td>
              <td style={{ padding: '0.75rem 1rem' }}>
                <GrowerOrderStatus status={order.status} />
              </td>
              <td style={{ padding: '0.75rem 1rem', textAlign: 'right' }}>
                {onTransitionOrder && order.status === 'PROCESSING' && (
                  <Button
                    size="sm"
                    variant="primary"
                    isLoading={isTransitioning}
                    onClick={() => onTransitionOrder({ orderId: order.id, targetStatus: 'READY_FOR_FULFILMENT' })}
                  >
                    Mark Ready
                  </Button>
                )}
                {onTransitionOrder && order.status === 'READY_FOR_FULFILMENT' && (
                  <Button
                    size="sm"
                    variant="primary"
                    isLoading={isTransitioning}
                    onClick={() => onTransitionOrder({ orderId: order.id, targetStatus: 'SHIPPED' })}
                  >
                    Mark Shipped
                  </Button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};
