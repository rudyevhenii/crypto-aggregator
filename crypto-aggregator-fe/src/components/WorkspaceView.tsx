import {useEffect, useState} from 'react';
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
// ДОДАНО: Імпорт LivePrice
import {api, ChartInterval, ChartWidget, LivePrice} from '../api';
import ChartWidgetCard from './ChartWidgetCard';
import SearchModal from './SearchModal';

export default function WorkspaceView() {
  const [workspaces, setWorkspaces] = useState<{ id: string, name: string }[]>([]);
  const [activeWsId, setActiveWsId] = useState<string | null>(null);
  const [widgets, setWidgets] = useState<ChartWidget[]>([]);
  const [isSearchOpen, setIsSearchOpen] = useState(false);

  // ДОДАНО: Стан для зберігання актуальних цін для всіх графіків
  const [livePrices, setLivePrices] = useState<Record<string, LivePrice>>({});

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
    loadWorkspaces();
  }, []);

  useEffect(() => {
    if (!activeWsId) return;
    api.getWorkspaceById(activeWsId).then(detail => {
      setWidgets(detail.chartWidgets.sort((a, b) => a.position - b.position));
    }).catch(console.error);
  }, [activeWsId]);

  // ДОДАНО: Єдине SSE з'єднання для всього дашборду
  useEffect(() => {
    if (!activeWsId) return;

    const source = api.streamAllPrices();

    source.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data);
        // За специфікацією OpenAPI це може бути масив LivePrice[] або один об'єкт
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

    // Закриваємо з'єднання при демонтажі або зміні воркспейсу
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

  const getGridClass = () => {
    const count = widgets.length;
    if (count === 0) return 'flex items-center justify-center';
    if (count === 1) return 'grid-cols-1';
    if (count === 2) return 'grid-cols-2';
    if (count <= 4) return 'grid-cols-2 grid-rows-2';
    return 'grid-cols-3 grid-rows-2';
  };

  return (
    <div className="flex flex-col h-full bg-[#0b0e14] p-3">
      {/* КОМПАКТНА ПАНЕЛЬ УПРАВЛІННЯ */}
      <div className="flex items-center justify-between mb-3 bg-[#181a20] px-3 py-2 rounded-md border border-[#2b3139]">

        <div className="flex items-center gap-2">
          <select
            value={activeWsId || ''}
            onChange={(e) => setActiveWsId(e.target.value)}
            className="bg-[#0b0e11] border border-[#2b3139] text-[#eaecef] text-sm font-semibold px-2 py-1.5 rounded focus:outline-none focus:border-[#fcd535] cursor-pointer min-w-[150px]"
          >
            {workspaces.map(ws => (
              <option key={ws.id} value={ws.id}>{ws.name}</option>
            ))}
          </select>

          {activeWsId && (
            <div className="flex items-center gap-0.5 border-l border-[#2b3139] pl-2 ml-1">
              <button onClick={handleRenameWorkspace}
                      className="p-1.5 text-[#848e9c] hover:text-[#eaecef] hover:bg-[#2b3139] rounded transition-colors"
                      title="Rename Workspace">
                <Edit2 size={14}/>
              </button>
              <button onClick={handleDeleteWorkspace}
                      className="p-1.5 text-[#848e9c] hover:text-[#f6465d] hover:bg-[#f6465d]/10 rounded transition-colors"
                      title="Delete Workspace">
                <Trash2 size={14}/>
              </button>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={handleCreateWorkspace}
            className="flex items-center gap-1.5 bg-[#2b3139] text-[#eaecef] text-sm px-3 py-1.5 rounded font-medium hover:bg-[#474d57] transition-colors"
          >
            <FolderPlus size={16}/> New Workspace
          </button>
          <button
            disabled={!activeWsId}
            onClick={() => setIsSearchOpen(true)}
            className="flex items-center gap-1.5 bg-[#fcd535] text-[#0b0e14] text-sm px-3 py-1.5 rounded font-semibold hover:bg-[#e0bc2e] transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <Plus size={16}/> Add Chart
          </button>
        </div>
      </div>

      {/* GRID */}
      <div className="flex-1 overflow-hidden">
        {!activeWsId ? (
          <div
            className="h-full flex flex-col items-center justify-center text-[#848e9c] border border-dashed border-[#2b3139] rounded-md">
            <p className="mb-3 text-sm">You don't have any workspaces yet.</p>
            <button onClick={handleCreateWorkspace} className="text-[#fcd535] text-sm hover:underline">
              Create your first workspace
            </button>
          </div>
        ) : widgets.length === 0 ? (
          <div
            className="h-full flex flex-col items-center justify-center text-[#848e9c] border border-dashed border-[#2b3139] rounded-md">
            <p className="mb-3 text-sm">Your workspace is empty.</p>
            <button onClick={() => setIsSearchOpen(true)} className="text-[#fcd535] text-sm hover:underline">
              Add your first chart
            </button>
          </div>
        ) : (
          <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleDragEnd}>
            <SortableContext items={widgets.map(w => w.id)} strategy={rectSortingStrategy}>
              {/* ЗМЕНШЕНО ВІДСТУПИ МІЖ ГРАФІКАМИ ДО gap-2 */}
              <div className={`grid gap-2 h-full ${getGridClass()}`}>
                {widgets.map(widget => (
                  <ChartWidgetCard
                    key={widget.id}
                    widget={widget}
                    // ДОДАНО: Передаємо актуальну ціну для конкретної торгової пари у віджет
                    livePrice={livePrices[widget.tradingPair]}
                    onDelete={handleDeleteWidget}
                    onUpdateInterval={handleUpdateInterval}
                  />
                ))}
              </div>
            </SortableContext>
          </DndContext>
        )}
      </div>

      <SearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} onAdd={handleAddWidget}/>
    </div>
  );
}