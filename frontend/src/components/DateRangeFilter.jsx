import { useEffect, useRef, useState } from 'react';
import { CalendarDays, ChevronDown } from 'lucide-react';
import { formatDate } from '../utils/format.js';

/** Header filter button that edits a { from, to } ISO date range in a popover. */
export default function DateRangeFilter({ from, to, onChange }) {
  const [open, setOpen] = useState(false);
  const [draft, setDraft] = useState({ from: from || '', to: to || '' });
  const ref = useRef(null);

  useEffect(() => setDraft({ from: from || '', to: to || '' }), [from, to]);
  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, [open]);

  const invalid = draft.from && draft.to && draft.to < draft.from;
  const label =
    from || to ? `${from ? formatDate(from) : 'Any'} – ${to ? formatDate(to) : 'Any'}` : 'Date Range';

  return (
    <div className="filter-popover" ref={ref}>
      <button
        type="button"
        className={`input select-button ${from || to ? 'has-value' : ''}`}
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
      >
        <CalendarDays size={14} aria-hidden="true" />
        <span>{label}</span>
        <ChevronDown size={14} aria-hidden="true" />
      </button>
      {open && (
        <div className="popover" role="dialog" aria-label="Date range">
          <label className="field">
            <span>Running from</span>
            <input
              type="date"
              className="input"
              value={draft.from}
              onChange={(e) => setDraft((d) => ({ ...d, from: e.target.value }))}
            />
          </label>
          <label className="field">
            <span>Running until</span>
            <input
              type="date"
              className="input"
              value={draft.to}
              onChange={(e) => setDraft((d) => ({ ...d, to: e.target.value }))}
            />
          </label>
          {invalid && <p className="field-error">End must be on or after start</p>}
          <div className="popover-actions">
            <button
              type="button"
              className="btn btn-link"
              onClick={() => {
                onChange({ from: '', to: '' });
                setOpen(false);
              }}
            >
              Reset
            </button>
            <button
              type="button"
              className="btn btn-primary btn-sm"
              disabled={invalid}
              onClick={() => {
                onChange(draft);
                setOpen(false);
              }}
            >
              Apply
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
