import { useEffect, useId, useMemo, useRef, useState } from 'react';
import { Check, ChevronDown, X } from 'lucide-react';

/** Searchable multi-select. options: [{ value, label, hint }]; value: array of option values. */
export default function MultiSelect({ options, value, onChange, placeholder, loading, error, id }) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState('');
  const ref = useRef(null);
  const listId = useId();

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return q ? options.filter((o) => o.label.toLowerCase().includes(q)) : options;
  }, [options, query]);

  const selected = options.filter((o) => value.includes(o.value));
  const toggle = (v) => onChange(value.includes(v) ? value.filter((x) => x !== v) : [...value, v]);

  return (
    <div className={`multiselect ${error ? 'has-error' : ''}`} ref={ref}>
      <div className="input multiselect-control" onClick={() => setOpen(true)}>
        {selected.map((o) => (
          <span key={o.value} className="chip">
            {o.label}
            <button
              type="button"
              aria-label={`Remove ${o.label}`}
              onClick={(e) => {
                e.stopPropagation();
                toggle(o.value);
              }}
            >
              <X size={12} />
            </button>
          </span>
        ))}
        <input
          id={id}
          role="combobox"
          aria-expanded={open}
          aria-controls={listId}
          aria-autocomplete="list"
          value={query}
          placeholder={selected.length ? '' : placeholder}
          onChange={(e) => {
            setQuery(e.target.value);
            setOpen(true);
          }}
          onFocus={() => setOpen(true)}
          onKeyDown={(e) => {
            if (e.key === 'Escape') setOpen(false);
            if (e.key === 'Backspace' && !query && selected.length) toggle(selected.at(-1).value);
          }}
        />
        <ChevronDown size={16} className="multiselect-caret" aria-hidden="true" />
      </div>
      {open && (
        <ul className="multiselect-list" id={listId} role="listbox" aria-multiselectable="true">
          {loading && <li className="multiselect-empty">Loading…</li>}
          {!loading && filtered.length === 0 && <li className="multiselect-empty">No matches</li>}
          {filtered.map((o) => {
            const isSelected = value.includes(o.value);
            return (
              <li
                key={o.value}
                role="option"
                aria-selected={isSelected}
                className={isSelected ? 'selected' : ''}
                onMouseDown={(e) => e.preventDefault()}
                onClick={() => toggle(o.value)}
              >
                <span className="check">{isSelected && <Check size={14} />}</span>
                <span>
                  {o.label}
                  {o.hint && <span className="option-hint">{o.hint}</span>}
                </span>
              </li>
            );
          })}
        </ul>
      )}
    </div>
  );
}
