import {LivePrice, TradingPair, Exchange, ChartInterval} from '../api';
import {Select} from './ui';

type Props = {
  exchanges: Exchange[];
  pairs: TradingPair[];
  intervals: ChartInterval[];
  selectedExchange: string;
  selectedPair: string;
  selectedInterval: ChartInterval;
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
    <aside className="w-[320px] glass-surface flex flex-col h-full overflow-y-auto relative z-20">

      {/* Selector Block */}
      <div className="p-4 border-b border-white/5 space-y-4">
        <Select
          label="Exchange"
          value={selectedExchange}
          onChange={(value) => onExchangeChange(value as Exchange)}
          options={exchanges.map(ex => ({value: ex, label: ex}))}
        />

        <Select
          label="Trading Pair"
          value={selectedPair}
          onChange={(value) => onPairChange(value as TradingPair)}
          options={pairs.map(p => ({value: p, label: p.replace('_', '/')}))}
        />

        <Select
          label="Time Interval"
          value={selectedInterval}
          onChange={(value) => onIntervalChange(value as ChartInterval)}
          options={intervals.map(i => ({value: i, label: i.replace(/_/g, ' ')}))}
        />
      </div>

      {/* Price Overview Block */}
      <div className="p-4 border-b border-white/5">
        <h3 className="text-zinc-50 font-semibold mb-4 text-sm tracking-wider uppercase">Price Overview</h3>
        <div className="space-y-3 text-sm">
          <div className="flex justify-between">
            <span className="text-zinc-400">Last Price</span>
            <span className={`font-medium ${colorClass}`}>{livePrice?.lastPrice?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-zinc-400">High (24h)</span>
            <span className="text-zinc-50">{livePrice?.highPrice24h?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-zinc-400">Low (24h)</span>
            <span className="text-zinc-50">{livePrice?.lowPrice24h?.toLocaleString() ?? '—'}</span>
          </div>
          <div className="flex justify-between">
            <span className="text-zinc-400">Volume</span>
            <span className="text-zinc-50">{livePrice?.volume24h?.toLocaleString() ?? '—'}</span>
          </div>
        </div>
      </div>

    </aside>
  );
}
