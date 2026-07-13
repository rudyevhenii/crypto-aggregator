
type SelectProps = {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  options: {value: string; label: string}[];
  className?: string;
};

export const Select = ({label, value, onChange, options, className = ''}: SelectProps) => {
  return (
    <div className="w-full">
      {label && (
        <label className="block text-xs text-zinc-400 mb-1.5 font-medium tracking-wider uppercase">
          {label}
        </label>
      )}
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={`
            w-full appearance-none bg-[#0b0e11] border border-white/10 text-zinc-50 truncate
            px-3 py-2 pr-8 rounded-lg text-sm
            transition-all duration-200 ease-out
            hover:border-zinc-600
            focus:outline-none focus:border-[#fcd535] focus:shadow-[0_0_0_3px_rgba(252,213,53,0.1)]
            ${className}
          `}
        >
          {options.map(opt => (
            <option key={opt.value} value={opt.value} className="bg-[#0b0e11]">
              {opt.label}
            </option>
          ))}
        </select>
        <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-zinc-400">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 4.5L6 7.5L9 4.5"/>
          </svg>
        </div>
      </div>
    </div>
  );
};
