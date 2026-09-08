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
