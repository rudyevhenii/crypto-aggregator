import {useCallback, useEffect, useRef, useState} from 'react';
import {useSortable} from '@dnd-kit/sortable';
import {CSS} from '@dnd-kit/utilities';
import {GripHorizontal, Trash2} from 'lucide-react';
import {api, ChartInterval, ChartWidget, HistoricalPrice, LivePrice} from '../api';
import {Select} from './ui';
import ChartArea, {ChartHandle} from './ChartArea';

type Props = {
  widget: ChartWidget;
  livePrice?: LivePrice;
  onDelete: (id: string) => void;
  onUpdateInterval: (id: string, interval: ChartInterval) => void;
  fillHeight?: boolean;
};

export default function ChartWidgetCard({widget, livePrice, onDelete, onUpdateInterval, fillHeight = false}: Props) {
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);
  const [hasMoreHistory, setHasMoreHistory] = useState(true);
  const [intervals, setIntervals] = useState<ChartInterval[]>([]);

  const chartRef = useRef<ChartHandle>(null);

  const {attributes, listeners, setNodeRef, transform, transition, isDragging} = useSortable({id: widget.id});
  const style = {
    transform: CSS.Transform.toString(transform),
    transition,
    zIndex: isDragging ? 10 : 1,
    opacity: isDragging ? 0.8 : 1,
  };

  useEffect(() => {
    let isMounted = true;
    api.getIntervals(widget.exchange)
      .then(data => {
        if (isMounted) setIntervals(data);
      })
      .catch(() => {
        // Intervals load failure handled by empty state
      });

    return () => {
      isMounted = false;
    };
  }, [widget.exchange]);

  useEffect(() => {
    let isMounted = true;
    setHasMoreHistory(true);

    api.getHistoricalPrices(widget.exchange, {
      tradingPair: widget.tradingPair,
      chartInterval: widget.chartInterval
    }).then(data => {
      if (isMounted) setHistorical(data);
    }).catch(() => {
      // Historical prices load failure handled by empty state
    });

    return () => {
      isMounted = false;
    };
  }, [widget.exchange, widget.tradingPair, widget.chartInterval]);

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
    } catch {
      // History load failure handled by empty state
    }
  }, [hasMoreHistory, widget.exchange, widget.tradingPair, widget.chartInterval]);

  const displayPair = widget.tradingPair.replace('_', '/');

  return (
    <div ref={setNodeRef} style={style}
         className={`flex flex-col glass-surface rounded-xl overflow-hidden relative group ${fillHeight ? 'h-full' : 'aspect-video'}`}>
      <div className="h-9 bg-white/[0.02] border-b border-white/5 flex items-center px-3 justify-between shrink-0">
        <div className="flex items-center gap-3">
          <div className="flex items-baseline gap-1.5">
            <span className="text-zinc-50 font-bold text-xs">{displayPair}</span>
            <span className="text-zinc-400 text-[9px] uppercase tracking-wider">{widget.exchange}</span>
          </div>
          <div className="h-3 w-px bg-white/10"/>

          <Select
            value={widget.chartInterval}
            onChange={(value) => onUpdateInterval(widget.id, value as ChartInterval)}
            options={intervals.map(int => ({value: int, label: int.replace(/_/g, ' ')}))}
            className="!w-auto !bg-transparent !border-none !pr-8 !pl-2 !text-xs"
          />
        </div>
        <div className="flex items-center gap-2 opacity-50 hover:opacity-100 transition-opacity">
          <button {...attributes} {...listeners}
                  className="text-zinc-400 hover:text-zinc-50 cursor-grab active:cursor-grabbing p-1 rounded hover:bg-white/5 transition-colors">
            <GripHorizontal size={14}/>
          </button>
          <button onClick={() => onDelete(widget.id)}
                  className="text-zinc-400 hover:text-[#f6465d] transition-colors p-1 rounded hover:bg-[#f6465d]/10">
            <Trash2 size={14}/>
          </button>
        </div>
      </div>
      <div className="flex-1 relative">
        {historical ? (
          <ChartArea ref={chartRef} interval={widget.chartInterval} historical={historical} onLoadMore={handleLoadMore}
                     isWidget={true} exchange={widget.exchange} tradingPair={widget.tradingPair}/>
        ) : (
          <div className="absolute inset-0 flex items-center justify-center text-zinc-400 text-xs">Loading...</div>
        )}
      </div>
    </div>
  );
}
