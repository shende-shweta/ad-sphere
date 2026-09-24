import { useState } from 'react';
import { Link } from 'react-router-dom';
import { activityApi } from '../../api/services.js';
import { useDebounce } from '../../hooks/useDebounce.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import PageHeader from '../../components/PageHeader.jsx';
import SearchInput from '../../components/SearchInput.jsx';
import FilterPopover from '../../components/FilterPopover.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Spinner from '../../components/Spinner.jsx';

const ENTITY_TYPE_OPTIONS = [
  { value: 'CAMPAIGN', label: 'Campaign' },
  { value: 'PLACEMENT', label: 'Placement' },
  { value: 'AUDIENCE', label: 'Audience' },
  { value: 'DEAL', label: 'Deal' },
];

const ACTION_OPTIONS = [
  { value: 'CREATED', label: 'Created' },
  { value: 'UPDATED', label: 'Updated' },
  { value: 'DELETED', label: 'Deleted' },
  { value: 'STATUS_CHANGED', label: 'Status Changed' },
  { value: 'BID_PLACED', label: 'Bid Placed' },
];

const ACTION_LABELS = {
  CREATED: 'Created',
  UPDATED: 'Updated',
  DELETED: 'Deleted',
  STATUS_CHANGED: 'Status Changed',
  BID_PLACED: 'Bid Placed',
};

const ENTITY_LINKS = {
  CAMPAIGN: (id) => `/campaigns/${id}`,
  PLACEMENT: () => '/campaigns',
  AUDIENCE: () => '/audience',
  DEAL: () => '/inventory',
};

function formatTimestamp(ts) {
  if (!ts) return '';
  return new Date(ts).toLocaleDateString(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export default function ActivityListPage() {
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState({ entityType: '', action: '' });
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const actorId = useDebounce(search);

  const params = {
    ...(filters.entityType ? { entityType: filters.entityType } : {}),
    ...(filters.action ? { action: filters.action } : {}),
    ...(actorId ? { actorId } : {}),
    ...(from ? { from } : {}),
    ...(to ? { to } : {}),
    page,
    size,
  };
  const { data, error, loading } = usePagedList(activityApi.list, params);
  const rows = data?.content || [];

  const setFilter = (name, value) => {
    setFilters((f) => ({ ...f, [name]: value }));
    setPage(0);
  };

  return (
    <>
      <PageHeader
        title="Activity Log"
        subtitle="Browse all recorded actions across campaigns, placements, audiences, and deals."
        actions={
          <>
            <SearchInput
              value={search}
              onChange={(v) => {
                setSearch(v);
                setPage(0);
              }}
              placeholder="Filter by actor..."
            />
            <FilterPopover
              filters={[
                { name: 'entityType', label: 'Entity Type', options: ENTITY_TYPE_OPTIONS },
                { name: 'action', label: 'Action', options: ACTION_OPTIONS },
              ]}
              values={filters}
              onChange={setFilter}
              onClear={() => {
                setFilters({ entityType: '', action: '' });
                setPage(0);
              }}
            />
          </>
        }
      />
      <div className="filter-bar">
        <label className="field">
          <span>From</span>
          <input
            type="date"
            className="input"
            value={from}
            onChange={(e) => {
              setFrom(e.target.value);
              setPage(0);
            }}
          />
        </label>
        <label className="field">
          <span>To</span>
          <input
            type="date"
            className="input"
            value={to}
            onChange={(e) => {
              setTo(e.target.value);
              setPage(0);
            }}
          />
        </label>
        {(from || to) && (
          <button
            className="btn btn-link"
            onClick={() => {
              setFrom('');
              setTo('');
              setPage(0);
            }}
          >
            Clear dates
          </button>
        )}
      </div>
      <section className="card">
        {error ? (
          <EmptyState title="Could not load activity" message={error.message} />
        ) : loading && !data ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <EmptyState title="No activity found" message="Try a different search or filter." />
        ) : (
          <>
            <div className={`table-wrap ${loading ? 'is-loading' : ''}`}>
              <table className="table table-cards">
                <thead>
                  <tr>
                    <th>Time</th>
                    <th>Actor</th>
                    <th>Action</th>
                    <th>Entity</th>
                    <th>Details</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((row) => {
                    const linkFn = ENTITY_LINKS[row.entityType];
                    const showLink = row.action !== 'DELETED' && linkFn;
                    return (
                      <tr key={row.id}>
                        <td data-label="Time">{formatTimestamp(row.timestamp)}</td>
                        <td data-label="Actor">{row.actor}</td>
                        <td data-label="Action">
                          <StatusBadge
                            status={row.action}
                            label={ACTION_LABELS[row.action] || row.action}
                          />
                        </td>
                        <td data-label="Entity">
                          {showLink ? (
                            <Link to={linkFn(row.entityId)} className="cell-title">
                              {row.entityName}
                            </Link>
                          ) : (
                            <span className="cell-title">{row.entityName}</span>
                          )}
                          <span className="cell-sub">{row.entityType}</span>
                        </td>
                        <td data-label="Details">{row.summary}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
            <Pagination
              page={data.page}
              size={data.size}
              totalPages={data.totalPages}
              totalElements={data.totalElements}
              onChange={setPage}
              onSizeChange={(s) => {
                setSize(s);
                setPage(0);
              }}
            />
          </>
        )}
      </section>
    </>
  );
}
