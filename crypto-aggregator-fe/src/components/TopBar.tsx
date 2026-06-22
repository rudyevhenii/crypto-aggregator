import { LivePrice, TradingPair } from '../api';

type Props = {
  pair: TradingPair | null;
  livePrice: LivePrice | null;
};

export default function TopBar({ pair, livePrice }: Props) {
  if (!pair) return <div className="h-20 border-b border-[#2b3139] bg-[#181a20]"></div>;

  const displayPair = pair.replace('_', '/');
  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const sign = isPositive ? '+' : '';

  return (
    <div className="flex items-center px-6 h-20 border-b border-[#2b3139] bg-[#181a20]">
      {/* Pair Info */}
      <div className="flex flex-col mr-8">
        <h1 className="text-2xl font-bold text-[#eaecef]">{displayPair}</h1>
        <span className="text-xs text-[#848e9c] underline decoration-dashed underline-offset-4 cursor-pointer">
          Bitcoin
        </span>
      </div>

      {/* Live Price */}
      <div className="flex flex-col mr-10">
        <div className={`text-2xl font-bold ${colorClass}`}>
          {livePrice?.lastPrice ? livePrice.lastPrice.toLocaleString(undefined, { minimumFractionDigits: 2 }) : '—'}
        </div>
        <div className={`text-xs ${colorClass}`}>
          {livePrice?.priceChangePercent24h != null ? `${sign}${livePrice.priceChangePercent24h.toFixed(2)}%` : '—'}
        </div>
      </div>

      {/* 24h Stats */}
      <div className="flex space-x-8 text-xs">
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h High</span>
          <span className="text-[#eaecef] font-medium">{livePrice?.highPrice24h?.toLocaleString() ?? '—'}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h Low</span>
          <span className="text-[#eaecef] font-medium">{livePrice?.lowPrice24h?.toLocaleString() ?? '—'}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h Volume</span>
          <span className="text-[#eaecef] font-medium">{livePrice?.volume24h?.toLocaleString() ?? '—'}</span>
        </div>
      </div>
    </div>
  );
}