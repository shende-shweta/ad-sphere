import { useCallback, useEffect, useState } from 'react';

/**
 * Fetches a paged list whenever `params` change. `fetcher(params, signal)` must return the
 * backend PageResponse shape: { content, page, size, totalElements, totalPages }.
 */
export function usePagedList(fetcher, params) {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [reloadKey, setReloadKey] = useState(0);
  const key = JSON.stringify(params);

  useEffect(() => {
    const controller = new AbortController();
    setLoading(true);
    setError(null);
    fetcher(JSON.parse(key), controller.signal)
      .then(setData)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err);
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false);
      });
    return () => controller.abort();
  }, [fetcher, key, reloadKey]);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);
  return { data, error, loading, reload, setData };
}
