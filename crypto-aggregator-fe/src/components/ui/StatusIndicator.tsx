import {type ReactNode} from 'react';

type Status = 'CONNECTED' | 'DISCONNECTED' | 'RECONNECTING' | 'ERROR';

type StatusIndicatorProps = {
  status?: Status;
  label?: ReactNode;
  className?: string;
};

const statusClasses: Record<Status, string> = {
  CONNECTED: 'bg-[#0ecb81] shadow-[0_0_8px_rgba(14,203,129,0.4)]',
  RECONNECTING: 'bg-[#fcd535] shadow-[0_0_8px_rgba(252,213,53,0.4)] animate-pulse',
  ERROR: 'bg-[#f6465d] shadow-[0_0_8px_rgba(246,70,93,0.4)]',
  DISCONNECTED: 'bg-[#848e9c]',
};

export default function StatusIndicator({status, label, className = ''}: StatusIndicatorProps) {
  const dotClass = status ? statusClasses[status] : 'bg-[#848e9c]';

  return (
    <div className={`flex items-center gap-2 ${className}`}>
      <div className={`w-2 h-2 rounded-full ${dotClass}`}/>
      {label && <span className="text-zinc-50 text-xs font-medium">{label}</span>}
    </div>
  );
}
