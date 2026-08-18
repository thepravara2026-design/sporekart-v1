import { FC, ReactNode, CSSProperties } from 'react';
import { spacing } from '../../design-system/tokens';

export interface GridProps {
  children: ReactNode;
  cols?: number;
  minWidth?: string;
  gap?: keyof typeof spacing | string;
  className?: string;
  style?: CSSProperties;
}

export const Grid: FC<GridProps> = ({
  children,
  cols,
  minWidth = '280px',
  gap = 6,
  className = '',
  style = {},
}) => {
  const gridTemplate = cols
    ? `repeat(${cols}, minmax(0, 1fr))`
    : `repeat(auto-fill, minmax(${minWidth}, 1fr))`;

  const gapValue = typeof gap === 'number' && gap in spacing ? spacing[gap as keyof typeof spacing] : String(gap);

  return (
    <div
      className={className}
      style={{
        display: 'grid',
        gridTemplateColumns: gridTemplate,
        gap: gapValue,
        width: '100%',
        ...style,
      }}
    >
      {children}
    </div>
  );
};
