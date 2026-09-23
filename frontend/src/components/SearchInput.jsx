import { Search } from 'lucide-react';

export default function SearchInput({ value, onChange, placeholder, label }) {
  return (
    <div className="search-box">
      <Search size={14} aria-hidden="true" />
      <input
        type="search"
        value={value}
        placeholder={placeholder}
        aria-label={label || placeholder}
        onChange={(e) => onChange(e.target.value)}
      />
    </div>
  );
}
