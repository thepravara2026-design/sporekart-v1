import { FC } from 'react';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';

/**
 * CheckoutSkeleton — loading state for checkout initialization. Mirrors the
 * real checkout layout (form column + summary column) to avoid layout shift.
 */
export const CheckoutSkeleton: FC = () => {
  return (
    <div data-testid="checkout-skeleton">
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', alignItems: 'start' }}>
        <Card>
          <CardHeader>
            <CardTitle>
              <Skeleton height="1.1rem" width="40%" />
            </CardTitle>
          </CardHeader>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
            <Skeleton height="1rem" width="90%" />
            <Skeleton height="1rem" width="75%" />
            <Skeleton height="1rem" width="85%" />
            <Skeleton height="1rem" width="60%" />
          </div>
        </Card>
        <Card>
          <CardHeader>
            <CardTitle>
              <Skeleton height="1.1rem" width="45%" />
            </CardTitle>
          </CardHeader>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.9rem' }}>
            <Skeleton height="1rem" width="70%" />
            <Skeleton height="1rem" width="55%" />
            <Skeleton height="1.4rem" width="80%" />
          </div>
        </Card>
      </div>
    </div>
  );
};