import {useEffect, useRef, useState} from 'react';
import {Search, X} from 'lucide-react';
import {api, Exchange, ExchangePair} from '../api';
import {Input, Badge, Card} from './ui';

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

  const handleAdd = (pairId: string) => {
    onAdd(pairId);
    onClose();
    setQuery('');
    setSelectedExchange(null);
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50 flex items-center justify-center p-4">
      <Card className="w-full max-w-lg shadow-2xl flex flex-col max-h-[80vh] gradient-border">

        <div className="p-4 border-b border-white/5 flex items-center gap-3">
          <Search size={20} className="text-zinc-400"/>
          <Input
            autoFocus
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Search markets (e.g., BTC, ETH)"
            className="flex-1 bg-transparent border-none focus:shadow-none p-0 text-lg"
          />
          <button onClick={onClose} className="text-zinc-400 hover:text-zinc-50 p-1 rounded-lg hover:bg-white/5 transition-colors">
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
                  px-3 py-1 rounded-full text-xs font-semibold border transition-all
                  ${isActive
                    ? 'border-[#fcd535] text-[#fcd535] bg-[#fcd535]/10 shadow-[0_0_10px_rgba(252,213,53,0.2)]'
                    : 'border-white/10 text-zinc-400 hover:text-zinc-50 hover:border-white/20'
                  }
                `}
              >
                {exchange}
              </button>
            );
          })}
        </div>

        <div className="overflow-y-auto p-2 flex-1">
          {loading && <div className="p-4 text-center text-zinc-400">Loading markets...</div>}

          {!loading && results.length === 0 && (
            <div className="p-4 text-center text-zinc-400">No markets found.</div>
          )}

          {!loading && results.map(pair => (
            <div
              key={pair.id}
              onClick={() => handleAdd(pair.id)}
              className="flex justify-between items-center p-3 hover:bg-white/5 rounded-lg cursor-pointer transition-colors group"
            >
              <div className="flex flex-col">
                <span className="text-zinc-50 font-bold group-hover:text-[#fcd535] transition-colors">
                  {pair.tradingPair.replace('_', '/')}
                </span>
                <span className="text-zinc-400 text-xs mt-0.5">{pair.exchange}</span>
              </div>
              <Badge variant="neutral" className="group-hover:border-[#fcd535]/30">
                Add Chart
              </Badge>
            </div>
          ))}
        </div>

      </Card>
    </div>
  );
}
