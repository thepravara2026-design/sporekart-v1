import { FC } from 'react';
import { CheckoutWarningResponse } from '../../../services/cartApi';
import { Alert } from '../../../components/ui/Alert';
import {
  CHECKOUT_WARNING_TYPES,
  getCheckoutWarningTitle,
  getCheckoutWarningVariant,
  isBlockingCheckoutWarning,
} from '../utils/checkoutUtils';

export interface CheckoutValidationAlertProps {
  warnings?: CheckoutWarningResponse[];
  revalidationNotice?: string | null;
}

/**
 * CheckoutValidationAlert — surfaces backend checkout warnings (price change,
 * item unavailable, limited stock) and the price-revalidation notice shown
 * when totals change after the customer reviewed them. Blocking warnings are
 * announced assertively; everything is actionable.
 */
export const CheckoutValidationAlert: FC<CheckoutValidationAlertProps> = ({
  warnings = [],
  revalidationNotice,
}) => {
  const blockingWarnings = warnings.filter((warning) => isBlockingCheckoutWarning(warning.type));
  const advisoryWarnings = warnings.filter((warning) => !isBlockingCheckoutWarning(warning.type));

  if (warnings.length === 0 && !revalidationNotice) return null;

  return (
    <div data-testid="checkout-validation-alert" style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
      {blockingWarnings.map((warning, index) => (
        <Alert key={`blocking-${warning.productId}-${index}`} variant="error" title={getCheckoutWarningTitle(warning.type)}>
          {warning.message}
        </Alert>
      ))}
      {advisoryWarnings.map((warning, index) => (
        <Alert
          key={`advisory-${warning.type}-${warning.productId}-${index}`}
          variant={getCheckoutWarningVariant(warning.type)}
          title={getCheckoutWarningTitle(warning.type)}
        >
          {warning.message}
        </Alert>
      ))}
      {revalidationNotice && (
        <Alert variant="warning" title="Order total changed">
          {revalidationNotice}
        </Alert>
      )}
      {blockingWarnings.length > 0 && warningHint(blockingWarnings)}
    </div>
  );
};

/** Actionable hint appended to blocking warnings (never a generic error). */
const warningHint = (warnings: CheckoutWarningResponse[]) => {
  const unavailable = warnings.filter((warning) => warning.type === CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE).length;
  const message =
    unavailable > 0
      ? 'One or more items in your order are no longer available. Return to your cart to review the items before proceeding.'
      : 'Please review the warnings above before continuing.';
  return (
    <p style={{ margin: 0, fontSize: '0.85rem', color: 'var(--text-secondary)' }} role="note">
      {message}
    </p>
  );
};