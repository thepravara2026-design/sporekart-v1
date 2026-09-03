import { FC, useState } from 'react';
import { Card, CardHeader, CardTitle } from '../../../components/ui/Card';
import { Button } from '../../../components/ui/Button';
import { RadioGroup, RadioOption } from '../../../components/ui/RadioGroup';
import { CreditCard } from 'lucide-react';
import { PAYMENT_METHODS, PaymentMethod } from '../constants/checkoutConstants';

export interface CheckoutSubmitProps {
  isPlacingOrder?: boolean;
  onSubmit: (paymentMethod: PaymentMethod) => void;
  onBack: () => void;
}

const paymentOptions: RadioOption[] = PAYMENT_METHODS.map((method) => ({
  value: method.value,
  label: method.label,
  description: method.description,
}));

/**
 * CheckoutSubmit — the final payment step. Collects the payment method and
 * exposes a single Place Order CTA. The CTA and payment-method radios are
 * disabled while an order is being placed, and onSubmit is not called while a
 * submission is pending, which together prevent duplicate submissions.
 */
export const CheckoutSubmit: FC<CheckoutSubmitProps> = ({ isPlacingOrder = false, onSubmit, onBack }) => {
  const [paymentMethod, setPaymentMethod] = useState<PaymentMethod>(PAYMENT_METHODS[0].value);

  return (
    <Card data-testid="checkout-submit">
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
          onClick={() => {
            if (isPlacingOrder) return;
            onSubmit(paymentMethod);
          }}
        >
          {isPlacingOrder ? 'Placing Order...' : 'Place Order & Pay'}
        </Button>
        <Button type="button" variant="ghost" onClick={onBack} disabled={isPlacingOrder}>
          Back to Review
        </Button>
      </div>
    </Card>
  );
};