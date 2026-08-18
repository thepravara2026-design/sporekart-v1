import { FC } from 'react';
import { Card } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';

export const ProductDetailSkeleton: FC = () => {
  return (
    <Card style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '2rem', padding: '2rem' }}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
        <Skeleton height="320px" borderRadius="var(--radius-lg)" />
        <div style={{ display: 'flex', gap: '0.75rem' }}>
          <Skeleton width="72px" height="72px" borderRadius="var(--radius-md)" />
          <Skeleton width="72px" height="72px" borderRadius="var(--radius-md)" />
          <Skeleton width="72px" height="72px" borderRadius="var(--radius-md)" />
        </div>
      </div>
      <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
        <Skeleton width="30%" height="24px" />
        <Skeleton width="85%" height="36px" />
        <Skeleton width="20%" height="20px" />
        <Skeleton width="40%" height="40px" style={{ marginTop: '0.5rem' }} />
        <div style={{ display: 'flex', gap: '1rem', marginTop: '1rem' }}>
          <Skeleton width="120px" height="44px" borderRadius="var(--radius-md)" />
          <Skeleton width="180px" height="44px" borderRadius="var(--radius-md)" />
        </div>
        <Skeleton width="100%" height="100px" style={{ marginTop: '1.5rem' }} />
      </div>
    </Card>
  );
};
