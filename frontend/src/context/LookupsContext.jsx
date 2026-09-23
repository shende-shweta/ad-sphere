import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { lookupApi } from '../api/services.js';
import Spinner from '../components/Spinner.jsx';
import EmptyState from '../components/EmptyState.jsx';

const LookupsContext = createContext(null);

/** Loads enum values/labels once so every select and badge uses the backend's vocabulary. */
export function LookupsProvider({ children }) {
  const [lookups, setLookups] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    lookupApi.all().then(setLookups).catch(setError);
  }, []);

  const value = useMemo(() => {
    if (!lookups) return null;
    const labels = {};
    Object.entries(lookups).forEach(([key, options]) => {
      labels[key] = Object.fromEntries(options.map((o) => [o.value, o.label]));
    });
    return { ...lookups, label: (key, value) => labels[key]?.[value] ?? value ?? '—' };
  }, [lookups]);

  if (error) return <EmptyState title="Could not load the application" message={error.message} />;
  if (!value) return <Spinner fullPage label="Loading" />;
  return <LookupsContext.Provider value={value}>{children}</LookupsContext.Provider>;
}

export function useLookups() {
  const ctx = useContext(LookupsContext);
  if (!ctx) throw new Error('useLookups must be used inside LookupsProvider');
  return ctx;
}
