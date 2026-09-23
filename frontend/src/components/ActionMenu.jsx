import { useEffect, useRef, useState } from 'react';
import { MoreHorizontal } from 'lucide-react';

/** "⋯" row menu. items: [{ label, onClick, icon, danger, hidden }] */
export default function ActionMenu({ items, label = 'Actions' }) {
  const [open, setOpen] = useState(false);
  const ref = useRef(null);
  const visible = items.filter((i) => !i.hidden);

  useEffect(() => {
    if (!open) return undefined;
    const close = (e) => ref.current && !ref.current.contains(e.target) && setOpen(false);
    const onKey = (e) => e.key === 'Escape' && setOpen(false);
    document.addEventListener('mousedown', close);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', close);
      document.removeEventListener('keydown', onKey);
    };
  }, [open]);

  if (!visible.length) return null;
  return (
    <div className="action-menu" ref={ref}>
      <button
        className="icon-btn"
        aria-label={label}
        aria-haspopup="menu"
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
      >
        <MoreHorizontal size={18} />
      </button>
      {open && (
        <div className="menu menu-right" role="menu">
          {visible.map(({ label: itemLabel, onClick, icon: Icon, danger }) => (
            <button
              key={itemLabel}
              role="menuitem"
              className={`menu-item ${danger ? 'danger' : ''}`}
              onClick={() => {
                setOpen(false);
                onClick();
              }}
            >
              {Icon && <Icon size={16} aria-hidden="true" />} {itemLabel}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
