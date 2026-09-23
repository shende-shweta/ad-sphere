import { useEffect, useRef, useState } from 'react';
import { Filter } from 'lucide-react';

/** "Filters" button with a popover of select filters. filters: [{ name, label, options }] */
export default function FilterPopover({ filters, values, onChange, onClear }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const activeCount = filters.filter((f) => values[f.name]).length;

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  return (
    <div className="filter-popover" ref={ref}>
      <button className="btn btn-outline btn-sm" aria-expanded={open} onClick={() => setOpen((o) => !o)}>
        <Filter size={14} aria-hidden="true" /> Filters
        {activeCount > 0 && <span className="count-pill">{activeCount}</span>}
      </button>
      {open && (
        <div className="popover" role="dialog" aria-label="Filters">
          {filters.map((f) => (
            <label key={f.name} className="field">
              <span>{f.label}</span>
              <select
                className="input select"
                value={values[f.name] || ''}
                onChange={(e) => onChange(f.name, e.target.value)}
              >
                <option value="">All</option>
                {f.options.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </label>
          ))}
          <button className="btn btn-link" onClick={onClear} disabled={!activeCount}>
            Clear filters
          </button>
        </div>
      )}
    </div>
  );
}
