import {useEffect, useState} from 'react';
import {api, Exchange, ExchangeHealthDto, ExchangeMetadata, LivePrice, Ticker24h, TradingPair} from '../api';
import {Button, Card} from './ui';

// --- SVG Mini Chart (Sparkline) ---
function Sparkline({data, isPositive}: { data: number[]; isPositive: boolean }) {
  if (!data || data.length === 0) return <div className="w-24 h-8"/>;

  const min = Math.min(...data);
  const max = Math.max(...data);
  const range = max - min || 1;
  const points = data.map((d, i) => `${(i / (data.length - 1)) * 100},${100 - ((d - min) / range) * 100}`).join(' ');
  const color = isPositive ? '#0ecb81' : '#f6465d';

  return (
    <svg viewBox="0 0 100 100" className="w-24 h-10 overflow-visible" preserveAspectRatio="none">
      <polyline points={points} fill="none" stroke={color} strokeWidth="2.5" strokeLinecap="round"
                strokeLinejoin="round"/>
    </svg>
  );
}

// --- Table Row ---
function DashboardRow({
                        index, exchange, pair, priceData, onClick
                      }: {
  index: number;
  exchange: Exchange;
  pair: TradingPair;
  priceData?: LivePrice | Ticker24h;
  onClick: () => void;
}) {
  const [history, setHistory] = useState<number[]>([]);

  useEffect(() => {
    let isMounted = true;
    api.getHistoricalPrices(exchange, {tradingPair: pair, chartInterval: 'FIFTEEN_MINUTES', limit: 96})
      .then(data => {
        if (isMounted) setHistory(data.map(d => Number(d.close)));
      })
      .catch(console.error);

    return () => {
      isMounted = false;
    };
  }, [exchange, pair]);

  const displayPair = pair.replace('_', '/');
  const isPositive = (priceData?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';
  const sign = isPositive ? '+' : '';

  return (
    <tr
      onClick={onClick}
      className="border-b border-[#2b3139]/50 hover:bg-[#2b3139]/30 transition-colors cursor-pointer group"
    >
      <td className="p-4 text-[#848e9c] w-12 text-sm">{index + 1}</td>
      <td className="p-4 font-bold text-[#eaecef] text-sm">{displayPair}</td>
      <td className="p-4 text-right font-medium text-[#eaecef] text-sm">
        {priceData?.lastPrice ? priceData.lastPrice.toLocaleString(undefined, {minimumFractionDigits: 2}) : '—'}
      </td>
      <td className={`p-4 text-right font-medium text-sm ${colorClass}`}>
        {priceData?.priceChangePercent24h != null ? `${sign}${priceData.priceChangePercent24h.toFixed(2)}%` : '—'}
      </td>
      <td className="p-4">
        <div className="flex justify-end">
          <Sparkline data={history} isPositive={isPositive}/>
        </div>
      </td>
    </tr>
  );
}

// --- Main Dashboard ---
type Props = {
  metadata: ExchangeMetadata[];
  onSelectPair: (exchange: Exchange, pair: TradingPair) => void;
};

export default function Dashboard({metadata, onSelectPair}: Props) {
  const [activeTab, setActiveTab] = useState<Exchange | null>(null);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice | Ticker24h>>({});
  const [health, setHealth] = useState<ExchangeHealthDto | null>(null);
  const [visibleCount, setVisibleCount] = useState(10);

  useEffect(() => {
    if (metadata.length > 0 && !activeTab) {
      setActiveTab(metadata[0].exchange);
    }
  }, [metadata, activeTab]);

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
        return 'bg-[#0ecb81] glow-success';
      case 'RECONNECTING':
        return 'bg-[#fcd535] shadow-[0_0_8px_#fcd535] animate-pulse';
      case 'ERROR':
        return 'bg-[#f6465d] glow-danger';
      case 'DISCONNECTED':
      default:
        return 'bg-[#848e9c]';
    }
  };

  const activeMetadata = metadata.find(m => m.exchange === activeTab);
  const pairsToList = activeMetadata?.supportedPairs || [];

  return (
    <div className="flex-1 bg-[#0b0e14] overflow-y-auto p-8">
      <div className="max-w-5xl mx-auto">

        <h1 className="text-3xl font-bold text-[#eaecef] mb-2 tracking-tight">Market Overview</h1>
        <p className="text-[#848e9c] mb-8">Real-time market data from integrated exchanges</p>

        {/* Exchange Tabs */}
        <div className="flex items-center gap-1 mb-6 border-b border-[#2b3139] pb-px">
          {metadata.map(m => (
            <button
              key={m.exchange}
              onClick={() => setActiveTab(m.exchange)}
              className={`
                px-5 py-3 text-sm font-semibold transition-all relative rounded-t-lg
                ${activeTab === m.exchange
                  ? 'text-[#fcd535] bg-[#2b3139]/30'
                  : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-[#2b3139]/20'
                }
              `}
            >
              {m.exchange}
              {activeTab === m.exchange && (
                <div className="absolute bottom-0 left-0 w-full h-0.5 bg-[#fcd535] rounded-full"/>
              )}
            </button>
          ))}

          {/* Health Indicator */}
          {activeTab && (
            <div className="ml-auto flex items-center gap-2 bg-[#181a20] border border-[#2b3139] px-3 py-1.5 rounded-md">
              <div className={`w-2 h-2 rounded-full ${getStatusColor(health?.connectionStatus)}`}/>
              <span className="text-[#eaecef] text-xs font-medium">Market is Open</span>
            </div>
          )}
        </div>

        {/* Table */}
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
              <tr className="border-b border-[#2b3139] text-[#848e9c] text-xs uppercase tracking-wider bg-[#0b0e14]/50">
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
                  onClick={() => activeTab && onSelectPair(activeTab, pair)}
                />
              ))}
              </tbody>
            </table>
          </div>

          {pairsToList.length === 0 && (
            <div className="p-8 text-center text-[#848e9c]">
              No trading pairs available for this exchange.
            </div>
          )}

          {visibleCount < pairsToList.length && (
            <div className="p-4 flex justify-center border-t border-[#2b3139]">
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
