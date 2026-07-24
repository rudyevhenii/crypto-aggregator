import React from 'react';

type InputProps = {
  label?: string;
  error?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
} & React.InputHTMLAttributes<HTMLInputElement>;

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({label, error, leftIcon, rightIcon, className = '', ...props}, ref) => {
    return (
      <div className="w-full">
        {label && (
          <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">
            {label}
          </label>
        )}
        <div className="relative">
          {leftIcon && (
            <div className="absolute left-3 top-1/2 -translate-y-1/2 text-[#848e9c]">
              {leftIcon}
            </div>
          )}
          <input
            ref={ref}
            className={`
              w-full bg-[#0b0e11] border text-zinc-50 px-3 py-2 rounded-lg
              transition-all duration-200 ease-out
              placeholder:text-zinc-500
              focus:outline-none focus:border-[#fcd535] focus:shadow-[0_0_0_3px_rgba(252,213,53,0.1)]
              hover:border-zinc-600
              ${error ? 'border-[#f6465d]' : 'border-white/10'}
              ${leftIcon ? 'pl-10' : ''}
              ${rightIcon ? 'pr-10' : ''}
              ${className}
            `}
            {...props}
          />
          {rightIcon && (
            <div className="absolute right-3 top-1/2 -translate-y-1/2 text-[#848e9c]">
              {rightIcon}
            </div>
          )}
        </div>
        {error && (
          <p className="mt-1.5 text-xs text-[#f6465d]">{error}</p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';
