const TONES = {
  ACTIVE: 'success',
  WON: 'success',
  PAUSED: 'warning',
  DRAFT: 'neutral',
  INACTIVE: 'neutral',
  COMPLETED: 'info',
  SOLD: 'info',
  REJECTED: 'danger',
  CREATED: 'success',
  UPDATED: 'info',
  DELETED: 'danger',
  STATUS_CHANGED: 'warning',
  BID_PLACED: 'info',
};

export default function StatusBadge({ status, label }) {
  return <span className={`badge badge-${TONES[status] || 'neutral'}`}>{label || status}</span>;
}
