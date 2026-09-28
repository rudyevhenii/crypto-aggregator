import {useEffect, useImperativeHandle, useMemo, useRef, useState, useCallback, forwardRef} from 'react';
import {CandlestickData, CandlestickSeries, createChart, IChartApi, ISeriesApi, UTCTimestamp} from 'lightweight-charts';
import {ChartInterval, HistoricalPrice, intervalToSeconds, LivePrice} from '../api';
import {formatPrice, formatVolume} from '../utils/format';

const browserTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;

const createLocalFormatter = (options: Intl.DateTimeFormatOptions) =>
  new Intl.DateTimeFormat('en', {
    ...options,
    timeZone: browserTimeZone,
    hour12: false,
  });

const timeFormatter = createLocalFormatter({
  hour: '2-digit',
  minute: '2-digit',
});

const dateTimeFormatter = createLocalFormatter({
  day: 'numeric',
  month: 'short',
  year: 'numeric',
  hour: '2-digit',
  minute: '2-digit',
});

const formatTime = (timestamp: number) => timeFormatter.format(new Date(timestamp * 1000));

const formatDateTime = (timestamp: number) => {
  const date = new Date(timestamp * 1000);
  const parts = dateTimeFormatter.formatToParts(date);
  const get = (type: string) => parts.find(part => part.type === type)?.value || '';
  const day = get('day');
  const month = get('month');
  const year = get('year');
  const hour = get('hour');
  const minute = get('minute');
  return `${day} ${month} '${year}, ${hour}:${minute}`;
};

const formatBusinessDay = (businessDay: {day: number; month: number; year: number}) => {
  const date = new Date(businessDay.year, businessDay.month - 1, businessDay.day);
  return timeFormatter.format(date);
};

const localTimeFormatter = (time: number | {day: number; month: number; year: number} | string) => {
  if (typeof time === 'number') {
    return formatTime(time);
  }
  if (typeof time === 'object' && time !== null && 'day' in time && 'month' in time && 'year' in time) {
    return formatBusinessDay(time as {day: number; month: number; year: number});
  }
  if (typeof time === 'string') {
    const parsed = new Date(time);
    if (!isNaN(parsed.getTime())) {
      return formatTime(Math.floor(parsed.getTime() / 1000));
    }
  }
  return '';
};

const localDateTimeFormatter = (time: number | {day: number; month: number; year: number} | string) => {
  if (typeof time === 'number') {
    return formatDateTime(time);
  }
  if (typeof time === 'object' && time !== null && 'day' in time && 'month' in time && 'year' in time) {
    const {day, month, year} = time as {day: number; month: number; year: number};
    return formatDateTime(new Date(year, month - 1, day).getTime() / 1000);
  }
  if (typeof time === 'string') {
    const parsed = new Date(time);
    if (!isNaN(parsed.getTime())) {
      return formatDateTime(Math.floor(parsed.getTime() / 1000));
    }
  }
  return '';
};

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
  livePrice: {
    lastPrice: number;
    priceChangePercent24h: number;
    highPrice24h: number;
    lowPrice24h: number;
    volume24h: number;
  } | null;
  health: { connectionStatus: string } | null;
};

const getPrecisionParams = (price: number) => {
  if (price > 1000) return {precision: 2, minMove: 0.01};
  if (price > 10) return {precision: 3, minMove: 0.001};
  if (price > 1) return {precision: 4, minMove: 0.0001};
  if (price > 0.01) return {precision: 5, minMove: 0.00001};
  return {precision: 6, minMove: 0.000001};
};

const ChartArea = forwardRef<ChartHandle, Props>(({
                                                      interval,
                                                      historical,
                                                      onLoadMore,
                                                      isWidget = false,
                                                      exchange,
                                                      tradingPair,
                                                      livePrice,
                                                      health,
                                                    }, ref) => {
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

  const [hoverData, setHoverData] = useState<{
    open: number;
    high: number;
    low: number;
    close: number;
    volume: number;
  } | null>(null);

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
    setHoverData(null);

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
    const uniqueCandles = new Map<number, CandlestickData & {volume?: number}>();

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
          volume: h.volume != null ? Number(h.volume) : undefined,
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
        timeScale: {
          borderColor: '#27272a',
          timeVisible: true,
          tickMarkFormatter: localTimeFormatter,
        },
        localization: {locale: 'en', timeFormatter: localDateTimeFormatter},
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

      chartRef.current.subscribeCrosshairMove((param) => {
        if (!param || !param.time || !param.point || param.point.x < 0 || param.point.y < 0) {
          setHoverData(null);
          return;
        }

        const data = param.seriesData.get(seriesRef.current as ISeriesApi<'Candlestick'>);
        if (data && 'open' in data && 'high' in data && 'low' in data && 'close' in data) {
          const candleData = data as CandlestickData;
          const candleTime = candleData.time as number;
          const storedCandle = parsedCandlesRef.current?.candles.find(c => c.time === candleTime);
          const candleVolume = storedCandle && 'volume' in storedCandle ? (storedCandle as CandlestickData & {volume?: number}).volume : undefined;
          setHoverData({
            open: Number(candleData.open),
            high: Number(candleData.high),
            low: Number(candleData.low),
            close: Number(candleData.close),
            volume: candleVolume != null ? Number(candleVolume) : (livePrice?.volume24h ?? 0),
          });
        } else {
          setHoverData(null);
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

  const getStatusColor = (status?: string) => {
    switch (status) {
      case 'CONNECTED':
        return 'bg-[#0ecb81] shadow-[0_0_6px_rgba(14,203,129,0.4)]';
      case 'RECONNECTING':
        return 'bg-[#fcd535] shadow-[0_0_6px_rgba(252,213,53,0.4)] animate-pulse';
      case 'ERROR':
        return 'bg-[#f6465d] shadow-[0_0_6px_rgba(246,70,93,0.4)]';
      case 'DISCONNECTED':
      default:
        return 'bg-[#848e9c]';
    }
  };

  const isPositive = (livePrice?.priceChangePercent24h ?? 0) >= 0;
  const changeColor = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const changeSign = isPositive ? '+' : '';

  const display = hoverData ?? {
    open: livePrice?.lastPrice ?? 0,
    high: livePrice?.highPrice24h ?? 0,
    low: livePrice?.lowPrice24h ?? 0,
    close: livePrice?.lastPrice ?? 0,
    volume: livePrice?.volume24h ?? 0,
  };

  const candleColor = hoverData
    ? hoverData.close >= hoverData.open
      ? 'text-[#0ecb81]'
      : 'text-[#f6465d]'
    : changeColor;

  return (
    <div className={`w-full h-full ${isWidget ? '' : 'pt-2 px-2 pb-2'}`}>
      <div
        className={`w-full h-full ${isWidget ? '' : 'bg-[#181a20] rounded-sm border border-[#2b3139]'} relative flex flex-col`}>
        {!isWidget && (
          <div className="flex items-center px-3 h-8 border-b border-[#2b3139] text-sm flex-shrink-0">
            <div className="flex items-center gap-3 text-[11px] text-zinc-300 tabular-nums">
              <span className="text-zinc-500">O</span>
              <span className={candleColor}>{formatPrice(display.open)}</span>

              <span className="text-zinc-500">H</span>
              <span className={candleColor}>{formatPrice(display.high)}</span>

              <span className="text-zinc-500">L</span>
              <span className={candleColor}>{formatPrice(display.low)}</span>

              <span className="text-zinc-500">C</span>
              <span className={candleColor}>{formatPrice(display.close)}</span>

              <span className="text-zinc-500">V</span>
              <span className={candleColor}>{formatVolume(display.volume)}</span>

              <span className={`ml-1 font-semibold ${changeColor}`}>
                {livePrice?.priceChangePercent24h != null ? `${changeSign}${livePrice.priceChangePercent24h.toFixed(2)}%` : '—'}
              </span>

              <span className="ml-2 flex items-center gap-1 text-zinc-400">
                <span className={`w-1.5 h-1.5 rounded-full ${getStatusColor(health?.connectionStatus)}`}/>
              </span>
            </div>
          </div>
        )}

        <div ref={containerRef} className="flex-1 w-full"/>
      </div>
    </div>
  );
});

ChartArea.displayName = 'ChartArea';

export default ChartArea;
