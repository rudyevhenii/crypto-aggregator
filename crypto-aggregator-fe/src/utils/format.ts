export const formatPrice = (price?: number): string => {
  if (price == null) return '—';
  return price.toLocaleString(undefined, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

export const formatVolume = (volume?: number): string => {
  if (volume == null) return '—';
  return volume.toLocaleString(undefined, {
    maximumFractionDigits: 0,
  });
};

export const formatInterval = (interval: string): string => {
  const map: Record<string, string> = {
    ONE_SECOND: '1s',
    ONE_MINUTE: '1m',
    THREE_MINUTES: '3m',
    FIVE_MINUTES: '5m',
    FIFTEEN_MINUTES: '15m',
    THIRTY_MINUTES: '30m',
    ONE_HOUR: '1h',
    TWO_HOURS: '2h',
    FOUR_HOURS: '4h',
    SIX_HOURS: '6h',
    EIGHT_HOURS: '8h',
    TWELVE_HOURS: '12h',
    ONE_DAY: '1D',
    THREE_DAYS: '3D',
    FIFTEEN_DAYS: '15D',
    ONE_WEEK: '1W',
    ONE_MONTH: '1M',
  };
  return map[interval] ?? interval;
};
