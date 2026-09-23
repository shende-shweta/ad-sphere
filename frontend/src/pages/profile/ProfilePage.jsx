import { useNavigate } from 'react-router-dom';
import { LogOut, ShieldCheck, UserCog } from 'lucide-react';
import { useAuth } from '../../auth/AuthContext.jsx';
import { useLookups } from '../../context/LookupsContext.jsx';
import PageHeader from '../../components/PageHeader.jsx';
import NavCard from '../../components/NavCard.jsx';
import { formatDateTime, initials } from '../../utils/format.js';

export default function ProfilePage() {
  const { user, logout } = useAuth();
  const lookups = useLookups();
  const navigate = useNavigate();

  return (
    <>
      <PageHeader title="Profile" subtitle="Manage your account" />
      <section className="card profile-card">
        <span className="avatar avatar-lg">{initials(user.fullName)}</span>
        <div className="profile-info">
          <strong>{user.fullName}</strong>
          <span className="muted">{user.email}</span>
          <span className="muted small">
            {lookups.label('roles', user.role)}
            {user.jobTitle ? ` · ${user.jobTitle}` : ''} · Last sign-in {formatDateTime(user.lastLoginAt)}
          </span>
        </div>
        <button className="btn btn-outline-primary btn-sm push-right" onClick={() => navigate('/profile/edit')}>
          Edit Profile
        </button>
      </section>
      <div className="nav-card-list">
        <NavCard to="/profile/edit" icon={UserCog} title="Account Settings" description="Update your profile and preferences" />
        <NavCard to="/profile/security" icon={ShieldCheck} title="Security" description="Password, 2FA, login activity" />
        <NavCard
          icon={LogOut}
          danger
          title="Sign Out"
          description="Log out from your account"
          onClick={() => {
            logout();
            navigate('/login');
          }}
        />
      </div>
    </>
  );
}
