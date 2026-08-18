import { FC } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';

export interface OrderErrorStateProps {
  message: string;
  onRetry?: () => void;
}

/** Error state for order history/detail loads with an optional retry. */
export const OrderErrorState: FC<OrderErrorStateProps> = ({ message, onRetry }) => (
  <div data-testid="orders-error">
    <Alert variant="error" title="Unable to load orders">
      {message}
      {onRetry && (
        <div style={{ marginTop: '0.75rem' }}>
          <Button variant="secondary" size="sm" onClick={onRetry}>
            Try Again
          </Button>
        </div>
      )}
    </Alert>
  </div>
);