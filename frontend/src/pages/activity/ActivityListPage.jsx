import { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import { Search } from 'lucide-react';
import PageHeader from '../../components/PageHeader.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';
import DateRangeFilter from '../../components/DateRangeFilter.jsx';
import { activityApi } from '../../api/services.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import useDebounce from '../../hooks/useDebounce.js';
import { formatDateTime } from '../../utils/format.js';

const ENTITY_TYPES = ['', 'CAMPAIGN', 'PLACEMENT', 'AUDIENCE', 'DEAL'];
const ACTIONS = ['', 'CREATED', 'UPDATED', 'DELETED', 'STATUS_CHANGED', 'BID_PLACED'];

function entityLabel(value) {
  return value ? value.charAt(0) + value.slice(1).toLowerCase() : '';
}

function actionLabel(value) {
  return value ? value.replace(/_/g, ' ').replace(/\b\w/g, (c) => c.toUpperCase()) : '';
}

export default function ActivityListPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [entityType, setEntityType] = useState('');
  const [action, setAction] = useState('');
  const [actorRaw, setActorRaw] = useState('');
  const [dateRange, setDateRange] = useState({ from: '', to: '' });
  const actorId = useDebounce(actorRaw, 400);

  const params = useMemo(
    () => ({
      page,
      size,
      ...(entityType && { entityType }),
      ...(action && { action }),
      ...(actorId && { actorId }),
      ...(dateRange.from && { from: dateRange.from }),
      ...(dateRange.to && { to: dateRange.to }),
    }),
    [page, size, entityType, action, actorId, dateRange],
  );

  const { data, loading } = usePagedList(activityApi.list, params);

  const resetFilters = () => {
    setEntityType('');
    setAction('');
    setActorRaw('');
    setDateRange({ from: '', to: '' });
    setPage(0);
  };

  const hasFilters = entityType || action || actorRaw || dateRange.from || dateRange.to;

  function renderEntityLink(row) {
    if (row.action === 'DELETED') return row.entityName;
    if (row.entityType === 'CAMPAIGN') {
      return (
        <Link to={`/campaigns/${row.entityId}`} className="cell-title">
          {row.entityName}
        </Link>
      );
    }
    return row.entityName;
  }

  return (
    <div className="page">
      <PageHeader title="Activity Log" subtitle="Audit trail of all system actions" />
      <div className="card">
        <div className="activity-filters">
          <div className="filter-bar">
            <select
              className="input select"
              value={entityType}
              onChange={(e) => {
                setEntityType(e.target.value);
                setPage(0);
              }}
              aria-label="Filter by entity type"
            >
              <option value="">All Entities</option>
              {ENTITY_TYPES.filter(Boolean).map((t) => (
                <option key={t} value={t}>
                  {entityLabel(t)}
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
              <option value="">All Actions</option>
              {ACTIONS.filter(Boolean).map((a) => (
                <option key={a} value={a}>
                  {actionLabel(a)}
                </option>
              ))}
            </select>

            <div className="search-box">
              <Search size={16} />
              <input
                type="text"
                placeholder="Search actor…"
                value={actorRaw}
                onChange={(e) => {
                  setActorRaw(e.target.value);
                  setPage(0);
                }}
                aria-label="Search by actor"
              />
            </div>

            <DateRangeFilter
              from={dateRange.from}
              to={dateRange.to}
              onChange={(r) => {
                setDateRange(r);
                setPage(0);
              }}
            />

            {hasFilters && (
              <button className="btn btn-link" onClick={resetFilters}>
                Clear filters
              </button>
            )}
          </div>
        </div>

        <div className={`table-wrap${loading ? ' is-loading' : ''}`}>
          <table className="table table-cards">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Entity Type</th>
                <th>Entity</th>
                <th>Summary</th>
              </tr>
            </thead>
            <tbody>
              {data?.content?.length === 0 && !loading && (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <h2>No activity found</h2>
                      <p>Adjust filters or wait for actions to be recorded.</p>
                    </div>
                  </td>
                </tr>
              )}
              {data?.content?.map((row) => (
                <tr key={row.id}>
                  <td data-label="Timestamp">{formatDateTime(row.timestamp)}</td>
                  <td data-label="Actor">{row.actor}</td>
                  <td data-label="Action">
                    <StatusBadge status={row.action} label={actionLabel(row.action)} />
                  </td>
                  <td data-label="Entity Type">{entityLabel(row.entityType)}</td>
                  <td data-label="Entity">{renderEntityLink(row)}</td>
                  <td data-label="Summary">{row.summary}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {data && data.totalElements > 0 && (
          <Pagination
            page={data.page}
            size={data.size}
            totalElements={data.totalElements}
            totalPages={data.totalPages}
            onPageChange={setPage}
            onSizeChange={(s) => {
              setSize(s);
              setPage(0);
            }}
          />
        )}
      </div>
    </div>
  );
}
