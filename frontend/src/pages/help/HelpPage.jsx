import { BookOpen, CircleHelp, Headset } from 'lucide-react';
import PageHeader from '../../components/PageHeader.jsx';
import NavCard from '../../components/NavCard.jsx';

export default function HelpPage() {
  return (
    <>
      <PageHeader title="Help" subtitle="Get support and resources" />
      <div className="nav-card-list">
        <NavCard to="/help/docs" icon={BookOpen} title="Documentation" description="Read user guides and API docs" />
        <NavCard to="/help/faqs" icon={CircleHelp} title="FAQs" description="Find answers to common questions" />
        <NavCard to="/help/contact" icon={Headset} title="Contact Support" description="Get in touch with our team" />
      </div>
    </>
  );
}
