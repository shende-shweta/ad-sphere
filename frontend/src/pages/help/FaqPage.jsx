import { useEffect, useMemo, useState } from 'react';
import { helpApi } from '../../api/services.js';
import PageHeader from '../../components/PageHeader.jsx';
import SearchInput from '../../components/SearchInput.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';

export default function FaqPage() {
  const [faqs, setFaqs] = useState(null);
  const [error, setError] = useState(null);
  const [search, setSearch] = useState('');

  useEffect(() => {
    helpApi.faqs().then(setFaqs).catch(setError);
  }, []);

  const grouped = useMemo(() => {
    const q = search.trim().toLowerCase();
    const list = (faqs || []).filter(
      (f) => !q || f.question.toLowerCase().includes(q) || f.answer.toLowerCase().includes(q),
    );
    return list.reduce((acc, f) => ({ ...acc, [f.category]: [...(acc[f.category] || []), f] }), {});
  }, [faqs, search]);

  return (
    <>
      <PageHeader
        title="FAQs"
        subtitle="Find answers to common questions"
        backTo="/help"
        backLabel="Help"
        actions={<SearchInput value={search} onChange={setSearch} placeholder="Search FAQs..." />}
      />
      {error ? (
        <EmptyState title="Could not load FAQs" message={error.message} />
      ) : !faqs ? (
        <Spinner />
      ) : Object.keys(grouped).length === 0 ? (
        <EmptyState title="No matching questions" message="Try another search or contact support." />
      ) : (
        Object.entries(grouped).map(([category, items]) => (
          <section key={category} className="card">
            <h2 className="section-title">{category}</h2>
            {items.map((f) => (
              <details key={f.question} className="faq">
                <summary>{f.question}</summary>
                <p>{f.answer}</p>
              </details>
            ))}
          </section>
        ))
      )}
    </>
  );
}
