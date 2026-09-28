import {useEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import {
  AlarmClock,
  ArrowUpRight,
  ArrowDownRight,
  ArrowUpFromLine,
  ArrowDownFromLine,
  CornerRightDown,
  CornerRightUp,
  X,
  Bell,
  Mail,
  TrendingUp,
  TrendingDown,
  ChevronDown,
  Trash2,
} from 'lucide-react';
import {
  ConditionType,
  ConditionPayload,
  DeliveryMethod,
  PriceAlert,
  PriceAlertRequest,
  PriceAlertUpdateRequest,
  TriggerPolicy,
  TriggerType,
} from '../../api';
import {Input, Button} from '../ui';
import {useDeleteAlert} from '../../hooks/usePriceAlerts';

type AlertModalProps = {
  isOpen: boolean;
  onClose: () => void;
  onCreate?: (alert: PriceAlertRequest) => void;
  onUpdate?: (id: string, alert: PriceAlertUpdateRequest) => void;
  exchange: string;
  tradingPair: string;
  alert?: PriceAlert | null;
  currentPrice?: number;
};

type TargetPricePayload = {
  conditionType: 'GREATER_THAN' | 'LESS_THAN' | 'CROSSED_UP' | 'CROSSED_DOWN';
  targetPrice: string;
};

type PercentagePayload = {
  conditionType: 'PERCENT_UP' | 'PERCENT_DOWN';
  initialPrice: string;
  percentageChange: string;
};

type TrailingPayload = {
  conditionType: 'TRAILING_DROP' | 'TRAILING_RISE';
  trailingPercentage: string;
  referencePrice: string;
};

type FormConditionPayload = TargetPricePayload | PercentagePayload | TrailingPayload;

type FormTriggerPolicy = TriggerPolicy;

const CONDITION_OPTIONS: {value: ConditionType; label: string; icon: React.ReactNode}[] = [
  {value: 'GREATER_THAN', label: 'Greater than', icon: <ArrowUpRight className="w-4 h-4"/>},
  {value: 'LESS_THAN', label: 'Less than', icon: <ArrowDownRight className="w-4 h-4"/>},
  {value: 'CROSSED_UP', label: 'Crossed up', icon: <ArrowUpFromLine className="w-4 h-4"/>},
  {value: 'CROSSED_DOWN', label: 'Crossed down', icon: <ArrowDownFromLine className="w-4 h-4"/>},
  {value: 'PERCENT_UP', label: 'Percent up', icon: <TrendingUp className="w-4 h-4"/>},
  {value: 'PERCENT_DOWN', label: 'Percent down', icon: <TrendingDown className="w-4 h-4"/>},
  {value: 'TRAILING_DROP', label: 'Trailing drop', icon: <CornerRightDown className="w-4 h-4"/>},
  {value: 'TRAILING_RISE', label: 'Trailing rise', icon: <CornerRightUp className="w-4 h-4"/>},
];

const DELIVERY_OPTIONS: {value: DeliveryMethod; label: string; icon: React.ReactNode}[] = [
  {value: 'EMAIL', label: 'Email', icon: <Mail size={16}/>},
];

const INITIAL_PAYLOAD: FormConditionPayload = {
  conditionType: 'GREATER_THAN',
  targetPrice: '',
};

const EXPIRATION_OPTIONS = [
  {value: '1h', label: '1 hour'},
  {value: '24h', label: '24 hours'},
  {value: '7d', label: '7 days'},
  {value: '30d', label: '30 days'},
  {value: 'never', label: 'Never'},
];

function buildExpirationDate(value: string): string | undefined {
  if (value === 'never') return undefined;
  const date = new Date();
  if (value === '1h') date.setHours(date.getHours() + 1);
  else if (value === '24h') date.setHours(date.getHours() + 24);
  else if (value === '7d') date.setDate(date.getDate() + 7);
  else if (value === '30d') date.setDate(date.getDate() + 30);
  return date.toISOString();
}

function expirationValueFromDate(date?: string): string {
  if (!date) return 'never';
  const target = new Date(date);
  const now = new Date();
  const diffMs = target.getTime() - now.getTime();
  const diffHours = diffMs / (1000 * 60 * 60);
  if (diffHours <= 1.5) return '1h';
  if (diffHours <= 25) return '24h';
  const diffDays = diffHours / 24;
  if (diffDays <= 8) return '7d';
  if (diffDays <= 31) return '30d';
  return 'never';
}

export default function AlertModal({
                                      isOpen,
                                      onClose,
                                      onCreate,
                                      onUpdate,
                                      exchange,
                                      tradingPair,
                                      alert,
                                      currentPrice,
                                    }: AlertModalProps) {
  const [dragOffset, setDragOffset] = useState({x: 0, y: 0});
  const [isDragging, setIsDragging] = useState(false);
  const [dragStart, setDragStart] = useState({x: 0, y: 0});
  const [conditionType, setConditionType] = useState<ConditionType>('GREATER_THAN');
  const [payload, setPayload] = useState<FormConditionPayload>(INITIAL_PAYLOAD);
  const [deliveryMethods, setDeliveryMethods] = useState<DeliveryMethod[]>(['EMAIL']);
  const [triggerPolicy, setTriggerPolicy] = useState<FormTriggerPolicy>({triggerType: 'ONE_TIME'});
  const [cooldownMinutes, setCooldownMinutes] = useState(5);
  const [isConditionOpen, setIsConditionOpen] = useState(false);
  const [expiration, setExpiration] = useState('1h');
  const modalRef = useRef<HTMLDivElement>(null);
  const headerRef = useRef<HTMLDivElement>(null);
  const conditionRef = useRef<HTMLDivElement>(null);
  const initialPriceRef = useRef<number | undefined>(undefined);

  const isEditMode = Boolean(alert?.id);

  const deleteMutation = useDeleteAlert();

  useEffect(() => {
    if (!isOpen) return;

    if (alert) {
      setConditionType(alert.conditionPayload.conditionType);
      setPayload(alert.conditionPayload as FormConditionPayload);
      setDeliveryMethods(alert.deliveryMethods);
      setTriggerPolicy(alert.triggerPolicy);
      setCooldownMinutes('cooldownMinutes' in alert.triggerPolicy ? alert.triggerPolicy.cooldownMinutes : 5);
      setExpiration(expirationValueFromDate(alert.expiresAt));
      initialPriceRef.current = currentPrice ?? undefined;
      setDragOffset({x: 0, y: 0});
      setIsConditionOpen(false);
    } else {
      setConditionType('GREATER_THAN');
      setDeliveryMethods(['EMAIL']);
      setTriggerPolicy({triggerType: 'ONE_TIME'});
      setCooldownMinutes(5);
      setIsConditionOpen(false);
      initialPriceRef.current = currentPrice ?? undefined;
      setPayload({conditionType: 'GREATER_THAN', targetPrice: currentPrice != null ? String(currentPrice) : ''});
      setExpiration('1h');
      setDragOffset({x: 0, y: 0});
    }
  }, [isOpen, alert, currentPrice]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (conditionRef.current && !conditionRef.current.contains(event.target as Node)) {
        setIsConditionOpen(false);
      }
    };

    if (isConditionOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isConditionOpen]);

  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.target !== headerRef.current && !headerRef.current?.contains(e.target as Node)) {
      return;
    }
    setIsDragging(true);
    setDragStart({x: e.clientX - dragOffset.x, y: e.clientY - dragOffset.y});
  };

  useEffect(() => {
    if (!isDragging) return;

    const handleMouseMove = (e: MouseEvent) => {
      setDragOffset({
        x: e.clientX - dragStart.x,
        y: e.clientY - dragStart.y,
      });
    };

    const handleMouseUp = () => {
      setIsDragging(false);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isDragging, dragStart]);

  const handleConditionChange = (value: ConditionType) => {
    setConditionType(value);
    setIsConditionOpen(false);
    const initialPrice = initialPriceRef.current != null ? String(initialPriceRef.current) : '';
    if (value === 'GREATER_THAN' || value === 'LESS_THAN' || value === 'CROSSED_UP' || value === 'CROSSED_DOWN') {
      setPayload({conditionType: value, targetPrice: initialPrice});
    } else if (value === 'PERCENT_UP' || value === 'PERCENT_DOWN') {
      setPayload({conditionType: value, initialPrice: initialPrice, percentageChange: ''});
    } else {
      setPayload({conditionType: value, trailingPercentage: '', referencePrice: initialPrice});
    }
  };

  const handleSubmit = async () => {
    const fullPayload: ConditionPayload = payload as ConditionPayload;

    const request: PriceAlertUpdateRequest = {
      deliveryMethods,
      conditionPayload: fullPayload,
      triggerPolicy: triggerPolicy.triggerType === 'RECURRING'
        ? {triggerType: 'RECURRING', cooldownMinutes}
        : {triggerType: 'ONE_TIME'},
      expiresAt: buildExpirationDate(expiration),
    };

    if (isEditMode && alert && onUpdate) {
      onUpdate(alert.id, request);
    } else if (!isEditMode && onCreate) {
      const createRequest: PriceAlertRequest = {
        exchange: exchange as any,
        tradingPair: tradingPair as any,
        ...request,
      };
      onCreate(createRequest);
    }

    onClose();
  };

  const handleDelete = async () => {
    if (!alert?.id) return;
    await deleteMutation.mutateAsync(alert.id);
    onClose();
  };

  const isFormValid = (): boolean => {
    if (deliveryMethods.length === 0) return false;

    if (conditionType === 'GREATER_THAN' || conditionType === 'LESS_THAN' ||
        conditionType === 'CROSSED_UP' || conditionType === 'CROSSED_DOWN') {
      const p = payload as TargetPricePayload;
      return !!p.targetPrice && parseFloat(p.targetPrice) > 0;
    }

    if (conditionType === 'PERCENT_UP' || conditionType === 'PERCENT_DOWN') {
      const p = payload as PercentagePayload;
      return !!p.initialPrice && parseFloat(p.initialPrice) > 0 &&
             !!p.percentageChange && parseFloat(p.percentageChange) > 0;
    }

    if (conditionType === 'TRAILING_DROP' || conditionType === 'TRAILING_RISE') {
      const p = payload as TrailingPayload;
      return !!p.trailingPercentage && parseFloat(p.trailingPercentage) > 0 &&
             !!p.referencePrice && parseFloat(p.referencePrice) > 0;
    }

    return false;
  };

  const selectedCondition = CONDITION_OPTIONS.find(c => c.value === conditionType);

  const renderPayloadFields = () => {
    if (conditionType === 'GREATER_THAN' || conditionType === 'LESS_THAN' ||
        conditionType === 'CROSSED_UP' || conditionType === 'CROSSED_DOWN') {
      const p = payload as TargetPricePayload;
      return (
        <div className="space-y-3">
          <label className="block text-xs text-[#848e9c] font-medium tracking-wide">Value</label>
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 flex items-center justify-center rounded-sm bg-[#131722] border border-[#2b3139] text-[#fcd535] shrink-0">
              {selectedCondition?.icon}
            </div>
            <div className="flex-1">
              <Input
                type="number"
                step="any"
                value={p.targetPrice}
                onChange={(e) => setPayload({...p, targetPrice: e.target.value})}
                placeholder="0.00"
                className="h-10"
              />
            </div>
          </div>
        </div>
      );
    }

    if (conditionType === 'PERCENT_UP' || conditionType === 'PERCENT_DOWN') {
      const p = payload as PercentagePayload;
      return (
        <div className="space-y-3">
          <div>
            <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">Initial price</label>
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 flex items-center justify-center rounded-sm bg-[#131722] border border-[#2b3139] text-[#fcd535] shrink-0">
                {selectedCondition?.icon}
              </div>
              <div className="flex-1">
                <Input
                  type="number"
                  step="any"
                  value={p.initialPrice}
                  onChange={(e) => setPayload({...p, initialPrice: e.target.value})}
                  placeholder="0.00"
                  className="h-10"
                />
              </div>
            </div>
          </div>
          <div>
            <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">Percentage change</label>
            <div className="flex items-center gap-3">
              <div className="h-10 w-10 flex items-center justify-center rounded-sm bg-[#131722] border border-[#2b3139] text-[#fcd535] shrink-0">
                {selectedCondition?.icon}
              </div>
              <div className="flex-1">
                <Input
                  type="number"
                  step="any"
                  value={p.percentageChange}
                  onChange={(e) => setPayload({...p, percentageChange: e.target.value})}
                  placeholder="1.00"
                  className="h-10"
                />
              </div>
            </div>
          </div>
        </div>
      );
    }

    const p = payload as TrailingPayload;
    return (
      <div className="space-y-3">
        <div>
          <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">Trailing percentage</label>
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 flex items-center justify-center rounded-sm bg-[#131722] border border-[#2b3139] text-[#fcd535] shrink-0">
              {selectedCondition?.icon}
            </div>
            <div className="flex-1">
              <Input
                type="number"
                step="any"
                value={p.trailingPercentage}
                onChange={(e) => setPayload({...p, trailingPercentage: e.target.value})}
                placeholder="1.00"
                className="h-10"
              />
            </div>
          </div>
        </div>
        <div>
          <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">Reference price</label>
          <div className="flex items-center gap-3">
            <div className="h-10 w-10 flex items-center justify-center rounded-sm bg-[#131722] border border-[#2b3139] text-[#fcd535] shrink-0">
              {selectedCondition?.icon}
            </div>
            <div className="flex-1">
              <Input
                type="number"
                step="any"
                value={p.referencePrice}
                onChange={(e) => setPayload({...p, referencePrice: e.target.value})}
                placeholder="0.00"
                className="h-10"
              />
            </div>
          </div>
        </div>
      </div>
    );
  };

  if (!isOpen) return null;

  const modalContent = (
    <div
      className="fixed inset-0 z-[9999] w-screen h-screen flex items-center justify-center bg-black/60"
      onClick={(event) => {
        if (event.target === event.currentTarget) {
          onClose();
        }
      }}
    >
      <div
        ref={modalRef}
        className="w-[440px] max-h-[90vh] glass-surface rounded-lg shadow-2xl flex flex-col overflow-hidden"
        style={{transform: `translate(${dragOffset.x}px, ${dragOffset.y}px)`}}
      >
        {/* Header - draggable area */}
        <div
          ref={headerRef}
          onMouseDown={handleMouseDown}
          className="flex items-center justify-between px-4 py-3 border-b border-white/10 cursor-move select-none"
        >
          <div className="flex items-center gap-2">
            <AlarmClock size={18} className="text-[#fcd535]"/>
            <span className="text-sm font-semibold text-zinc-100">
              {isEditMode ? 'Edit alert on' : 'Create alert on'}
            </span>
            <span className="text-sm font-medium text-zinc-300">{tradingPair.replace('_', '/')}</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded hover:bg-white/5 transition-colors"
          >
            <X size={16} className="text-zinc-400"/>
          </button>
        </div>

        {/* Body */}
        <div className="p-4 space-y-5 overflow-y-auto">
          {/* Condition */}
          <div className="space-y-2">
            <label className="block text-xs text-[#848e9c] font-medium tracking-wide">Condition</label>
            <div ref={conditionRef} className="relative">
              <button
                type="button"
                onClick={() => setIsConditionOpen(!isConditionOpen)}
                className="w-full flex items-center justify-between bg-[#0b0e11] border border-[#2b3139] rounded-sm px-3 py-2 text-sm text-zinc-100 hover:border-zinc-600 transition-colors"
              >
                <span className="flex items-center gap-2">
                  <span className="text-[#848e9c]">{selectedCondition?.icon}</span>
                  <span>{selectedCondition?.label}</span>
                </span>
                <ChevronDown size={16} className={`text-zinc-500 transition-transform ${isConditionOpen ? 'rotate-180' : ''}`}/>
              </button>
              {isConditionOpen && (
                <div className="absolute z-50 w-full mt-1 bg-[#0b0e11] border border-[#2b3139] rounded-sm shadow-xl overflow-hidden">
                  {CONDITION_OPTIONS.map(option => (
                    <button
                      key={option.value}
                      type="button"
                      onClick={() => handleConditionChange(option.value)}
                      className={`w-full flex items-center gap-2 px-3 py-2 text-sm text-left hover:bg-white/5 transition-colors ${
                        conditionType === option.value ? 'text-[#fcd535]' : 'text-zinc-300'
                      }`}
                    >
                      <span className="text-[#848e9c]">{option.icon}</span>
                      <span>{option.label}</span>
                    </button>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Condition payload */}
          {renderPayloadFields()}

          {/* Trigger */}
          <div className={`grid gap-4 ${triggerPolicy.triggerType === 'RECURRING' ? 'grid-cols-2' : 'grid-cols-1'}`}>
            <div className="space-y-2">
              <label className="block text-xs text-[#848e9c] font-medium tracking-wide">Trigger</label>
              <select
                value={triggerPolicy.triggerType}
                onChange={(e) => {
                  const type = e.target.value as TriggerType;
                  setTriggerPolicy(
                    type === 'RECURRING'
                      ? {triggerType: type, cooldownMinutes}
                      : {triggerType: type}
                  );
                }}
                className="w-full bg-[#0b0e11] border border-[#2b3139] rounded-sm px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:border-zinc-600 transition-colors appearance-none"
              >
                <option value="ONE_TIME">Once only</option>
                <option value="RECURRING">Repeat</option>
              </select>
            </div>
            {triggerPolicy.triggerType === 'RECURRING' && (
              <div className="space-y-2">
                <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">Cooldown interval (minutes)</label>
                <Input
                  type="number"
                  min={1}
                  value={cooldownMinutes}
                  onChange={(e) => setCooldownMinutes(parseInt(e.target.value) || 1)}
                  className="h-10"
                />
              </div>
            )}
          </div>

          {/* Expiration */}
          <div className="space-y-2">
            <label className="block text-xs text-[#848e9c] font-medium tracking-wide">Expiration</label>
            <select
              value={expiration}
              onChange={(e) => setExpiration(e.target.value)}
              className="w-full bg-[#0b0e11] border border-[#2b3139] rounded-sm px-3 py-2 text-sm text-zinc-100 focus:outline-none focus:border-zinc-600 transition-colors appearance-none"
            >
              {EXPIRATION_OPTIONS.map(option => (
                <option key={option.value} value={option.value}>{option.label}</option>
              ))}
            </select>
          </div>

          {/* Notifications */}
          <div className="space-y-2">
            <label className="block text-xs text-[#848e9c] font-medium tracking-wide">Notifications</label>
            <button
              type="button"
              onClick={() => {/* open notification selector */}}
              className="w-full flex items-center justify-between bg-[#0b0e11] border border-[#2b3139] rounded-sm px-3 py-2 text-sm text-zinc-100 hover:border-zinc-600 transition-colors"
            >
              <span className="flex items-center gap-2">
                <Bell size={16} className="text-[#848e9c]"/>
                {deliveryMethods.length > 0
                  ? deliveryMethods.map(m => DELIVERY_OPTIONS.find(o => o.value === m)?.label).join(', ')
                  : 'None'}
              </span>
              <span className="text-zinc-500">›</span>
            </button>
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between w-full px-4 py-3 border-t border-white/10">
          <div>
            {isEditMode && (
              <button
                type="button"
                onClick={handleDelete}
                className="p-2 rounded-md border border-red-500/50 text-red-500 hover:bg-red-500/15 hover:text-red-400 transition-colors"
                title="Delete alert"
              >
                <Trash2 size={16}/>
              </button>
            )}
          </div>
          <div className="flex items-center gap-2">
            <Button variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleSubmit} disabled={!isFormValid()}>
              {isEditMode ? 'Save' : 'Create'}
            </Button>
          </div>
        </div>
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
}
