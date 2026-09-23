import PageHeader from '../../components/PageHeader.jsx';
import NavCard from '../../components/NavCard.jsx';
import { SECTIONS } from './sections.js';

export default function SettingsPage() {
  return (
    <>
      <PageHeader title="Settings" subtitle="Manage application settings" />
      <div className="nav-card-list">
        {Object.entries(SECTIONS).map(([key, s]) => (
          <NavCard key={key} to={`/settings/${key}`} icon={s.icon} title={s.title} description={s.description} />
        ))}
      </div>
    </>
  );
}
