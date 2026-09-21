import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {api} from '../api';
import {PriceAlertUpdateRequest} from '../api';

export function useGetAlerts() {
  return useQuery({
    queryKey: ['price-alerts'],
    queryFn: api.getPriceAlerts,
  });
}

export function useCreateAlert() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.createPriceAlert,
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: ['price-alerts']});
    },
  });
}

export function useUpdateAlert() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({id, payload}: {id: string; payload: PriceAlertUpdateRequest}) =>
      api.updatePriceAlert(id, payload),
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: ['price-alerts']});
    },
  });
}

export function useDeleteAlert() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.deletePriceAlert,
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: ['price-alerts']});
    },
  });
}

export function useActivateAlert() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.activatePriceAlert,
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: ['price-alerts']});
    },
  });
}

export function useDeactivateAlert() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.deactivatePriceAlert,
    onSuccess: () => {
      queryClient.invalidateQueries({queryKey: ['price-alerts']});
    },
  });
}
