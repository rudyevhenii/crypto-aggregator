import {useState} from 'react';
import {useSearchParams} from 'react-router-dom';
import {Edit2, FolderPlus, Plus, Trash2} from 'lucide-react';
import {DndContext, closestCenter} from '@dnd-kit/core';
import {rectSortingStrategy, SortableContext} from '@dnd-kit/sortable';
import {Button, Card, Select} from '../ui';
import ChartWidgetCard from '../ChartWidgetCard';
import SearchModal from '../SearchModal';
import RenameWorkspaceModal from '../modals/RenameWorkspaceModal';
import DeleteWorkspaceModal from '../modals/DeleteWorkspaceModal';
import CreateWorkspaceModal from '../modals/CreateWorkspaceModal';
import useWorkspace from '../../hooks/useWorkspace';

export default function WorkspaceRoute() {
  const [searchParams, setSearchParams] = useSearchParams();
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
  } = useWorkspace(searchParams, setSearchParams);

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
            onChange={(value) => setSearchParams({workspace: value})}
            options={workspaces.map(ws => ({value: ws.id, label: ws.name}))}
            className="min-w-[150px]"
          />

          {activeWsId && (
            <div className="flex items-center gap-0.5 border-l border-white/5 pl-2 ml-1">
              <button onClick={openRenameModal}
                      className="p-1.5 text-zinc-400 hover:text-zinc-50 hover:bg-white/5 rounded-lg transition-colors"
                      title="Rename Workspace">
                <Edit2 size={14}/>
              </button>
              <button onClick={openDeleteModal}
                      className="p-1.5 text-zinc-400 hover:text-[#f6465d] hover:bg-[#f6465d]/10 rounded-lg transition-colors"
                      title="Delete Workspace">
                <Trash2 size={14}/>
              </button>
            </div>
          )}
        </div>

        <div className="flex items-center gap-2">
          <Button variant="secondary" size="sm" onClick={() => setIsCreateModalOpen(true)} leftIcon={<FolderPlus size={16}/>}>
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
