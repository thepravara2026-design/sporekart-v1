import { FC } from 'react';
import { Card, CardHeader, CardContent, CardFooter } from '../../../components/ui/Card';
import { Skeleton } from '../../../components/ui/Skeleton';

export const ProductCardSkeleton: FC = () => {
  return (
    <Card style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Skeleton height="160px" borderRadius="var(--radius-md)" />
      <CardHeader style={{ marginTop: '0.75rem' }}>
        <Skeleton width="60%" height="20px" />
        <Skeleton width="90%" height="24px" style={{ marginTop: '0.5rem' }} />
      </CardHeader>
      <CardContent style={{ flex: 1, marginTop: '0.5rem' }}>
        <Skeleton width="100%" height="16px" />
        <Skeleton width="80%" height="16px" style={{ marginTop: '0.35rem' }} />
        <Skeleton width="40%" height="28px" style={{ marginTop: '1rem' }} />
      </CardContent>
      <CardFooter style={{ marginTop: 'auto', paddingTop: '1rem' }}>
        <Skeleton width="100%" height="40px" borderRadius="var(--radius-md)" />
      </CardFooter>
    </Card>
  );
};
