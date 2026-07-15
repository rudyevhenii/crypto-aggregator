import {useCallback, useEffect, useMemo, useState} from 'react';
import {api, Exchange, ExchangeHealthDto, ExchangeMetadata, LivePrice, Ticker24h, TradingPair} from '../api';

const PAGE_SIZE = 10;

type UseExchangeOverviewReturn = {
  metadata: ExchangeMetadata[];
  activeTab: Exchange | null;
  livePrices: Record<string, LivePrice | Ticker24h>;
  health: ExchangeHealthDto | null;
  loadedTickers: Ticker24h[];
  currentPage: number;
  isLoadingMore: boolean;
  activeMetadata: ExchangeMetadata | undefined;
  allPairs: TradingPair[];
  hasMore: boolean;
  setActiveTab: (exchange: Exchange) => void;
  handleLoadMore: () => Promise<void>;
  handleSelectPair: (exchange: Exchange, pair: TradingPair) => void;
  getStatusColor: (status?: string) => string;
};

export default function useExchangeOverview(): UseExchangeOverviewReturn {
  const [metadata, setMetadata] = useState<ExchangeMetadata[]>([]);
  const [activeTab, setActiveTab] = useState<Exchange | null>(null);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice | Ticker24h>>({});
  const [health, setHealth] = useState<ExchangeHealthDto | null>(null);
  const [loadedTickers, setLoadedTickers] = useState<Ticker24h[]>([]);
  const [currentPage, setCurrentPage] = useState(0);
  const [isLoadingMore, setIsLoadingMore] = useState(false);

  useEffect(() => {
    api.getMetadata().then(data => {
      setMetadata(data);
      if (data.length > 0) {
        setActiveTab(data[0].exchange);
      }
    }).catch(() => {
      // Initial metadata load failure is handled by empty state
    });
  }, []);

  const activeMetadata = metadata.find(m => m.exchange === activeTab);
  const allPairs = useMemo(() => activeMetadata?.supportedPairs || [], [activeMetadata]);

  const loadPage = useCallback(async (exchange: Exchange, page: number) => {
    const start = page * PAGE_SIZE;
    const pairsToLoad = allPairs.slice(start, start + PAGE_SIZE);

    if (pairsToLoad.length === 0) return;

    if (page === 0) {
      setIsLoadingMore(true);
    }

    try {
      const tickers = await api.get24hTickersByExchange(exchange, pairsToLoad);
      const sortedTickers = tickers.sort((a, b) => {
        const indexA = pairsToLoad.indexOf(a.tradingPair);
        const indexB = pairsToLoad.indexOf(b.tradingPair);
        return indexA - indexB;
      });
      setLoadedTickers(prev => page === 0 ? sortedTickers : [...prev, ...sortedTickers]);
      setCurrentPage(page);
    } catch {
      if (page === 0) {
        setLoadedTickers([]);
      }
    } finally {
      setIsLoadingMore(false);
    }
  }, [allPairs]);

  useEffect(() => {
    if (!activeTab) return;
    setLoadedTickers([]);
    setCurrentPage(0);
    setLivePrices({});
    void loadPage(activeTab, 0);
  }, [activeTab, loadPage]);

  useEffect(() => {
    if (!activeTab) return;

    let priceSource: EventSource | null = null;
    let healthSource: EventSource | null = null;

    const loadedPairs = new Set(loadedTickers.map(t => t.tradingPair));

    priceSource = api.streamPricesByExchange(activeTab);
    priceSource.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const updates = Array.isArray(data) ? data : [data];

        setLivePrices(prev => {
          const newMap = {...prev};
          updates.forEach((p: LivePrice) => {
            if (p.tradingPair && loadedPairs.has(p.tradingPair)) {
              newMap[p.tradingPair] = p;
            }
          });
          return newMap;
        });
      } catch {
        // SSE parse error handled silently
      }
    };
    priceSource.onerror = () => priceSource?.close();

    setHealth(null);
    healthSource = api.streamExchangeHealth(activeTab);
    healthSource.onmessage = (event) => {
      try {
        setHealth(JSON.parse(event.data));
      } catch {
        // SSE parse error handled silently
      }
    };
    healthSource.onerror = () => {
      setHealth(prev => prev ? {...prev, connectionStatus: 'DISCONNECTED'} : null);
      healthSource?.close();
    };

    return () => {
      if (priceSource) priceSource.close();
      if (healthSource) healthSource.close();
      setLivePrices({});
    };
  }, [activeTab, loadedTickers]);

  const handleLoadMore = useCallback(async () => {
    if (!activeTab || isLoadingMore) return;
    await loadPage(activeTab, currentPage + 1);
  }, [activeTab, currentPage, isLoadingMore, loadPage]);

  const handleSelectPair = useCallback((exchange: Exchange, pair: TradingPair) => {
    window.location.href = `/app/chart/${exchange}/${pair}`;
  }, []);

  const getStatusColor = useCallback((status?: string) => {
    switch (status) {
      case 'CONNECTED':
        return 'bg-[#0ecb81] shadow-[0_0_8px_rgba(14,203,129,0.4)]';
      case 'RECONNECTING':
        return 'bg-[#fcd535] shadow-[0_0_8px_rgba(252,213,53,0.4)] animate-pulse';
      case 'ERROR':
        return 'bg-[#f6465d] shadow-[0_0_8px_rgba(246,70,93,0.4)]';
      case 'DISCONNECTED':
      default:
        return 'bg-[#848e9c]';
    }
  }, []);

  const hasMore = allPairs.length > loadedTickers.length;

  return {
    metadata,
    activeTab,
    livePrices,
    health,
    loadedTickers,
    currentPage,
    isLoadingMore,
    activeMetadata,
    allPairs,
    hasMore,
    setActiveTab,
    handleLoadMore,
    handleSelectPair,
    getStatusColor,
  };
}
