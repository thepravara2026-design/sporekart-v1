import { FC, ReactNode } from 'react';
import { spacing } from '../../design-system/tokens';

interface InlineProps {
  children: ReactNode;
  gap?: keyof typeof spacing;
  align?: 'flex-start' | 'center' | 'flex-end' | 'baseline';
  justify?: 'flex-start' | 'center' | 'flex-end' | 'space-between';
  wrap?: boolean;
  style?: React.CSSProperties;
}

export const Inline: FC<InlineProps> = ({
  children,
  gap = 4,
  align = 'center',
  justify = 'flex-start',
  wrap = true,
  style = {},
}) => {
  return (
    <div
      style={{
        display: 'flex',
        flexDirection: 'row',
        gap: spacing[gap],
        alignItems: align,
        justifyContent: justify,
        flexWrap: wrap ? 'wrap' : 'nowrap',
        ...style,
      }}
    >
      {children}
    </div>
  );
};
