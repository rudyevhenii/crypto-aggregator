import {useEffect, useRef, useState} from 'react';
import {Edit2, FolderPlus, Plus, Trash2} from 'lucide-react';
import {
  closestCenter,
  DndContext,
  type DragEndEvent,
  KeyboardSensor,
  PointerSensor,
  useSensor,
  useSensors
} from '@dnd-kit/core';
import {arrayMove, rectSortingStrategy, SortableContext, sortableKeyboardCoordinates} from '@dnd-kit/sortable';
import {api, ChartInterval, ChartWidget, LivePrice} from '../api';
import {Button, Card, Select} from './ui';
import ChartWidgetCard from './ChartWidgetCard';
import SearchModal from './SearchModal';

export default function WorkspaceView() {
  const [workspaces, setWorkspaces] = useState<{ id: string, name: string }[]>([]);
  const [activeWsId, setActiveWsId] = useState<string | null>(null);
  const [widgets, setWidgets] = useState<ChartWidget[]>([]);
  const [widgetsLoading, setWidgetsLoading] = useState(false);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  const [livePrices, setLivePrices] = useState<Record<string, LivePrice>>({});

  const initializedRef = useRef(false);

  const loadWorkspaces = async (wsIdToSelect?: string) => {
    const list = await api.getWorkspaces();
    setWorkspaces(list);

    if (list.length > 0) {
      const targetId = wsIdToSelect || list[0].id;
      setActiveWsId(targetId);
    } else {
      setActiveWsId(null);
      setWidgets([]);
    }
  };

  useEffect(() => {
    let isMounted = true;
    api.getWorkspaces().then(list => {
      if (!isMounted) return;
      setWorkspaces(list);

      if (!initializedRef.current) {
        initializedRef.current = true;
        const savedId = localStorage.getItem('activeWsId');
        const validSavedId = savedId && list.some((w: { id: string }) => w.id === savedId) ? savedId : undefined;
        const targetId = validSavedId || (list.length > 0 ? list[0].id : null);
        setActiveWsId(targetId);
      }
    }).catch(console.error);

    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    localStorage.setItem('activeWsId', activeWsId || '');
  }, [activeWsId]);

  useEffect(() => {
    if (!activeWsId) {
      setWidgets([]);
      return;
    }
    setWidgets([]);
    setWidgetsLoading(true);
    api.getWorkspaceWidgets(activeWsId).then(widgetList => {
      setWidgets(widgetList.sort((a, b) => a.position - b.position));
    }).catch(console.error)
      .finally(() => setWidgetsLoading(false));
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
      } catch (e) {
        console.error("Global SSE Parse Error:", e);
      }
    };

    return () => source.close();
  }, [activeWsId]);

  const sensors = useSensors(
    useSensor(PointerSensor, {activationConstraint: {distance: 5}}),
    useSensor(KeyboardSensor, {coordinateGetter: sortableKeyboardCoordinates})
  );

  const handleCreateWorkspace = async () => {
    const name = prompt("Enter new workspace name:", "New Dashboard");
    if (name && name.trim()) {
      try {
        const newWs = await api.createWorkspace(name.trim());
        await loadWorkspaces(newWs.id);
      } catch {
        alert("Failed to create workspace.");
      }
    }
  };

  const handleRenameWorkspace = async () => {
    if (!activeWsId) return;
    const currentWs = workspaces.find(w => w.id === activeWsId);
    const newName = prompt("Enter new name:", currentWs?.name);

    if (newName && newName.trim() && newName !== currentWs?.name) {
      try {
        await api.updateWorkspace(activeWsId, newName.trim());
        await loadWorkspaces(activeWsId);
      } catch {
        alert("Failed to rename workspace.");
      }
    }
  };

  const handleDeleteWorkspace = async () => {
    if (!activeWsId) return;
    if (confirm("Are you sure you want to delete this workspace and all its charts?")) {
      try {
        await api.deleteWorkspace(activeWsId);
        await loadWorkspaces();
      } catch {
        alert("Failed to delete workspace.");
      }
    }
  };

  const handleAddWidget = async (exchangePairId: string) => {
    if (!activeWsId) return;
    const newWidget = await api.addChartWidget(activeWsId, exchangePairId);
    setWidgets(prev => [...prev, newWidget]);
  };

  // TODO: after page reload user should land on workspace before he reloaded
  const handleDeleteWidget = async (widgetId: string) => {
    if (!activeWsId) return;
    setWidgets(prev => prev.filter(w => w.id !== widgetId));
    await api.deleteChartWidget(activeWsId, widgetId);
  };

  const handleUpdateInterval = async (widgetId: string, interval: ChartInterval) => {
    if (!activeWsId) return;
    setWidgets(prev => prev.map(w => w.id === widgetId ? {...w, chartInterval: interval} : w));
    await api.updateChartWidget(activeWsId, widgetId, interval);
  };

  const handleDragEnd = async (event: DragEndEvent) => {
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
  };

  const getGridConfig = () => {
    const count = widgets.length;
    if (count === 0) return {gridClass: 'flex items-center justify-center', rows: '', scrollable: false, fillHeight: false};
    if (count === 1) return {gridClass: 'grid-cols-1', rows: 'grid-rows-1', scrollable: false, fillHeight: true};
    if (count === 2) return {gridClass: 'grid-cols-2', rows: 'grid-rows-1', scrollable: false, fillHeight: true};
    if (count === 4) return {gridClass: 'grid-cols-2', rows: 'grid-rows-2', scrollable: false, fillHeight: true};
    if (count <= 6) return {gridClass: 'grid-cols-3', rows: 'grid-rows-2', scrollable: false, fillHeight: true};
    return {gridClass: 'grid-cols-3', rows: '', scrollable: true, fillHeight: false};
  };

  return (
    <div className="flex flex-col h-full bg-[#09090b] p-3 relative overflow-hidden">
      {/* Ambient background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-[#fcd535]/[0.02] rounded-full blur-3xl"/>
      </div>

      {/* Compact Control Panel */}
      <Card className="mb-3 p-3 flex items-center justify-between gradient-border relative z-10 shrink-0">
        <div className="flex items-center gap-2">
          <Select
            value={activeWsId || ''}
            onChange={(value) => setActiveWsId(value)}
            options={workspaces.map(ws => ({value: ws.id, label: ws.name}))}
            className="min-w-[150px]"
          />

          {activeWsId && (
            <div className="flex items-center gap-0.5 border-l border-white/5 pl-2 ml-1">
              <button onClick={handleRenameWorkspace}
                      className="p-1.5 text-zinc-400 hover:text-zinc-50 hover:bg-white/5 rounded-lg transition-colors"
                      title="Rename Workspace">
                <Edit2 size={14}/>
              </button>
              <button onClick={handleDeleteWorkspace}
                      className="p-1.5 text-zinc-400 hover:text-[#f6465d] hover:bg-[#f6465d]/10 rounded-lg transition-colors"
                      title="Delete Workspace">
                <Trash2 size={14}/>
              </button>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" onClick={handleCreateWorkspace} leftIcon={<FolderPlus size={16}/>}>
            New Workspace
          </Button>
          <Button size="sm" onClick={() => setIsSearchOpen(true)} leftIcon={<Plus size={16}/>} disabled={!activeWsId} className="shadow-[0_0_20px_rgba(252,213,53,0.3)]">
            Add Chart
          </Button>
        </div>
      </Card>

      {/* Scrollable Grid Wrapper */}
      <div className={`flex-1 min-h-0 pr-8 relative z-10 ${(() => { const cfg = getGridConfig(); return cfg.scrollable ? 'overflow-y-auto' : 'overflow-hidden'; })()}`}>
        {!activeWsId ? (
          <Card className="h-full flex flex-col items-center justify-center border-dashed border-white/10">
            <p className="mb-3 text-sm text-zinc-400">You don't have any workspaces yet.</p>
            <Button variant="ghost" size="sm" onClick={handleCreateWorkspace}>
              Create your first workspace
            </Button>
          </Card>
        ) : widgetsLoading ? (
          <Card className="h-full flex items-center justify-center border-dashed border-white/10">
            <p className="text-sm text-zinc-400">Loading charts...</p>
          </Card>
        ) : widgets.length === 0 ? (
          <Card className="h-full flex flex-col items-center justify-center border-dashed border-white/10">
            <p className="mb-3 text-sm text-zinc-400">Your workspace is empty.</p>
            <Button variant="ghost" size="sm" onClick={() => setIsSearchOpen(true)}>
              Add your first chart
            </Button>
          </Card>
        ) : (() => {
          const cfg = getGridConfig();
          return (
            <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
              <SortableContext items={widgets.map(w => w.id)} strategy={rectSortingStrategy}>
                <div className={`grid gap-2 ${cfg.scrollable ? '' : 'h-full'} ${cfg.gridClass} ${cfg.rows}`}>
                  {widgets.map(widget => (
                    <ChartWidgetCard
                      key={widget.id}
                      widget={widget}
                      livePrice={livePrices[widget.tradingPair]}
                      onDelete={handleDeleteWidget}
                      onUpdateInterval={handleUpdateInterval}
                      fillHeight={cfg.fillHeight}
                    />
                  ))}
                </div>
              </SortableContext>
            </DndContext>
          );
        })()}
      </div>

      <SearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} onAdd={handleAddWidget}/>
    </div>
  );
}
