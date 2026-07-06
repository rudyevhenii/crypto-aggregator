import {Activity, LayoutGrid, LogOut} from 'lucide-react';

type Props = {
  activeView: 'market' | 'workspace';
  onViewChange: (view: 'market' | 'workspace') => void;
  onLogout: () => void;
  children: React.ReactNode;
};

export default function MainLayout({activeView, onViewChange, onLogout, children}: Props) {
  return (
    <div className="flex h-screen w-full bg-[#0b0e14] font-sans overflow-hidden">

      {/* Ліва навігація */}
      <aside className="w-16 bg-[#181a20] border-r border-[#2b3139] flex flex-col items-center py-4 shrink-0 z-50">

        <div className="w-10 h-10 bg-[#fcd535]/10 rounded-xl flex items-center justify-center mb-8">
          <Activity className="text-[#fcd535]" size={22}/>
        </div>

        <nav className="flex flex-col gap-4 w-full px-2">
          <button
            onClick={() => onViewChange('market')}
            className={`w-full aspect-square rounded-xl flex items-center justify-center transition-all ${
              activeView === 'market'
                ? 'bg-[#2b3139] text-[#eaecef] shadow-inner'
                : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-[#2b3139]/50'
            }`}
            title="Market Overview"
          >
            <Activity size={20}/>
          </button>

          <button
            onClick={() => onViewChange('workspace')}
            className={`w-full aspect-square rounded-xl flex items-center justify-center transition-all ${
              activeView === 'workspace'
                ? 'bg-[#2b3139] text-[#eaecef] shadow-inner'
                : 'text-[#848e9c] hover:text-[#eaecef] hover:bg-[#2b3139]/50'
            }`}
            title="Multichart Workspace"
          >
            <LayoutGrid size={20}/>
          </button>
        </nav>

        <div className="mt-auto w-full px-2">
          <button
            onClick={onLogout}
            className="w-full aspect-square rounded-xl flex items-center justify-center text-[#848e9c] hover:text-[#f6465d] hover:bg-[#f6465d]/10 transition-colors"
            title="Log Out"
          >
            <LogOut size={20}/>
          </button>
        </div>
      </aside>

      {/* Головний контент */}
      <main className="flex-1 flex flex-col min-w-0">
        {children}
      </main>

    </div>
  );
}