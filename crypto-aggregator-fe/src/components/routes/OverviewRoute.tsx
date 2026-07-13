import {useEffect, useState} from 'react';
import {api, Exchange, ExchangeHealthDto, ExchangeMetadata, LivePrice, Ticker24h, TradingPair} from '../../api';
import {Button, Card} from '../ui';
import DashboardRow from '../DashboardRow';

export default function OverviewRoute() {
  const [metadata, setMetadata] = useState<ExchangeMetadata[]>([]);
  const [activeTab, setActiveTab] = useState<Exchange | null>(null);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice | Ticker24h>>({});
  const [health, setHealth] = useState<ExchangeHealthDto | null>(null);
  const [visibleCount, setVisibleCount] = useState(10);

  useEffect(() => {
    api.getMetadata().then(data => {
      setMetadata(data);
      if (data.length > 0) {
        setActiveTab(data[0].exchange);
      }
    }).catch(console.error);
  }, []);

  useEffect(() => {
    setVisibleCount(10);
  }, [activeTab]);

  useEffect(() => {
    if (!activeTab) return;

    let priceSource: EventSource | null = null;
    let healthSource: EventSource | null = null;

    api.get24hTickers(activeTab)
      .then(tickers => {
        const initialMap: Record<string, Ticker24h> = {};
        tickers.forEach(t => {
          initialMap[t.tradingPair] = t;
        });
        setLivePrices(initialMap);

        priceSource = api.streamPricesByExchange(activeTab);
        priceSource.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            const updates = Array.isArray(data) ? data : [data];

            setLivePrices(prev => {
              const newMap = {...prev};
              updates.forEach((p: LivePrice) => {
                if (p.tradingPair) newMap[p.tradingPair] = p;
              });
              return newMap;
            });
          } catch (err) {
            console.error("Dashboard SSE Parse Error:", err);
          }
        };
        priceSource.onerror = () => priceSource?.close();
      })
      .catch(console.error);

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
  }, [activeTab]);

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

  const activeMetadata = metadata.find(m => m.exchange === activeTab);
  const pairsToList = activeMetadata?.supportedPairs || [];

  const handleSelectPair = (exchange: Exchange, pair: TradingPair) => {
    window.location.href = `/app/chart/${exchange}/${pair}`;
  };

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
              {pairsToList.slice(0, visibleCount).map((pair, index) => (
                <DashboardRow
                  key={`${activeTab}-${pair}`}
                  index={index}
                  exchange={activeTab!}
                  pair={pair}
                  priceData={livePrices[pair]}
                  onClick={() => activeTab && handleSelectPair(activeTab, pair)}
                />
              ))}
              </tbody>
            </table>
          </div>

          {pairsToList.length === 0 && (
            <div className="p-8 text-center text-zinc-400">
              No trading pairs available for this exchange.
            </div>
          )}

          {visibleCount < pairsToList.length && (
            <div className="p-4 flex justify-center border-t border-white/5">
              <Button variant="secondary" onClick={() => setVisibleCount(prev => prev + 10)}>
                Load More
              </Button>
            </div>
          )}
        </Card>
      </div>
    </div>
  );
}
