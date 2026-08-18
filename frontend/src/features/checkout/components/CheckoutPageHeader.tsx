import { FC } from 'react';

export interface CheckoutPageHeaderProps {
  customerName?: string;
}

/**
 * CheckoutPageHeader — brief intro under the page title guiding the customer
 * through the checkout flow.
 */
export const CheckoutPageHeader: FC<CheckoutPageHeaderProps> = ({ customerName }) => {
  return (
    <div data-testid="checkout-page-header" style={{ marginBottom: '1.5rem' }}>
      <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.95rem' }}>
        {customerName
          ? `${customerName}, review your delivery details and place your order securely.`
          : 'Review your delivery details and place your order securely.'}
      </p>
    </div>
  );
};