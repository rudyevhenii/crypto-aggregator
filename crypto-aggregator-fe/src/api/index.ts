import {
  ChartInterval,
  ChartWidget,
  Exchange,
  ExchangeMetadata,
  ExchangePair,
  HistoricalPrice,
  HistoricalPriceRequest,
  Ticker24h,
  TradingPair,
  WidgetPositionUpdate,
  WorkspaceDetail
} from "./types.ts";

export * from './types';

// --- API CLIENT ---
const BASE_URL = 'http://localhost:8080';

// ДОДАНО: Універсальна обгортка для авторизованих запитів
async function fetchAuth(endpoint: string, options: RequestInit = {}) {
  const token = localStorage.getItem('accessToken');

  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
    ...(token ? {'Authorization': `Bearer ${token}`} : {})
  };

  return fetch(`${BASE_URL}${endpoint}`, {...options, headers});
}

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

  // 👇 ДОДАЙ ЦЕЙ МЕТОД
  streamAllPrices: (): EventSource => {
    return new EventSource(`${BASE_URL}/api/stream/exchanges/prices`);
  },

  // ДОДАНО: Ендпоінт для стрімінгу всіх цін конкретної біржі
  streamPricesByExchange: (exchange: Exchange): EventSource => {
    return new EventSource(`${BASE_URL}/api/stream/exchanges/${exchange}/prices`);
  },

  // ДОДАНО: Ендпоінт для статусу біржі
  streamExchangeHealth: (exchange: Exchange): EventSource => {
    return new EventSource(`${BASE_URL}/api/stream/exchanges/${exchange}/health`);
  },

  get24hTickers: async (exchange: Exchange): Promise<Ticker24h[]> => {
    const res = await fetch(`${BASE_URL}/api/historical/exchanges/${exchange}/tickers/24h`);
    if (!res.ok) return [];
    return res.json();
  },

  // --- WORKSPACE & WIDGETS ---
  // Оновлено на fetchAuth
  searchExchangePairs: async (pattern: string): Promise<ExchangePair[]> => {
    const res = await fetchAuth(`/api/exchange-pairs/search?pattern=${encodeURIComponent(pattern)}`);
    if (!res.ok) {
      console.error('Search failed:', res.status);
      return [];
    }
    return res.json();
  },

  // Оновлено на fetchAuth
  getAllExchangePairs: async (): Promise<ExchangePair[]> => {
    const res = await fetchAuth(`/api/exchange-pairs`);
    if (!res.ok) {
      console.error('Fetch all pairs failed:', res.status);
      return [];
    }
    return res.json();
  },

  // --- WORKSPACE & WIDGETS ---

  getWorkspaces: async () => {
    const res = await fetchAuth(`/api/workspaces`);
    if (!res.ok) throw new Error('Failed to fetch workspaces');
    return res.json();
  },

  getWorkspaceById: async (id: string): Promise<WorkspaceDetail> => {
    const res = await fetchAuth(`/api/workspaces/${id}`);
    if (!res.ok) throw new Error('Failed to fetch workspace details');
    return res.json();
  },

  createWorkspace: async (name: string) => {
    const res = await fetchAuth(`/api/workspaces`, {
      method: 'POST',
      body: JSON.stringify({name}) // fetchAuth вже має 'Content-Type': 'application/json'
    });
    if (!res.ok) throw new Error('Failed to create workspace');
    return res.json();
  },

  updateWorkspace: async (id: string, name: string) => {
    const res = await fetchAuth(`/api/workspaces/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({name})
    });
    if (!res.ok) throw new Error('Failed to rename workspace');
    return res.json();
  },

  deleteWorkspace: async (id: string) => {
    const res = await fetchAuth(`/api/workspaces/${id}`, {method: 'DELETE'});
    if (!res.ok) throw new Error('Failed to delete workspace');
  },

  addChartWidget: async (workspaceId: string, exchangePairId: string): Promise<ChartWidget> => {
    const res = await fetchAuth(`/api/workspaces/${workspaceId}/widgets`, {
      method: 'POST',
      body: JSON.stringify({exchangePairId})
    });
    if (!res.ok) throw new Error('Failed to add widget');
    return res.json();
  },

  updateChartWidget: async (workspaceId: string, widgetId: string, chartInterval: ChartInterval) => {
    const res = await fetchAuth(`/api/workspaces/${workspaceId}/widgets/${widgetId}`, {
      method: 'PATCH',
      body: JSON.stringify({chartInterval})
    });
    if (!res.ok) throw new Error('Failed to update widget');
    return res.json();
  },

  deleteChartWidget: async (workspaceId: string, widgetId: string) => {
    const res = await fetchAuth(`/api/workspaces/${workspaceId}/widgets/${widgetId}`, {
      method: 'DELETE'
    });
    if (!res.ok) throw new Error('Failed to delete widget');
  },

  updateWidgetPositions: async (workspaceId: string, positions: WidgetPositionUpdate[]) => {
    const res = await fetchAuth(`/api/workspaces/${workspaceId}/widgets/positions`, {
      method: 'PUT',
      body: JSON.stringify(positions)
    });
    if (!res.ok) throw new Error('Failed to update positions');
  }
};

export {intervalToSeconds} from './types';
