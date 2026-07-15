import {type ReactNode} from 'react';

type AmbientGlowProps = {
  children?: ReactNode;
  className?: string;
};

export default function AmbientGlow({children, className = ''}: AmbientGlowProps) {
  return (
    <div className={`absolute inset-0 overflow-hidden pointer-events-none ${className}`}>
      {children}
    </div>
  );
}
