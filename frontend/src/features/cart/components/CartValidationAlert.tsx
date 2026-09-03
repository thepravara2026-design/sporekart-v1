import { FC } from 'react';
import { Alert } from '../../../components/ui/Alert';

export interface CartValidationAlertProps {
  title?: string;
  message: string;
  variant?: 'error' | 'warning';
}

/**
 * CartValidationAlert — cart-level validation/error banner (e.g. a failed
 * clear operation, a stale cart, or a checkout-preventing condition). Errors
 * are announced via the Alert's assertive live region.
 */
export const CartValidationAlert: FC<CartValidationAlertProps> = ({
  title = 'Cart update failed',
  message,
  variant = 'error',
}) => {
  return (
    <div data-testid="cart-validation-alert" style={{ marginBottom: '1rem' }}>
      <Alert variant={variant} title={title}>
        {message}
      </Alert>
    </div>
  );
};