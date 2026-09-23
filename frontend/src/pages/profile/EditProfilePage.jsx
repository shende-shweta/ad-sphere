import { useNavigate } from 'react-router-dom';
import { profileApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import { SelectField, TextField } from '../../components/FormField.jsx';
import { TIMEZONES } from '../settings/sections.js';
import { collect, email, maxLength, phone, required } from '../../utils/validators.js';

const validate = (v) =>
  collect({
    fullName: required(v.fullName, 'Full name') || maxLength(v.fullName, 120, 'Full name'),
    email: required(v.email, 'Email') || email(v.email),
    phone: phone(v.phone),
    jobTitle: maxLength(v.jobTitle, 120, 'Job title'),
  });

export default function EditProfilePage() {
  const { user, setUser } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const form = useForm(
    {
      fullName: user.fullName || '',
      email: user.email || '',
      phone: user.phone || '',
      jobTitle: user.jobTitle || '',
      timezone: user.timezone || 'UTC',
    },
    validate,
  );
  const bind = (name) => ({ name, value: form.values[name], onChange: form.handleChange, onBlur: form.handleBlur, error: form.errors[name] });

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      const updated = await profileApi.update({
        ...form.values,
        phone: form.values.phone.trim() || null,
        jobTitle: form.values.jobTitle.trim() || null,
      });
      setUser(updated);
      toast.success('Profile updated');
      navigate('/profile');
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      toast.error(err.message);
      form.setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="Account Settings" subtitle="Update your profile and preferences" backTo="/profile" backLabel="Profile" />
      <form className="card form-card" onSubmit={submit} noValidate>
        <div className="form-grid">
          <TextField label="Full Name" required autoComplete="name" {...bind('fullName')} />
          <TextField label="Email" required type="email" autoComplete="email" {...bind('email')} />
          <TextField label="Phone" type="tel" autoComplete="tel" placeholder="+1 555 010 0000" {...bind('phone')} />
          <TextField label="Job Title" {...bind('jobTitle')} />
          <SelectField label="Timezone" options={TIMEZONES.map((t) => ({ value: t, label: t }))} {...bind('timezone')} />
          <TextField label="Username" name="username" value={user.username} disabled help="Usernames cannot be changed" />
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-outline-primary" onClick={() => navigate('/profile')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Saving…' : 'Save Profile'}
          </button>
        </div>
      </form>
    </>
  );
}
