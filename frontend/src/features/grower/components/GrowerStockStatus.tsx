import { FC } from 'react';
import { getInventoryStatusMeta } from '../utils/growerUtils';
import { GrowerStatusBadge } from './GrowerStatusBadge';

export interface GrowerStockStatusProps {
  onHandQuantity: number;
  reservedQuantity: number;
  reorderPoint?: number;
}

export const GrowerStockStatus: FC<GrowerStockStatusProps> = ({
  onHandQuantity,
  reservedQuantity,
  reorderPoint = 10,
}) => {
  const meta = getInventoryStatusMeta(onHandQuantity, reservedQuantity, reorderPoint);
  const available = Math.max(0, onHandQuantity - reservedQuantity);

  return (
    <div style={{ display: 'inline-flex', alignItems: 'center', gap: '0.5rem' }}>
      <GrowerStatusBadge label={meta.label} variant={meta.variant} />
      <span style={{ fontSize: '0.75rem', color: '#9ca3af' }}>
        ({available} avail)
      </span>
    </div>
  );
};
