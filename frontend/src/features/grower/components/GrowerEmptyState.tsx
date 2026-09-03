import { FC, ReactNode } from 'react';
import { EmptyState } from '../../../components/ui/EmptyState';
import { Button } from '../../../components/ui/Button';

export interface GrowerEmptyStateProps {
  title: string;
  description: string;
  actionLabel?: string;
  onAction?: () => void;
  icon?: ReactNode;
}

export const GrowerEmptyState: FC<GrowerEmptyStateProps> = ({
  title,
  description,
  actionLabel,
  onAction,
  icon,
}) => {
  return (
    <EmptyState
      title={title}
      description={description}
      icon={icon}
      action={
        actionLabel && onAction ? (
          <Button variant="primary" onClick={onAction}>
            {actionLabel}
          </Button>
        ) : undefined
      }
      style={{
        backgroundColor: '#0d231a',
        border: '1px solid rgba(255, 255, 255, 0.08)',
        borderRadius: '0.5rem',
        padding: '3rem 1.5rem',
      }}
    />
  );
};
