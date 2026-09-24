import { ChevronLeft, ChevronRight } from 'lucide-react';

const DEFAULT_SIZE_OPTIONS = [10, 25, 50, 100];

/** Zero-based page navigation with a compact window of page numbers. */
export default function Pagination({
  page,
  totalPages,
  totalElements,
  size,
  onChange,
  onSizeChange,
  sizeOptions = DEFAULT_SIZE_OPTIONS,
}) {
  if (!totalPages) return null;
  const from = page * size + 1;
  const to = Math.min(totalElements, (page + 1) * size);

  const windowSize = 5;
  let start = Math.max(0, page - Math.floor(windowSize / 2));
  const end = Math.min(totalPages, start + windowSize);
  start = Math.max(0, end - windowSize);
  const pages = [];
  for (let i = start; i < end; i++) pages.push(i);

  return (
    <div className="pagination">
      <div className="pagination-summary">
        <span className="pagination-info">
          Showing {from}–{to} of {totalElements}
        </span>
        {onSizeChange && (
          <label className="pagination-size">
            Rows per page
            <select
              className="input select"
              value={size}
              onChange={(e) => onSizeChange(Number(e.target.value))}
            >
              {sizeOptions.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </label>
        )}
      </div>
      <nav className="pagination-controls" aria-label="Pagination">
        <button
          className="page-btn"
          disabled={page === 0}
          onClick={() => onChange(page - 1)}
          aria-label="Previous page"
        >
          <ChevronLeft size={16} />
        </button>
        {pages.map((p) => (
          <button
            key={p}
            className={`page-btn ${p === page ? 'active' : ''}`}
            aria-current={p === page ? 'page' : undefined}
            onClick={() => onChange(p)}
          >
            {p + 1}
          </button>
        ))}
        <button
          className="page-btn"
          disabled={page >= totalPages - 1}
          onClick={() => onChange(page + 1)}
          aria-label="Next page"
        >
          <ChevronRight size={16} />
        </button>
      </nav>
    </div>
  );
}
