import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';
import { Grid } from '../../../components/layout/Grid';

/**
 * CartSkeleton — loading placeholder matching the final cart layout
 * (items column + summary column). Decorative skeletons are aria-hidden and
 * dimensions are fixed to prevent layout shift.
 */
export const CartSkeleton: FC = () => {
  return (
    <div data-testid="cart-loading" aria-busy="true" aria-label="Loading your cart">
      <Grid cols={1} style={{ maxWidth: '560px' }}>
        <Card aria-hidden="true">
          <div style={{ display: 'flex', gap: '1rem' }}>
            <Skeleton width="88px" height="66px" borderRadius="var(--radius-md)" />
            <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <Skeleton width="60%" height="1.1rem" />
              <Skeleton width="30%" height="0.8rem" />
              <Skeleton width="45%" height="2.5rem" />
            </div>
          </div>
          <div style={{ marginTop: '1rem' }}>
            <Skeleton width="100%" height="1.25rem" />
          </div>
        </Card>
      </Grid>
      <div style={{ maxWidth: '560px', marginTop: '1rem' }}>
        <Grid cols={1}>
          <Card aria-hidden="true">
            <Skeleton width="50%" height="1.25rem" />
            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.6rem', marginTop: '1rem' }}>
              <Skeleton width="100%" height="0.9rem" />
              <Skeleton width="100%" height="0.9rem" />
              <Skeleton width="100%" height="2.75rem" />
            </div>
          </Card>
        </Grid>
      </div>
    </div>
  );
};