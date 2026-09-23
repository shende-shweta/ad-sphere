import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from './Sidebar.jsx';
import Header from './Header.jsx';

export default function Layout() {
  const [navOpen, setNavOpen] = useState(false);
  return (
    <div className={`app-shell ${navOpen ? 'nav-open' : ''}`}>
      <a href="#main" className="skip-link">
        Skip to content
      </a>
      <Sidebar onNavigate={() => setNavOpen(false)} />
      <div className="nav-backdrop" onClick={() => setNavOpen(false)} />
      <div className="app-main">
        <Header onToggleNav={() => setNavOpen((o) => !o)} />
        <main id="main" className="page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
