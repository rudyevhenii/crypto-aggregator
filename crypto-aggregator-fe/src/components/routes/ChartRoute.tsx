import {useEffect, useState} from 'react';
import {useParams, useNavigate} from 'react-router-dom';
import {api, Exchange, ChartInterval, LivePrice, ExchangeHealthDto, HistoricalPrice, TradingPair} from '../../api';
import TopBar from '../TopBar';
import Sidebar from '../Sidebar';
import ChartArea from '../ChartArea';
import {ChartHandle} from '../ChartArea';

export default function ChartRoute() {
  const {exchange, symbol} = useParams<{ exchange: string; symbol: string }>();
  const navigate = useNavigate();

  const [selectedExchange, setSelectedExchange] = useState<Exchange>('BINANCE');
  const [selectedPair, setSelectedPair] = useState<TradingPair>('BTC_USD');
  const [selectedInterval, setSelectedInterval] = useState<ChartInterval>('FIFTEEN_MINUTES');
  const [livePrice, setLivePrice] = useState<LivePrice | null>(null);
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);
  const [exchangeHealth, setExchangeHealth] = useState<ExchangeHealthDto | null>(null);
  const [metadata, setMetadata] = useState<{ exchange: Exchange; supportedPairs: TradingPair[]; supportedIntervals: ChartInterval[] }[]>([]);
  const [availablePairs, setAvailablePairs] = useState<TradingPair[]>([]);
  const [availableIntervals, setAvailableIntervals] = useState<ChartInterval[]>([]);

  const [chartHandle, setChartHandle] = useState<ChartHandle | null>(null);

  useEffect(() => {
    if (!exchange || !symbol) {
      navigate('/app/overview');
      return;
    }

    const ex = exchange.toUpperCase() as Exchange;
    const pair = symbol.toUpperCase() as TradingPair;

    setSelectedExchange(ex);
    setSelectedPair(pair);
  }, [exchange, symbol, navigate]);

  useEffect(() => {
    api.getMetadata().then(data => {
      setMetadata(data);
      const exData = data.find(m => m.exchange === selectedExchange);
      if (exData) {
        setAvailablePairs(exData.supportedPairs);
        setAvailableIntervals(exData.supportedIntervals);
        if (!exData.supportedPairs.includes(selectedPair)) {
          setSelectedPair(exData.supportedPairs[0] || 'BTC_USD');
        }
        const defaultInterval = exData.supportedIntervals.includes('FIFTEEN_MINUTES')
          ? 'FIFTEEN_MINUTES'
          : exData.supportedIntervals[0];
        setSelectedInterval(defaultInterval);
      }
    }).catch(console.error);
  }, [selectedExchange]);

  useEffect(() => {
    if (!selectedExchange || !selectedPair) return;

    api.getHistoricalPrices(selectedExchange, {
      tradingPair: selectedPair,
      chartInterval: selectedInterval
    })
      .then(setHistorical)
      .catch(console.error);

    const priceSource = api.streamPrices(selectedExchange, selectedPair);

    priceSource.onmessage = (event) => {
      try {
        const price: LivePrice = JSON.parse(event.data);
        setLivePrice(price);
        chartHandle?.applyLivePrice(price);
      } catch (err) {
        console.error("SSE Parse Error:", err);
      }
    };

    priceSource.onerror = () => priceSource.close();

    const healthSource = api.streamExchangeHealth(selectedExchange);

    healthSource.onmessage = (event) => {
      try {
        setExchangeHealth(JSON.parse(event.data));
      } catch (err) {
        console.error("Health SSE Parse Error:", err);
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
  }, [selectedExchange, selectedPair, selectedInterval, chartHandle]);

  const handleLoadMoreHistory = async (oldestTimestamp: string) => {
    try {
      const olderData = await api.getHistoricalPrices(selectedExchange, {
        tradingPair: selectedPair,
        chartInterval: selectedInterval,
        endTimeCursor: oldestTimestamp
      });

      if (olderData.length === 0) {
        return;
      } else {
        setHistorical(prev => prev ? [...olderData, ...prev] : olderData);
      }
    } catch (err) {
      console.error("Failed to load more history", err);
    }
  };

  return (
    <div className="flex flex-col h-full w-full">
      <TopBar
        exchange={selectedExchange}
        pair={selectedPair}
        livePrice={livePrice}
        health={exchangeHealth}
        onBack={() => navigate('/app/overview')}
      />

      <div className="flex flex-1 overflow-hidden">
        <main className="flex-1 flex flex-col">
          <ChartArea
            ref={setChartHandle}
            interval={selectedInterval}
            historical={historical}
            onLoadMore={handleLoadMoreHistory}
            exchange={selectedExchange}
            tradingPair={selectedPair}
          />
        </main>

        <Sidebar
          exchanges={metadata.map(m => m.exchange)}
          pairs={availablePairs}
          intervals={availableIntervals}
          selectedExchange={selectedExchange}
          selectedPair={selectedPair}
          selectedInterval={selectedInterval}
          livePrice={livePrice}
          onExchangeChange={(ex) => {
            setSelectedExchange(ex);
            const newExData = metadata.find(m => m.exchange === ex);
            if (newExData) {
              setSelectedPair(newExData.supportedPairs[0]);
              const defaultInterval = newExData.supportedIntervals.includes('FIFTEEN_MINUTES')
                ? 'FIFTEEN_MINUTES'
                : newExData.supportedIntervals[0];
              setSelectedInterval(defaultInterval);
            }
          }}
          onPairChange={setSelectedPair}
          onIntervalChange={setSelectedInterval}
        />
      </div>
    </div>
  );
}
