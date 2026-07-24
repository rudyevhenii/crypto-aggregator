import {Outlet, useNavigate, useLocation, useSearchParams} from 'react-router-dom';
import {useEffect} from 'react';
import {api} from '../../api';
import {WorkspaceProvider, useWorkspaceContext} from '../../contexts/WorkspaceContext';
import {ExchangePairsProvider} from '../../contexts/ExchangePairsContext';
import ExpandableSidebar from '../ExpandableSidebar';

function AppLayoutInner() {
  const navigate = useNavigate();
  const {pathname} = useLocation();
  const {
    workspaces,
    activeWsId,
    handleCreateWorkspace,
    confirmRename,
    confirmDelete,
  } = useWorkspaceContext();

  useEffect(() => {
    if (!localStorage.getItem('accessToken')) {
      navigate('/');
    }
  }, [navigate]);

  return (
    <div className="flex h-screen w-full bg-[#09090b] font-sans overflow-hidden relative">

      {/* Ambient background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#fcd535]/5 rounded-full blur-3xl"/>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#3b82f6]/5 rounded-full blur-3xl"/>
      </div>

      {/* Expandable Sidebar */}
      <ExpandableSidebar
        workspaces={workspaces}
        activeWsId={activeWsId}
        activeView={pathname.startsWith('/app/workspace') ? 'workspace' : 'market'}
        onViewChange={(view) => navigate(view === 'market' ? '/app/overview' : '/app/workspace')}
        onLogout={async () => {
          try {
            await api.logout();
          } catch {
            // ignore logout API failure and continue with local cleanup
          } finally {
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('appView');
            navigate('/');
          }
        }}
        onSelectWorkspace={(id) => navigate(`/app/workspace?workspace=${id}`)}
        onCreateWorkspace={handleCreateWorkspace}
        onRenameWorkspace={confirmRename}
        onDeleteWorkspace={confirmDelete}
      />

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 relative z-10">
        <div className="flex-1 overflow-y-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}

export default function AppLayout() {
  const [searchParams, setSearchParams] = useSearchParams();

  return (
    <ExchangePairsProvider>
      <WorkspaceProvider searchParams={searchParams} setSearchParams={setSearchParams}>
        <AppLayoutInner />
      </WorkspaceProvider>
    </ExchangePairsProvider>
  );
}
