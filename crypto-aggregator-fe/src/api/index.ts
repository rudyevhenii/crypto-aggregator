// --- TYPES (Generated from OpenAPI) ---
export type Exchange = 'BINANCE' | 'COINBASE' | 'KRAKEN';

export type TradingPair =
  | 'BTC_USD' | 'ETH_USD' | 'SOL_USD' | 'ADA_USD' | 'XRP_USD'
  | 'DOT_USD' | 'DOGE_USD' | 'LINK_USD' | 'LTC_USD' | 'AVAX_USD';

export type ChartInterval =
  | 'ONE_SECOND' | 'ONE_MINUTE' | 'THREE_MINUTES' | 'FIVE_MINUTES'
  | 'FIFTEEN_MINUTES' | 'THIRTY_MINUTES' | 'ONE_HOUR' | 'TWO_HOURS'
  | 'FOUR_HOURS' | 'SIX_HOURS' | 'EIGHT_HOURS' | 'TWELVE_HOURS'
  | 'ONE_DAY' | 'THREE_DAYS' | 'FIFTEEN_DAYS' | 'ONE_WEEK' | 'ONE_MONTH';

export interface HistoricalPriceRequest {
  tradingPair: TradingPair;
  chartInterval: ChartInterval;
  limit?: number;
  endTimeCursor?: string;
}

export interface HistoricalPrice {
  openTime: string;
  open: number;
  high: number;
  low: number;
  close: number;
  volume: number;
}

export interface LivePrice {
  exchange: Exchange;
  tradingPair: TradingPair;
  lastPrice: number;
  priceChangePercent24h: number;
  highPrice24h: number;
  lowPrice24h: number;
  volume24h: number;
  timestamp: string;
}

export interface ExchangeMetadata {
  exchange: Exchange;
  supportedPairs: TradingPair[];
  supportedIntervals: ChartInterval[];
}

// --- API CLIENT ---
const BASE_URL = 'http://localhost:8080';

export const api = {
  getExchanges: async (): Promise<Exchange[]> => {
    const res = await fetch(`${BASE_URL}/api/exchanges`);
    return res.json();
  },

  getPairs: async (exchange: Exchange): Promise<TradingPair[]> => {
    const res = await fetch(`${BASE_URL}/api/exchanges/${exchange}/pairs`);
    return res.json();
  },

  getIntervals: async (exchange: Exchange): Promise<ChartInterval[]> => {
    const res = await fetch(`${BASE_URL}/api/exchanges/${exchange}/intervals`);
    return res.json();
  },

  getMetadata: async (): Promise<ExchangeMetadata[]> => {
    const res = await fetch(`${BASE_URL}/api/exchanges/metadata`);
    return res.json();
  },

  getHistoricalPrices: async (
    exchange: Exchange,
    request: HistoricalPriceRequest
  ): Promise<HistoricalPrice[]> => {
    const params = new URLSearchParams({
      tradingPair: request.tradingPair,
      chartInterval: request.chartInterval,
    });

    if (request.limit) {
      params.append('limit', request.limit.toString());
    }

    // ДОДАНО: Передаємо курсор, якщо він є
    if (request.endTimeCursor) {
      params.append('endTimeCursor', request.endTimeCursor);
    }

    const res = await fetch(`${BASE_URL}/api/historical/exchanges/${exchange}/klines?${params}`);

    if (!res.ok) {
      console.error("Failed to fetch historical data", await res.text());
      return [];
    }

    return res.json();
  },
  streamPrices: (exchange: Exchange, pair: TradingPair): EventSource => {
    return new EventSource(`${BASE_URL}/api/stream/exchanges/${exchange}/prices/${pair}`);
  },
};

// --- UTILS ---
export function intervalToSeconds(interval: ChartInterval): number {
  switch (interval) {
    case 'ONE_SECOND': return 1;
    case 'ONE_MINUTE': return 60;
    case 'THREE_MINUTES': return 180;
    case 'FIVE_MINUTES': return 300;
    case 'FIFTEEN_MINUTES': return 900;
    case 'THIRTY_MINUTES': return 1800;
    case 'ONE_HOUR': return 3600;
    case 'TWO_HOURS': return 7200;
    case 'FOUR_HOURS': return 14400;
    case 'SIX_HOURS': return 21600;
    case 'EIGHT_HOURS': return 28800;
    case 'TWELVE_HOURS': return 43200;
    case 'ONE_DAY': return 86400;
    case 'THREE_DAYS': return 259200;
    case 'ONE_WEEK': return 604800;
    case 'FIFTEEN_DAYS': return 1296000;
    case 'ONE_MONTH': return 2592000;
    default: return 60;
  }
}