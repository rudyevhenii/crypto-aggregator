import {createContext, useContext, useState, useCallback, useEffect, useRef, ReactNode} from 'react';
import {useSearchParams} from 'react-router-dom';
import {api, Workspace, ChartWidget, ChartInterval, LivePrice, ExchangePair} from '../api';
import {arrayMove} from '@dnd-kit/sortable';
import {useSensors, useSensor, PointerSensor, KeyboardSensor, type DragEndEvent} from '@dnd-kit/core';

type GridConfig = {
  gridClass: string;
  rows: string;
  fillHeight: boolean;
};

const MAX_WIDGETS = 6;

type WorkspaceContextType = {
  workspaces: Workspace[];
  widgets: ChartWidget[];
  widgetsLoading: boolean;
  isSearchOpen: boolean;
  livePrices: Record<string, LivePrice>;
  activeWsId: string | null;
  sensors: ReturnType<typeof useSensors>;
  focusedWidgetId: string | null;
  setFocusedWidgetId: (id: string | null) => void;
  setIsSearchOpen: (open: boolean) => void;
  handleCreateWorkspace: (name: string) => Promise<void>;
  isRenameModalOpen: boolean;
  isDeleteModalOpen: boolean;
  openRenameModal: () => void;
  closeRenameModal: () => void;
  confirmRename: (id: string, newName: string) => Promise<void>;
  openDeleteModal: () => void;
  closeDeleteModal: () => void;
  confirmDelete: () => Promise<void>;
  handleAddWidget: (pair: ExchangePair) => Promise<void>;
  handleDeleteWidget: (widgetId: string) => Promise<void>;
  handleUpdateInterval: (widgetId: string, interval: ChartInterval) => Promise<void>;
  handleDragEnd: (event: DragEndEvent) => Promise<void>;
  getGridConfig: () => GridConfig;
  isMaxWidgetsReached: boolean;
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
  pathname: string;
};

export function WorkspaceProvider({children, searchParams, setSearchParams, pathname}: Props) {
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [widgets, setWidgets] = useState<ChartWidget[]>([]);
  const [widgetsLoading, setWidgetsLoading] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice>>({});
  const [isRenameModalOpen, setIsRenameModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [focusedWidgetId, setFocusedWidgetId] = useState<string | null>(null);

  const initializedRef = useRef(false);
  const searchParamsRef = useRef(searchParams);
  searchParamsRef.current = searchParams;
  const activeWsId = searchParams.get('workspace');

  const loadWorkspaces = useCallback(async (wsIdToSelect?: string) => {
    const list = await api.getWorkspaces();
    setWorkspaces(list);

    if (list.length > 0) {
      const targetId = wsIdToSelect || list[0].id;
      if (pathname.startsWith('/app/workspace')) {
        setSearchParams({workspace: targetId}, {replace: true});
      }
    } else {
      if (pathname.startsWith('/app/workspace')) {
        setSearchParams({}, {replace: true});
      }
      setWidgets([]);
    }
  }, [setSearchParams, pathname]);

  useEffect(() => {
    let isMounted = true;
    api.getWorkspaces().then(list => {
      if (!isMounted) return;
      setWorkspaces(list);

      if (!initializedRef.current) {
        initializedRef.current = true;
        const urlWsId = searchParamsRef.current.get('workspace');
        const validUrlWsId = urlWsId && list.some(w => w.id === urlWsId) ? urlWsId : undefined;
        const targetId = validUrlWsId || (list.length > 0 ? list[0].id : null);
        if (targetId && pathname.startsWith('/app/workspace')) {
          setSearchParams({workspace: targetId}, {replace: true});
        } else if (!targetId && pathname.startsWith('/app/workspace')) {
          setSearchParams({}, {replace: true});
        }
      }
    }).catch(() => {
      // Workspace load failure handled by empty state
    });

    return () => {
      isMounted = false;
    };
  }, [setSearchParams, pathname]);

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
        const price: LivePrice = JSON.parse(event.data);

        setLivePrices(prev => {
          if (price.tradingPair) {
            return {...prev, [price.tradingPair]: price};
          }
          return prev;
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

  const confirmRename = useCallback(async (id: string, newName: string) => {
    await api.updateWorkspace(id, newName.trim());
    await loadWorkspaces(id);
  }, [loadWorkspaces]);

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

  const handleAddWidget = useCallback(async (pair: ExchangePair) => {
    if (!activeWsId) return;
    if (widgets.length >= MAX_WIDGETS) return;
    const newWidget = await api.addChartWidget(activeWsId, pair.id);
    setWidgets(prev => [...prev, newWidget]);
  }, [activeWsId, widgets.length]);

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
    if (count === 0) return {gridClass: 'flex items-center justify-center', rows: '', fillHeight: false};
    if (count === 1) return {gridClass: 'grid-cols-1', rows: 'grid-rows-1', fillHeight: true};
    if (count === 2) return {gridClass: 'grid-cols-2', rows: 'grid-rows-1', fillHeight: true};
    if (count === 3) return {gridClass: 'grid-cols-3', rows: 'grid-rows-1', fillHeight: true};
    if (count === 4) return {gridClass: 'grid-cols-2', rows: 'grid-rows-2', fillHeight: true};
    if (count === 5) return {gridClass: 'grid-cols-3', rows: 'grid-rows-2', fillHeight: true};
    if (count === 6) return {gridClass: 'grid-cols-3', rows: 'grid-rows-2', fillHeight: true};
    return {gridClass: 'grid-cols-3', rows: 'grid-rows-2', fillHeight: true};
  }, [widgets.length]);

  const isMaxWidgetsReached = widgets.length >= MAX_WIDGETS;

  return (
    <WorkspaceContext.Provider value={{
      workspaces,
      widgets,
      widgetsLoading,
      isSearchOpen,
      livePrices,
      activeWsId,
      sensors,
      focusedWidgetId,
      setFocusedWidgetId,
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
      isMaxWidgetsReached,
    }}>
      {children}
    </WorkspaceContext.Provider>
  );
}
