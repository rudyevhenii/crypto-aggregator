import {useEffect, useState, useCallback} from 'react';
import {api, Exchange, ExchangeHealthDto, ExchangeMetadata, LivePrice, Ticker24h, TradingPair} from '../../api';
import {Button, Card} from '../ui';
import DashboardRow from '../DashboardRow';

const PAGE_SIZE = 10;

export default function OverviewRoute() {
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
    }).catch(console.error);
  }, []);

  const activeMetadata = metadata.find(m => m.exchange === activeTab);
  const allPairs = activeMetadata?.supportedPairs || [];

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
    } catch (err) {
      console.error('Failed to load tickers page', err);
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
      } catch (err) {
        console.error("Dashboard SSE Parse Error:", err);
      }
    };
    priceSource.onerror = () => priceSource?.close();

    setHealth(null);
    healthSource = api.streamExchangeHealth(activeTab);
    healthSource.onmessage = (event) => {
      try {
        setHealth(JSON.parse(event.data));
      } catch (err) {
        console.error("Health SSE Parse Error:", err);
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

  const getStatusColor = (status?: string) => {
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
  };

  const handleSelectPair = (exchange: Exchange, pair: TradingPair) => {
    window.location.href = `/app/chart/${exchange}/${pair}`;
  };

  const handleLoadMore = useCallback(async () => {
    if (!activeTab || isLoadingMore) return;
    const start = (currentPage + 1) * PAGE_SIZE;
    const pairsToLoad = allPairs.slice(start, start + PAGE_SIZE);

    if (pairsToLoad.length === 0) return;

    setIsLoadingMore(true);
    try {
      const tickers = await api.get24hTickersByExchange(activeTab, pairsToLoad);
      const sortedTickers = tickers.sort((a, b) => {
        const indexA = pairsToLoad.indexOf(a.tradingPair);
        const indexB = pairsToLoad.indexOf(b.tradingPair);
        return indexA - indexB;
      });
      setLoadedTickers(prev => [...prev, ...sortedTickers]);
      setCurrentPage(prev => prev + 1);
    } catch (err) {
      console.error('Failed to load more tickers', err);
    } finally {
      setIsLoadingMore(false);
    }
  }, [activeTab, currentPage, isLoadingMore, allPairs]);

  const hasMore = allPairs.length > loadedTickers.length;

  return (
    <div className="flex-1 bg-[#09090b] overflow-y-auto p-8 relative">
      {/* Ambient background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 left-1/2 -translate-x-1/2 w-[600px] h-[400px] bg-[#fcd535]/[0.03] rounded-full blur-3xl"/>
      </div>

      <div className="max-w-5xl mx-auto relative z-10">

        <h1 className="text-3xl font-bold text-zinc-50 mb-2 tracking-tight">Market Overview</h1>
        <p className="text-zinc-400 mb-8">Real-time market data from integrated exchanges</p>

        {/* Exchange Tabs */}
        <div className="flex items-center gap-1 mb-6 border-b border-white/5 pb-px">
          {metadata.map(m => (
            <button
              key={m.exchange}
              onClick={() => setActiveTab(m.exchange)}
              className={`
                px-5 py-3 text-sm font-semibold transition-all relative rounded-t-lg
                ${activeTab === m.exchange
                  ? 'text-[#fcd535] bg-white/5'
                  : 'text-zinc-400 hover:text-zinc-50 hover:bg-white/5'
                }
              `}
            >
              {m.exchange}
              {activeTab === m.exchange && (
                <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[#fcd535] rounded-full shadow-[0_0_8px_rgba(252,213,53,0.4)]"/>
              )}
            </button>
          ))}

          {/* Health Indicator */}
          {activeTab && (
            <div className="ml-auto flex items-center gap-2 glass-surface px-3 py-1.5 rounded-md">
              <div className={`w-2 h-2 rounded-full ${getStatusColor(health?.connectionStatus)}`}/>
              <span className="text-zinc-50 text-xs font-medium">Market is Open</span>
            </div>
          )}
        </div>

        {/* Table */}
        <Card className="overflow-hidden gradient-border">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
              <tr className="border-b border-white/5 text-zinc-400 text-xs uppercase tracking-wider bg-white/[0.02]">
                <th className="p-4 font-medium">#</th>
                <th className="p-4 font-medium">Trading Pair</th>
                <th className="p-4 font-medium text-right">Live Price</th>
                <th className="p-4 font-medium text-right">24h Change</th>
                <th className="p-4 font-medium text-right">Chart (24h)</th>
              </tr>
              </thead>
              <tbody className="text-sm">
              {loadedTickers.map((ticker, index) => (
                <DashboardRow
                  key={`${activeTab}-${ticker.tradingPair}`}
                  index={index}
                  exchange={activeTab!}
                  pair={ticker.tradingPair}
                  priceData={livePrices[ticker.tradingPair] ?? ticker}
                  onClick={() => activeTab && handleSelectPair(activeTab, ticker.tradingPair)}
                />
              ))}
              </tbody>
            </table>
          </div>

          {allPairs.length === 0 && (
            <div className="p-8 text-center text-zinc-400">
              No trading pairs available for this exchange.
            </div>
          )}

          {allPairs.length > 0 && loadedTickers.length === 0 && (
            <div className="p-8 text-center text-zinc-400">
              No data available at the moment.
            </div>
          )}

          {hasMore && (
            <div className="border-t border-white/5">
              <Button
                variant="ghost"
                onClick={handleLoadMore}
                isLoading={isLoadingMore}
                disabled={isLoadingMore}
                className="w-full rounded-none hover:bg-white/5 border-t border-white/5 text-zinc-400 hover:text-white transition-colors py-4"
              >
                {isLoadingMore ? 'Loading...' : 'Load More'}
              </Button>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
