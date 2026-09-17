import {useState} from 'react';
import {ArrowLeft} from 'lucide-react';
import SearchModal from './SearchModal';
import {Select} from './ui';
import {formatInterval} from '../utils/format';
import {ChartInterval, Exchange, TradingPair, ExchangePair} from '../api';

type Props = {
  exchange: Exchange;
  pair: TradingPair;
  intervals: ChartInterval[];
  selectedInterval: ChartInterval;
  onIntervalChange: (interval: ChartInterval) => void;
  onExchangeChange: (exchange: Exchange) => void;
  onPairChange: (pair: TradingPair) => void;
  onBack?: () => void;
};

export default function TopBar({
                                  exchange,
                                  pair,
                                  intervals,
                                  selectedInterval,
                                  onIntervalChange,
                                  onExchangeChange,
                                  onPairChange,
                                  onBack,
                                }: Props) {
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const displayPair = pair.replace('_', '/');

  const handleSelectPair = (pair: ExchangePair) => {
    onExchangeChange(pair.exchange);
    onPairChange(pair.tradingPair);
    setIsSearchOpen(false);
  };

  return (
    <div className="flex items-center h-10 px-3 border-b border-white/5 glass-surface shrink-0 relative z-30 gap-2">
      {onBack && (
        <button
          onClick={onBack}
          className="p-1.5 rounded-md bg-white/5 hover:bg-white/10 border border-white/10 transition-all"
          title="Back to Overview"
        >
          <ArrowLeft size={16} className="text-zinc-400"/>
        </button>
      )}

      {/* Clickable Pair + Exchange */}
      <button
        onClick={() => setIsSearchOpen(true)}
        className="flex items-center gap-2 hover:bg-white/5 rounded-md px-2 py-1 transition-colors"
        title="Change market"
      >
        <span className="text-sm font-bold text-zinc-50 tracking-tight">{displayPair}</span>
        <span className="text-[11px] text-zinc-400 font-medium">{exchange}</span>
      </button>

      <div className="h-4 w-px bg-white/10"/>

      {/* Interval selector moved to left group */}
      <div className="flex items-center">
        <Select
          value={selectedInterval}
          onChange={(value) => onIntervalChange(value as ChartInterval)}
          options={intervals.map(i => ({value: i, label: formatInterval(i)}))}
          className="!w-auto !bg-transparent !border-none !pr-2 !pl-2 !text-xs !text-zinc-300 hover:!text-zinc-50 hover:!bg-white/5 hover:rounded-md"
          hideArrow
        />
      </div>

      <SearchModal
        isOpen={isSearchOpen}
        onClose={() => setIsSearchOpen(false)}
        onAdd={handleSelectPair}
      />
    </div>
  );
}
