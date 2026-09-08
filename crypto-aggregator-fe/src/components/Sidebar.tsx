import {TradingPair, Exchange, ChartInterval} from '../api';
import {Select} from './ui';

type Props = {
  exchanges: Exchange[];
  pairs: TradingPair[];
  intervals: ChartInterval[];
  selectedExchange: string;
  selectedPair: string;
  selectedInterval: ChartInterval;
  onExchangeChange: (e: Exchange) => void;
  onPairChange: (p: TradingPair) => void;
  onIntervalChange: (i: ChartInterval) => void;
};

export default function Sidebar({
                                  exchanges, pairs, intervals,
                                  selectedExchange, selectedPair, selectedInterval,
                                  onExchangeChange, onPairChange, onIntervalChange
                                }: Props) {

  return (
    <aside className="w-[320px] glass-surface flex flex-col h-full overflow-y-auto relative z-20 rounded-tl-sm mt-2">

      {/* Controls Section */}
      <div className="p-4 border-b border-gray-800 space-y-4">
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

    </aside>
  );
}
