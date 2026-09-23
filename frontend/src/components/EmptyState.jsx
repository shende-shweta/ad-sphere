import { Inbox } from 'lucide-react';

export default function EmptyState({ title, message, action, icon: Icon = Inbox }) {
  return (
    <div className="empty-state">
      <Icon size={32} aria-hidden="true" />
      <h2>{title}</h2>
      {message && <p>{message}</p>}
      {action}
    </div>
  );
}
