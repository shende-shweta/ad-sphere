import { useState } from 'react';
import { Gavel } from 'lucide-react';
import { dealApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { EDIT_ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useDebounce } from '../../hooks/useDebounce.js';
import { usePagedList } from '../../hooks/usePagedList.js';
import PageHeader from '../../components/PageHeader.jsx';
import SearchInput from '../../components/SearchInput.jsx';
import FilterPopover from '../../components/FilterPopover.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Pagination from '../../components/Pagination.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Spinner from '../../components/Spinner.jsx';
import BidModal from './BidModal.jsx';
import { formatNumber, formatPrice } from '../../utils/format.js';

export default function InventoryListPage() {
  const lookups = useLookups();
  const toast = useToast();
  const { can } = useAuth();
  const [search, setSearch] = useState('');
  const [filters, setFilters] = useState({ type: '', status: '' });
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [bidDeal, setBidDeal] = useState(null);
  const q = useDebounce(search);

  const { data, error, loading, reload } = usePagedList(dealApi.list, { q, ...filters, page, size });
  const rows = data?.content || [];
  const canBid = can(EDIT_ROLES);

  const setFilter = (name, value) => {
    setFilters((f) => ({ ...f, [name]: value }));
    setPage(0);
  };

  const onBidResult = (result) => {
    setBidDeal(null);
    (result.status === 'WON' ? toast.success : toast.error)(result.message);
    reload();
  };

  return (
    <>
      <PageHeader
        title="Inventory"
        subtitle="Manage deals and available inventory. Winning bids sell the deal and start serving your ads."
        actions={
          <>
            <SearchInput
              value={search}
              onChange={(v) => {
                setSearch(v);
                setPage(0);
              }}
              placeholder="Search deals..."
            />
            <FilterPopover
              filters={[
                { name: 'type', label: 'Type', options: lookups.dealTypes },
                { name: 'status', label: 'Status', options: lookups.dealStatuses },
              ]}
              values={filters}
              onChange={setFilter}
              onClear={() => {
                setFilters({ type: '', status: '' });
                setPage(0);
              }}
            />
          </>
        }
      />
      <section className="card">
        {error ? (
          <EmptyState title="Could not load inventory" message={error.message} />
        ) : loading && !data ? (
          <Spinner />
        ) : rows.length === 0 ? (
          <EmptyState title="No deals match" message="Try a different search or filter." />
        ) : (
          <>
            <div className={`table-wrap ${loading ? 'is-loading' : ''}`}>
              <table className="table table-cards">
                <thead>
                  <tr>
                    <th>Deal Name</th>
                    <th>Type</th>
                    <th className="num">Bid Price</th>
                    <th className="num">Available Impressions</th>
                    <th>Status</th>
                    <th className="col-actions">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map((d) => (
                    <tr key={d.id}>
                      <td data-label="Deal">
                        <span className="cell-title">{d.name}</span>
                        <span className="cell-sub">
                          {d.publisher} · {lookups.label('adFormats', d.adFormat)}
                        </span>
                        {d.status === 'SOLD' && (
                          <span className="cell-sub sold-note">
                            Won at {formatPrice(d.winningBid)} · serving “{d.campaignName}”
                          </span>
                        )}
                      </td>
                      <td data-label="Type">{lookups.label('dealTypes', d.type)}</td>
                      <td data-label="Bid Price" className="num">{formatPrice(d.bidPrice)}</td>
                      <td data-label="Impressions" className="num">{formatNumber(d.availableImpressions)}</td>
                      <td data-label="Status">
                        <StatusBadge status={d.status} label={lookups.label('dealStatuses', d.status)} />
                      </td>
                      <td className="col-actions">
                        {canBid && d.status === 'ACTIVE' ? (
                          <button className="btn btn-outline-primary btn-sm" onClick={() => setBidDeal(d)}>
                            <Gavel size={14} aria-hidden="true" /> Bid
                          </button>
                        ) : (
                          <span className="muted" aria-label="No actions available">—</span>
                        )}
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
      {bidDeal && <BidModal deal={bidDeal} onClose={() => setBidDeal(null)} onResult={onBidResult} />}
    </>
  );
}
