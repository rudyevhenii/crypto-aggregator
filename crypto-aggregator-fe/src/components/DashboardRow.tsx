import {useMemo, useEffect, useState} from 'react';
import {api, Exchange, TradingPair, LivePrice, Ticker24h} from '../api';

type Props = {
  index: number;
  exchange: Exchange;
  pair: TradingPair;
  priceData?: LivePrice | Ticker24h;
  onClick: () => void;
};

export default function DashboardRow({index, exchange, pair, priceData, onClick}: Props) {
  const [history, setHistory] = useState<number[]>([]);

  useEffect(() => {
    let isMounted = true;
    api.getHistoricalPrices(exchange, {tradingPair: pair, chartInterval: 'FIFTEEN_MINUTES', limit: 96})
      .then(data => {
        if (isMounted) setHistory(data.map(d => Number(d.close)));
      });

    return () => {
      isMounted = false;
    };
  }, [exchange, pair]);

  const displayPair = pair.replace('_', '/');
  const isPositive = (priceData?.priceChangePercent24h ?? 0) >= 0;
  const colorClass = isPositive ? 'text-[#0ecb81]' : 'text-[#f6465d]';

  // ❗ ВИСОКОЕФЕКТИВНЕ: Обчислюємо точки спарклайну в useMemo
  const sparklinePoints = useMemo(() => {
    if (history.length <= 1) return '';

    const min = Math.min(...history);
    const max = Math.max(...history);
    const range = max - min || 1;

    return history
      .map((d, i) => `${(i / (history.length - 1)) * 100},${100 - ((d - min) / range) * 100}`)
      .join(' ');
  }, [history]);

  return (
    <tr
      onClick={onClick}
      className="border-b border-white/5 hover:bg-white/5 transition-colors cursor-pointer group"
    >
      <td className="p-4 text-zinc-400 w-12 text-sm">{index + 1}</td>
      <td className="p-4 font-bold text-zinc-50 text-sm">{displayPair}</td>
      <td className="p-4 text-right font-medium text-zinc-50 text-sm">
        {priceData?.lastPrice ? priceData.lastPrice.toLocaleString(undefined, {minimumFractionDigits: 2}) : '—'}
      </td>
      <td className={`p-4 text-right font-medium text-sm ${colorClass}`}>
        {priceData?.priceChangePercent24h != null ? `${isPositive ? '+' : ''}${priceData.priceChangePercent24h.toFixed(2)}%` : '—'}
      </td>
      <td className="p-4">
        <div className="flex justify-end">
          <svg viewBox="0 0 100 100" className="w-24 h-10 overflow-visible" preserveAspectRatio="none">
            {sparklinePoints && (
              <polyline
                points={sparklinePoints}
                fill="none"
                stroke={isPositive ? '#0ecb81' : '#f6465d'}
                strokeWidth="2.5"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            )}
          </svg>
        </div>
      </td>
    </tr>
  );
}
