import {ArrowLeft, AlarmClock} from 'lucide-react';
import {Select} from './ui';
import {formatInterval} from '../utils/format';
import {ChartInterval, Exchange, TradingPair} from '../api';

type Props = {
  exchange: Exchange;
  pair: TradingPair;
  intervals: ChartInterval[];
  selectedInterval: ChartInterval;
  onIntervalChange: (interval: ChartInterval) => void;
  onSearchOpen?: () => void;
  onBack?: () => void;
  onAlertClick?: () => void;
};

export default function TopBar({
                                  exchange,
                                  pair,
                                  intervals,
                                  selectedInterval,
                                  onIntervalChange,
                                  onSearchOpen,
                                  onBack,
                                  onAlertClick,
                                }: Props) {
  const displayPair = pair.replace('_', '/');

  return (
    <div className="flex items-center h-12 px-3 border-b border-white/5 glass-surface shrink-0 relative z-30 gap-2">
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
        onClick={onSearchOpen}
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

      <div className="h-4 w-px bg-white/10"/>

      {/* Alert button */}
      {onAlertClick && (
        <button
          onClick={onAlertClick}
          className="flex items-center gap-1.5 px-2 py-1.5 rounded-md bg-white/5 hover:bg-white/10 border border-white/10 transition-all"
          title="Create alert"
        >
          <AlarmClock size={16} className="text-zinc-300"/>
          <span className="text-xs font-medium text-zinc-300">Alert</span>
        </button>
      )}
    </div>
  );
}
