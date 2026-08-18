import React from 'react';
import { Badge } from '../../../components/ui/Badge';
import { InventorySyncStatus } from '../types/seller';

export interface InventoryStatusBadgeProps {
  status: InventorySyncStatus;
  testId?: string;
}

export const InventoryStatusBadge: React.FC<InventoryStatusBadgeProps> = ({
  status,
  testId = 'inventory-status-badge',
}) => {
  const getVariant = () => {
    switch (status) {
      case 'SYNCED':
        return 'success';
      case 'SYNCING':
        return 'info';
      case 'SYNC_ERROR':
        return 'danger';
      case 'OUT_OF_SYNC':
        return 'warning';
      default:
        return 'neutral';
    }
  };

  const getLabel = () => {
    switch (status) {
      case 'SYNCED':
        return 'Synced';
      case 'SYNCING':
        return 'Syncing...';
      case 'SYNC_ERROR':
        return 'Sync Error';
      case 'OUT_OF_SYNC':
        return 'Out of Sync';
      default:
        return status;
    }
  };

  return (
    <Badge data-testid={testId} variant={getVariant()} className="font-mono text-xs tracking-wide">
      {getLabel()}
    </Badge>
  );
};
