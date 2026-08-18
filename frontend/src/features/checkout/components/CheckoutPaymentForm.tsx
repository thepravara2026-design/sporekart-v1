import { FC, useState } from 'react';
import { CheckoutPreviewResponse } from '../../../services/cartApi';
import { formatPrice } from '../../catalog/utils/catalogUtils';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { RadioGroup, RadioOption } from '../../../components/ui/RadioGroup';
import { CreditCard } from 'lucide-react';
import { PAYMENT_METHODS, PaymentMethod } from '../constants/checkoutConstants';
import { CHECKOUT_WARNING_TYPES } from '../utils/checkoutUtils';

export interface CheckoutPaymentFormProps {
  preview?: CheckoutPreviewResponse;
  isPreviewLoading?: boolean;
  isPlacingOrder?: boolean;
  onSubmit: (paymentMethod: PaymentMethod) => void;
  onBack: () => void;
}

const paymentOptions: RadioOption[] = PAYMENT_METHODS.map((method) => ({
  value: method.value,
  label: method.label,
  description: method.description,
}));

const getWarningVariant = (type: string): 'warning' | 'error' | 'info' => {
  if (type === CHECKOUT_WARNING_TYPES.ITEM_UNAVAILABLE) return 'error';
  if (type === CHECKOUT_WARNING_TYPES.PRICE_CHANGED) return 'warning';
  return 'info';
};

/**
 * CheckoutPaymentForm — payment step. Shows the server-authoritative order
 * summary (from the checkout preview) alongside payment method selection and
 * the Place Order CTA. Any checkout warnings (e.g. a product price changed
 * since it was added) are surfaced before the customer commits.
 */
export const CheckoutPaymentForm: FC<CheckoutPaymentFormProps> = ({
  preview,
  isPreviewLoading = false,
  isPlacingOrder = false,
  onSubmit,
  onBack,
}) => {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PAYMENT_METHODS[0].value);

  const breakdown = preview?.breakdown;
  const currency = breakdown?.currency ?? 'INR';
  const isComputing = isPreviewLoading && !breakdown;

  return (
    <div data-testid="checkout-payment-form">
      {preview && preview.warnings.length > 0 && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginBottom: '1.25rem' }}>
          {preview.warnings.map((warning, index) => (
            <Alert key={`${warning.type}-${warning.productId}-${index}`} variant={getWarningVariant(warning.type)} title={warning.type}>
              {warning.message}
            </Alert>
          ))}
        </div>
      )}

      <div className="checkout-layout" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '1.5rem', alignItems: 'start' }}>
        <Card>
          <CardHeader>
            <CardTitle>Payment Method</CardTitle>
          </CardHeader>
          <RadioGroup
            name="payment-method"
            label="Select how you would like to pay"
            options={paymentOptions}
            value={paymentMethod}
            onChange={(value) => setPaymentMethod(value as PaymentMethod)}
            disabled={isPlacingOrder}
          />
          <div style={{ marginTop: '1.5rem', display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            <Button
              type="button"
              size="lg"
              fullWidth
              isLoading={isPlacingOrder}
              leftIcon={<CreditCard size={18} />}
              onClick={() => onSubmit(paymentMethod)}
            >
              {isPlacingOrder ? 'Placing Order...' : 'Place Order & Pay'}
            </Button>
            <Button type="button" variant="ghost" onClick={onBack} disabled={isPlacingOrder}>
              Back to Shipping
            </Button>
          </div>
        </Card>

        {breakdown ? (
          <Card data-testid="checkout-order-summary">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <dl style={{ margin: 0, display: 'flex', flexDirection: 'column', gap: '0.6rem', fontSize: '0.9rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <dt style={{ color: 'var(--text-secondary)' }}>Items ({preview?.items.reduce((sum, item) => sum + item.quantity, 0)})</dt>
                <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-subtotal">
                  {formatPrice(breakdown.subtotal, currency)}
                </dd>
              </div>
              {breakdown.discountTotal > 0 && (
                <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <dt style={{ color: 'var(--text-secondary)' }}>Discount</dt>
                  <dd style={{ margin: 0, fontWeight: 600, color: '#10b981' }} data-testid="checkout-summary-discount">
                    −{formatPrice(breakdown.discountTotal, currency)}
                  </dd>
                </div>
              )}
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <dt style={{ color: 'var(--text-secondary)' }}>Tax</dt>
                <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-tax">
                  {formatPrice(breakdown.taxTotal, currency)}
                </dd>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <dt style={{ color: 'var(--text-secondary)' }}>Shipping</dt>
                <dd style={{ margin: 0, fontWeight: 600 }} data-testid="checkout-summary-shipping">
                  {formatPrice(breakdown.shippingFee, currency)}
                </dd>
              </div>
              <div
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  fontSize: '1.05rem',
                  borderTop: '1px solid var(--border-color)',
                  paddingTop: '0.75rem',
                  marginTop: '0.25rem',
                }}
              >
                <dt style={{ fontWeight: 800 }}>Total</dt>
                <dd style={{ margin: 0, fontWeight: 800, color: 'var(--accent-primary)' }} data-testid="checkout-summary-total">
                  {formatPrice(breakdown.grandTotal, currency)}
                </dd>
              </div>
            </dl>
          </Card>
        ) : (
          <Card data-testid="checkout-order-summary">
            <CardHeader>
              <CardTitle>Order Summary</CardTitle>
            </CardHeader>
            <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)' }} role="status">
              {isComputing ? 'Calculating your order totals...' : 'Order totals are calculated by the store.'}
            </p>
          </Card>
        )}
      </div>
    </div>
  );
};