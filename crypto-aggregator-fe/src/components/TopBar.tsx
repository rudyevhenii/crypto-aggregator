import { LivePrice, TradingPair, Exchange, ExchangeHealthDto } from '../api';

type Props = {
  exchange: Exchange | null;
  pair: TradingPair | null;
  livePrice: LivePrice | null;
  health: ExchangeHealthDto | null;
  onBack?: () => void;
};

export default function TopBar({ exchange, pair, livePrice, health, onBack }: Props) {
  if (!pair) return <div className="h-20 border-b border-[#2b3139] bg-[#181a20]"></div>;

  const displayPair = pair.replace('_', '/');
  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const sign = isPositive ? '+' : '';

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'CONNECTED': return 'bg-[#0ecb81] shadow-[0_0_8px_#0ecb81]';
      case 'RECONNECTING': return 'bg-[#fcd535] shadow-[0_0_8px_#fcd535] animate-pulse';
      case 'ERROR': return 'bg-[#f6465d] shadow-[0_0_8px_#f6465d]';
      case 'DISCONNECTED': default: return 'bg-[#848e9c]';
    }
  };

  // ❗ ДОДАНО: Функція для динамічного форматування ціни
  const formatPrice = (price?: number) => {
    if (price == null) return '—';
    let precision = 2;
    if (price > 1000) precision = 2;
    else if (price > 10) precision = 3;
    else if (price > 1) precision = 4;
    else if (price > 0.01) precision = 5;
    else precision = 6;

    return price.toLocaleString(undefined, {
      minimumFractionDigits: precision,
      maximumFractionDigits: precision,
    });
  };

  return (
    <div className="flex items-center px-6 h-20 border-b border-[#2b3139] bg-[#181a20] shrink-0">

      {/* Кнопка "Назад" */}
      {onBack && (
        <button
          onClick={onBack}
          className="mr-6 text-[#848e9c] hover:text-[#eaecef] transition-colors flex items-center gap-2 text-sm font-medium focus:outline-none"
        >
          <span className="text-lg">←</span>
          <span>Dashboard</span>
        </button>
      )}

      {/* Pair Info */}
      <div className="flex flex-col mr-8">
        <h1 className="text-2xl font-bold text-[#eaecef]">{displayPair}</h1>
        <span className="text-xs text-[#848e9c] underline decoration-dashed underline-offset-4 cursor-pointer">
          Bitcoin
        </span>
      </div>

      {/* ❗ ВИПРАВЛЕНО: Live Price */}
      <div className="flex flex-col mr-10">
        <div className={`text-2xl font-bold ${colorClass}`}>
          {formatPrice(livePrice?.lastPrice)}
        </div>
        <div className={`text-xs ${colorClass}`}>
          {livePrice?.priceChangePercent24h != null ? `${sign}${livePrice.priceChangePercent24h.toFixed(2)}%` : '—'}
        </div>
      </div>

      {/* ❗ ВИПРАВЛЕНО: 24h Stats (High, Low) */}
      <div className="flex space-x-8 text-xs">
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h High</span>
          <span className="text-[#eaecef] font-medium">{formatPrice(livePrice?.highPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h Low</span>
          <span className="text-[#eaecef] font-medium">{formatPrice(livePrice?.lowPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-[#848e9c] mb-1">24h Volume</span>
          <span className="text-[#eaecef] font-medium">
            {/* Volume зазвичай велике число, тому залишаємо 2 знаки або без дробів */}
            {livePrice?.volume24h ? livePrice.volume24h.toLocaleString(undefined, { maximumFractionDigits: 0 }) : '—'}
          </span>
        </div>
      </div>

      {/* Exchange Status */}
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