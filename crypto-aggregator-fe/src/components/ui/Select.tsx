
type SelectProps = {
  label?: string;
  value: string;
  onChange: (value: string) => void;
  options: {value: string; label: string}[];
  placeholder?: string;
  className?: string;
};

export const Select = ({label, value, onChange, options, placeholder, className = ''}: SelectProps) => {
  return (
    <div className="w-full">
      {label && (
        <label className="block text-xs text-[#848e9c] mb-1.5 font-medium tracking-wide">
          {label}
        </label>
      )}
      <div className="relative">
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className={`
            w-full appearance-none bg-[#0b0e11] border border-[#2b3139] text-[#eaecef]
            px-3 py-2 rounded-lg text-sm
            transition-all duration-200 ease-out
            hover:border-[#474d57]
            focus:outline-none focus:border-[#fcd535] focus:shadow-[0_0_0_3px_rgba(252,213,53,0.1)]
            ${className}
          `}
        >
          {placeholder && <option value="">{placeholder}</option>}
          {options.map(opt => (
            <option key={opt.value} value={opt.value} className="bg-[#0b0e11]">
              {opt.label}
            </option>
          ))}
        </select>
        <div className="absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-[#848e9c]">
          <svg width="12" height="12" viewBox="0 0 12 12" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M3 4.5L6 7.5L9 4.5"/>
          </svg>
        </div>
      </div>
    </div>
  );
};
