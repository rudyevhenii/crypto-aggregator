import { Exchange, TradingPair, ChartInterval, LivePrice } from '../api';
import { ChevronDown } from 'lucide-react';

type Props = {
  exchanges: Exchange[];
  pairs: TradingPair[];
  intervals: ChartInterval[];
  selectedExchange: Exchange | null;
  selectedPair: TradingPair | null;
  selectedInterval: ChartInterval | null;
  livePrice: LivePrice | null;
  onExchangeChange: (e: Exchange) => void;
  onPairChange: (p: TradingPair) => void;
  onIntervalChange: (i: ChartInterval) => void;
};

export default function Sidebar({
                                  exchanges, pairs, intervals,
                                  selectedExchange, selectedPair, selectedInterval, livePrice,
                                  onExchangeChange, onPairChange, onIntervalChange
                                }: Props) {

  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';

  return (
    <aside className="w-[320px] bg-[#181a20] border-l border-[#2b3139] flex flex-col h-full overflow-y-auto">

      {/* Selector Block */}
      <div className="p-4 border-b border-[#2b3139] space-y-4">
        <div>
          <label className="block text-xs text-[#848e9c] mb-1.5">Exchange</label>
          <div className="relative">
            <select
              value={selectedExchange ?? ''}
              onChange={(e) => onExchangeChange(e.target.value as Exchange)}
              className="w-full appearance-none bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded text-sm hover:border-[#474d57] transition-colors focus:outline-none focus:border-[#fcd535]"
            >
              {exchanges.map((ex) => <option key={ex} value={ex}>{ex}</option>)}
            </select>
            <ChevronDown size={14} className="absolute right-3 top-2.5 text-[#848e9c] pointer-events-none" />
          </div>
        </div>

        <div>
          <label className="block text-xs text-[#848e9c] mb-1.5">Trading Pair</label>
          <div className="relative">
            <select
              value={selectedPair ?? ''}
              onChange={(e) => onPairChange(e.target.value as TradingPair)}
              className="w-full appearance-none bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded text-sm hover:border-[#474d57] transition-colors focus:outline-none focus:border-[#fcd535]"
            >
              {pairs.map((p) => <option key={p} value={p}>{p.replace('_', '/')}</option>)}
            </select>
            <ChevronDown size={14} className="absolute right-3 top-2.5 text-[#848e9c] pointer-events-none" />
          </div>
        </div>

        <div>
          <label className="block text-xs text-[#848e9c] mb-1.5">Time Interval</label>
          <div className="relative">
            <select
              value={selectedInterval ?? ''}
              onChange={(e) => onIntervalChange(e.target.value as ChartInterval)}
              className="w-full appearance-none bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] px-3 py-2 rounded text-sm hover:border-[#474d57] transition-colors focus:outline-none focus:border-[#fcd535]"
            >
              {intervals.map((i) => <option key={i} value={i}>{i.replace(/_/g, ' ')}</option>)}
            </select>
            <ChevronDown size={14} className="absolute right-3 top-2.5 text-[#848e9c] pointer-events-none" />
          </div>
        </div>
      </div>

      {/* Price Overview Block (From mockup) */}
      <div className="p-4 border-b border-[#2b3139]">
        <h3 className="text-[#eaecef] font-semibold mb-4 text-sm">Price Overview</h3>
        <div className="space-y-3 text-sm">
          <div className="flex justify-between">
            <span className="text-[#848e9c]">Last Price</span>
            <span className={`font-medium ${colorClass}`}>{livePrice?.lastPrice?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#848e9c]">High (24h)</span>
            <span className="text-[#eaecef]">{livePrice?.highPrice24h?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#848e9c]">Low (24h)</span>
            <span className="text-[#eaecef]">{livePrice?.lowPrice24h?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-[#848e9c]">Volume</span>
            <span className="text-[#eaecef]">{livePrice?.volume24h?.toLocaleString() ?? '—'}</span>
          </div>
        </div>
      </div>

    </aside>
  );
}