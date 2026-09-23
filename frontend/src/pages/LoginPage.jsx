import { useCallback, useState } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../auth/AuthContext.jsx';
import { useForm } from '../hooks/useForm.js';
import { TextField } from '../components/FormField.jsx';
import Logo from '../components/Logo.jsx';
import { collect, required } from '../utils/validators.js';

const validate = (v) =>
  collect({ username: required(v.username, 'Username'), password: required(v.password, 'Password') });

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const form = useForm({ username: '', password: '' }, useCallback(validate, []));

  if (user) return <Navigate to={location.state?.from?.pathname || '/campaigns'} replace />;

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      await login(form.values.username.trim(), form.values.password);
      navigate(location.state?.from?.pathname || '/campaigns', { replace: true });
    } catch (err) {
      setServerError(err.message);
      form.setSubmitting(false);
    }
  };

  return (
    <div className="login-page">
      <form className="login-card card" onSubmit={submit} noValidate>
        <div className="login-brand">
          <Logo size={40} />
          <div>
            <h1>AdSphere</h1>
            <p className="subtitle">Sign in to manage your advertising</p>
          </div>
        </div>
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError}
          </div>
        )}
        <TextField
          label="Username"
          name="username"
          autoComplete="username"
          autoFocus
          value={form.values.username}
          onChange={form.handleChange}
          onBlur={form.handleBlur}
          error={form.errors.username}
          required
        />
        <div className="password-field">
          <TextField
            label="Password"
            name="password"
            type={showPassword ? 'text' : 'password'}
            autoComplete="current-password"
            value={form.values.password}
            onChange={form.handleChange}
            onBlur={form.handleBlur}
            error={form.errors.password}
            required
          />
          <button
            type="button"
            className="icon-btn password-toggle"
            aria-label={showPassword ? 'Hide password' : 'Show password'}
            onClick={() => setShowPassword((s) => !s)}
          >
            {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
          </button>
        </div>
        <button className="btn btn-primary btn-block" type="submit" disabled={form.submitting}>
          {form.submitting ? 'Signing in…' : 'Sign in'}
        </button>
        <details className="demo-accounts">
          <summary>Demo accounts</summary>
          <ul>
            <li><code>admin / Admin@123</code> — Administrator</li>
            <li><code>manager / Manager@123</code> — Campaign manager</li>
            <li><code>viewer / Viewer@123</code> — Read-only</li>
          </ul>
        </details>
      </form>
    </div>
  );
}
