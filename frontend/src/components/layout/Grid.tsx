import { FC, ReactNode } from 'react';
import { spacing } from '../../design-system/tokens';

interface GridProps {
  children: ReactNode;
  cols?: number;
  minWidth?: string;
  gap?: keyof typeof spacing;
  style?: React.CSSProperties;
}

export const Grid: FC<GridProps> = ({
  children,
  cols,
  minWidth = '280px',
  gap = 6,
  style = {},
}) => {
  const gridTemplate = cols
    ? `repeat(${cols}, minmax(0, 1fr))`
    : `repeat(auto-fill, minmax(${minWidth}, 1fr))`;

  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: gridTemplate,
        gap: spacing[gap],
        width: '100%',
        ...style,
      }}
    >
      {children}
    </div>
  );
};
