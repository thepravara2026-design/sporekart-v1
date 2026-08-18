import { FC } from 'react';
import { GrowerOrderStatusType } from '../types/growerOrder';
import { getOrderStatusMeta } from '../utils/growerUtils';
import { GrowerStatusBadge } from './GrowerStatusBadge';

export interface GrowerOrderStatusProps {
  status: GrowerOrderStatusType;
}

export const GrowerOrderStatus: FC<GrowerOrderStatusProps> = ({ status }) => {
  const meta = getOrderStatusMeta(status);
  return <GrowerStatusBadge label={meta.label} variant={meta.variant} />;
};
