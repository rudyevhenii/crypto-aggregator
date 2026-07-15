type PriceDisplayProps = {
  value?: number | string;
  currency?: string;
  change?: number;
  changePercent?: number;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
};

const sizeClasses = {
  sm: 'text-sm',
  md: 'text-base',
  lg: 'text-xl',
};

export default function PriceDisplay({
  value,
  currency = 'USDT',
  change,
  changePercent,
  size = 'md',
  className = '',
}: PriceDisplayProps) {
  const formattedValue = typeof value === 'number' ? value.toFixed(2) : value;

  const changeColor =
    change === undefined
      ? 'text-zinc-50'
      : change >= 0
        ? 'text-[#0ecb81]'
        : 'text-[#f6465d]';

  const changePrefix = change !== undefined && change >= 0 ? '+' : '';

  return (
    <div className={`flex flex-col ${className}`}>
      <span className={`font-mono font-semibold text-zinc-50 ${sizeClasses[size]}`}>
        {formattedValue} {currency}
      </span>
      {(change !== undefined || changePercent !== undefined) && (
        <span className={`font-mono text-xs ${changeColor}`}>
          {change !== undefined && `${changePrefix}${change.toFixed(2)} `}
          {changePercent !== undefined && `(${changePrefix}${changePercent.toFixed(2)}%)`}
        </span>
      )}
    </div>
  );
}
