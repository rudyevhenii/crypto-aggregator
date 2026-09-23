import {useEffect, useRef, useState} from 'react';
import {
  AlarmClock,
  X,
  Bell,
  FileText,
  Play,
  Pause,
  Settings2,
  Trash2,
} from 'lucide-react';
import {PriceAlert, PriceAlertLog, ConditionType} from '../api';
import {useActivateAlert, useDeactivateAlert, useDeleteAlert} from '../hooks/usePriceAlerts';
import {api} from '../api';

type AlertSidebarProps = {
  alerts: PriceAlert[];
  onEditAlert?: (alert: PriceAlert) => void;
  onCreateAlert?: () => void;
};

const CONDITION_LABELS: Record<ConditionType, string> = {
  GREATER_THAN: 'Greater than',
  LESS_THAN: 'Less than',
  CROSSED_UP: 'Crossed up',
  CROSSED_DOWN: 'Crossed down',
  PERCENT_UP: 'Percent up',
  PERCENT_DOWN: 'Percent down',
  TRAILING_DROP: 'Trailing drop',
  TRAILING_RISE: 'Trailing rise',
};

export default function AlertSidebar({
                                        alerts = [],
                                        onEditAlert,
                                        onCreateAlert,
                                      }: AlertSidebarProps) {
  const [isExpanded, setIsExpanded] = useState(false);
  const [activeTab, setActiveTab] = useState<'alerts' | 'log'>('alerts');

  const [logs, setLogs] = useState<PriceAlertLog[]>([]);
  const [hasMore, setHasMore] = useState(false);
  const [isLoadingLogs, setIsLoadingLogs] = useState(false);
  const [logCursor, setLogCursor] = useState<{ lastCreatedAt?: string; lastId?: string }>({});
  const observerRef = useRef<HTMLDivElement | null>(null);

  const activateMutation = useActivateAlert();
  const deactivateMutation = useDeactivateAlert();
  const deleteMutation = useDeleteAlert();

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

  const formatDateTime = (dateString: string): string => {
    const date = new Date(dateString);
    return date.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const handleToggle = async (id: string, active: boolean) => {
    if (active) {
      await deactivateMutation.mutateAsync(id);
    } else {
      await activateMutation.mutateAsync(id);
    }
  };

  const handleDelete = async (id: string) => {
    await deleteMutation.mutateAsync(id);
  };

  const loadLogs = async (cursor?: { lastCreatedAt?: string; lastId?: string }) => {
    if (isLoadingLogs) return;
    setIsLoadingLogs(true);
    try {
      const data = await api.getPriceAlertLogs({
        lastCreatedAt: cursor?.lastCreatedAt,
        lastId: cursor?.lastId,
      });

      setLogs(prev => {
        const next = cursor ? [...prev, ...data] : data;
        return next;
      });

      if (data.length > 0) {
        const last = data[data.length - 1];
        setLogCursor({
          lastCreatedAt: last.createdAt,
          lastId: last.id,
        });
        setHasMore(data.length >= 50);
      } else {
        setHasMore(false);
      }
    } catch {
      // log load failure handled by empty state
    } finally {
      setIsLoadingLogs(false);
    }
  };

  useEffect(() => {
    if (activeTab === 'log') {
      setLogs([]);
      setLogCursor({});
      setHasMore(false);
      loadLogs();
    }
  }, [activeTab]);

  useEffect(() => {
    if (activeTab !== 'log') return;

    const el = observerRef.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      entries => {
        if (entries[0].isIntersecting && hasMore && !isLoadingLogs) {
          loadLogs(logCursor);
        }
      },
      {root: el, threshold: 0.1}
    );

    const sentinel = el.querySelector('[data-log-sentinel]');
    if (sentinel) observer.observe(sentinel);

    return () => observer.disconnect();
  }, [activeTab, hasMore, isLoadingLogs, logCursor]);

  return (
    <div
      className={`flex flex-col h-full border-l border-t border-[#2b3139] bg-[#181a20] rounded-tl-sm mt-2 overflow-hidden transition-all duration-300 ${
        isExpanded ? 'w-80' : 'w-12'
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
                    : 'text-zinc-400 hover:text-zinc-200 hover:bg-gray-700/50'
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
                    : 'text-zinc-400 hover:text-zinc-200 hover:bg-gray-700/50'
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
          <div className="flex-1 overflow-y-auto" ref={observerRef}>
            {activeTab === 'alerts' && (
              <div className="p-2 pb-4 space-y-1">
                {/* Alerts list */}
                {alerts.map(alert => (
                  <div
                    key={alert.id}
                    className={`group flex items-start gap-2 p-2.5 rounded-lg border border-gray-700/50 transition-colors ${
                      alert.active
                        ? 'bg-[#1e222d] hover:border-[#fcd535]/50'
                        : 'bg-[#1e222d]/60 border-gray-700/50 opacity-60'
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
                        onClick={() => handleToggle(alert.id, alert.active)}
                        className="p-1 rounded hover:bg-white/10 transition-colors"
                        title={alert.active ? 'Deactivate' : 'Activate'}
                      >
                        {alert.active ? (
                          <Pause size={12} className="text-[#0ecb81]"/>
                        ) : (
                          <Play size={12} className="text-zinc-500"/>
                        )}
                      </button>
                      <button
                        onClick={() => onEditAlert?.(alert)}
                        className="p-1 rounded hover:bg-white/10 transition-colors"
                        title="Edit"
                      >
                        <Settings2 size={12} className="text-zinc-500 hover:text-zinc-200"/>
                      </button>
                      <button
                        onClick={() => handleDelete(alert.id)}
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
                    {onCreateAlert && (
                      <button
                        onClick={onCreateAlert}
                        className="mt-3 text-xs text-[#fcd535] hover:underline"
                      >
                        Create your first alert
                      </button>
                    )}
                  </div>
                )}
              </div>
            )}

            {activeTab === 'log' && (
              <div className="p-2">
                {!isLoadingLogs && logs.length === 0 ? (
                  <div className="flex flex-col items-center justify-center py-12 text-center">
                    <FileText size={32} className="text-zinc-600 mb-3"/>
                    <p className="text-sm text-zinc-500">No alert trigger logs yet</p>
                  </div>
                ) : (
                  <div className="space-y-2">
                    {logs.map(log => (
                      <div
                        key={log.id}
                        className="p-3 rounded-lg bg-[#1e222d] border border-gray-700/50"
                      >
                        <div className="text-xs text-zinc-300 mb-1">
                          {log.message || 'Alert triggered'}
                        </div>
                        <div className="flex items-center justify-between text-[10px] text-zinc-500">
                          <span>Price: {log.triggeredPrice}</span>
                          <span>{formatDateTime(log.createdAt)}</span>
                        </div>
                        <div className="mt-1.5 flex items-center gap-1 text-[10px] text-zinc-400">
                          <span className="inline-flex items-center gap-1 px-1.5 py-0.5 rounded bg-[#131722] border border-gray-700/60">
                            {log.deliveryMethods.includes('EMAIL') ? 'Email' : log.deliveryMethods.join(', ')}
                          </span>
                          <span className="text-[#0ecb81]">Sent</span>
                        </div>
                      </div>
                    ))}

                    <div data-log-sentinel className="h-1"/>

                    {isLoadingLogs && (
                      <div className="text-center text-[10px] text-zinc-500 py-2">Loading...</div>
                    )}

                    {!hasMore && logs.length > 0 && !isLoadingLogs && (
                      <div className="text-center text-[10px] text-zinc-600 py-2">No more logs</div>
                    )}
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
