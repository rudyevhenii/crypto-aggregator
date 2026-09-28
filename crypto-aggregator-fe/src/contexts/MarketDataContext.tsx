import {createContext, useContext, useState, ReactNode} from 'react';

type MarketDataContextType = {
  currentPrice: number | undefined;
  setCurrentPrice: (price: number | undefined) => void;
};

const MarketDataContext = createContext<MarketDataContextType | null>(null);

export function useMarketDataContext() {
  const ctx = useContext(MarketDataContext);
  if (!ctx) throw new Error('useMarketDataContext must be used within MarketDataProvider');
  return ctx;
}

type Props = {
  children: ReactNode;
};

export function MarketDataProvider({children}: Props) {
  const [currentPrice, setCurrentPrice] = useState<number | undefined>(undefined);

  return (
    <MarketDataContext.Provider value={{currentPrice, setCurrentPrice}}>
      {children}
    </MarketDataContext.Provider>
  );
}
