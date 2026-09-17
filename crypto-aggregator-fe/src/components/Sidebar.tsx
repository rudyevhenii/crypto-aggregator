export default function Sidebar() {
  return (
    <aside className="w-[320px] glass-surface flex flex-col h-full overflow-y-auto relative z-20 rounded-tl-sm mt-2">
      <div className="p-4 border-b border-gray-800">
        <div className="text-xs text-zinc-500 uppercase tracking-wider font-medium">Alerts Log</div>
        <div className="mt-2 text-xs text-zinc-400">This panel will be used for alerts history.</div>
      </div>
    </aside>
  );
}
