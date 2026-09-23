const TONES = {
  ACTIVE: 'success',
  WON: 'success',
  PAUSED: 'warning',
  DRAFT: 'neutral',
  INACTIVE: 'neutral',
  COMPLETED: 'info',
  SOLD: 'info',
  REJECTED: 'danger',
};

export default function StatusBadge({ status, label }) {
  return <span className={`badge badge-${TONES[status] || 'neutral'}`}>{label || status}</span>;
}
