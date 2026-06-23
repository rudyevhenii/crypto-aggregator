import { LivePrice, TradingPair, Exchange, ExchangeHealthDto } from '../api';

type Props = {
  exchange: Exchange | null;
  pair: TradingPair | null;
  livePrice: LivePrice | null;
  health: ExchangeHealthDto | null;
};

export default function TopBar({ exchange, pair, livePrice, health }: Props) {
  if (!pair) return <div className="h-20 border-b border-[#2b3139] bg-[#181a20]"></div>;

  const displayPair = pair.replace('_', '/');
  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const sign = isPositive ? '+' : '';

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'CONNECTED':
        return 'bg-[#0ecb81] shadow-[0_0_8px_#0ecb81]';
      case 'RECONNECTING':
        return 'bg-[#fcd535] shadow-[0_0_8px_#fcd535] animate-pulse';
      case 'ERROR':
        return 'bg-[#f6465d] shadow-[0_0_8px_#f6465d]';
      case 'DISCONNECTED':
      default:
        return 'bg-[#848e9c]';
    }
  };

  return (
    <div className="flex items-center px-6 h-20 border-b border-[#2b3139] bg-[#181a20] shrink-0">

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

      {/* Exchange Status */}
      {/* Використовуємо ml-auto, щоб відштовхнути бейдж у праву частину екрану */}
      <div className="flex items-center ml-auto">
        {exchange && (
          <div className="flex items-center gap-2 bg-[#2b3139] px-3 py-1.5 rounded-md">
            <div className={`w-2.5 h-2.5 rounded-full ${getStatusColor(health?.connectionStatus)}`} />
            <span className="text-[#eaecef] text-sm font-medium">{exchange}</span>
          </div>
        )}
      </div>
    </div>
  );
}