import {useEffect, useRef, useState} from 'react';
import {Search, X} from 'lucide-react';
import {api, Exchange, ExchangePair} from '../api';

type Props = {
  isOpen: boolean;
  onClose: () => void;
  onAdd: (pairId: string) => void;
};

const EXCHANGES: Exchange[] = ['BINANCE', 'COINBASE', 'KRAKEN'];

export default function SearchModal({isOpen, onClose, onAdd}: Props) {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [selectedExchange, setSelectedExchange] = useState<Exchange | null>(null);
  const [results, setResults] = useState<ExchangePair[]>([]);
  const [loading, setLoading] = useState(false);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (!isOpen) return;

    debounceTimerRef.current = setTimeout(() => {
      setDebouncedQuery(query);
    }, 500);

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [query, isOpen]);

  useEffect(() => {
    if (!isOpen) return;

    let isMounted = true;

    const fetchData = async () => {
      setLoading(true);
      try {
        const data = await api.searchExchangePairs({
          exchange: selectedExchange ?? undefined,
          tradingPair: debouncedQuery || undefined,
        });
        if (isMounted) setResults(data);
      } finally {
        if (isMounted) setLoading(false);
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [debouncedQuery, selectedExchange, isOpen]);

  const handleExchangeClick = (exchange: Exchange) => {
    setSelectedExchange(prev => (prev === exchange ? null : exchange));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <div
        className="bg-[#181a20] border border-[#2b3139] rounded-xl w-full max-w-lg shadow-2xl flex flex-col max-h-[80vh]">

        <div className="p-4 border-b border-[#2b3139] flex items-center gap-3">
          <Search size={20} className="text-[#848e9c]"/>
          <input
            autoFocus
            type="text"
            placeholder="Search markets (e.g., BTC, ETH)"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            className="flex-1 bg-transparent text-[#eaecef] focus:outline-none text-lg"
          />
          <button onClick={onClose} className="text-[#848e9c] hover:text-[#eaecef]">
            <X size={20}/>
          </button>
        </div>

        <div className="px-4 pt-3 pb-1 flex items-center gap-2">
          {EXCHANGES.map(exchange => {
            const isActive = selectedExchange === exchange;
            return (
              <button
                key={exchange}
                onClick={() => handleExchangeClick(exchange)}
                className={`
                  px-3 py-1 rounded-full text-xs font-semibold border transition-colors
                  ${isActive
                    ? 'border-[#fcd535] text-[#fcd535]'
                    : 'border-[#2b3139] text-[#848e9c] hover:text-[#eaecef] hover:border-[#848e9c]'
                  }
                `}
              >
                {exchange}
              </button>
            );
          })}
        </div>

        <div className="overflow-y-auto p-2 flex-1">
          {loading && <div className="p-4 text-center text-[#848e9c]">Loading markets...</div>}

          {!loading && results.length === 0 && (
            <div className="p-4 text-center text-[#848e9c]">No markets found.</div>
          )}

          {!loading && results.map(pair => (
            <div
              key={pair.id}
              onClick={() => {
                onAdd(pair.id);
                onClose();
                setQuery('');
                setSelectedExchange(null);
              }}
              className="flex justify-between items-center p-3 hover:bg-[#2b3139]/50 rounded-lg cursor-pointer transition-colors group"
            >
              <div className="flex flex-col">
                <span className="text-[#eaecef] font-bold group-hover:text-[#fcd535] transition-colors">
                  {pair.tradingPair.replace('_', '/')}
                </span>
                <span className="text-[#848e9c] text-xs mt-0.5">{pair.exchange}</span>
              </div>
              <span className="text-xs font-semibold bg-[#2b3139] text-[#eaecef] px-2 py-1 rounded">
                Add Chart
              </span>
            </div>
          ))}
        </div>

      </div>
    </div>
  );
}
