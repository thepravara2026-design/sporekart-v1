import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { RefreshCw } from 'lucide-react';
import { ApiError } from '../../../services/apiError';

export interface CheckoutErrorStateProps {
  error: Error | null;
  onRetry: () => void;
}

/**
 * CheckoutErrorState — retryable checkout failure. Shows a safe, user-facing
 * message (never stack traces) and preserves the backend requestId as a
 * diagnostic reference when provided.
 */
export const CheckoutErrorState: FC<CheckoutErrorStateProps> = ({ error, onRetry }) => {
  const isApiError = error instanceof ApiError;
  const message =
    isApiError && error.message
      ? error.message
      : 'A network error occurred. Please verify your connection and try again.';

  return (
    <div data-testid="checkout-error">
      <Alert variant="error" title="Unable to start checkout">
        <p style={{ margin: 0 }}>{message}</p>
        {isApiError && error.requestId && (
          <p style={{ margin: '0.25rem 0 0', fontSize: '0.75rem', opacity: 0.8 }}>Reference: {error.requestId}</p>
        )}
        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.75rem', flexWrap: 'wrap' }}>
          <Button size="sm" variant="primary" leftIcon={<RefreshCw size={14} />} onClick={onRetry}>
            Retry
          </Button>
          <Link to="/cart" className="btn btn-secondary btn-sm">
            Return to Cart
          </Link>
        </div>
      </Alert>
    </div>
  );
};