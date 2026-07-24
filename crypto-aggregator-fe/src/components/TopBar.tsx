import {ExchangeHealthDto, LivePrice} from '../api';
import {ArrowLeft} from 'lucide-react';
import {Badge} from './ui';

type Props = {
  exchange: string | null | undefined;
  pair: string | null | undefined;
  livePrice: LivePrice | null;
  health: ExchangeHealthDto | null;
  onBack?: () => void;
};

export default function TopBar({exchange, pair, livePrice, health, onBack}: Props) {
  if (!pair) return null;

  const displayPair = pair.replace('_', '/');
  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const sign = isPositive ? '+' : '';

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'CONNECTED':
        return 'bg-[#0ecb81] shadow-[0_0_8px_rgba(14,203,129,0.4)]';
      case 'RECONNECTING':
        return 'bg-[#fcd535] shadow-[0_0_8px_rgba(252,213,53,0.4)] animate-pulse';
      case 'ERROR':
        return 'bg-[#f6465d] shadow-[0_0_8px_rgba(246,70,93,0.4)]';
      case 'DISCONNECTED':
      default:
        return 'bg-[#848e9c]';
    }
  };

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
    <div className="flex items-center px-6 h-20 border-b border-white/5 glass-surface shrink-0 relative z-30">

      {/* Back Button */}
      {onBack && (
        <button
          onClick={onBack}
          className="mr-6 bg-white/5 hover:bg-white/10 backdrop-blur-md border border-white/10 transition-all rounded-full p-2 flex items-center justify-center group relative"
          title="Back to Overview"
        >
          <ArrowLeft size={18} className="text-zinc-400 group-hover:text-white transition-colors"/>
          <span className="absolute left-full ml-3 px-2 py-1 bg-[#181a20] border border-white/10 rounded-md text-xs text-zinc-300 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
            Back to Overview
          </span>
        </button>
      )}

      {/* Pair Info */}
      <div className="flex flex-col mr-8">
        <h1 className="text-2xl font-bold text-zinc-50 tracking-tight">{displayPair}</h1>
        <span className="text-xs text-zinc-400 underline decoration-dashed underline-offset-4 cursor-pointer hover:text-zinc-50 transition-colors">
          Bitcoin
        </span>
      </div>

      {/* Live Price */}
      <div className="flex flex-col mr-10">
        <div className={`text-2xl font-bold ${colorClass} tracking-tight`}>
          {formatPrice(livePrice?.lastPrice)}
        </div>
        <div className={`text-xs ${colorClass}`}>
          {livePrice?.priceChangePercent24h != null ? `${sign}${livePrice.priceChangePercent24h.toFixed(2)}%` : '—'}
        </div>
      </div>

      {/* 24h Stats */}
      <div className="flex space-x-8 text-xs">
        <div className="flex flex-col">
          <span className="text-zinc-400 mb-1">24h High</span>
          <span className="text-zinc-50 font-medium">{formatPrice(livePrice?.highPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-zinc-400 mb-1">24h Low</span>
          <span className="text-zinc-50 font-medium">{formatPrice(livePrice?.lowPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <span className="text-zinc-400 mb-1">24h Volume</span>
          <span className="text-zinc-50 font-medium">
            {livePrice?.volume24h ? livePrice.volume24h.toLocaleString(undefined, {maximumFractionDigits: 0}) : '—'}
          </span>
        </div>
      </div>

      {/* Exchange Status */}
      <div className="flex items-center ml-auto">
        {exchange && (
          <Badge variant="neutral" className="gap-2">
            <div className={`w-2 h-2 rounded-full ${getStatusColor(health?.connectionStatus)}`}/>
            <span className="text-zinc-50 text-sm font-medium">{exchange}</span>
          </Badge>
        )}
      </div>
    </div>
  );
}
