import {useCallback, useEffect, useRef, useState} from 'react';
import {
  api,
  ChartInterval,
  Exchange,
  ExchangeHealthDto,
  ExchangeMetadata,
  HistoricalPrice,
  LivePrice,
  TradingPair
} from '../api';
import {ChartHandle} from '../components/ChartArea';

export default function useMarketData() {
  const [metadata, setMetadata] = useState<ExchangeMetadata[]>([]);

  const [selectedExchange, setSelectedExchange] = useState<Exchange>('BINANCE');
  const [selectedPair, setSelectedPair] = useState<TradingPair>('BTC_USD');
  const [selectedInterval, setSelectedInterval] = useState<ChartInterval>('FIFTEEN_MINUTES');

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
    }).catch(() => {
      // Metadata load failure handled by empty state
    });
  }, []);

  // 2. Оновлюємо списки залежно від обраної біржі
  const activeExchangeData = metadata.find(m => m.exchange === selectedExchange);
  const availablePairs = activeExchangeData?.supportedPairs || [];
  const availableIntervals = activeExchangeData?.supportedIntervals || [];

  // 3. Завантаження історії та SSE (Ціни + Статус Біржі)
  useEffect(() => {
    // Скидаємо прапорець пагінації при зміні торгової пари або інтервалу
    setHasMoreHistory(true);

    api.getHistoricalPrices(selectedExchange, {
      tradingPair: selectedPair,
      chartInterval: selectedInterval
    })
      .then(setHistorical)
      .catch(() => {
        // Historical prices load failure handled by empty state
      });

    // --- Потік Цін ---
    const priceSource = api.streamPrices(selectedExchange, selectedPair);

    priceSource.onmessage = (event) => {
      try {
        const price: LivePrice = JSON.parse(event.data);
        setLivePrice(price);
        chartRef.current?.applyLivePrice(price);
      } catch {
        // SSE parse error handled silently
      }
    };

    priceSource.onerror = () => priceSource.close();

    // --- Потік Статусу Біржі ---
    const healthSource = api.streamExchangeHealth(selectedExchange);

    healthSource.onmessage = (event) => {
      try {
        const health: ExchangeHealthDto = JSON.parse(event.data);
        setExchangeHealth(health);
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
  }, [selectedExchange, selectedPair, selectedInterval]);

  // 4. Функція для завантаження старіших даних (Пагінація)
  const handleLoadMoreHistory = useCallback(async (oldestTimestamp: string) => {
    if (!hasMoreHistory) return;

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
    } catch {
      // History load failure handled by empty state
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

