import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ChevronDown, LogOut, Menu, Search, UserRound } from 'lucide-react';
import { useAuth } from '../auth/AuthContext.jsx';
import { initials } from '../utils/format.js';

export default function Header({ onToggleNav }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const [query, setQuery] = useState(params.get('name') || '');
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const close = (e) => menuRef.current && !menuRef.current.contains(e.target) && setMenuOpen(false);
    document.addEventListener('mousedown', close);
    return () => document.removeEventListener('mousedown', close);
  }, []);

  const submit = (e) => {
    e.preventDefault();
    const q = query.trim();
    navigate(q ? `/campaigns?name=${encodeURIComponent(q)}` : '/campaigns');
  };

  const firstName = user?.fullName?.split(' ')[0];

  return (
    <header className="topbar">
      <button className="icon-btn nav-toggle" aria-label="Toggle navigation" onClick={onToggleNav}>
        <Menu size={20} />
      </button>
      <form className="search-box topbar-search" role="search" onSubmit={submit}>
        <Search size={16} aria-hidden="true" />
        <input
          type="search"
          placeholder="Search campaigns..."
          aria-label="Search campaigns"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </form>
      <div className="user-menu" ref={menuRef}>
        <button
          className="user-menu-trigger"
          aria-haspopup="menu"
          aria-expanded={menuOpen}
          onClick={() => setMenuOpen((o) => !o)}
        >
          <span className="avatar avatar-sm">{initials(user?.fullName)}</span>
          <span className="user-name">{firstName}</span>
          <ChevronDown size={16} aria-hidden="true" />
        </button>
        {menuOpen && (
          <div className="menu" role="menu">
            <div className="menu-header">
              <strong>{user?.fullName}</strong>
              <span>{user?.email}</span>
            </div>
            <Link role="menuitem" to="/profile" className="menu-item" onClick={() => setMenuOpen(false)}>
              <UserRound size={16} /> Profile
            </Link>
            <button
              role="menuitem"
              className="menu-item"
              onClick={() => {
                logout();
                navigate('/login');
              }}
            >
              <LogOut size={16} /> Sign out
            </button>
          </div>
        )}
      </div>
    </header>
  );
}
