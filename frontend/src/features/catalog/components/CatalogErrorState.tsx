import { FC } from 'react';
import { Alert } from '../../../components/ui/Alert';
import { Button } from '../../../components/ui/Button';
import { RefreshCw } from 'lucide-react';

export interface CatalogErrorStateProps {
  title?: string;
  message?: string;
  onRetry?: () => void;
  className?: string;
}

export const CatalogErrorState: FC<CatalogErrorStateProps> = ({
  title = 'Unable to load catalog products',
  message = 'A network error occurred. Please verify backend connection.',
  onRetry,
  className = '',
}) => {
  return (
    <div className={`catalog-error-state ${className}`} style={{ width: '100%', margin: '1.5rem 0' }}>
      <Alert variant="error" title={title}>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
          <p style={{ margin: 0 }}>{message}</p>
          {onRetry && (
            <div>
              <Button size="sm" variant="outline" leftIcon={<RefreshCw size={14} />} onClick={onRetry}>
                Retry
              </Button>
            </div>
          )}
        </div>
      </Alert>
    </div>
  );
};
