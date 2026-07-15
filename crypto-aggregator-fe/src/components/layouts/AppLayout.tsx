import {Outlet, useNavigate} from 'react-router-dom';
import {useEffect} from 'react';
import TopBar from '../TopBar';

export default function AppLayout() {
  const navigate = useNavigate();

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

      {/* Left Navigation */}
      <aside className="w-16 glass-surface flex flex-col items-center py-4 shrink-0 z-50 relative">

        <div className="w-10 h-10 bg-[#fcd535]/10 rounded-xl flex items-center justify-center mb-8 border border-[#fcd535]/20">
          <svg className="text-[#fcd535]" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/>
          </svg>
        </div>

        <nav className="flex flex-col gap-3 w-full px-2">
          <a
            href="/app/overview"
            className="w-full aspect-square rounded-xl flex items-center justify-center transition-all text-[#848e9c] hover:text-[#eaecef] hover:bg-white/5 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#fcd535]/50"
            title="Market Overview"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="7" height="7"/>
              <rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/>
              <rect x="3" y="14" width="7" height="7"/>
            </svg>
          </a>

          <a
            href="/app/workspace"
            className="w-full aspect-square rounded-xl flex items-center justify-center transition-all text-[#848e9c] hover:text-[#eaecef] hover:bg-white/5 focus:outline-none focus-visible:ring-2 focus-visible:ring-[#fcd535]/50"
            title="Multichart Workspace"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="21" x2="9" y2="9"/>
            </svg>
          </a>
        </nav>

        <div className="mt-auto w-full px-2">
          <button
            onClick={() => {
              localStorage.removeItem('accessToken');
              localStorage.removeItem('refreshToken');
              localStorage.removeItem('appView');
              navigate('/');
            }}
            className="w-full aspect-square rounded-xl flex items-center justify-center text-[#848e9c] hover:text-[#f6465d] hover:bg-[#f6465d]/10 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-[#f6465d]/50"
            title="Log Out"
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 relative z-10">
        <TopBar exchange={null} pair={null} livePrice={null} health={null} />
        <div className="flex-1 overflow-y-auto">
          <Outlet />
        </div>
      </main>

    </div>
  );
}
