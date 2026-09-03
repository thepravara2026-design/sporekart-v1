import { FC } from 'react';
import { ProductStatus } from '../types/catalog';
import { Badge } from '../../../components/ui/Badge';
import { getStockAvailabilityInfo } from '../utils/catalogUtils';
import { CheckCircle, AlertTriangle, XCircle, Zap } from 'lucide-react';

export interface ProductAvailabilityProps {
  status: ProductStatus;
  availableStock?: number | null;
  className?: string;
}

export const ProductAvailability: FC<ProductAvailabilityProps> = ({ status, availableStock, className = '' }) => {
  const { state, label, badgeVariant } = getStockAvailabilityInfo(status, availableStock);

  const getIcon = () => {
    switch (state) {
      case 'NORMAL':
        return <CheckCircle size={14} />;
      case 'LIMITED':
        return <AlertTriangle size={14} />;
      case 'ORDER_NOW':
        return <Zap size={14} />;
      case 'OUT_OF_STOCK':
      default:
        return <XCircle size={14} />;
    }
  };

  const getCustomStyle = (): React.CSSProperties | undefined => {
    if (state === 'ORDER_NOW') {
      return {
        backgroundColor: 'rgba(249, 115, 22, 0.18)',
        color: '#f97316',
        border: '1px solid rgba(249, 115, 22, 0.4)',
        fontWeight: 700,
      };
    }
    return undefined;
  };

  return (
    <Badge variant={badgeVariant} icon={getIcon()} className={className} style={getCustomStyle()}>
      {label}
    </Badge>
  );
};
