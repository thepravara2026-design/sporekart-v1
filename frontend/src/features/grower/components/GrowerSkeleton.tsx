import { FC } from 'react';
import { Skeleton } from '../../../components/ui/Skeleton';

export interface GrowerSkeletonProps {
  type?: 'card' | 'table' | 'dashboard' | 'form';
  count?: number;
}

export const GrowerSkeleton: FC<GrowerSkeletonProps> = ({ type = 'card', count = 3 }) => {
  if (type === 'dashboard') {
    return (
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem', width: '100%' }}>
        {Array.from({ length: 4 }).map((_, i) => (
          <Skeleton key={i} style={{ height: '110px', borderRadius: '0.5rem', backgroundColor: 'rgba(255, 255, 255, 0.05)' }} />
        ))}
      </div>
    );
  }

  if (type === 'table') {
    return (
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', width: '100%' }}>
        {Array.from({ length: count }).map((_, i) => (
          <Skeleton key={i} style={{ height: '48px', borderRadius: '0.375rem', backgroundColor: 'rgba(255, 255, 255, 0.05)' }} />
        ))}
      </div>
    );
  }

  return (
    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '1rem', width: '100%' }}>
      {Array.from({ length: count }).map((_, i) => (
        <Skeleton key={i} style={{ height: '180px', borderRadius: '0.5rem', backgroundColor: 'rgba(255, 255, 255, 0.05)' }} />
      ))}
    </div>
  );
};
