const TONES = {
  ACTIVE: 'success',
  WON: 'success',
  CREATED: 'success',
  PAUSED: 'warning',
  STATUS_CHANGED: 'warning',
  DRAFT: 'neutral',
  INACTIVE: 'neutral',
  COMPLETED: 'info',
  SOLD: 'info',
  UPDATED: 'info',
  BID_PLACED: 'info',
  REJECTED: 'danger',
  DELETED: 'danger',
};

export default function StatusBadge({ status, label }) {
  return <span className={`badge badge-${TONES[status] || 'neutral'}`}>{label || status}</span>;
}
