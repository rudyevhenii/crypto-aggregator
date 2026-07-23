import {useState, useEffect} from 'react';
import {DndContext, closestCenter} from '@dnd-kit/core';
import {rectSortingStrategy, SortableContext} from '@dnd-kit/sortable';
import {Button, Card} from '../ui';
import ChartWidgetCard from '../ChartWidgetCard';
import SearchModal from '../SearchModal';
import RenameWorkspaceModal from '../modals/RenameWorkspaceModal';
import DeleteWorkspaceModal from '../modals/DeleteWorkspaceModal';
import CreateWorkspaceModal from '../modals/CreateWorkspaceModal';
import {useWorkspaceContext} from '../../contexts/WorkspaceContext';

function WorkspaceRouteInner() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const {
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
    closeRenameModal,
    confirmRename,
    closeDeleteModal,
    confirmDelete,
    handleAddWidget,
    handleDeleteWidget,
    handleUpdateInterval,
    handleDragEnd,
    getGridConfig,
  } = useWorkspaceContext();

  // Keyboard shortcut for search
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault();
        setIsSearchOpen(!isSearchOpen);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [setIsSearchOpen, isSearchOpen]);

  return (
    <div className="flex flex-col h-full bg-[#09090b] p-3 relative overflow-hidden">
      {/* Ambient background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-[#fcd535]/[0.02] rounded-full blur-3xl"/>
      </div>

      {/* Floating Search Bar - centered within main content */}
      <div className="relative z-20 w-full max-w-xl mx-auto mb-4 pointer-events-none">
        <button
          onClick={() => setIsSearchOpen(true)}
          className="
            w-full flex items-center gap-3 px-4 py-3 rounded-2xl pointer-events-auto
            bg-white/5 backdrop-blur-xl
            border border-white/10
            text-zinc-400 hover:text-zinc-50 hover:bg-white/10 hover:border-white/20
            transition-all duration-200
            shadow-lg shadow-black/5
          "
        >
          <span className="flex-1 text-left text-sm">Search markets...</span>
        </button>
      </div>

      {/* Scrollable Grid Wrapper */}
      <div className={`flex-1 min-h-0 relative z-10 ${(() => { const cfg = getGridConfig(); return cfg.scrollable ? 'overflow-y-auto' : 'overflow-hidden'; })()}`}>
        {!activeWsId ? (
          <Card className="h-full flex flex-col items-center justify-center border-dashed border-white/10">
            <p className="mb-3 text-sm text-zinc-400">You don't have any workspaces yet.</p>
            <Button variant="ghost" size="sm" onClick={() => setIsCreateModalOpen(true)}>
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

      <RenameWorkspaceModal
        isOpen={isRenameModalOpen}
        onClose={closeRenameModal}
        currentName={workspaces.find(w => w.id === activeWsId)?.name || ''}
        onConfirm={confirmRename}
      />
      {activeWsId && (
        <DeleteWorkspaceModal
          isOpen={isDeleteModalOpen}
          onClose={closeDeleteModal}
          workspaceName={workspaces.find(w => w.id === activeWsId)?.name || ''}
          onConfirm={confirmDelete}
        />
      )}
      <CreateWorkspaceModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onConfirm={handleCreateWorkspace}
      />

      <SearchModal isOpen={isSearchOpen} onClose={() => setIsSearchOpen(false)} onAdd={handleAddWidget}/>
    </div>
  );
}

export default function WorkspaceRoute() {
  return <WorkspaceRouteInner />;
}
