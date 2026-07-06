import {forwardRef, useEffect, useImperativeHandle, useRef} from 'react';
import {CandlestickData, CandlestickSeries, createChart, IChartApi, ISeriesApi, UTCTimestamp} from 'lightweight-charts';
import {ChartInterval, HistoricalPrice, intervalToSeconds, LivePrice} from '../api';

export type ChartHandle = {
  applyLivePrice: (p: LivePrice) => void;
};

type Props = {
  interval: ChartInterval;
  historical: HistoricalPrice[] | null;
  onLoadMore?: (oldestTime: string) => void;
  isWidget?: boolean;
};

const getPrecisionParams = (price: number) => {
  if (price > 1000) return {precision: 2, minMove: 0.01};
  if (price > 10) return {precision: 3, minMove: 0.001};
  if (price > 1) return {precision: 4, minMove: 0.0001};
  if (price > 0.01) return {precision: 5, minMove: 0.00001};
  return {precision: 6, minMove: 0.000001};
};

const ChartArea = forwardRef<ChartHandle, Props>(({interval, historical, onLoadMore, isWidget = false}, ref) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);

  const lastBucketRef = useRef<number | null>(null);
  const currentCandleRef = useRef<CandlestickData | null>(null);
  const isFetchingRef = useRef<boolean>(false);

  const dataLengthRef = useRef<number>(0);
  const oldestTimeRef = useRef<string | null>(null); // 👈 ДОДАНО: Реф для зберігання найстарішого часу
  const onLoadMoreRef = useRef(onLoadMore);

  useEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  useEffect(() => {
    dataLengthRef.current = 0;
  }, [interval]);

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
        const newCandle: CandlestickData = {
          time: bucket as UTCTimestamp,
          open: price,
          high: price,
          low: price,
          close: price
        };
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
      layout: {background: {color: isWidget ? 'transparent' : '#181a20'}, textColor: '#848e9c'},
      grid: {vertLines: {color: '#2b3139', style: 1}, horzLines: {color: '#2b3139', style: 1}},
      rightPriceScale: {borderColor: '#2b3139'},
      timeScale: {borderColor: '#2b3139', timeVisible: true},
      crosshair: {
        vertLine: {color: '#848e9c', labelBackgroundColor: '#2b3139'},
        horzLine: {color: '#848e9c', labelBackgroundColor: '#2b3139'}
      }
    });

    chartRef.current = chart;

    seriesRef.current = chart.addSeries(CandlestickSeries, {
      upColor: '#0ecb81',
      downColor: '#f6465d',
      borderVisible: false,
      wickUpColor: '#0ecb81',
      wickDownColor: '#f6465d',
    });

    chart.timeScale().subscribeVisibleLogicalRangeChange((logicalRange) => {
      if (logicalRange !== null && logicalRange.from < 20 && !isFetchingRef.current) {
        const currentOnLoadMore = onLoadMoreRef.current;
        const oldestTime = oldestTimeRef.current; // 👈 ВИПРАВЛЕНО: Беремо гарантовано найстаріший час

        if (oldestTime && currentOnLoadMore) {
          isFetchingRef.current = true;
          currentOnLoadMore(oldestTime);
        }
      }
    });

    const resizeObserver = new ResizeObserver(() => {
      if (containerRef.current && chartRef.current) {
        chartRef.current.applyOptions({
          width: containerRef.current.clientWidth,
          height: containerRef.current.clientHeight
        });
      }
    });
    resizeObserver.observe(containerRef.current);

    return () => {
      resizeObserver.disconnect();
      chart.remove();
    };
  }, [isWidget]);

  useEffect(() => {
    if (!seriesRef.current || !historical || !Array.isArray(historical)) return;

    let oldestTimeStr: string | null = null;
    let minTime = Infinity;

    const uniqueCandles = new Map<number, CandlestickData>();

    historical
      .filter((h) => h.openTime != null && h.open != null && h.high != null && h.low != null && h.close != null)
      .forEach((h) => {
        const timeMs = new Date(h.openTime as string).getTime();

        // 👈 ДОДАНО: Динамічний пошук найстарішого запису
        if (timeMs < minTime) {
          minTime = timeMs;
          oldestTimeStr = h.openTime;
        }

        const time = Math.floor(timeMs / 1000) as UTCTimestamp;
        uniqueCandles.set(time, {
          time: time,
          open: Number(h.open),
          high: Number(h.high),
          low: Number(h.low),
          close: Number(h.close),
        });
      });

    oldestTimeRef.current = oldestTimeStr; // 👈 Зберігаємо для курсора

    const data: CandlestickData[] = Array.from(uniqueCandles.values())
      .sort((a, b) => (a.time as number) - (b.time as number));

    if (data.length > 0) {
      const lastCandle = data[data.length - 1];
      const formatParams = getPrecisionParams(lastCandle.close);

      seriesRef.current.applyOptions({
        priceFormat: {type: 'price', precision: formatParams.precision, minMove: formatParams.minMove},
      });

      if (chartRef.current && dataLengthRef.current > 0 && data.length > dataLengthRef.current) {
        const visibleRange = chartRef.current.timeScale().getVisibleLogicalRange();
        seriesRef.current.setData(data);

        if (visibleRange !== null) {
          const addedItemsCount = data.length - dataLengthRef.current;
          chartRef.current.timeScale().setVisibleLogicalRange({
            from: visibleRange.from + addedItemsCount,
            to: visibleRange.to + addedItemsCount,
          });
        }
      } else {
        seriesRef.current.setData(data);
      }

      dataLengthRef.current = data.length;
      lastBucketRef.current = lastCandle.time as number;
      currentCandleRef.current = {...lastCandle};

      isFetchingRef.current = false;
    } else {
      lastBucketRef.current = null;
      currentCandleRef.current = null;
      seriesRef.current.setData([]);
      dataLengthRef.current = 0;
      isFetchingRef.current = false;
    }
  }, [historical]);

  return (
    <div className={`w-full h-full ${isWidget ? '' : 'p-4 bg-[#0b0e14]'}`}>
      <div
        className={`w-full h-full ${isWidget ? '' : 'bg-[#181a20] rounded-sm border border-[#2b3139]'} relative flex flex-col`}>
        {!isWidget && (
          <div className="flex items-center px-4 h-10 border-b border-[#2b3139] text-sm flex-shrink-0">
            <div className="text-[#eaecef] font-medium border-b-2 border-[#fcd535] py-2 mr-6">Chart</div>
          </div>
        )}
        <div ref={containerRef} className="flex-1 w-full"/>
      </div>
    </div>
  );
});

export default ChartArea;