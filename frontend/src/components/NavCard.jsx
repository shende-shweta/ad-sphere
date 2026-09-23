import { Link } from 'react-router-dom';
import { ChevronRight } from 'lucide-react';

/** Row-style link card used on the Settings, Help and Profile pages. */
export default function NavCard({ to, onClick, icon: Icon, title, description, danger }) {
  const content = (
    <>
      <span className={`nav-card-icon ${danger ? 'danger' : ''}`}>
        <Icon size={18} aria-hidden="true" />
      </span>
      <span className="nav-card-text">
        <span className="nav-card-title">{title}</span>
        <span className="nav-card-desc">{description}</span>
      </span>
      <ChevronRight size={18} className="nav-card-chevron" aria-hidden="true" />
    </>
  );
  return to ? (
    <Link to={to} className="nav-card">
      {content}
    </Link>
  ) : (
    <button type="button" className="nav-card" onClick={onClick}>
      {content}
    </button>
  );
}
