import React from 'react';

type CardProps = {
  children: React.ReactNode;
  className?: string;
  hoverable?: boolean;
  glowColor?: string;
};

export const Card = ({children, className = '', hoverable = false, glowColor}: CardProps) => {
  return (
    <div
      className={`
        relative overflow-hidden
        glass-surface
        rounded-xl
        transition-all duration-300 ease-out
        ${hoverable ? 'hover:border-[#fcd535]/40 hover:shadow-[0_0_30px_rgba(252,213,53,0.08)]' : ''}
        ${className}
      `}
      style={glowColor ? {boxShadow: `0 0 40px ${glowColor}15`} : undefined}
    >
      {children}
    </div>
  );
};
