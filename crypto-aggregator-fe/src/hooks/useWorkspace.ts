import {useCallback, useEffect, useRef, useState} from 'react';
import {useSearchParams} from 'react-router-dom';
import {
  type DragEndEvent,
  useSensor,
  useSensors,
  PointerSensor,
  KeyboardSensor,
} from '@dnd-kit/core';
import {arrayMove} from '@dnd-kit/sortable';
import {api, ChartInterval, ChartWidget, LivePrice, Workspace} from '../api';

type GridConfig = {
  gridClass: string;
  rows: string;
  scrollable: boolean;
  fillHeight: boolean;
};

type UseWorkspaceReturn = {
  workspaces: Workspace[];
  widgets: ChartWidget[];
  widgetsLoading: boolean;
  isSearchOpen: boolean;
  livePrices: Record<string, LivePrice>;
  activeWsId: string | null;
  sensors: ReturnType<typeof useSensors>;
  setIsSearchOpen: (open: boolean) => void;
  handleCreateWorkspace: () => Promise<void>;
  handleRenameWorkspace: () => Promise<void>;
  handleDeleteWorkspace: () => Promise<void>;
  handleAddWidget: (exchangePairId: string) => Promise<void>;
  handleDeleteWidget: (widgetId: string) => Promise<void>;
  handleUpdateInterval: (widgetId: string, interval: ChartInterval) => Promise<void>;
  handleDragEnd: (event: DragEndEvent) => Promise<void>;
  getGridConfig: () => GridConfig;
};

export default function useWorkspace(searchParams: ReturnType<typeof useSearchParams>[0], setSearchParams: ReturnType<typeof useSearchParams>[1]): UseWorkspaceReturn {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [widgets, setWidgets] = useState<ChartWidget[]>([]);
  const [widgetsLoading, setWidgetsLoading] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice>>({});

  const initializedRef = useRef(false);

  const activeWsId = searchParams.get('workspace');

  const loadWorkspaces = useCallback(async (wsIdToSelect?: string) => {
    const list = await api.getWorkspaces();
    setWorkspaces(list);

    if (list.length > 0) {
      const targetId = wsIdToSelect || list[0].id;
      setSearchParams({ workspace: targetId }, { replace: true });
    } else {
      setSearchParams({}, { replace: true });
      setWidgets([]);
    }
  }, [setSearchParams]);

  useEffect(() => {
    let isMounted = true;
    api.getWorkspaces().then(list => {
      if (!isMounted) return;
      setWorkspaces(list);

      if (!initializedRef.current) {
        initializedRef.current = true;
        const urlWsId = searchParams.get('workspace');
        const validUrlWsId = urlWsId && list.some(w => w.id === urlWsId) ? urlWsId : undefined;
        const targetId = validUrlWsId || (list.length > 0 ? list[0].id : null);
        if (targetId) {
          setSearchParams({ workspace: targetId }, { replace: true });
        } else {
          setSearchParams({}, { replace: true });
        }
      }
    }).catch(() => {
      // Workspace load failure handled by empty state
    });

    return () => {
      isMounted = false;
    };
  }, [searchParams, setSearchParams]);

  useEffect(() => {
    if (!activeWsId) {
      setWidgets([]);
      return;
    }
    setWidgets([]);
    setWidgetsLoading(true);
    api.getWorkspaceWidgets(activeWsId).then(widgetList => {
      setWidgets(widgetList.sort((a, b) => a.position - b.position));
    }).catch(() => {
      // Widget load failure handled by empty state
    }).finally(() => setWidgetsLoading(false));
  }, [activeWsId]);

  useEffect(() => {
    if (!activeWsId) return;

    const source = api.streamAllPrices();

    source.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        const updates: LivePrice[] = Array.isArray(data) ? data : [data];

        setLivePrices(prev => {
          const next = {...prev};
          updates.forEach(p => {
            if (p.tradingPair) next[p.tradingPair] = p;
          });
          return next;
        });
      } catch {
        // SSE parse error handled silently
      }
    };

    return () => source.close();
  }, [activeWsId]);

  const sensors = useSensors(
    useSensor(PointerSensor, { activationConstraint: { distance: 5 } }),
    useSensor(KeyboardSensor)
  );

  const handleCreateWorkspace = useCallback(async () => {
    const name = prompt('Enter new workspace name:', 'New Dashboard');
    if (name && name.trim()) {
      try {
        const newWs = await api.createWorkspace(name.trim());
        await loadWorkspaces(newWs.id);
      } catch {
        alert('Failed to create workspace.');
      }
    }
  }, [loadWorkspaces]);

  const handleRenameWorkspace = useCallback(async () => {
    if (!activeWsId) return;
    const currentWs = workspaces.find(w => w.id === activeWsId);
    const newName = prompt('Enter new name:', currentWs?.name);

    if (newName && newName.trim() && newName !== currentWs?.name) {
      try {
        await api.updateWorkspace(activeWsId, newName.trim());
        await loadWorkspaces(activeWsId);
      } catch {
        alert('Failed to rename workspace.');
      }
    }
  }, [activeWsId, workspaces, loadWorkspaces]);

  const handleDeleteWorkspace = useCallback(async () => {
    if (!activeWsId) return;
    if (confirm('Are you sure you want to delete this workspace and all its charts?')) {
      try {
        await api.deleteWorkspace(activeWsId);
        await loadWorkspaces();
      } catch {
        alert('Failed to delete workspace.');
      }
    }
  }, [activeWsId, loadWorkspaces]);

  const handleAddWidget = useCallback(async (exchangePairId: string) => {
    if (!activeWsId) return;
    const newWidget = await api.addChartWidget(activeWsId, exchangePairId);
    setWidgets(prev => [...prev, newWidget]);
  }, [activeWsId]);

  const handleDeleteWidget = useCallback(async (widgetId: string) => {
    if (!activeWsId) return;
    setWidgets(prev => prev.filter(w => w.id !== widgetId));
    await api.deleteChartWidget(activeWsId, widgetId);
  }, [activeWsId]);

  const handleUpdateInterval = useCallback(async (widgetId: string, interval: ChartInterval) => {
    if (!activeWsId) return;
    setWidgets(prev => prev.map(w => w.id === widgetId ? {...w, chartInterval: interval} : w));
    await api.updateChartWidget(activeWsId, widgetId, interval);
  }, [activeWsId]);

  const handleDragEnd = useCallback(async (event: DragEndEvent) => {
    const {active, over} = event;
    if (!over || active.id === over.id) return;
    const oldIndex = widgets.findIndex(w => w.id === active.id);
    const newIndex = widgets.findIndex(w => w.id === over.id);

    const newOrder = arrayMove(widgets, oldIndex, newIndex);
    setWidgets(newOrder);

    if (activeWsId) {
      const payload = newOrder.map((w, index) => ({chartWidgetId: w.id, position: index + 1}));
      await api.updateWidgetPositions(activeWsId, payload);
    }
  }, [widgets, activeWsId]);

  const getGridConfig = useCallback((): GridConfig => {
    const count = widgets.length;
    if (count === 0) return {gridClass: 'flex items-center justify-center', rows: '', scrollable: false, fillHeight: false};
    if (count === 1) return {gridClass: 'grid-cols-1', rows: 'grid-rows-1', scrollable: false, fillHeight: true};
    if (count === 2) return {gridClass: 'grid-cols-2', rows: 'grid-rows-1', scrollable: false, fillHeight: true};
    if (count === 4) return {gridClass: 'grid-cols-2', rows: 'grid-rows-2', scrollable: false, fillHeight: true};
    if (count <= 6) return {gridClass: 'grid-cols-3', rows: 'grid-rows-2', scrollable: false, fillHeight: true};
    return {gridClass: 'grid-cols-3', rows: '', scrollable: true, fillHeight: false};
  }, [widgets.length]);

  return {
    workspaces,
    widgets,
    widgetsLoading,
    isSearchOpen,
    livePrices,
    activeWsId,
    sensors,
    setIsSearchOpen,
    handleCreateWorkspace,
    handleRenameWorkspace,
    handleDeleteWorkspace,
    handleAddWidget,
    handleDeleteWidget,
    handleUpdateInterval,
    handleDragEnd,
    getGridConfig,
  };
}
