import {forwardRef, useEffect, useImperativeHandle, useMemo, useRef, useCallback} from 'react';
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
  exchange: string;
  tradingPair: string;
};

const getPrecisionParams = (price: number) => {
  if (price > 1000) return {precision: 2, minMove: 0.01};
  if (price > 10) return {precision: 3, minMove: 0.001};
  if (price > 1) return {precision: 4, minMove: 0.0001};
  if (price > 0.01) return {precision: 5, minMove: 0.00001};
  return {precision: 6, minMove: 0.000001};
};

const ChartArea = forwardRef<ChartHandle, Props>(({interval, historical, onLoadMore, isWidget = false, exchange, tradingPair}, ref) => {
  const containerRef = useRef<HTMLDivElement | null>(null);
  const chartRef = useRef<IChartApi | null>(null);
  const seriesRef = useRef<ISeriesApi<'Candlestick'> | null>(null);

  const lastBucketRef = useRef<number | null>(null);
  const currentCandleRef = useRef<CandlestickData | null>(null);
  const isFetchingRef = useRef<boolean>(false);

  const dataLengthRef = useRef<number>(0);
  const oldestTimeRef = useRef<string | null>(null);
  const onLoadMoreRef = useRef(onLoadMore);
  const parsedCandlesRef = useRef<{candles: CandlestickData[], oldestTime: string | null} | null>(null);

  useEffect(() => {
    onLoadMoreRef.current = onLoadMore;
  }, [onLoadMore]);

  // ❗ ОЧИЩЕННЯ: Скидаємо стан графіка при перемиканні таймфрейму, щоб уникнути конфліктів
  useEffect(() => {
    dataLengthRef.current = 0;
    lastBucketRef.current = null;
    currentCandleRef.current = null;
    oldestTimeRef.current = null;
    isFetchingRef.current = false;

    if (seriesRef.current) {
      seriesRef.current.setData([]);
    }
  }, [interval, exchange, tradingPair]);

  const applyLivePrice = useCallback((p: LivePrice) => {
    if (!seriesRef.current || !p.timestamp || p.lastPrice == null) return;

    // ❗ ЗАХИСТ ВІД ЗМІШУВАННЯ ДАНИХ: Ігноруємо ціни з інших бірж/пар
    if (p.exchange !== exchange || p.tradingPair !== tradingPair) return;

    // ❗ ЗАХИСТ: Не малюємо живі ціни, поки не завантажилась історія для поточного інтервалу
    if (lastBucketRef.current === null || !currentCandleRef.current) return;

    const ts = Math.floor(new Date(p.timestamp).getTime() / 1000);
    const bucket = Math.floor(ts / intervalToSeconds(interval)) * intervalToSeconds(interval);
    const price = Number(p.lastPrice);

    if (bucket < lastBucketRef.current) {
      return;
    }

    if (lastBucketRef.current === bucket) {
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
        close: price,
      };
      currentCandleRef.current = newCandle;
      lastBucketRef.current = bucket;
      seriesRef.current.update(newCandle);
    }
  }, [interval, exchange, tradingPair]);

  useImperativeHandle(ref, () => ({
    applyLivePrice,
  }), [applyLivePrice]);

  // ❗ ВИСОКОЕФЕКТИВНЕ: Парсимо дані в useMemo, щоб уникнути повторних обчислень
  const parsedCandles = useMemo(() => {
    if (!historical || !Array.isArray(historical)) {
      parsedCandlesRef.current = {candles: [] as CandlestickData[], oldestTime: null as string | null};
      return {candles: [] as CandlestickData[], oldestTime: null as string | null};
    }

    let oldestTimeStr: string | null = null;
    let minTime = Infinity;
    const uniqueCandles = new Map<number, CandlestickData>();

    historical
      .filter((h) => h.openTime != null && h.open != null && h.high != null && h.low != null && h.close != null)
      .forEach((h) => {
        const timeMs = new Date(h.openTime as string).getTime();

        if (timeMs < minTime) {
          minTime = timeMs;
          oldestTimeStr = h.openTime as string;
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

    const candles = Array.from(uniqueCandles.values())
      .sort((a, b) => (a.time as number) - (b.time as number));

    parsedCandlesRef.current = {candles, oldestTime: oldestTimeStr};
    return {candles, oldestTime: oldestTimeStr};
  }, [historical]);

  useEffect(() => {
    if (!seriesRef.current) return;

    const {candles, oldestTime} = parsedCandles;
    oldestTimeRef.current = oldestTime;

    if (candles.length > 0) {
      const lastCandle = candles[candles.length - 1];
      const formatParams = getPrecisionParams(lastCandle.close);

      seriesRef.current.applyOptions({
        priceFormat: {type: 'price', precision: formatParams.precision, minMove: formatParams.minMove},
      });

      if (chartRef.current && dataLengthRef.current > 0 && candles.length > dataLengthRef.current) {
        const visibleRange = chartRef.current.timeScale().getVisibleLogicalRange();
        seriesRef.current.setData(candles);

        if (visibleRange !== null) {
          const addedItemsCount = candles.length - dataLengthRef.current;
          chartRef.current.timeScale().setVisibleLogicalRange({
            from: visibleRange.from + addedItemsCount,
            to: visibleRange.to + addedItemsCount,
          });
        }
      } else {
        seriesRef.current.setData(candles);
      }

      dataLengthRef.current = candles.length;
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
  }, [parsedCandles]);

  useEffect(() => {
    if (!containerRef.current) return;

    let resizeObserver: ResizeObserver | null = null;

    const initChart = () => {
      if (!containerRef.current || chartRef.current) return;

      const { clientWidth, clientHeight } = containerRef.current;
      if (clientWidth <= 0 || clientHeight <= 0) return;

      const chart = createChart(containerRef.current, {
        width: clientWidth,
        height: clientHeight,
        layout: {background: {color: isWidget ? 'transparent' : '#181a20'}, textColor: '#a1a1aa'},
        grid: {vertLines: {color: '#27272a', style: 1}, horzLines: {color: '#27272a', style: 1}},
        rightPriceScale: {borderColor: '#27272a'},
        timeScale: {borderColor: '#27272a', timeVisible: true},
        localization: {locale: 'en'},
        crosshair: {
          vertLine: {color: '#52525b', labelBackgroundColor: '#181a20'},
          horzLine: {color: '#52525b', labelBackgroundColor: '#181a20'}
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
          const oldestTime = oldestTimeRef.current;

          if (oldestTime && currentOnLoadMore) {
            isFetchingRef.current = true;
            currentOnLoadMore(oldestTime);
          }
        }
      });

      // Apply any existing data that arrived before the chart was ready
      const existing = parsedCandlesRef.current;
      if (existing && existing.candles.length > 0) {
        const { candles, oldestTime } = existing;
        const lastCandle = candles[candles.length - 1];
        const formatParams = getPrecisionParams(lastCandle.close);

        seriesRef.current.applyOptions({
          priceFormat: {type: 'price', precision: formatParams.precision, minMove: formatParams.minMove},
        });

        seriesRef.current.setData(candles);
        dataLengthRef.current = candles.length;
        lastBucketRef.current = lastCandle.time as number;
        currentCandleRef.current = {...lastCandle};
        oldestTimeRef.current = oldestTime;
        isFetchingRef.current = false;
      }
    };

    const handleResize = () => {
      if (!containerRef.current) return;

      const { clientWidth, clientHeight } = containerRef.current;

      if (clientWidth > 0 && clientHeight > 0) {
        if (!chartRef.current) {
          initChart();
        } else {
          chartRef.current.applyOptions({
            width: clientWidth,
            height: clientHeight
          });
        }
      }
    };

    // Attempt immediate initialization if container already has size
    handleResize();

    resizeObserver = new ResizeObserver(handleResize);
    resizeObserver.observe(containerRef.current);

    return () => {
      if (resizeObserver) {
        resizeObserver.disconnect();
      }
      if (chartRef.current) {
        chartRef.current.remove();
        chartRef.current = null;
        seriesRef.current = null;
      }
    };
  }, [isWidget]);

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

ChartArea.displayName = 'ChartArea';

export default ChartArea;
