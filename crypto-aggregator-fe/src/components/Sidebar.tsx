import {LivePrice, TradingPair, Exchange, ChartInterval} from '../api';
import {Select} from './ui';

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
        <Select
          label="Exchange"
          value={selectedExchange ?? ''}
          onChange={(value) => onExchangeChange(value as Exchange)}
          options={exchanges.map(ex => ({value: ex, label: ex}))}
          placeholder="Select exchange"
        />

        <Select
          label="Trading Pair"
          value={selectedPair ?? ''}
          onChange={(value) => onPairChange(value as TradingPair)}
          options={pairs.map(p => ({value: p, label: p.replace('_', '/')}))}
          placeholder="Select pair"
        />

        <Select
          label="Time Interval"
          value={selectedInterval ?? ''}
          onChange={(value) => onIntervalChange(value as ChartInterval)}
          options={intervals.map(i => ({value: i, label: i.replace(/_/g, ' ')}))}
          placeholder="Select interval"
        />
      </div>

      {/* Price Overview Block */}
      <div className="p-4 border-b border-[#2b3139]">
        <h3 className="text-[#eaecef] font-semibold mb-4 text-sm tracking-wide">Price Overview</h3>
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
