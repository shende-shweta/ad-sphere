import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { settingsApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { ROLES } from '../../auth/roles.js';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Toggle from '../../components/Toggle.jsx';
import { SelectField, TextField } from '../../components/FormField.jsx';
import NotFoundPage from '../NotFoundPage.jsx';
import { CURRENCIES, LANGUAGES, SECTIONS, TIMEZONES, validateSettings } from './sections.js';

export default function SettingsSectionPage() {
  const { section } = useParams();
  const meta = SECTIONS[section];
  const { can } = useAuth();
  const toast = useToast();
  const readOnly = !can([ROLES.ADMIN]);
  const form = useForm(null, validateSettings);
  const [loadError, setLoadError] = useState(null);

  useEffect(() => {
    settingsApi
      .get()
      .then(form.setValues)
      .catch(setLoadError);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (!meta) return <NotFoundPage />;
  if (loadError) return <EmptyState title="Could not load settings" message={loadError.message} />;
  if (!form.values) return <Spinner />;

  const { values, errors, handleChange, handleBlur } = form;
  const bind = (name) => ({ name, value: values[name] ?? '', onChange: handleChange, onBlur: handleBlur, error: errors[name], disabled: readOnly });
  const toggle = (name) => ({ name, checked: values[name], onChange: handleChange, disabled: readOnly });

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      const saved = await settingsApi.update({
        ...values,
        sessionTimeoutMinutes: Number(values.sessionTimeoutMinutes),
        passwordMinLength: Number(values.passwordMinLength),
        webhookUrl: values.webhookUrl?.trim() || null,
      });
      form.setValues(saved);
      toast.success(`${meta.title} saved`);
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      toast.error(err.message);
    } finally {
      form.setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title={meta.title} subtitle={meta.description} backTo="/settings" backLabel="Settings" />
      <form className="card form-card" onSubmit={submit} noValidate>
        {readOnly && (
          <div className="alert alert-info">Only administrators can change settings. You are viewing them read-only.</div>
        )}

        {section === 'general' && (
          <div className="form-grid">
            <TextField label="Platform Name" required maxLength={80} {...bind('platformName')} />
            <SelectField label="Timezone" options={TIMEZONES.map((t) => ({ value: t, label: t }))} {...bind('timezone')} />
            <SelectField label="Language" options={LANGUAGES} {...bind('language')} />
            <SelectField label="Currency" options={CURRENCIES.map((c) => ({ value: c, label: c }))} {...bind('currency')} />
          </div>
        )}

        {section === 'integrations' && (
          <>
            <div className="toggle-list">
              <Toggle label="Web analytics" description="Send campaign conversions to your analytics suite" {...toggle('analyticsIntegration')} />
              <Toggle label="CRM" description="Sync audiences with your CRM" {...toggle('crmIntegration')} />
              <Toggle label="Team chat" description="Post budget and delivery alerts to a chat channel" {...toggle('slackIntegration')} />
            </div>
            <TextField label="Webhook URL" placeholder="https://example.com/hooks/ads" help="Receives JSON events for campaign changes" {...bind('webhookUrl')} />
          </>
        )}

        {section === 'notifications' && (
          <div className="toggle-list">
            <Toggle label="Email notifications" description="Receive account and campaign emails" {...toggle('emailNotifications')} />
            <Toggle label="System notifications" description="Show in-app alerts" {...toggle('systemNotifications')} />
            <Toggle label="Budget alerts" description="Notify when a campaign spends 80% of its budget" {...toggle('budgetAlerts')} />
            <Toggle label="Weekly performance report" description="A summary email every Monday" {...toggle('weeklyReport')} />
          </div>
        )}

        {section === 'security' && (
          <>
            <div className="toggle-list">
              <Toggle label="Require two-factor authentication" description="All users must enroll a second factor" {...toggle('twoFactorRequired')} />
            </div>
            <div className="form-grid">
              <TextField label="Session timeout (minutes)" type="number" min="5" max="1440" {...bind('sessionTimeoutMinutes')} />
              <TextField label="Minimum password length" type="number" min="8" max="64" {...bind('passwordMinLength')} />
            </div>
          </>
        )}

        {!readOnly && (
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={form.submitting}>
              {form.submitting ? 'Saving…' : 'Save Changes'}
            </button>
          </div>
        )}
      </form>
    </>
  );
}
