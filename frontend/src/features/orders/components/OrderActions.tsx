import { FC, useState } from 'react';
import { Button } from '../../../components/ui/Button';
import { Dialog } from '../../../components/ui/Dialog';
import { Input } from '../../../components/ui/Input';
import { FormField } from '../../../components/ui/FormField';
import { OrderDto } from '../../../services/orderApi';
import { CANCELABLE_ORDER_STATUSES } from '../constants/orderConstants';
import { useCancelOrder } from '../hooks/useOrder';

export interface OrderActionsProps {
  order: OrderDto;
}

/**
 * Order-level customer actions. Cancellation is only surfaced for statuses the
 * backend OrderStatus.isCancellable() permits — the backend still enforces the
 * rule and rejects anything it does not allow.
 */
export const OrderActions: FC<OrderActionsProps> = ({ order }) => {
  const [cancelOpen, setCancelOpen] = useState(false);
  const [reason, setReason] = useState('');
  const cancelMutation = useCancelOrder();

  const cancellable = CANCELABLE_ORDER_STATUSES.includes(order.status as (typeof CANCELABLE_ORDER_STATUSES)[number]);

  if (!cancellable) return null;

  const handleCancel = () => {
    cancelMutation.mutate(
      { orderReference: order.orderNumber, reason: reason.trim() || undefined },
      { onSettled: () => setCancelOpen(false) }
    );
  };

  return (
    <div data-testid="order-actions">
      <Button
        variant="destructive"
        size="sm"
        onClick={() => setCancelOpen(true)}
        aria-haspopup="dialog"
      >
        Cancel Order
      </Button>

      <Dialog
        isOpen={cancelOpen}
        onClose={() => setCancelOpen(false)}
        title="Cancel this order?"
        description={`Order ${order.orderNumber} will be cancelled and any reserved stock released. This action cannot be undone.`}
      >
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleCancel();
          }}
        >
          <FormField label="Reason (optional)" htmlFor="order-cancel-reason">
            <Input
              id="order-cancel-reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="Why are you cancelling this order?"
            />
          </FormField>
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '1rem' }}>
            <Button variant="secondary" size="sm" onClick={() => setCancelOpen(false)}>
              Keep Order
            </Button>
            <Button variant="destructive" size="sm" type="submit" isLoading={cancelMutation.isPending}>
              Confirm Cancellation
            </Button>
          </div>
        </form>
      </Dialog>
    </div>
  );
};