import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { RefreshCw } from 'lucide-react';
import { ApiError } from '../../../services/apiError';

export interface CartErrorStateProps {
  error: Error | null;
  onRetry: () => void;
}

/**
 * CartErrorState — retryable cart fetch failure. Shows a safe, user-facing
 * message (never raw stack traces or internal exception text). A requestId is
 * preserved as a diagnostic reference when the backend provides one.
 */
export const CartErrorState: FC<CartErrorStateProps> = ({ error, onRetry }) => {
  const isApiError = error instanceof ApiError;
  const message =
    isApiError && error.message
      ? error.message
      : 'A network error occurred. Please verify your connection and try again.';

  return (
    <div data-testid="cart-error">
      <Alert variant="error" title="Unable to load your cart">
        <p style={{ margin: 0 }}>{message}</p>
        {isApiError && error.requestId && (
          <p style={{ margin: '0.25rem 0 0', fontSize: '0.75rem', opacity: 0.8 }}>
            Reference: {error.requestId}
          </p>
        )}
        <div style={{ display: 'flex', gap: '0.75rem', marginTop: '0.75rem', flexWrap: 'wrap' }}>
          <Button size="sm" variant="primary" leftIcon={<RefreshCw size={14} />} onClick={onRetry}>
            Retry
          </Button>
          <Link to="/products" className="btn btn-secondary btn-sm">
            Continue Shopping
          </Link>
        </div>
      </Alert>
    </div>
  );
};