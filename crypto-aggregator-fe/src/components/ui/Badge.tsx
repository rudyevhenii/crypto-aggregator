import React from 'react';

type BadgeVariant = 'success' | 'warning' | 'error' | 'info' | 'neutral';

type BadgeProps = {
  variant?: BadgeVariant;
  children: React.ReactNode;
  className?: string;
  dot?: boolean;
};

const variantClasses: Record<BadgeVariant, string> = {
  success: 'bg-[#0ecb81]/10 text-[#0ecb81] border-[#0ecb81]/20',
  warning: 'bg-[#fcd535]/10 text-[#fcd535] border-[#fcd535]/20',
  error: 'bg-[#f6465d]/10 text-[#f6465d] border-[#f6465d]/20',
  info: 'bg-[#3b82f6]/10 text-[#3b82f6] border-[#3b82f6]/20',
  neutral: 'bg-white/5 text-zinc-400 border-white/10',
};

const dotColors: Record<BadgeVariant, string> = {
  success: 'bg-[#0ecb81]',
  warning: 'bg-[#fcd535]',
  error: 'bg-[#f6465d]',
  info: 'bg-[#3b82f6]',
  neutral: 'bg-zinc-400',
};

export const Badge = ({variant = 'neutral', children, className = '', dot = false}: BadgeProps) => {
  return (
    <span
      className={`
        inline-flex items-center gap-1.5
        px-2.5 py-1 rounded-full
        text-xs font-semibold border
        ${variantClasses[variant]}
        ${className}
      `}
    >
      {dot && <span className={`w-1.5 h-1.5 rounded-full ${dotColors[variant]}`}/>}
      {children}
    </span>
  );
};
