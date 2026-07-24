import {Button, Card} from '../ui';
import DashboardRow from '../DashboardRow';
import useExchangeOverview from '../../hooks/useExchangeOverview';

export default function OverviewRoute() {
  const {
    metadata,
    activeTab,
    livePrices,
    health,
    loadedTickers,
    isLoadingMore,
    allPairs,
    hasMore,
    setActiveTab,
    handleLoadMore,
    handleSelectPair,
    getStatusColor,
  } = useExchangeOverview();

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
