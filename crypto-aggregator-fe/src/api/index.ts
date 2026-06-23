import {
  ChartInterval,
  Exchange,
  ExchangeMetadata,
  HistoricalPrice,
  HistoricalPriceRequest,
  TradingPair
} from "./types.ts";

export * from './types';

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

  // ДОДАНО: Ендпоінт для статусу біржі
  streamExchangeHealth: (exchange: Exchange): EventSource => {
    return new EventSource(`${BASE_URL}/api/stream/exchanges/${exchange}/health`);
  },
};

export { intervalToSeconds } from './types';
