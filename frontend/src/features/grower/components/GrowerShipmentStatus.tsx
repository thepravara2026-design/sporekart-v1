import { FC } from 'react';
import { GrowerStatusBadge } from './GrowerStatusBadge';

export interface GrowerShipmentStatusProps {
  status: 'LABEL_CREATED' | 'PICKED_UP' | 'IN_TRANSIT' | 'OUT_FOR_DELIVERY' | 'DELIVERED' | 'FAILED';
}

export const GrowerShipmentStatus: FC<GrowerShipmentStatusProps> = ({ status }) => {
  const getVariant = () => {
    switch (status) {
      case 'DELIVERED':
        return 'success';
      case 'IN_TRANSIT':
      case 'OUT_FOR_DELIVERY':
      case 'PICKED_UP':
        return 'info';
      case 'LABEL_CREATED':
        return 'warning';
      case 'FAILED':
        return 'danger';
      default:
        return 'secondary';
    }
  };

  return <GrowerStatusBadge label={status.replace(/_/g, ' ')} variant={getVariant()} />;
};
