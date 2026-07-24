import { useState } from 'react';
import { Activity, LogOut, Edit2, Trash2, FolderPlus, LayoutDashboard, PanelLeftClose, PanelRightOpen } from 'lucide-react';
import { Workspace } from '../api';
import CreateWorkspaceModal from './modals/CreateWorkspaceModal';
import RenameWorkspaceModal from './modals/RenameWorkspaceModal';
import DeleteWorkspaceModal from './modals/DeleteWorkspaceModal';

type Props = {
  workspaces: Workspace[];
  activeWsId: string | null;
  activeView: 'market' | 'workspace';
  onViewChange: (view: 'market' | 'workspace') => void;
  onLogout: () => void;
  onSelectWorkspace: (id: string) => void;
  onCreateWorkspace: (name: string) => Promise<void>;
  onRenameWorkspace: (id: string, newName: string) => Promise<void>;
  onDeleteWorkspace: (id: string) => Promise<void>;
};

export default function ExpandableSidebar({
  workspaces,
  activeWsId,
  activeView,
  onViewChange,
  onLogout,
  onSelectWorkspace,
  onCreateWorkspace,
  onRenameWorkspace,
  onDeleteWorkspace,
}: Props) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isRenameModalOpen, setIsRenameModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [hoveredWorkspace, setHoveredWorkspace] = useState<string | null>(null);
  const [editingWorkspace, setEditingWorkspace] = useState<Workspace | null>(null);

  const handleRename = async (newName: string) => {
    if (editingWorkspace) {
      await onRenameWorkspace(editingWorkspace.id, newName);
      setIsRenameModalOpen(false);
      setEditingWorkspace(null);
    }
  };

  const handleDelete = async () => {
    if (editingWorkspace) {
      await onDeleteWorkspace(editingWorkspace.id);
      setIsDeleteModalOpen(false);
      setEditingWorkspace(null);
    }
  };

  return (
    <aside
      className={`
        shrink-0 flex flex-col h-screen justify-between items-center
        transition-all duration-300 ease-[cubic-bezier(0.4,0,0.2,1)]
        ${isExpanded ? 'w-[250px]' : 'w-[60px]'}
        glass-surface border-r border-white/10
      `}
    >
      {/* Top Header */}
      <div className={`h-16 flex items-center w-full shrink-0 relative ${isExpanded ? 'justify-start pl-4' : 'justify-center px-0'}`}>
        {isExpanded ? (
          <div className="flex items-center gap-3">
            <div className="relative w-8 h-8 flex items-center justify-center shrink-0">
              <div className="absolute inset-0 flex items-center justify-center rounded-lg border border-[#fcd535]/20 bg-[#fcd535]/10">
                <Activity className="text-[#fcd535]" size={20} />
              </div>
            </div>
            <span className="text-zinc-50 font-bold text-base tracking-tight">
              CryptoAggregator
            </span>
          </div>
        ) : (
          <button
            onClick={() => setIsExpanded(true)}
            className="group relative flex items-center justify-center w-8 h-8"
            title="Expand sidebar"
          >
            {/* Default: Logo */}
            <div className="absolute inset-0 flex items-center justify-center rounded-lg border border-[#fcd535]/20 bg-[#fcd535]/10 transition-opacity duration-200 group-hover:opacity-0">
              <Activity className="text-[#fcd535]" size={20} />
            </div>
            {/* Hover: Expand icon */}
            <div className="absolute inset-0 flex items-center justify-center rounded-lg border border-white/20 bg-white/5 opacity-0 transition-opacity duration-200 group-hover:opacity-100">
              <PanelRightOpen size={18} className="text-zinc-300" />
            </div>
          </button>
        )}

        {/* Right: Collapse toggle */}
        {isExpanded && (
          <button
            onClick={() => setIsExpanded(false)}
            className="absolute right-4 p-1 text-zinc-400 hover:text-zinc-50 hover:bg-white/5 rounded-md transition-colors"
            title="Collapse sidebar"
          >
            <PanelLeftClose size={18} />
          </button>
        )}
      </div>

      {/* Middle Block: Primary Navigation */}
      <nav className="flex flex-col gap-2 flex-1 overflow-y-auto w-full p-2">
        {/* Market Overview */}
        <button
          onClick={() => onViewChange('market')}
          className={`
            w-full flex items-center rounded-xl transition-all
            ${isExpanded ? 'px-3 py-2.5 justify-start gap-3' : 'aspect-square justify-center'}
            focus:outline-none focus-visible:ring-2 focus-visible:ring-[#fcd535]/50
            ${activeView === 'market'
              ? 'text-[#fcd535]'
              : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-white/5'
            }
          `}
          title="Market Overview"
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="shrink-0">
            <rect x="3" y="3" width="7" height="7"/>
            <rect x="14" y="3" width="7" height="7"/>
            <rect x="14" y="14" width="7" height="7"/>
            <rect x="3" y="14" width="7" height="7"/>
          </svg>
          <span className={`
            text-sm font-medium whitespace-nowrap overflow-hidden
            transition-opacity duration-300 delay-100
            ${isExpanded ? 'opacity-100' : 'opacity-0 w-0'}
          `}>
            Market Overview
          </span>
        </button>

        {/* Workspaces */}
        {isExpanded ? (
          <div className="mt-4 pt-4 border-t border-white/5 transition-opacity duration-300 delay-100 opacity-100">
            <div className="flex items-center justify-between px-3 mb-2">
              <span className="text-[11px] font-semibold text-zinc-500 uppercase tracking-wider">Workspaces</span>
              <button
                onClick={() => setIsCreateModalOpen(true)}
                className="p-1 text-zinc-500 hover:text-[#fcd535] hover:bg-white/5 rounded-lg transition-colors"
                title="New Workspace"
              >
                <FolderPlus size={14} />
              </button>
            </div>
            <div className="space-y-0.5">
              {workspaces.map(ws => (
                <div
                  key={ws.id}
                  className="relative group"
                  onMouseEnter={() => setHoveredWorkspace(ws.id)}
                  onMouseLeave={() => setHoveredWorkspace(null)}
                >
                  <button
                    onClick={() => onSelectWorkspace(ws.id)}
                    className={`
                      w-full flex items-center pl-6 pr-3 py-2 rounded-lg text-sm transition-all
                      ${ws.id === activeWsId
                        ? 'bg-white/10 text-[#fcd535]'
                        : 'text-zinc-400 hover:text-zinc-50 hover:bg-white/5'
                      }
                    `}
                  >
                    <span className="truncate">{ws.name}</span>
                  </button>
                  {hoveredWorkspace === ws.id && (
                    <div className="absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-0.5">
                      <button
                        onClick={() => { setEditingWorkspace(ws); setIsRenameModalOpen(true); }}
                        className="p-1 text-zinc-400 hover:text-zinc-50 hover:bg-white/5 rounded transition-colors"
                        title="Rename"
                      >
                        <Edit2 size={12} />
                      </button>
                      <button
                        onClick={() => { setEditingWorkspace(ws); setIsDeleteModalOpen(true); }}
                        className="p-1 text-zinc-400 hover:text-[#f6465d] hover:bg-[#f6465d]/10 rounded transition-colors"
                        title="Delete"
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  )}
                </div>
              ))}
              {workspaces.length === 0 && (
                <p className="px-3 text-xs text-zinc-500 italic">No workspaces yet</p>
              )}
            </div>
          </div>
        ) : (
          <button
            onClick={() => setIsExpanded(true)}
            className="w-full flex items-center justify-center aspect-square rounded-xl text-zinc-400 hover:text-zinc-50 hover:bg-white/5 transition-colors"
            title="Workspaces"
          >
            <LayoutDashboard size={20} />
          </button>
        )}
      </nav>

      {/* Bottom Block: Logout */}
      <div className="w-full p-2">
        <button
          onClick={onLogout}
          className={`
            w-full flex items-center rounded-xl transition-all
            ${isExpanded ? 'px-3 py-2.5 justify-start gap-3' : 'aspect-square justify-center'}
            text-[#848e9c] hover:text-[#f6465d] hover:bg-[#f6465d]/10
          `}
          title="Log Out"
        >
          <LogOut size={20} className="shrink-0" />
          <span className={`
            text-sm font-medium whitespace-nowrap overflow-hidden
            transition-opacity duration-300 delay-100
            ${isExpanded ? 'opacity-100' : 'opacity-0 w-0'}
          `}>
            Log Out
          </span>
        </button>
      </div>

      {/* Modals */}
      <CreateWorkspaceModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onConfirm={onCreateWorkspace}
      />
      <RenameWorkspaceModal
        isOpen={isRenameModalOpen}
        onClose={() => { setIsRenameModalOpen(false); setEditingWorkspace(null); }}
        currentName={editingWorkspace?.name || ''}
        onConfirm={handleRename}
      />
      {editingWorkspace && (
        <DeleteWorkspaceModal
          isOpen={isDeleteModalOpen}
          onClose={() => { setIsDeleteModalOpen(false); setEditingWorkspace(null); }}
          workspaceName={editingWorkspace.name}
          onConfirm={handleDelete}
        />
      )}
    </aside>
  );
}
