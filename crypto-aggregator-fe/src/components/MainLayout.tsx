import {Activity, LayoutGrid, LogOut} from 'lucide-react';
import {useState} from 'react';

type Props = {
  activeView: 'market' | 'workspace';
  onViewChange: (view: 'market' | 'workspace') => void;
  onLogout: () => void;
  children: React.ReactNode;
};

export default function MainLayout({activeView, onViewChange, onLogout, children}: Props) {
  const [hoveredItem, setHoveredItem] = useState<string | null>(null);

  return (
    <div className="flex h-screen w-full bg-[#09090b] font-sans overflow-hidden relative">

      {/* Ambient background glow */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-1/4 left-1/4 w-96 h-96 bg-[#fcd535]/5 rounded-full blur-3xl"/>
        <div className="absolute bottom-1/4 right-1/4 w-96 h-96 bg-[#3b82f6]/5 rounded-full blur-3xl"/>
      </div>

      {/* Left Navigation */}
      <aside className="w-16 bg-[#09090b]/80 backdrop-blur-xl border-r border-white/10 flex flex-col items-center py-4 shrink-0 z-50 relative">

        <div className="w-10 h-10 bg-[#fcd535]/10 rounded-xl flex items-center justify-center mb-8 border border-[#fcd535]/20">
          <Activity className="text-[#fcd535]" size={22}/>
        </div>

        <nav className="flex flex-col gap-3 w-full px-2">
          <div
            className="relative"
            onMouseEnter={() => setHoveredItem('market')}
            onMouseLeave={() => setHoveredItem(null)}
          >
            <button
              onClick={() => onViewChange('market')}
              className={`
                w-full aspect-square rounded-xl flex items-center justify-center transition-all
                focus:outline-none focus-visible:ring-2 focus-visible:ring-[#fcd535]/50
                ${activeView === 'market'
                  ? 'bg-white/10 text-[#fcd535] border-l-2 border-[#fcd535] shadow-[0_0_15px_rgba(252,213,53,0.15)]'
                  : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-white/5'
                }
              `}
            >
              <Activity size={20}/>
            </button>
            {hoveredItem === 'market' && (
              <div className="absolute left-full ml-3 px-2 py-1 bg-[#181a20] border border-white/10 rounded-md text-xs text-zinc-300 whitespace-nowrap z-50">
                Market Overview
              </div>
            )}
          </div>

          <div
            className="relative"
            onMouseEnter={() => setHoveredItem('workspace')}
            onMouseLeave={() => setHoveredItem(null)}
          >
            <button
              onClick={() => onViewChange('workspace')}
              className={`
                w-full aspect-square rounded-xl flex items-center justify-center transition-all
                focus:outline-none focus-visible:ring-2 focus-visible:ring-[#fcd535]/50
                ${activeView === 'workspace'
                  ? 'bg-white/10 text-[#fcd535] border-l-2 border-[#fcd535] shadow-[0_0_15px_rgba(252,213,53,0.15)]'
                  : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-white/5'
                }
              `}
            >
              <LayoutGrid size={20}/>
            </button>
            {hoveredItem === 'workspace' && (
              <div className="absolute left-full ml-3 px-2 py-1 bg-[#181a20] border border-white/10 rounded-md text-xs text-zinc-300 whitespace-nowrap z-50">
                Multichart Workspace
              </div>
            )}
          </div>
        </nav>

        <div className="mt-auto w-full px-2">
          <div
            className="relative"
            onMouseEnter={() => setHoveredItem('logout')}
            onMouseLeave={() => setHoveredItem(null)}
          >
            <button
              onClick={onLogout}
              className="w-full aspect-square rounded-xl flex items-center justify-center text-[#848e9c] hover:text-[#f6465d] hover:bg-[#f6465d]/10 transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-[#f6465d]/50"
            >
              <LogOut size={20}/>
            </button>
            {hoveredItem === 'logout' && (
              <div className="absolute left-full ml-3 px-2 py-1 bg-[#181a20] border border-white/10 rounded-md text-xs text-zinc-300 whitespace-nowrap z-50">
                Log Out
              </div>
            )}
          </div>
        </div>
      </aside>

      {/* Main Content */}
      <main className="flex-1 flex flex-col min-w-0 relative z-10">
        {children}
      </main>

    </div>
  );
}
