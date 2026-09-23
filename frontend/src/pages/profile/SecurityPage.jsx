import { profileApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import { TextField } from '../../components/FormField.jsx';
import { formatDateTime } from '../../utils/format.js';
import { collect, required } from '../../utils/validators.js';

const EMPTY = { currentPassword: '', newPassword: '', confirmPassword: '' };

const validate = (v) =>
  collect({
    currentPassword: required(v.currentPassword, 'Current password'),
    newPassword:
      required(v.newPassword, 'New password') ||
      (v.newPassword.length < 8 ? 'Password must be at least 8 characters' : undefined) ||
      (!/[A-Za-z]/.test(v.newPassword) || !/\d/.test(v.newPassword)
        ? 'Password must contain at least one letter and one number'
        : undefined),
    confirmPassword:
      required(v.confirmPassword, 'Confirmation') ||
      (v.confirmPassword !== v.newPassword ? 'Passwords do not match' : undefined),
  });

export default function SecurityPage() {
  const { user } = useAuth();
  const toast = useToast();
  const form = useForm(EMPTY, validate, { newPassword: ['confirmPassword'] });
  const bind = (name) => ({ name, type: 'password', value: form.values[name], onChange: form.handleChange, onBlur: form.handleBlur, error: form.errors[name] });

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      await profileApi.changePassword({
        currentPassword: form.values.currentPassword,
        newPassword: form.values.newPassword,
      });
      form.setValues(EMPTY);
      form.setErrors({});
      toast.success('Password changed');
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      toast.error(err.message);
    } finally {
      form.setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="Security" subtitle="Password, 2FA, login activity" backTo="/profile" backLabel="Profile" />
      <section className="card">
        <h2 className="section-title">Login activity</h2>
        <dl className="detail-grid">
          <div>
            <dt>Last sign-in</dt>
            <dd>{formatDateTime(user.lastLoginAt)}</dd>
          </div>
          <div>
            <dt>Password last changed</dt>
            <dd>{formatDateTime(user.passwordChangedAt)}</dd>
          </div>
        </dl>
      </section>
      <form className="card form-card narrow" onSubmit={submit} noValidate>
        <h2 className="section-title">Change password</h2>
        <TextField label="Current Password" required autoComplete="current-password" {...bind('currentPassword')} />
        <TextField label="New Password" required autoComplete="new-password" help="At least 8 characters with a letter and a number" {...bind('newPassword')} />
        <TextField label="Confirm New Password" required autoComplete="new-password" {...bind('confirmPassword')} />
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Updating…' : 'Update Password'}
          </button>
        </div>
      </form>
    </>
  );
}
