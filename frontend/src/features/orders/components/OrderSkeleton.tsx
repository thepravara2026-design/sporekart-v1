import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';

/** Loading skeleton for the order history list. */
export const OrderSkeleton: FC = () => (
  <div data-testid="orders-loading" style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }} aria-label="Loading your orders">
    {[0, 1, 2].map((key) => (
      <Card key={key} style={{ padding: '1.25rem' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', gap: '1rem' }}>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', flex: 1 }}>
            <Skeleton width="180px" height="1rem" />
            <Skeleton width="240px" height="0.75rem" />
            <Skeleton width="120px" height="0.75rem" />
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem', alignItems: 'flex-end' }}>
            <Skeleton width="90px" height="1.4rem" borderRadius="999px" />
            <Skeleton width="100px" height="1rem" />
          </div>
        </div>
      </Card>
    ))}
  </div>
);