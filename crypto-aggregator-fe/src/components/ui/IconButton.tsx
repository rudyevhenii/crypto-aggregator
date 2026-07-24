import {type ReactNode, type ButtonHTMLAttributes} from 'react';

type IconButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  icon: ReactNode;
  tooltip?: string;
  variant?: 'default' | 'ghost' | 'danger';
};

const variantClasses: Record<string, string> = {
  default: 'text-zinc-400 hover:text-zinc-50 hover:bg-white/5',
  ghost: 'text-zinc-400 hover:text-zinc-50',
  danger: 'text-zinc-400 hover:text-[#f6465d] hover:bg-[#f6465d]/10',
};

export default function IconButton({
  icon,
  tooltip,
  variant = 'default',
  className = '',
  ...props
}: IconButtonProps) {
  return (
    <button
      type="button"
      title={tooltip}
      className={`
        relative inline-flex items-center justify-center
        w-8 h-8 rounded-lg transition-colors duration-200
        ${variantClasses[variant]}
        ${className}
      `}
      {...props}
    >
      {icon}
    </button>
  );
}
