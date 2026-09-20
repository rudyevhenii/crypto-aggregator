import {useState} from 'react';
import {
  AlarmClock,
  X,
  Bell,
  FileText,
  Trash2,
} from 'lucide-react';
import {PriceAlert, PriceAlertLog, ConditionType} from '../api';

type AlertSidebarProps = {
  alerts: PriceAlert[];
  logs: PriceAlertLog[];
  onToggleAlert?: (id: string) => void;
  onDeleteAlert?: (id: string) => void;
};

const CONDITION_LABELS: Record<ConditionType, string> = {
  GREATER_THAN: 'Crossing up',
  LESS_THAN: 'Crossing down',
  CROSSED_UP: 'Crossed up',
  CROSSED_DOWN: 'Crossed down',
  PERCENT_UP: 'Percent up',
  PERCENT_DOWN: 'Percent down',
  TRAILING_DROP: 'Trailing drop',
  TRAILING_RISE: 'Trailing rise',
};

const MOCK_ALERTS: PriceAlert[] = [
  {
    id: '1',
    userId: 'user-1',
    exchange: 'BINANCE',
    tradingPair: 'SOL_USD',
    triggerPolicy: { triggerType: 'ONE_TIME' },
    deliveryMethods: ['EMAIL'],
    active: true,
    conditionPayload: {
      conditionType: 'CROSSED_UP',
      targetPrice: '101.28',
    },
    createdAt: '2024-01-15T10:00:00Z',
    updatedAt: '2024-01-15T10:00:00Z',
  },
];

export default function AlertSidebar({
                                        alerts = MOCK_ALERTS,
                                        logs = [],
                                        onToggleAlert,
                                        onDeleteAlert,
                                      }: AlertSidebarProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [activeTab, setActiveTab] = useState<'alerts' | 'log'>('alerts');

  const formatCondition = (alert: PriceAlert): string => {
    const payload = alert.conditionPayload;
    const label = CONDITION_LABELS[payload.conditionType];
    if ('targetPrice' in payload) {
      return `${label} ${payload.targetPrice}`;
    }
    if ('percentageChange' in payload) {
      return `${label} ${payload.percentageChange}%`;
    }
    if ('trailingPercentage' in payload) {
      return `${label} ${payload.trailingPercentage}%`;
    }
    return label;
  };

  const formatDate = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  };

  return (
    <div
      className={`flex flex-col h-full border-l border-[#2b3139] transition-all duration-300 ${
        isExpanded ? 'w-80 glass-surface rounded-r-sm' : 'w-12 glass-surface'
      }`}
    >
      {/* Collapsed toolbar */}
      {!isExpanded && (
        <div className="flex flex-col items-center py-2 gap-1">
          <button
            onClick={() => setIsExpanded(true)}
            className="p-2 rounded-md hover:bg-white/5 transition-colors group"
            title="Alerts"
          >
            <AlarmClock size={20} className="text-zinc-400 group-hover:text-zinc-100"/>
          </button>
        </div>
      )}

      {/* Expanded sidebar */}
      {isExpanded && (
        <>
          {/* Header with tabs */}
          <div className="flex items-center justify-between px-3 py-2 border-b border-[#2b3139]">
            <div className="flex rounded-sm bg-[#131722] p-0.5 w-full">
              <button
                onClick={() => setActiveTab('alerts')}
                className={`flex-1 flex items-center justify-center gap-1.5 px-2 py-1.5 rounded-sm text-xs font-medium transition-colors ${
                  activeTab === 'alerts'
                    ? 'bg-[#1e222d] text-zinc-100 shadow-sm'
                    : 'text-zinc-400 hover:text-zinc-200'
                }`}
              >
                <Bell size={14}/>
                Alerts
              </button>
              <button
                onClick={() => setActiveTab('log')}
                className={`flex-1 flex items-center justify-center gap-1.5 px-2 py-1.5 rounded-sm text-xs font-medium transition-colors ${
                  activeTab === 'log'
                    ? 'bg-[#1e222d] text-zinc-100 shadow-sm'
                    : 'text-zinc-400 hover:text-zinc-200'
                }`}
              >
                <FileText size={14}/>
                Log
              </button>
            </div>
            <button
              onClick={() => setIsExpanded(false)}
              className="p-1.5 rounded-md hover:bg-white/5 transition-colors ml-2"
            >
              <X size={16} className="text-zinc-400"/>
            </button>
          </div>

          {/* Content */}
          <div className="flex-1 overflow-y-auto">
            {activeTab === 'alerts' && (
              <div className="p-2 space-y-1">
                {/* Alerts list */}
                {alerts.map(alert => (
                  <div
                    key={alert.id}
                    className={`group flex items-start gap-2 p-2.5 rounded-sm border transition-colors ${
                      alert.active
                        ? 'bg-[#131722] border-[#2b3139] hover:border-[#fcd535]/50'
                        : 'bg-[#131722]/50 border-[#2b3139] opacity-60'
                    }`}
                  >
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-1.5 mb-1">
                        <span className="text-xs font-medium text-zinc-100 truncate">
                          {alert.tradingPair.replace('_', '/')}
                        </span>
                        <span className="text-[10px] text-zinc-500 uppercase">
                          {alert.exchange}
                        </span>
                      </div>
                      <div className="text-[11px] text-zinc-400 mb-1.5">
                        {formatCondition(alert)}
                      </div>
                      <div className="flex items-center gap-2 text-[10px] text-zinc-500">
                        <span>{formatDate(alert.createdAt)}</span>
                        {alert.active && (
                          <span className="flex items-center gap-1 text-[#0ecb81]">
                            <span className="w-1 h-1 rounded-full bg-[#0ecb81]"/>
                            Active
                          </span>
                        )}
                      </div>
                    </div>
                    <div className="flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button
                        onClick={() => onToggleAlert?.(alert.id)}
                        className="p-1 rounded hover:bg-white/10 transition-colors"
                        title={alert.active ? 'Deactivate' : 'Activate'}
                      >
                        <Bell size={12} className={alert.active ? 'text-[#0ecb81]' : 'text-zinc-500'}/>
                      </button>
                      <button
                        onClick={() => onDeleteAlert?.(alert.id)}
                        className="p-1 rounded hover:bg-white/10 transition-colors"
                        title="Delete"
                      >
                        <Trash2 size={12} className="text-zinc-500 hover:text-[#f6465d]"/>
                      </button>
                    </div>
                  </div>
                ))}

                {alerts.length === 0 && (
                  <div className="flex flex-col items-center justify-center py-8 text-center">
                    <AlarmClock size={32} className="text-zinc-600 mb-2"/>
                    <p className="text-xs text-zinc-500">No alerts yet</p>
                  </div>
                )}
              </div>
            )}

            {activeTab === 'log' && (
              <div className="p-4">
                {logs.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <FileText size={32} className="text-zinc-600 mb-3"/>
                    <p className="text-sm text-zinc-500">No alert trigger logs yet</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {logs.map(log => (
                      <div
                        key={log.id}
                        className="p-3 rounded-sm bg-[#131722] border border-[#2b3139]"
                      >
                        <div className="text-xs text-zinc-300 mb-1">
                          {log.message || 'Alert triggered'}
                        </div>
                        <div className="text-[10px] text-zinc-500">
                          {new Date(log.triggeredAt).toLocaleString()}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
