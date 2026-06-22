import { forwardRef, useEffect, useImperativeHandle, useRef } from 'react';
import { createChart, IChartApi, ISeriesApi, CandlestickData, UTCTimestamp, CandlestickSeries } from 'lightweight-charts';
import { HistoricalPrice, LivePrice, ChartInterval, intervalToSeconds } from '../api';

export type ChartHandle = {
  applyLivePrice: (p: LivePrice) => void;
};

type Props = {
  interval: ChartInterval;
  historical: HistoricalPrice[] | null;
  onLoadMore?: (oldestTime: string) => void;
};

const ChartArea = forwardRef<ChartHandle, Props>(({ interval, historical, onLoadMore }, ref) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);

  const lastBucketRef = useRef<number | null>(null);
  const currentCandleRef = useRef<CandlestickData | null>(null);

  const isFetchingRef = useRef<boolean>(false);

  // ДОДАНО: Рефи для уникнення "Closure Trap"
  const historicalRef = useRef(historical);
  const onLoadMoreRef = useRef(onLoadMore);

  // Синхронізуємо рефи з актуальними пропсами при кожному рендері
  useEffect(() => {
    historicalRef.current = historical;
    onLoadMoreRef.current = onLoadMore;
  }, [historical, onLoadMore]);

  useImperativeHandle(ref, () => ({
    applyLivePrice: (p: LivePrice) => {
      if (!seriesRef.current || !p.timestamp || p.lastPrice == null) return;
      const ts = Math.floor(new Date(p.timestamp).getTime() / 1000);
      const bucket = Math.floor(ts / intervalToSeconds(interval)) * intervalToSeconds(interval);
      const price = Number(p.lastPrice);

      if (lastBucketRef.current === bucket && currentCandleRef.current) {
        currentCandleRef.current.close = price;
        currentCandleRef.current.high = Math.max(currentCandleRef.current.high, price);
        currentCandleRef.current.low = Math.min(currentCandleRef.current.low, price);
        seriesRef.current.update(currentCandleRef.current);
      } else {
        const newCandle: CandlestickData = { time: bucket as UTCTimestamp, open: price, high: price, low: price, close: price };
        currentCandleRef.current = newCandle;
        lastBucketRef.current = bucket;
        seriesRef.current.update(newCandle);
      }
    },
  }), [interval]);

  useEffect(() => {
    if (!containerRef.current) return;

    const chart = createChart(containerRef.current, {
      width: containerRef.current.clientWidth,
      height: containerRef.current.clientHeight,
      layout: { background: { color: '#181a20' }, textColor: '#848e9c' },
      grid: { vertLines: { color: '#2b3139', style: 1 }, horzLines: { color: '#2b3139', style: 1 } },
      rightPriceScale: { borderColor: '#2b3139' },
      timeScale: { borderColor: '#2b3139', timeVisible: true },
      crosshair: { vertLine: { color: '#848e9c', labelBackgroundColor: '#2b3139' }, horzLine: { color: '#848e9c', labelBackgroundColor: '#2b3139' } }
    });

    chartRef.current = chart;
    seriesRef.current = chart.addSeries(CandlestickSeries, {
      upColor: '#0ecb81', downColor: '#f6465d', borderVisible: false, wickUpColor: '#0ecb81', wickDownColor: '#f6465d',
    });

    // ОНОВЛЕНО: Тепер використовуємо .current, щоб отримати найсвіжіші дані
    chart.timeScale().subscribeVisibleLogicalRangeChange((logicalRange) => {
      if (logicalRange !== null && logicalRange.from < 20 && !isFetchingRef.current) {
        const currentHistory = historicalRef.current;
        const currentOnLoadMore = onLoadMoreRef.current;

        if (currentHistory && currentHistory.length > 0 && currentOnLoadMore) {
          isFetchingRef.current = true;
          currentOnLoadMore(currentHistory[0].openTime as string);
        }
      }
    });

    const resizeObserver = new ResizeObserver(() => {
      if (containerRef.current && chartRef.current) {
        chartRef.current.applyOptions({ width: containerRef.current.clientWidth, height: containerRef.current.clientHeight });
      }
    });
    resizeObserver.observe(containerRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
    };
  }, []);

  useEffect(() => {
    if (!seriesRef.current || !historical || !Array.isArray(historical)) return;

    isFetchingRef.current = false;

    const data: CandlestickData[] = historical
      .filter((h) => h.openTime != null && h.open != null && h.high != null && h.low != null && h.close != null)
      .map((h) => ({
        time: Math.floor(new Date(h.openTime as string).getTime() / 1000) as UTCTimestamp,
        open: Number(h.open), high: Number(h.high), low: Number(h.low), close: Number(h.close),
      }))
      .sort((a, b) => (a.time as number) - (b.time as number));

    if (data.length > 0) {
      seriesRef.current.setData(data);
      const lastCandle = data[data.length - 1];
      lastBucketRef.current = lastCandle.time as number;
      currentCandleRef.current = { ...lastCandle };
    } else {
      lastBucketRef.current = null;
      currentCandleRef.current = null;
      seriesRef.current.setData([]);
    }
  }, [historical]);

  return (
    <div className="w-full h-full p-4 bg-[#0b0e14]">
      <div className="w-full h-full bg-[#181a20] rounded-sm border border-[#2b3139] relative flex flex-col">
        <div className="flex items-center px-4 h-12 border-b border-[#2b3139] text-sm flex-shrink-0">
          <div className="text-[#eaecef] font-medium border-b-2 border-[#fcd535] py-3 mr-6">Chart</div>
        </div>
        <div ref={containerRef} className="flex-1 w-full" />
      </div>
    </div>
  );
});

export default ChartArea;