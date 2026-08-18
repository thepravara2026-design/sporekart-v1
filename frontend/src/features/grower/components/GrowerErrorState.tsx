import { FC } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';

export interface GrowerErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export const GrowerErrorState: FC<GrowerErrorStateProps> = ({
  message = 'Failed to load grower operational data. Please verify network connectivity.',
  onRetry,
}) => {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem', margin: '1rem 0' }}>
      <Alert variant="error">{message}</Alert>
      {onRetry && (
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <Button size="sm" variant="secondary" onClick={onRetry}>
            Retry Request
          </Button>
        </div>
      )}
    </div>
  );
};
