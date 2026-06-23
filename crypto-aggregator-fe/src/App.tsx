import { useState } from 'react';
import TopBar from './components/TopBar';
import Sidebar from './components/Sidebar';
import ChartArea from './components/ChartArea';
import Dashboard from './components/Dashboard';
import useMarketData from './hooks/useMarketData';
import { Exchange, TradingPair } from './api';

function App() {
  // Керування сторінками: 'dashboard' - головна, 'chart' - термінал
  const [currentView, setCurrentView] = useState<'dashboard' | 'chart'>('dashboard');

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

  // Функція переходу на графік при кліку на рядок в таблиці
  const handleSelectPair = (exchange: Exchange, pair: TradingPair) => {
    setSelectedExchange(exchange);
    setSelectedPair(pair);

    // Встановлюємо дефолтний інтервал при переході
    const exData = metadata.find(m => m.exchange === exchange);
    if (exData && !selectedInterval) {
      setSelectedInterval(
        exData.supportedIntervals.includes('FIFTEEN_MINUTES')
          ? 'FIFTEEN_MINUTES'
          : exData.supportedIntervals[0]
      );
    }
    setCurrentView('chart');
  };

  return (
    <div className="flex flex-col h-screen w-full bg-[#0b0e14] font-sans overflow-hidden">

      {currentView === 'dashboard' ? (
        <Dashboard
          metadata={metadata}
          onSelectPair={handleSelectPair}
        />
      ) : (
        <div className="flex flex-col h-full">
          {/* TopBar показується ТІЛЬКИ в режимі графіка */}
          <TopBar
            exchange={selectedExchange}
            pair={selectedPair}
            livePrice={livePrice}
            health={exchangeHealth}
            // Передайте цю функцію у TopBar, якщо хочете додати кнопку "Back" біля логотипу
            onBack={() => setCurrentView('dashboard')}
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
      )}

    </div>
  );
}

export default App;