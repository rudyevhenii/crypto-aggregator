import TopBar from './components/TopBar';
import Sidebar from './components/Sidebar';
import ChartArea from './components/ChartArea';
import useMarketData from './hooks/useMarketData';

function App() {
  const {
    metadata,
    selectedExchange,
    selectedPair,
    selectedInterval,
    livePrice,
    historical,
    exchangeHealth,
    chartRef,
    availablePairs,
    availableIntervals,
    handleLoadMoreHistory,
    setSelectedExchange,
    setSelectedPair,
    setSelectedInterval,
  } = useMarketData();

  return (
    <div className="flex flex-col h-screen w-full bg-[#0b0e14] font-sans overflow-hidden">
      {/* Передаємо health та exchange у TopBar */}
      <TopBar
        exchange={selectedExchange}
        pair={selectedPair}
        livePrice={livePrice}
        health={exchangeHealth}
      />

      <div className="flex flex-1 overflow-hidden">
        <main className="flex-1 flex flex-col">
          {selectedExchange && selectedPair && selectedInterval ? (
            <ChartArea
              ref={chartRef}
              interval={selectedInterval}
              historical={historical}
              onLoadMore={handleLoadMoreHistory}
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
