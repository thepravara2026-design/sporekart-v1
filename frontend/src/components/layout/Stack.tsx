import { FC, ReactNode, CSSProperties } from 'react';
import { spacing } from '../../design-system/tokens';

export interface StackProps {
  children: ReactNode;
  gap?: keyof typeof spacing | string;
  align?: 'flex-start' | 'center' | 'flex-end' | 'stretch';
  className?: string;
  style?: CSSProperties;
}

export const Stack: FC<StackProps> = ({
  children,
  gap = 4,
  align = 'stretch',
  className = '',
  style = {},
}) => {
  const gapValue = typeof gap === 'number' && gap in spacing ? spacing[gap as keyof typeof spacing] : String(gap);

  return (
    <div
      className={className}
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: gapValue,
        alignItems: align,
        ...style,
      }}
    >
      {children}
    </div>
  );
};
