import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { Eye, Pencil, Plus, Trash2, LayoutGrid } from 'lucide-react';
import { campaignApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { EDIT_ROLES, ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useDebounce } from '../../hooks/useDebounce.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import PageHeader from '../../components/PageHeader.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';
import ActionMenu from '../../components/ActionMenu.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';
import DateRangeFilter from '../../components/DateRangeFilter.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { formatCurrency, formatDate } from '../../utils/format.js';

const PAGE_SIZE = 5;
const FILTER_KEYS = ['status', 'objective', 'name', 'from', 'to'];

export default function CampaignListPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const { can } = useAuth();
  const lookups = useLookups();
  const [params, setParams] = useSearchParams();
  const [nameInput, setNameInput] = useState(params.get('name') || '');
  const debouncedName = useDebounce(nameInput);
  const [selected, setSelected] = useState([]);
  const [pendingDelete, setPendingDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const filters = Object.fromEntries(FILTER_KEYS.map((k) => [k, params.get(k) || '']));
  const page = Number(params.get('page') || 0);

  const updateParams = useCallback(
    (changes) => {
      setParams(
        (prev) => {
          const next = new URLSearchParams(prev);
          Object.entries(changes).forEach(([k, v]) => (v ? next.set(k, v) : next.delete(k)));
          if (!('page' in changes)) next.delete('page');
          return next;
        },
        { replace: true },
      );
    },
    [setParams],
  );

  // Keep the header search box and this page's name filter in sync.
  const nameParam = params.get('name') || '';
  useEffect(() => setNameInput(nameParam), [nameParam]);
  useEffect(() => {
    if (debouncedName !== nameParam) updateParams({ name: debouncedName });
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedName]);

  const query = useMemo(
    () => ({ ...filters, page, size: PAGE_SIZE, sort: 'id,asc' }),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [params.toString()],
  );
  const { data, error, loading, reload } = usePagedList(campaignApi.list, query);

  useEffect(() => setSelected([]), [data]);

  const hasFilters = FILTER_KEYS.some((k) => filters[k]);
  const rows = data?.content || [];
  const allSelected = rows.length > 0 && rows.every((r) => selected.includes(r.id));
  const canEdit = can(EDIT_ROLES);
  const canDelete = can([ROLES.ADMIN]);

  const confirmDelete = async () => {
    setDeleting(true);
    try {
      for (const id of pendingDelete.ids) await campaignApi.remove(id);
      toast.success(
        pendingDelete.ids.length === 1 ? 'Campaign deleted' : `${pendingDelete.ids.length} campaigns deleted`,
      );
      setPendingDelete(null);
      reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setDeleting(false);
    }
  };

  return (
    <>
      <PageHeader
        title="Campaigns"
        subtitle="Manage and track your advertising campaigns"
        actions={
          canEdit && (
            <>
              <Link to="/campaigns/new" className="btn btn-primary">
                <Plus size={16} aria-hidden="true" /> Create Campaign
              </Link>
              <Link to="/placements/new" className="btn btn-outline-primary">
                <LayoutGrid size={16} aria-hidden="true" /> Create Placement
              </Link>
            </>
          )
        }
      />

      <section className="card">
        <div className="filter-bar" aria-label="Campaign filters">
          <select
            className="input select"
            aria-label="Filter by status"
            value={filters.status}
            onChange={(e) => updateParams({ status: e.target.value })}
          >
            <option value="">All Status</option>
            {lookups.campaignStatuses.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <DateRangeFilter
            from={filters.from}
            to={filters.to}
            onChange={({ from, to }) => updateParams({ from, to })}
          />
          <select
            className="input select"
            aria-label="Filter by objective"
            value={filters.objective}
            onChange={(e) => updateParams({ objective: e.target.value })}
          >
            <option value="">All Objectives</option>
            {lookups.objectives.map((o) => (
              <option key={o.value} value={o.value}>
                {o.label}
              </option>
            ))}
          </select>
          <input
            type="search"
            className="input"
            placeholder="Campaign name"
            aria-label="Filter by campaign name"
            value={nameInput}
            onChange={(e) => setNameInput(e.target.value)}
          />
          <button
            className="btn btn-link"
            disabled={!hasFilters}
            onClick={() => {
              setNameInput('');
              setParams({}, { replace: true });
            }}
          >
            Clear Filters
          </button>
          {canDelete && selected.length > 0 && (
            <button
              className="btn btn-danger-outline btn-sm push-right"
              onClick={() => setPendingDelete({ ids: selected })}
            >
              <Trash2 size={14} aria-hidden="true" /> Delete selected ({selected.length})
            </button>
          )}
        </div>

        {error ? (
          <EmptyState title="Could not load campaigns" message={error.message} action={<button className="btn btn-outline" onClick={reload}>Retry</button>} />
        ) : loading && !data ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <EmptyState
            title="No campaigns found"
            message={hasFilters ? 'Try adjusting or clearing the filters.' : 'Create your first campaign to get started.'}
          />
        ) : (
          <>
            <div className={`table-wrap ${loading ? 'is-loading' : ''}`}>
              <table className="table table-cards">
                <thead>
                  <tr>
                    <th className="col-check">
                      <input
                        type="checkbox"
                        aria-label="Select all campaigns on this page"
                        checked={allSelected}
                        onChange={() => setSelected(allSelected ? [] : rows.map((r) => r.id))}
                      />
                    </th>
                    <th>Campaign Name</th>
                    <th>Objective</th>
                    <th>Status</th>
                    <th>Start Date</th>
                    <th>End Date</th>
                    <th className="num">Budget</th>
                    <th className="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((c) => (
                    <tr key={c.id}>
                      <td className="col-check">
                        <input
                          type="checkbox"
                          aria-label={`Select ${c.name}`}
                          checked={selected.includes(c.id)}
                          onChange={() =>
                            setSelected((s) => (s.includes(c.id) ? s.filter((x) => x !== c.id) : [...s, c.id]))
                          }
                        />
                      </td>
                      <td data-label="Campaign">
                        <Link to={`/campaigns/${c.id}`} className="cell-title">
                          {c.name}
                        </Link>
                        <span className="cell-sub">#{c.code}</span>
                      </td>
                      <td data-label="Objective" className="muted">{lookups.label('objectives', c.objective)}</td>
                      <td data-label="Status">
                        <StatusBadge status={c.status} label={lookups.label('campaignStatuses', c.status)} />
                      </td>
                      <td data-label="Start">{formatDate(c.startDate)}</td>
                      <td data-label="End">{formatDate(c.endDate)}</td>
                      <td data-label="Budget" className="num">{formatCurrency(c.budget)}</td>
                      <td className="col-actions">
                        <ActionMenu
                          label={`Actions for ${c.name}`}
                          items={[
                            { label: 'View details', icon: Eye, onClick: () => navigate(`/campaigns/${c.id}`) },
                            { label: 'Edit', icon: Pencil, hidden: !canEdit, onClick: () => navigate(`/campaigns/${c.id}/edit`) },
                            { label: 'Delete', icon: Trash2, danger: true, hidden: !canDelete, onClick: () => setPendingDelete({ ids: [c.id], name: c.name }) },
                          ]}
                        />
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
              onChange={(p) => updateParams({ page: p ? String(p) : '' })}
            />
          </>
        )}
      </section>

      {pendingDelete && (
        <ConfirmDialog
          danger
          title={pendingDelete.ids.length === 1 ? 'Delete campaign?' : `Delete ${pendingDelete.ids.length} campaigns?`}
          message={
            pendingDelete.name
              ? `"${pendingDelete.name}" will be permanently deleted.`
              : 'The selected campaigns will be permanently deleted.'
          }
          confirmLabel="Delete"
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setPendingDelete(null)}
        />
      )}
    </>
  );
}
