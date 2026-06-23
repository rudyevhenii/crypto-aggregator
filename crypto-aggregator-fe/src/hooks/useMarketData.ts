import { useState, useEffect, useRef, useCallback } from 'react';
import { api, Exchange, TradingPair, ChartInterval, LivePrice, HistoricalPrice, ExchangeMetadata, ExchangeHealthDto } from '../api';
import { ChartHandle } from '../components/ChartArea';

export default function useMarketData() {
  const [metadata, setMetadata] = useState<ExchangeMetadata[]>([]);

  const [selectedExchange, setSelectedExchange] = useState<Exchange | null>(null);
  const [selectedPair, setSelectedPair] = useState<TradingPair | null>(null);
  const [selectedInterval, setSelectedInterval] = useState<ChartInterval | null>(null);

  const [livePrice, setLivePrice] = useState<LivePrice | null>(null);
  const [historical, setHistorical] = useState<HistoricalPrice[] | null>(null);
  const [exchangeHealth, setExchangeHealth] = useState<ExchangeHealthDto | null>(null);

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

  // 3. Завантаження історії та SSE (Ціни + Статус Біржі)
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

    // --- Потік Цін ---
    const priceSource = api.streamPrices(selectedExchange, selectedPair);

    priceSource.onmessage = (event) => {
      try {
        const price: LivePrice = JSON.parse(event.data);
        setLivePrice(price);
        chartRef.current?.applyLivePrice(price);
      } catch (err) {
        console.error("SSE Parse Error:", err);
      }
    };

    priceSource.onerror = () => priceSource.close();

    // --- Потік Статусу Біржі ---
    const healthSource = api.streamExchangeHealth(selectedExchange);

    healthSource.onmessage = (event) => {
      try {
        const health: ExchangeHealthDto = JSON.parse(event.data);
        setExchangeHealth(health);
      } catch (err) {
        console.error("Health SSE Parse Error:", err);
      }
    };

    healthSource.onerror = () => {
      setExchangeHealth(prev => prev ? { ...prev, connectionStatus: 'DISCONNECTED' } : null);
      healthSource.close();
    };

    return () => {
      priceSource.close();
      healthSource.close();
      setLivePrice(null);
      setExchangeHealth(null);
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

  return {
    metadata,
    selectedExchange,
    selectedPair,
    selectedInterval,
    livePrice,
    historical,
    exchangeHealth,
    hasMoreHistory,
    chartRef,
    availablePairs,
    availableIntervals,
    handleLoadMoreHistory,
    setSelectedExchange,
    setSelectedPair,
    setSelectedInterval,
  } as const;
}

