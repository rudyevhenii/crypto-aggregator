import {createContext, useContext, useState, useEffect, ReactNode} from 'react';
import {api, ExchangePair} from '../api';

type ExchangePairsContextType = {
  exchangePairs: Record<string, ExchangePair>;
  exchangePairsLoading: boolean;
};

const ExchangePairsContext = createContext<ExchangePairsContextType | null>(null);

export function useExchangePairs() {
  const ctx = useContext(ExchangePairsContext);
  if (!ctx) throw new Error('useExchangePairs must be used within ExchangePairsProvider');
  return ctx;
}

type Props = {
  children: ReactNode;
};

export function ExchangePairsProvider({children}: Props) {
  const [exchangePairs, setExchangePairs] = useState<Record<string, ExchangePair>>({});
  const [exchangePairsLoading, setExchangePairsLoading] = useState(true);

  useEffect(() => {
    let isMounted = true;
    setExchangePairsLoading(true);
    api.getAllExchangePairs().then(data => {
      if (!isMounted) return;
      const dict: Record<string, ExchangePair> = {};
      data.forEach(pair => {
        dict[pair.id] = pair;
      });
      setExchangePairs(dict);
      setExchangePairsLoading(false);
    }).catch(() => {
      if (!isMounted) return;
      setExchangePairsLoading(false);
    });

    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <ExchangePairsContext.Provider value={{exchangePairs, exchangePairsLoading}}>
      {children}
    </ExchangePairsContext.Provider>
  );
}
