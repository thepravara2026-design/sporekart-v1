import { FC, useState } from 'react';
import { Dialog } from '../../../components/ui/Dialog';
import { Button } from '../../../components/ui/Button';
import { Input } from '../../../components/ui/Input';
import { Alert } from '../../../components/ui/Alert';
import { StockAdjustmentPayload } from '../types/growerInventory';

export interface GrowerStockAdjustDialogProps {
  isOpen: boolean;
  sku: string;
  currentOnHand: number;
  onClose: () => void;
  onConfirm: (payload: StockAdjustmentPayload) => Promise<void>;
  isLoading?: boolean;
}

export const GrowerStockAdjustDialog: FC<GrowerStockAdjustDialogProps> = ({
  isOpen,
  sku,
  currentOnHand,
  onClose,
  onConfirm,
  isLoading = false,
}) => {
  const [newOnHand, setNewOnHand] = useState<number>(currentOnHand);
  const [reason, setReason] = useState<string>('Physical stock count update');
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newOnHand < 0) {
      setError('On-hand quantity cannot be negative.');
      return;
    }
    if (!reason.trim()) {
      setError('Adjustment reason is required.');
      return;
    }

    try {
      setError(null);
      await onConfirm({ sku, newOnHandQuantity: newOnHand, reason });
      onClose();
    } catch (err: unknown) {
      const errorObj = err as { response?: { status?: number }; status?: number; message?: string };
      if (errorObj?.response?.status === 409 || errorObj?.status === 409) {
        setError('Inventory was updated elsewhere. Please refresh and try again.');
      } else {
        setError(errorObj?.message || 'Failed to update stock quantity.');
      }
    }
  };

  return (
    <Dialog isOpen={isOpen} onClose={onClose} title={`Adjust Stock: ${sku}`}>
      <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '1rem', color: '#e5e7eb' }}>
        {error && <Alert variant="error">{error}</Alert>}

        <div>
          <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
            Current On-Hand
          </label>
          <Input type="number" value={currentOnHand} disabled style={{ backgroundColor: 'rgba(255,255,255,0.05)', color: '#9ca3af' }} />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
            New On-Hand Quantity
          </label>
          <Input
            type="number"
            min={0}
            value={newOnHand}
            onChange={(e) => setNewOnHand(parseInt(e.target.value, 10) || 0)}
            required
          />
        </div>

        <div>
          <label style={{ display: 'block', fontSize: '0.875rem', fontWeight: 500, marginBottom: '0.25rem' }}>
            Adjustment Reason
          </label>
          <Input
            type="text"
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            placeholder="e.g. Audit, Harvest addition, Defect removal"
            required
          />
        </div>

        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '0.75rem', marginTop: '0.5rem' }}>
          <Button type="button" variant="secondary" onClick={onClose} disabled={isLoading}>
            Cancel
          </Button>
          <Button type="submit" variant="primary" isLoading={isLoading}>
            Confirm Adjustment
          </Button>
        </div>
      </form>
    </Dialog>
  );
};
