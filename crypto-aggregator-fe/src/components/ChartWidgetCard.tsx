import { useEffect, useRef, useState, useCallback } from 'react';
import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { GripHorizontal, Trash2 } from 'lucide-react';
import { api, ChartWidget, ChartInterval, HistoricalPrice, LivePrice } from '../api';
import ChartArea, { ChartHandle } from './ChartArea';

type Props = {
  widget: ChartWidget;
  livePrice?: LivePrice; // 👈 ДОДАНО: Отримуємо ціну від батька
  onDelete: (id: string) => void;
  onUpdateInterval: (id: string, interval: ChartInterval) => void;
};

const ALL_INTERVALS: ChartInterval[] = [
  'ONE_MINUTE', 'FIVE_MINUTES', 'FIFTEEN_MINUTES', 'ONE_HOUR', 'FOUR_HOURS', 'ONE_DAY'
];

export default function ChartWidgetCard({ widget, livePrice, onDelete, onUpdateInterval }: Props) {
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);
  const [hasMoreHistory, setHasMoreHistory] = useState(true);
  const chartRef = useRef<ChartHandle>(null);

  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: widget.id });
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 10 : 1,
    opacity: isDragging ? 0.8 : 1,
  };

  // 1. Завантаження тільки історії (БЕЗ SSE)
  useEffect(() => {
    let isMounted = true;
    setHasMoreHistory(true);

    api.getHistoricalPrices(widget.exchange, {
      tradingPair: widget.tradingPair,
      chartInterval: widget.chartInterval
    }).then(data => {
      if (isMounted) setHistorical(data);
    }).catch(console.error);

    return () => { isMounted = false; };
  }, [widget.exchange, widget.tradingPair, widget.chartInterval]);

  // 2. 👈 ДОДАНО: Реакція на нову ціну з пропсів
  useEffect(() => {
    if (livePrice && chartRef.current) {
      chartRef.current.applyLivePrice(livePrice);
    }
  }, [livePrice]);

  const handleLoadMore = useCallback(async (oldestTimestamp: string) => {
    if (!hasMoreHistory) return;
    try {
      const olderData = await api.getHistoricalPrices(widget.exchange, {
        tradingPair: widget.tradingPair,
        chartInterval: widget.chartInterval,
        endTimeCursor: oldestTimestamp
      });
      if (olderData.length === 0) {
        setHasMoreHistory(false);
      } else {
        setHistorical(prev => prev ? [...olderData, ...prev] : olderData);
      }
    } catch (err) {
      console.error("Failed to load more history", err);
    }
  }, [hasMoreHistory, widget.exchange, widget.tradingPair, widget.chartInterval]);

  const displayPair = widget.tradingPair.replace('_', '/');

  return (
    <div ref={setNodeRef} style={style} className="flex flex-col bg-[#181a20] rounded-md border border-[#2b3139] overflow-hidden h-full relative group">
      <div className="h-9 bg-[#181a20] border-b border-[#2b3139] flex items-center px-3 justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="flex items-baseline gap-1.5">
            <span className="text-[#eaecef] font-bold text-xs">{displayPair}</span>
            <span className="text-[#848e9c] text-[9px] uppercase">{widget.exchange}</span>
          </div>
          <div className="h-3 w-px bg-[#2b3139]" />
          <select
            value={widget.chartInterval}
            onChange={(e) => onUpdateInterval(widget.id, e.target.value as ChartInterval)}
            className="bg-transparent text-[#848e9c] hover:text-[#eaecef] text-xs focus:outline-none cursor-pointer transition-colors"
          >
            {ALL_INTERVALS.map(int => (
              <option key={int} value={int} className="bg-[#0b0e11]">{int.replace(/_/g, ' ')}</option>
            ))}
          </select>
        </div>
        <div className="flex items-center gap-2 opacity-50 hover:opacity-100 transition-opacity">
          <button {...attributes} {...listeners} className="text-[#848e9c] hover:text-[#eaecef] cursor-grab active:cursor-grabbing p-1">
            <GripHorizontal size={14} />
          </button>
          <button onClick={() => onDelete(widget.id)} className="text-[#848e9c] hover:text-[#f6465d] transition-colors p-1">
            <Trash2 size={14} />
          </button>
        </div>
      </div>
      <div className="flex-1 relative">
        {historical ? (
          <ChartArea ref={chartRef} interval={widget.chartInterval} historical={historical} onLoadMore={handleLoadMore} isWidget={true} />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center text-[#848e9c] text-xs">Loading...</div>
        )}
      </div>
    </div>
  );
}