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
} from "./types.ts";

export * from './types';

// --- API CLIENT ---
const BASE_URL = 'http://localhost:8080';

// Змінні для керування чергою запитів під час оновлення токена
let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

// Додає запит у чергу очікування
const subscribeTokenRefresh = (cb: (token: string) => void) => {
  refreshSubscribers.push(cb);
};

// Викликається, коли токен успішно оновлено, щоб виконати всі запити з черги
const onTokenRefreshed = (token: string) => {
  refreshSubscribers.forEach(cb => cb(token));
  refreshSubscribers = [];
};

// ДОДАНО: Розумна обгортка, яка автоматично оновлює токен при 401 помилці
async function fetchAuth(endpoint: string, options: RequestInit = {}): Promise<Response> {
  const token = localStorage.getItem('accessToken');

  // Допоміжна функція для генерації хедерів
  const getHeaders = (accessToken: string | null) => ({
    'Content-Type': 'application/json',
    ...options.headers,
    ...(accessToken ? { 'Authorization': `Bearer ${accessToken}` } : {})
  });

  // Робимо оригінальний запит
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers: getHeaders(token)
  });

  // Якщо токен протух
  if (response.status === 401) {
    const refreshToken = localStorage.getItem('refreshToken');

    if (!refreshToken) {
      // Якщо рефреш-токена немає, розлогінюємо
      localStorage.removeItem('accessToken');
      window.location.href = '/';
      return response;
    }

    if (!isRefreshing) {
      isRefreshing = true;
      try {
        // Робимо запит на оновлення токена
        const refreshRes = await fetch(`${BASE_URL}/api/auth/refresh-token`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ refreshToken })
        });

        if (refreshRes.ok) {
          const tokens = await refreshRes.json();
          // Зберігаємо нові токени
          localStorage.setItem('accessToken', tokens.accessToken);
          localStorage.setItem('refreshToken', tokens.refreshToken);

          isRefreshing = false;
          onTokenRefreshed(tokens.accessToken);

          // Повторюємо запит, який впав з 401, вже з новим токеном
          return fetch(`${BASE_URL}${endpoint}`, {
            ...options,
            headers: getHeaders(tokens.accessToken)
          });
        } else {
          // Якщо refresh-токен теж протух (наприклад, пройшов тиждень)
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/'; // Викидаємо на сторінку логіну
        }
      } catch {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        window.location.href = '/';
      } finally {
        isRefreshing = false;
      }
    }

    // Якщо інший запит (від іншого графіка) вже запустив оновлення токена,
    // ми просто ставимо цей запит у чергу (Promise) і чекаємо на новий токен
    return new Promise(resolve => {
      subscribeTokenRefresh((newToken: string) => {
        resolve(fetch(`${BASE_URL}${endpoint}`, {
          ...options,
          headers: getHeaders(newToken)
        }));
      });
    });
  }

  return response;
}

export const api = {
  getIntervals: async (exchange: Exchange): Promise<ChartInterval[]> => {
    const res = await fetch(`${BASE_URL}/api/exchanges/${exchange}/intervals`);
    return res.json();
  },

  logout: async (): Promise<void> => {
    const res = await fetchAuth('/api/auth/logout', {method: 'POST'});
    if (!res.ok && res.status !== 204) {
      throw new Error('Logout failed');
    }
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

  get24hTickersByExchange: async (exchange: Exchange, tradingPairs: TradingPair[]): Promise<Ticker24h[]> => {
    if (!tradingPairs || tradingPairs.length === 0) {
      return [];
    }
    const params = new URLSearchParams();
    tradingPairs.forEach(pair => params.append('tradingPairs', pair));
    const res = await fetch(`${BASE_URL}/api/historical/exchanges/${exchange}/tickers/24h?${params.toString()}`);
    if (!res.ok) return [];
    return res.json();
  },

  // --- WORKSPACE & WIDGETS ---
  // Оновлено на fetchAuth
  searchExchangePairs: async (options: {
    exchange?: Exchange;
    tradingPair?: string;
  }): Promise<ExchangePair[]> => {
    const params = new URLSearchParams();

    if (options.exchange) {
      params.append('exchange', options.exchange);
    }

    if (options.tradingPair) {
      params.append('tradingPair', options.tradingPair);
    }

    const queryString = params.toString();
    const url = queryString
      ? `/api/exchange-pairs/search?${queryString}`
      : '/api/exchange-pairs/search';

    const res = await fetchAuth(url);
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

  getWorkspaceWidgets: async (workspaceId: string): Promise<ChartWidget[]> => {
    const res = await fetchAuth(`/api/workspaces/${workspaceId}/widgets`);
    if (!res.ok) throw new Error('Failed to fetch workspace widgets');
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
