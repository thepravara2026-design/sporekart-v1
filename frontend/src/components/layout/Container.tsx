import { FC, ReactNode } from 'react';
import { layoutContainerWidths } from '../../design-system/tokens';

interface ContainerProps {
  children: ReactNode;
  maxWidth?: keyof typeof layoutContainerWidths;
  className?: string;
  style?: React.CSSProperties;
}

export const Container: FC<ContainerProps> = ({
  children,
  maxWidth = 'xl',
  className = '',
  style = {},
}) => {
  return (
    <div
      className={`container-primitive ${className}`}
      style={{
        maxWidth: layoutContainerWidths[maxWidth],
        margin: '0 auto',
        paddingLeft: '1.5rem',
        paddingRight: '1.5rem',
        width: '100%',
        ...style,
      }}
    >
      {children}
    </div>
  );
};
