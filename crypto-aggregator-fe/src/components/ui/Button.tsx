import React from 'react';

type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
type ButtonSize = 'sm' | 'md' | 'lg';

type ButtonProps = {
  variant?: ButtonVariant;
  size?: ButtonSize;
  isLoading?: boolean;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
  children: React.ReactNode;
} & React.ButtonHTMLAttributes<HTMLButtonElement>;

const variantClasses: Record<ButtonVariant, string> = {
  primary:
    'bg-[#fcd535] text-[#09090b] font-semibold hover:bg-[#e0bc2e] focus-visible:ring-2 focus-visible:ring-[#fcd535]/50 focus-visible:ring-offset-2 focus-visible:ring-offset-[#09090b] shadow-[0_0_20px_rgba(252,213,53,0.3)]',
  secondary:
    'bg-white/5 text-zinc-50 font-medium hover:bg-white/10 border border-white/10 focus-visible:ring-2 focus-visible:ring-zinc-400/50 focus-visible:ring-offset-2 focus-visible:ring-offset-[#09090b]',
  ghost:
    'bg-transparent text-zinc-400 hover:text-zinc-50 hover:bg-white/5 focus-visible:ring-2 focus-visible:ring-zinc-400/50 focus-visible:ring-offset-2 focus-visible:ring-offset-[#09090b]',
  danger:
    'bg-[#f6465d]/10 text-[#f6465d] border border-[#f6465d]/50 hover:bg-[#f6465d]/20 focus-visible:ring-2 focus-visible:ring-[#f6465d]/50 focus-visible:ring-offset-2 focus-visible:ring-offset-[#09090b]',
};

const sizeClasses: Record<ButtonSize, string> = {
  sm: 'px-3 py-1.5 text-xs rounded-md',
  md: 'px-4 py-2 text-sm rounded-lg',
  lg: 'px-8 py-3.5 text-base rounded-xl',
};

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({variant = 'primary', size = 'md', isLoading = false, leftIcon, rightIcon, children, className = '', disabled, ...props}, ref) => {
    return (
      <button
        ref={ref}
        disabled={disabled || isLoading}
        className={`
          inline-flex items-center justify-center gap-2
          transition-all duration-200 ease-out
          disabled:opacity-50 disabled:cursor-not-allowed
          active:scale-[0.98]
          ${variantClasses[variant]}
          ${sizeClasses[size]}
          ${className}
        `}
        {...props}
      >
        {isLoading && (
          <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
        )}
        {!isLoading && leftIcon && <span className="flex-shrink-0">{leftIcon}</span>}
        {children}
        {!isLoading && rightIcon && <span className="flex-shrink-0">{rightIcon}</span>}
      </button>
    );
  }
);

Button.displayName = 'Button';
