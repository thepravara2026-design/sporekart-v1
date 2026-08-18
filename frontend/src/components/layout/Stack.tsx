import { FC, ReactNode } from 'react';
import { spacing } from '../../design-system/tokens';

interface StackProps {
  children: ReactNode;
  gap?: keyof typeof spacing;
  align?: 'flex-start' | 'center' | 'flex-end' | 'stretch';
  style?: React.CSSProperties;
}

export const Stack: FC<StackProps> = ({
  children,
  gap = 4,
  align = 'stretch',
  style = {},
}) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'column',
        gap: spacing[gap],
        alignItems: align,
        ...style,
      }}
    >
      {children}
    </div>
  );
};
