import {useEffect, useState} from 'react';
import {useParams, useNavigate, useSearchParams} from 'react-router-dom';
import {api, Exchange, ChartInterval, LivePrice, ExchangeHealthDto, HistoricalPrice, TradingPair} from '../../api';
import TopBar from '../TopBar';
import Sidebar from '../Sidebar';
import ChartArea from '../ChartArea';
import {ChartHandle} from '../ChartArea';

const CHART_INTERVALS: ChartInterval[] = [
  'ONE_SECOND', 'ONE_MINUTE', 'THREE_MINUTES', 'FIVE_MINUTES',
  'FIFTEEN_MINUTES', 'THIRTY_MINUTES', 'ONE_HOUR', 'TWO_HOURS',
  'FOUR_HOURS', 'SIX_HOURS', 'EIGHT_HOURS', 'TWELVE_HOURS',
  'ONE_DAY', 'THREE_DAYS', 'FIFTEEN_DAYS', 'ONE_WEEK', 'ONE_MONTH'
];

export default function ChartRoute() {
  const params = useParams<{ exchange: string; symbol: string }>();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();

  const exchange = params.exchange?.toUpperCase() as Exchange | undefined;
  const symbol = params.symbol?.toUpperCase() as TradingPair | undefined;

  const [livePrice, setLivePrice] = useState<LivePrice | null>(null);
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);
  const [exchangeHealth, setExchangeHealth] = useState<ExchangeHealthDto | null>(null);
  const [metadata, setMetadata] = useState<{ exchange: Exchange; supportedPairs: TradingPair[]; supportedIntervals: ChartInterval[] }[]>([]);
  const [availablePairs, setAvailablePairs] = useState<TradingPair[]>([]);
  const [availableIntervals, setAvailableIntervals] = useState<ChartInterval[]>([]);

  const [chartHandle, setChartHandle] = useState<ChartHandle | null>(null);

  const urlInterval = searchParams.get('interval');
  const isValidChartInterval = (val: string | null): val is ChartInterval => {
    if (!val) return false;
    return CHART_INTERVALS.includes(val as ChartInterval);
  };

  const defaultInterval = availableIntervals.includes('FIFTEEN_MINUTES')
    ? 'FIFTEEN_MINUTES'
    : availableIntervals[0] ?? 'FIFTEEN_MINUTES';

  const effectiveInterval = (() => {
    if (isValidChartInterval(urlInterval)) {
      if (availableIntervals.length > 0) {
        return availableIntervals.includes(urlInterval) ? urlInterval : defaultInterval;
      }
      return urlInterval;
    }
    return defaultInterval;
  })();

  useEffect(() => {
    if (effectiveInterval !== urlInterval) {
      setSearchParams(prev => {
        const next = new URLSearchParams(prev);
        next.set('interval', effectiveInterval);
        return next;
      }, { replace: true });
    }
  }, [effectiveInterval, urlInterval, setSearchParams]);

  useEffect(() => {
    if (!exchange || !symbol) {
      navigate('/app/overview');
      return;
    }
  }, [exchange, symbol, navigate]);

  useEffect(() => {
    if (!exchange || !symbol) return;

    api.getMetadata().then(data => {
      setMetadata(data);
      const exData = data.find(m => m.exchange === exchange);
      if (exData) {
        setAvailablePairs(exData.supportedPairs);
        setAvailableIntervals(exData.supportedIntervals);
        if (!exData.supportedPairs.includes(symbol)) {
          const fallback = exData.supportedPairs[0] || 'BTC_USD';
          const currentInterval = searchParams.get('interval');
          const query = currentInterval ? `?interval=${currentInterval}` : '';
          navigate(`/app/chart/${exchange}/${fallback}${query}`, { replace: true });
        }
      }
    }).catch(() => {
      // Metadata load failure handled by empty state
    });
    // searchParams is intentionally excluded to avoid re-running on every search change;
    // the interval query is read directly inside the effect when needed.
  }, [exchange, symbol, navigate]);

  useEffect(() => {
    if (!exchange || !symbol) return;

    api.getHistoricalPrices(exchange, {
      tradingPair: symbol,
      chartInterval: effectiveInterval
    })
      .then(setHistorical)
      .catch(() => {
        // Historical prices load failure handled by empty state
      });

    const priceSource = api.streamPrices(exchange, symbol);

    priceSource.onmessage = (event) => {
      try {
        const price: LivePrice = JSON.parse(event.data);
        setLivePrice(price);
        chartHandle?.applyLivePrice(price);
      } catch {
        // SSE parse error handled silently
      }
    };

    priceSource.onerror = () => priceSource.close();

    const healthSource = api.streamExchangeHealth(exchange);

    healthSource.onmessage = (event) => {
      try {
        setExchangeHealth(JSON.parse(event.data));
      } catch {
        // Health SSE parse error handled silently
      }
    };

    healthSource.onerror = () => {
      setExchangeHealth(prev => prev ? {...prev, connectionStatus: 'DISCONNECTED'} : null);
      healthSource.close();
    };

    return () => {
      priceSource.close();
      healthSource.close();
      setLivePrice(null);
      setExchangeHealth(null);
    };
  }, [exchange, symbol, effectiveInterval, chartHandle]);

  const handleLoadMoreHistory = async (oldestTimestamp: string) => {
    if (!exchange || !symbol) return;
    try {
      const olderData = await api.getHistoricalPrices(exchange, {
        tradingPair: symbol,
        chartInterval: effectiveInterval,
        endTimeCursor: oldestTimestamp
      });

      if (olderData.length === 0) {
        return;
      } else {
        setHistorical(prev => prev ? [...olderData, ...prev] : olderData);
      }
    } catch {
      // History load failure handled by empty state
    }
  };

  const handleIntervalChange = (newInterval: ChartInterval) => {
    setSearchParams(prev => {
      const next = new URLSearchParams(prev);
      next.set('interval', newInterval);
      return next;
    }, { replace: true });
  };

  const handleExchangeChange = (ex: Exchange) => {
    const currentInterval = searchParams.get('interval');
    const query = currentInterval ? `?interval=${currentInterval}` : '';
    navigate(`/app/chart/${ex}/${symbol}${query}`);
  };

  const handlePairChange = (pair: TradingPair) => {
    const currentInterval = searchParams.get('interval');
    const query = currentInterval ? `?interval=${currentInterval}` : '';
    navigate(`/app/chart/${exchange}/${pair}${query}`);
  };

  if (!exchange || !symbol) {
    return null;
  }

  return (
    <div className="flex flex-col h-full w-full">
      <TopBar
        exchange={exchange}
        pair={symbol}
        livePrice={livePrice}
        health={exchangeHealth}
        onBack={() => navigate('/app/overview')}
      />

      <div className="flex flex-1 overflow-hidden">
        <main className="flex-1 flex flex-col">
          <ChartArea
            ref={setChartHandle}
            interval={effectiveInterval}
            historical={historical}
            onLoadMore={handleLoadMoreHistory}
            exchange={exchange}
            tradingPair={symbol}
          />
        </main>

        <Sidebar
          exchanges={metadata.map(m => m.exchange)}
          pairs={availablePairs}
          intervals={availableIntervals}
          selectedExchange={exchange}
          selectedPair={symbol}
          selectedInterval={effectiveInterval}
          livePrice={livePrice}
          onExchangeChange={handleExchangeChange}
          onPairChange={handlePairChange}
          onIntervalChange={handleIntervalChange}
        />
      </div>
    </div>
  );
}
