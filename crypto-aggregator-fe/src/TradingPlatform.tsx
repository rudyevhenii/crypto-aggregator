import {useEffect, useState} from 'react';
import {Route, Routes, useNavigate} from 'react-router-dom';

import LandingPage from './components/LandingPage';
import Dashboard from './components/Dashboard';
import MainLayout from './components/MainLayout';
import WorkspaceView from './components/WorkspaceView';
import TopBar from './components/TopBar';
import Sidebar from './components/Sidebar';
import ChartArea from './components/ChartArea';

import useMarketData from './hooks/useMarketData';
import {Exchange, TradingPair} from './api';

function TradingPlatform() {
  const navigate = useNavigate();

  // ДОДАНО: 3 стани (таблиця, мультиграфік, одиночний графік)
  const [currentView, setCurrentView] = useState<'market' | 'workspace' | 'chart'>('market');

  useEffect(() => {
    if (!localStorage.getItem('accessToken')) {
      navigate('/');
    }
  }, [navigate]);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/');
  };

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

  // ВІДНОВЛЕНО: Стара логіка вибору пари з таблиці
  const handleSelectPair = (exchange: Exchange, pair: TradingPair) => {
    setSelectedExchange(exchange);
    setSelectedPair(pair);

    const exData = metadata.find(m => m.exchange === exchange);
    if (exData && !selectedInterval) {
      setSelectedInterval(
        exData.supportedIntervals.includes('FIFTEEN_MINUTES')
          ? 'FIFTEEN_MINUTES'
          : exData.supportedIntervals[0]
      );
    }
    // Переходимо на одиночний графік!
    setCurrentView('chart');
  };

  return (
    <MainLayout
      // Якщо ми на одиночному графіку, бокова панель має підсвічувати 'market'
      activeView={currentView === 'chart' ? 'market' : currentView}
      onViewChange={(view) => setCurrentView(view)}
      onLogout={handleLogout}
    >

      {currentView === 'market' && (
        <Dashboard
          metadata={metadata}
          onSelectPair={handleSelectPair}
        />
      )}

      {currentView === 'workspace' && (
        <WorkspaceView/>
      )}

      {/* ВІДНОВЛЕНО: Одиночний графік із Sidebar та TopBar */}
      {currentView === 'chart' && (
        <div className="flex flex-col h-full w-full">
          <TopBar
            exchange={selectedExchange}
            pair={selectedPair}
            livePrice={livePrice}
            health={exchangeHealth}
            onBack={() => setCurrentView('market')} // Кнопка "Назад" повертає в маркет
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

    </MainLayout>
  );
}

function App(): JSX.Element {
  return (
    <Routes>
      <Route path="/" element={<LandingPage/>}/>
      <Route path="/app" element={<TradingPlatform/>}/>
    </Routes>
  );
}

export default App;