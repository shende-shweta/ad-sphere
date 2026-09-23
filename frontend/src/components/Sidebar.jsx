import { NavLink, useLocation } from 'react-router-dom';
import {
  Megaphone,
  Users,
  Boxes,
  Settings,
  HelpCircle,
  UserRound,
} from 'lucide-react';
import Logo from './Logo.jsx';

const NAV = [
  { to: '/campaigns', label: 'Campaigns', icon: Megaphone, match: ['/campaigns', '/placements'] },
  { to: '/audience', label: 'Audience', icon: Users },
  { to: '/inventory', label: 'Inventory', icon: Boxes },
  { to: '/settings', label: 'Settings', icon: Settings },
  { to: '/help', label: 'Help', icon: HelpCircle },
  { to: '/profile', label: 'Profile', icon: UserRound },
];

export default function Sidebar({ onNavigate }) {
  const { pathname } = useLocation();
  return (
    <aside className="sidebar" aria-label="Main navigation">
      <div className="sidebar-brand">
        <Logo />
        <span>AdSphere</span>
      </div>
      <nav>
        <ul>
          {NAV.map(({ to, label, icon: Icon, match }) => (
            <li key={to}>
              <NavLink
                to={to}
                onClick={onNavigate}
                className={({ isActive }) =>
                  `nav-item ${
                    isActive || match?.some((m) => pathname.startsWith(m))
                      ? 'active'
                      : ''
                  }`
                }
              >
                <Icon size={18} aria-hidden="true" />
                <span>{label}</span>
              </NavLink>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
}
