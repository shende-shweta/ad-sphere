import { useState } from 'react';
import { Link } from 'react-router-dom';
import { activityApi } from '../../api/services.js';
import { useDebounce } from '../../hooks/useDebounce.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import PageHeader from '../../components/PageHeader.jsx';
import Pagination from '../../components/Pagination.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Spinner from '../../components/Spinner.jsx';

const ENTITY_TYPE_OPTIONS = [
  { value: '', label: 'All Entity Types' },
  { value: 'CAMPAIGN', label: 'Campaign' },
  { value: 'PLACEMENT', label: 'Placement' },
  { value: 'AUDIENCE', label: 'Audience' },
  { value: 'DEAL', label: 'Deal' },
];

const ACTION_OPTIONS = [
  { value: '', label: 'All Actions' },
  { value: 'CREATED', label: 'Created' },
  { value: 'UPDATED', label: 'Updated' },
  { value: 'DELETED', label: 'Deleted' },
  { value: 'STATUS_CHANGED', label: 'Status changed' },
  { value: 'BID_PLACED', label: 'Bid placed' },
];

const ACTION_TONES = {
  CREATED: 'success',
  UPDATED: 'info',
  DELETED: 'danger',
  STATUS_CHANGED: 'warning',
  BID_PLACED: 'neutral',
};

const ACTION_LABELS = {
  CREATED: 'Created',
  UPDATED: 'Updated',
  DELETED: 'Deleted',
  STATUS_CHANGED: 'Status changed',
  BID_PLACED: 'Bid placed',
};

const ENTITY_TYPE_LABELS = {
  CAMPAIGN: 'Campaign',
  PLACEMENT: 'Placement',
  AUDIENCE: 'Audience',
  DEAL: 'Deal',
};

const ENTITY_ROUTES = {
  CAMPAIGN: (id) => `/campaigns/${id}`,
  AUDIENCE: () => '/audience',
  DEAL: () => '/inventory',
};

function formatTimestamp(ts) {
  return new Date(ts).toLocaleString();
}

function EntityName({ action, entityType, entityId, entityName }) {
  if (action === 'DELETED') {
    return <span>{entityName}</span>;
  }
  const routeFn = ENTITY_ROUTES[entityType];
  if (!routeFn) {
    return <span>{entityName}</span>;
  }
  return <Link to={routeFn(entityId)}>{entityName}</Link>;
}

export default function ActivityListPage() {
  const [entityType, setEntityType] = useState('');
  const [action, setAction] = useState('');
  const [actorSearch, setActorSearch] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);

  const debouncedActor = useDebounce(actorSearch);

  const params = { page, size };
  if (entityType) params.entityType = entityType;
  if (action) params.action = action;
  if (debouncedActor) params.actorId = debouncedActor;
  if (fromDate) params.from = fromDate;
  if (toDate) params.to = toDate;

  const { data, error, loading } = usePagedList(activityApi.list, params);
  const rows = data?.content || [];

  const hasFilters = entityType || action || actorSearch || fromDate || toDate;

  const clearFilters = () => {
    setEntityType('');
    setAction('');
    setActorSearch('');
    setFromDate('');
    setToDate('');
    setPage(0);
  };

  return (
    <>
      <PageHeader title="Activity Log" subtitle="Chronological record of all changes" />
      <section className="card">
        <div
          className="activity-filters"
          style={{
            display: 'flex',
            gap: '0.75rem',
            flexWrap: 'wrap',
            padding: '1rem',
            borderBottom: '1px solid var(--border)',
          }}
        >
          <select
            className="input select"
            value={entityType}
            onChange={(e) => {
              setEntityType(e.target.value);
              setPage(0);
            }}
            aria-label="Filter by entity type"
          >
            {ENTITY_TYPE_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <select
            className="input select"
            value={action}
            onChange={(e) => {
              setAction(e.target.value);
              setPage(0);
            }}
            aria-label="Filter by action"
          >
            {ACTION_OPTIONS.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <input
            className="input"
            type="text"
            placeholder="Filter by actor..."
            value={actorSearch}
            onChange={(e) => {
              setActorSearch(e.target.value);
              setPage(0);
            }}
            aria-label="Filter by actor"
          />
          <input
            className="input"
            type="date"
            value={fromDate}
            onChange={(e) => {
              setFromDate(e.target.value);
              setPage(0);
            }}
            aria-label="From date"
          />
          <input
            className="input"
            type="date"
            value={toDate}
            onChange={(e) => {
              setToDate(e.target.value);
              setPage(0);
            }}
            aria-label="To date"
          />
          {hasFilters && (
            <button className="btn btn-ghost" onClick={clearFilters}>
              Clear
            </button>
          )}
        </div>
        {error ? (
          <EmptyState title="Could not load activity" message={error.message} />
        ) : loading && !data ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <EmptyState
            title="No activity found"
            message="Try a different filter or date range."
          />
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
                  {rows.map((row) => (
                    <tr key={row.id}>
                      <td data-label="Time">{formatTimestamp(row.timestamp)}</td>
                      <td data-label="Actor">{row.actor}</td>
                      <td data-label="Action">
                        <span
                          className={`badge badge-${ACTION_TONES[row.action] || 'neutral'}`}
                        >
                          {ACTION_LABELS[row.action] || row.action}
                        </span>
                      </td>
                      <td data-label="Entity">
                        <EntityName
                          action={row.action}
                          entityType={row.entityType}
                          entityId={row.entityId}
                          entityName={row.entityName}
                        />
                        <div className="muted" style={{ fontSize: '0.85em' }}>
                          {ENTITY_TYPE_LABELS[row.entityType] || row.entityType}
                        </div>
                      </td>
                      <td data-label="Details" className="muted">
                        {row.summary}
                      </td>
                    </tr>
                  ))}
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
