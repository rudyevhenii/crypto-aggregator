import {createContext, useContext, useState, useCallback, useEffect, useRef, ReactNode} from 'react';
import {useSearchParams} from 'react-router-dom';
import {api, Workspace, ChartWidget, ChartInterval, LivePrice} from '../api';
import {arrayMove} from '@dnd-kit/sortable';
import {useSensors, useSensor, PointerSensor, KeyboardSensor, type DragEndEvent} from '@dnd-kit/core';

type GridConfig = {
  gridClass: string;
  rows: string;
  scrollable: boolean;
  fillHeight: boolean;
};

type WorkspaceContextType = {
  workspaces: Workspace[];
  widgets: ChartWidget[];
  widgetsLoading: boolean;
  isSearchOpen: boolean;
  livePrices: Record<string, LivePrice>;
  activeWsId: string | null;
  sensors: ReturnType<typeof useSensors>;
  setIsSearchOpen: (open: boolean) => void;
  handleCreateWorkspace: (name: string) => Promise<void>;
  isRenameModalOpen: boolean;
  isDeleteModalOpen: boolean;
  openRenameModal: () => void;
  closeRenameModal: () => void;
  confirmRename: (newName: string) => Promise<void>;
  openDeleteModal: () => void;
  closeDeleteModal: () => void;
  confirmDelete: () => Promise<void>;
  handleAddWidget: (exchangePairId: string) => Promise<void>;
  handleDeleteWidget: (widgetId: string) => Promise<void>;
  handleUpdateInterval: (widgetId: string, interval: ChartInterval) => Promise<void>;
  handleDragEnd: (event: DragEndEvent) => Promise<void>;
  getGridConfig: () => GridConfig;
};

const WorkspaceContext = createContext<WorkspaceContextType | null>(null);

export function useWorkspaceContext() {
  const ctx = useContext(WorkspaceContext);
  if (!ctx) throw new Error('useWorkspaceContext must be used within WorkspaceProvider');
  return ctx;
}

type Props = {
  children: ReactNode;
  searchParams: ReturnType<typeof useSearchParams>[0];
  setSearchParams: ReturnType<typeof useSearchParams>[1];
};

export function WorkspaceProvider({children, searchParams, setSearchParams}: Props) {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [widgets, setWidgets] = useState<ChartWidget[]>([]);
  const [widgetsLoading, setWidgetsLoading] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice>>({});
  const [isRenameModalOpen, setIsRenameModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);

  const initializedRef = useRef(false);
  const activeWsId = searchParams.get('workspace');

  const loadWorkspaces = useCallback(async (wsIdToSelect?: string) => {
    const list = await api.getWorkspaces();
    setWorkspaces(list);

    if (list.length > 0) {
      const targetId = wsIdToSelect || list[0].id;
      setSearchParams({workspace: targetId}, {replace: true});
    } else {
      setSearchParams({}, {replace: true});
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
          setSearchParams({workspace: targetId}, {replace: true});
        } else {
          setSearchParams({}, {replace: true});
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
    useSensor(PointerSensor, {activationConstraint: {distance: 5}}),
    useSensor(KeyboardSensor)
  );

  const handleCreateWorkspace = useCallback(async (name: string) => {
    const trimmed = name.trim();
    if (!trimmed) return;
    const newWs = await api.createWorkspace(trimmed);
    await loadWorkspaces(newWs.id);
  }, [loadWorkspaces]);

  const openRenameModal = useCallback(() => {
    if (activeWsId) setIsRenameModalOpen(true);
  }, [activeWsId]);

  const closeRenameModal = useCallback(() => {
    setIsRenameModalOpen(false);
  }, []);

  const confirmRename = useCallback(async (newName: string) => {
    if (!activeWsId) return;
    await api.updateWorkspace(activeWsId, newName.trim());
    await loadWorkspaces(activeWsId);
  }, [activeWsId, loadWorkspaces]);

  const openDeleteModal = useCallback(() => {
    if (activeWsId) setIsDeleteModalOpen(true);
  }, [activeWsId]);

  const closeDeleteModal = useCallback(() => {
    setIsDeleteModalOpen(false);
  }, []);

  const confirmDelete = useCallback(async () => {
    if (!activeWsId) return;
    await api.deleteWorkspace(activeWsId);
    await loadWorkspaces();
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

  return (
    <WorkspaceContext.Provider value={{
      workspaces,
      widgets,
      widgetsLoading,
      isSearchOpen,
      livePrices,
      activeWsId,
      sensors,
      setIsSearchOpen,
      handleCreateWorkspace,
      isRenameModalOpen,
      isDeleteModalOpen,
      openRenameModal,
      closeRenameModal,
      confirmRename,
      openDeleteModal,
      closeDeleteModal,
      confirmDelete,
      handleAddWidget,
      handleDeleteWidget,
      handleUpdateInterval,
      handleDragEnd,
      getGridConfig,
    }}>
      {children}
    </WorkspaceContext.Provider>
  );
}
