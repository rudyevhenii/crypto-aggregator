import { useState, useEffect, useRef, useCallback } from 'react';
import TopBar from './components/TopBar';
import Sidebar from './components/Sidebar';
import ChartArea, { ChartHandle } from './components/ChartArea';
import { api, Exchange, TradingPair, ChartInterval, LivePrice, HistoricalPrice, ExchangeMetadata } from './api';

function App() {
  const [metadata, setMetadata] = useState<ExchangeMetadata[]>([]);

  const [selectedExchange, setSelectedExchange] = useState<Exchange | null>(null);
  const [selectedPair, setSelectedPair] = useState<TradingPair | null>(null);
  const [selectedInterval, setSelectedInterval] = useState<ChartInterval | null>(null);

  const [livePrice, setLivePrice] = useState<LivePrice | null>(null);
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);

  // Стан для відстеження, чи є ще старіші дані на бекенді
  const [hasMoreHistory, setHasMoreHistory] = useState<boolean>(true);

  const chartRef = useRef<ChartHandle>(null);

  // 1. Отримуємо всі метадані одним запитом при старті
  useEffect(() => {
    api.getMetadata().then(data => {
      setMetadata(data);
      if (data.length > 0) {
        const firstEx = data[0];
        setSelectedExchange(firstEx.exchange);
        setSelectedPair(firstEx.supportedPairs?.[0] || 'BTC_USD');

        const defaultInterval = firstEx.supportedIntervals?.includes('FIFTEEN_MINUTES')
          ? 'FIFTEEN_MINUTES'
          : firstEx.supportedIntervals?.[0];

        setSelectedInterval(defaultInterval);
      }
    }).catch(console.error);
  }, []);

  // 2. Оновлюємо списки залежно від обраної біржі
  const activeExchangeData = metadata.find(m => m.exchange === selectedExchange);
  const availablePairs = activeExchangeData?.supportedPairs || [];
  const availableIntervals = activeExchangeData?.supportedIntervals || [];

  // 3. Завантаження історії та SSE
  useEffect(() => {
    if (!selectedExchange || !selectedPair || !selectedInterval) return;

    // Скидаємо прапорець пагінації при зміні торгової пари або інтервалу
    setHasMoreHistory(true);

    api.getHistoricalPrices(selectedExchange, {
      tradingPair: selectedPair,
      chartInterval: selectedInterval
    })
      .then(setHistorical)
      .catch(console.error);

    // Якщо у вашому api.ts метод називається streamPairPrices, замініть streamPrices на streamPairPrices
    const eventSource = api.streamPrices(selectedExchange, selectedPair);

    eventSource.onmessage = (event) => {
      try {
        const price: LivePrice = JSON.parse(event.data);
        setLivePrice(price);
        chartRef.current?.applyLivePrice(price);
      } catch (err) {
        console.error("SSE Parse Error:", err);
      }
    };

    eventSource.onerror = () => eventSource.close();

    return () => {
      eventSource.close();
      setLivePrice(null);
    };
  }, [selectedExchange, selectedPair, selectedInterval]);

  // 4. Функція для завантаження старіших даних (Пагінація)
  const handleLoadMoreHistory = useCallback(async (oldestTimestamp: string) => {
    if (!hasMoreHistory || !selectedExchange || !selectedPair || !selectedInterval) return;

    try {
      const olderData = await api.getHistoricalPrices(selectedExchange, {
        tradingPair: selectedPair,
        chartInterval: selectedInterval,
        endTimeCursor: oldestTimestamp // Передаємо час найлівішої свічки
      });

      if (olderData.length === 0) {
        // Якщо даних більше немає, зупиняємо подальші запити
        setHasMoreHistory(false);
      } else {
        // Додаємо старі свічки на початок масиву
        setHistorical(prev => prev ? [...olderData, ...prev] : olderData);
      }
    } catch (err) {
      console.error("Failed to load more history", err);
    }
  }, [hasMoreHistory, selectedExchange, selectedPair, selectedInterval]);

  return (
    <div className="flex flex-col h-screen w-full bg-[#0b0e14] font-sans overflow-hidden">
      <TopBar pair={selectedPair} livePrice={livePrice} />

      <div className="flex flex-1 overflow-hidden">
        <main className="flex-1 flex flex-col">
          {selectedExchange && selectedPair && selectedInterval ? (
            <ChartArea
              ref={chartRef}
              interval={selectedInterval}
              historical={historical}
              onLoadMore={handleLoadMoreHistory} // Передаємо функцію пагінації у графік
            />
          ) : (
            <div className="flex-1 flex items-center justify-center text-[#848e9c]">
              Loading market data...
            </div>
          )}
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

export default App;