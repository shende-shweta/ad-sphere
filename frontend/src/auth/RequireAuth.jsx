import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from './AuthContext.jsx';
import Spinner from '../components/Spinner.jsx';
import EmptyState from '../components/EmptyState.jsx';

/** Guards a route: redirects anonymous users to login and blocks users without a required role. */
export default function RequireAuth({ children, roles }) {
  const { user, loading, can } = useAuth();
  const location = useLocation();

  if (loading) return <Spinner fullPage label="Loading your workspace" />;
  if (!user) return <Navigate to="/login" replace state={{ from: location }} />;
  if (roles && !can(roles)) {
    return (
      <EmptyState
        title="Access restricted"
        message="Your role does not allow this action. Ask an administrator for access."
      />
    );
  }
  return children;
}
