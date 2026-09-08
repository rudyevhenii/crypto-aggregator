import {ExchangeHealthDto, LivePrice} from '../api';
import {ArrowLeft, Info} from 'lucide-react';
import {Badge} from './ui';
import {formatPrice, formatVolume} from '../utils/format';

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
  const pillClass = isPositive ? 'bg-[#0ecb81]/10 text-[#0ecb81]' : 'bg-[#f6465d]/10 text-[#f6465d]';

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

  return (
    <div className="flex items-center px-4 h-16 border-b border-white/5 glass-surface shrink-0 relative z-30">

      {/* Back Button */}
      {onBack && (
        <button
          onClick={onBack}
          className="mr-4 bg-white/5 hover:bg-white/10 backdrop-blur-md border border-white/10 transition-all rounded-full p-2 flex items-center justify-center group relative"
          title="Back to Overview"
        >
          <ArrowLeft size={18} className="text-zinc-400 group-hover:text-white transition-colors"/>
          <span className="absolute left-full ml-3 px-2 py-1 bg-[#181a20] border border-white/10 rounded-md text-xs text-zinc-300 whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none">
            Back to Overview
          </span>
        </button>
      )}

      {/* Pair Info */}
      <div className="flex flex-col mr-6">
        <h1 className="text-lg font-bold text-zinc-50 tracking-tight">{displayPair}</h1>
        <span className="text-[11px] text-zinc-400 underline decoration-dashed underline-offset-4 cursor-pointer hover:text-zinc-50 transition-colors">
          Bitcoin
        </span>
      </div>

      {/* Live Price */}
      <div className="flex flex-col mr-6">
        <div className={`text-2xl font-bold ${colorClass} tracking-tight leading-none`}>
          {formatPrice(livePrice?.lastPrice)}
        </div>
        <div className={`mt-1 inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold ${pillClass}`}>
          {livePrice?.priceChangePercent24h != null ? `${sign}${livePrice.priceChangePercent24h.toFixed(2)}%` : '—'}
        </div>
      </div>

      {/* 24h Stats */}
      <div className="flex items-center gap-8 text-sm">
        <div className="flex flex-col">
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] text-zinc-500 uppercase tracking-wider">24h High</span>
            <span className="relative group">
              <Info size={14} className="text-zinc-500"/>
              <span className="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-48 p-2 rounded bg-[#181a20] border border-white/10 text-[11px] text-zinc-300 leading-snug opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50">
                Highest trading price reached over the last rolling 24-hour window.
              </span>
            </span>
          </div>
          <span className="text-zinc-100 font-semibold tabular-nums">{formatPrice(livePrice?.highPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] text-zinc-500 uppercase tracking-wider">24h Low</span>
            <span className="relative group">
              <Info size={14} className="text-zinc-500"/>
              <span className="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-48 p-2 rounded bg-[#181a20] border border-white/10 text-[11px] text-zinc-300 leading-snug opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50">
                Lowest trading price reached over the last rolling 24-hour window.
              </span>
            </span>
          </div>
          <span className="text-zinc-100 font-semibold tabular-nums">{formatPrice(livePrice?.lowPrice24h)}</span>
        </div>
        <div className="flex flex-col">
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] text-zinc-500 uppercase tracking-wider">24h Volume</span>
            <span className="relative group">
              <Info size={14} className="text-zinc-500"/>
              <span className="absolute top-full left-1/2 -translate-x-1/2 mt-2 w-48 p-2 rounded bg-[#181a20] border border-white/10 text-[11px] text-zinc-300 leading-snug opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none z-50">
                Total trading volume of the base asset over the last 24 hours.
              </span>
            </span>
          </div>
          <span className="text-zinc-100 font-semibold tabular-nums">
            {formatVolume(livePrice?.volume24h)}
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
