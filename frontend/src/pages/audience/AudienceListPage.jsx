import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Pencil, Plus, Power } from 'lucide-react';
import { audienceApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { EDIT_ROLES, ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useDebounce } from '../../hooks/useDebounce.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import PageHeader from '../../components/PageHeader.jsx';
import SearchInput from '../../components/SearchInput.jsx';
import FilterPopover from '../../components/FilterPopover.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';
import ActionMenu from '../../components/ActionMenu.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Spinner from '../../components/Spinner.jsx';
import { formatNumber } from '../../utils/format.js';

export default function AudienceListPage() {
  const lookups = useLookups();
  const toast = useToast();
  const { can } = useAuth();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState({ type: '', status: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const q = useDebounce(search);

  const { data, error, loading, reload } = usePagedList(audienceApi.list, {
    q,
    ...filters,
    page,
    size,
  });
  const rows = data?.content || [];
  const isAdmin = can([ROLES.ADMIN]);
  const canEdit = can(EDIT_ROLES);

  const setFilter = (name, value) => {
    setFilters((f) => ({ ...f, [name]: value }));
    setPage(0);
  };

  const toggleStatus = async (a) => {
    const next = a.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE';
    try {
      await audienceApi.setStatus(a.id, next);
      toast.success(`"${a.name}" is now ${lookups.label('audienceStatuses', next).toLowerCase()}`);
      reload();
    } catch (err) {
      toast.error(err.message);
    }
  };

  return (
    <>
      <PageHeader
        title="Audience"
        subtitle="Manage your audience segments"
        actions={
          <>
            <SearchInput
              value={search}
              onChange={(v) => {
                setSearch(v);
                setPage(0);
              }}
              placeholder="Search audience..."
            />
            <FilterPopover
              filters={[
                { name: 'type', label: 'Type', options: lookups.audienceTypes },
                { name: 'status', label: 'Status', options: lookups.audienceStatuses },
              ]}
              values={filters}
              onChange={setFilter}
              onClear={() => {
                setFilters({ type: '', status: '' });
                setPage(0);
              }}
            />
            {canEdit && (
              <button className="btn btn-primary" onClick={() => navigate('/audience/new')}>
                <Plus size={16} /> Create Audience
              </button>
            )}
          </>
        }
      />
      <section className="card">
        {error ? (
          <EmptyState title="Could not load audiences" message={error.message} />
        ) : loading && !data ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <EmptyState title="No audience segments match" message="Try a different search or filter." />
        ) : (
          <>
            <div className={`table-wrap ${loading ? 'is-loading' : ''}`}>
              <table className="table table-cards">
                <thead>
                  <tr>
                    <th>Audience Name</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th className="num">Est. Reach</th>
                    <th>Status</th>
                    <th className="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((a) => (
                    <tr key={a.id}>
                      <td data-label="Audience" className="cell-title">{a.name}</td>
                      <td data-label="Type">{lookups.label('audienceTypes', a.type)}</td>
                      <td data-label="Description" className="muted">{a.description}</td>
                      <td data-label="Est. Reach" className="num">{formatNumber(a.estimatedSize)}</td>
                      <td data-label="Status">
                        <StatusBadge status={a.status} label={lookups.label('audienceStatuses', a.status)} />
                      </td>
                      <td className="col-actions">
                        <ActionMenu
                          label={`Actions for ${a.name}`}
                          items={[
                            {
                              label: 'Edit',
                              icon: Pencil,
                              hidden: !canEdit,
                              onClick: () => navigate(`/audience/${a.id}/edit`),
                            },
                            {
                              label: a.status === 'ACTIVE' ? 'Deactivate' : 'Activate',
                              icon: Power,
                              hidden: !isAdmin,
                              onClick: () => toggleStatus(a),
                            },
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
