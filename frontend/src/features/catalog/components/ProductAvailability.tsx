import { FC } from 'react';
import { ProductStatus } from '../types/catalog';
import { Badge } from '../../../components/ui/Badge';
import { getStatusBadgeVariant, getStatusLabel } from '../utils/catalogUtils';
import { CheckCircle, AlertTriangle, XCircle, Info } from 'lucide-react';

export interface ProductAvailabilityProps {
  status: ProductStatus;
  className?: string;
}

export const ProductAvailability: FC<ProductAvailabilityProps> = ({ status, className = '' }) => {
  const variant = getStatusBadgeVariant(status);
  const label = getStatusLabel(status);

  const getIcon = () => {
    switch (status) {
      case 'ACTIVE':
        return <CheckCircle size={14} />;
      case 'OUT_OF_STOCK':
        return <XCircle size={14} />;
      case 'DISCONTINUED':
      case 'ARCHIVED':
        return <AlertTriangle size={14} />;
      default:
        return <Info size={14} />;
    }
  };

  return (
    <Badge variant={variant} icon={getIcon()} className={className}>
      {label}
    </Badge>
  );
};
