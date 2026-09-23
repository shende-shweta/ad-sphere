import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Pencil } from 'lucide-react';
import { campaignApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { EDIT_ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import { formatCurrency, formatDate, formatDateTime } from '../../utils/format.js';

export default function CampaignDetailPage() {
  const { id } = useParams();
  const { can } = useAuth();
  const lookups = useLookups();
  const [campaign, setCampaign] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    campaignApi.get(id).then(setCampaign).catch(setError);
  }, [id]);

  if (error) return <EmptyState title="Could not load campaign" message={error.message} />;
  if (!campaign) return <Spinner />;

  const details = [
    ['Campaign ID', campaign.code],
    ['Objective', lookups.label('objectives', campaign.objective)],
    ['Status', <StatusBadge key="s" status={campaign.status} label={lookups.label('campaignStatuses', campaign.status)} />],
    ['Flight', `${formatDate(campaign.startDate)} – ${formatDate(campaign.endDate)}`],
    ['Budget', formatCurrency(campaign.budget)],
    ['Last updated', formatDateTime(campaign.updatedAt)],
  ];

  return (
    <>
      <PageHeader
        title={campaign.name}
        subtitle={campaign.description}
        backTo="/campaigns"
        actions={
          can(EDIT_ROLES) && (
            <Link to={`/campaigns/${id}/edit`} className="btn btn-primary">
              <Pencil size={16} aria-hidden="true" /> Edit Campaign
            </Link>
          )
        }
      />
      <section className="card">
        <dl className="detail-grid">
          {details.map(([label, value]) => (
            <div key={label}>
              <dt>{label}</dt>
              <dd>{value}</dd>
            </div>
          ))}
        </dl>
      </section>
      <section className="card">
        <h2 className="section-title">Assigned Placements</h2>
        {campaign.placements.length === 0 ? (
          <p className="muted">No placements assigned yet.</p>
        ) : (
          <ul className="chip-list">
            {campaign.placements.map((p) => (
              <li key={p.id} className="chip">
                {p.name}
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  );
}
