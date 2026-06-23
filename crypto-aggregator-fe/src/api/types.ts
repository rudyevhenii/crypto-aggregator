// --- TYPES (Generated from OpenAPI) ---
export type Exchange = 'BINANCE' | 'COINBASE' | 'KRAKEN';

// Точне відображення вашого Java Enum
export type ConnectionStatus = 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING' | 'ERROR';

// Точне відображення вашого Java record ExchangeHealthDto
export interface ExchangeHealthDto {
  exchange: Exchange;
  connectionStatus: ConnectionStatus;
  timestamp: string; // Java Instant приходить у JSON як ISO рядок
}

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

