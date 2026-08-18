import { FC } from 'react';
import { Link } from 'react-router-dom';
import { Button } from '../../../components/ui/Button';
import { Card } from '../../../components/ui/Card';
import { Alert } from '../../../components/ui/Alert';
import { useReturnEligibility } from '../hooks/useReturnEligibility';
import { formatOrderDate } from '../utils/orderUtils';
import { PackageSearch } from 'lucide-react';

export interface OrderReturnHandoffProps {
  orderReference: string;
}

/**
 * Return handoff for an order. The backend eligibility check is the single
 * authority: the action is only surfaced when the order is actually eligible.
 * The full return workflow lives in the returns feature; here we navigate to it.
 */
export const OrderReturnHandoff: FC<OrderReturnHandoffProps> = ({ orderReference }) => {
  const { data, isLoading, isError } = useReturnEligibility(orderReference);

  if (isLoading) {
    return (
      <Card data-testid="order-return-handoff-loading">
        <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Checking return eligibility...
        </div>
      </Card>
    );
  }

  if (isError || !data) {
    return null;
  }

  if (!data.eligible) {
    return null;
  }

  return (
    <Card data-testid="order-return-handoff">
      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
          <PackageSearch size={20} style={{ color: 'var(--accent-primary)' }} aria-hidden="true" />
          <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>Returns</h3>
        </div>
        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          This order is eligible for return
          {data.returnDeadline ? ` until ${formatOrderDate(data.returnDeadline)}` : ''}.
        </p>
        <div>
          <Link to={`/orders/${orderReference}/return-request`}>
            <Button variant="outline" size="sm">
              Request Return
            </Button>
          </Link>
        </div>
      </div>
    </Card>
  );
};

export interface OrderSupportHandoffProps {
  orderReference: string;
}

/** Navigate-only support handoff — the support ticket flow ships in a later phase. */
export const OrderSupportHandoff: FC<OrderSupportHandoffProps> = ({ orderReference }) => (
  <Card data-testid="order-support-handoff">
    <h3 style={{ fontSize: '1.05rem', fontWeight: 700, margin: 0, color: 'var(--text-primary)' }}>
      Need help with this order?
    </h3>
    <Alert variant="info">
      For questions about order {orderReference}, our support team can assist you. Support
      tickets and live contact are available from your account.
    </Alert>
  </Card>
);