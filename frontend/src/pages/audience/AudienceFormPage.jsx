import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Info } from 'lucide-react';
import { audienceApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import { RadioGroup, SelectField, TextAreaField, TextField } from '../../components/FormField.jsx';
import { formatNumber } from '../../utils/format.js';
import { EMPTY_AUDIENCE, fromAudience, toAudiencePayload, validateAudience } from './audienceForm.js';

export default function AudienceFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const toast = useToast();
  const lookups = useLookups();
  const { can } = useAuth();
  const isAdmin = can([ROLES.ADMIN]);

  const form = useForm(EMPTY_AUDIENCE, validateAudience);
  const [loading, setLoading] = useState(isEdit);
  const [loadError, setLoadError] = useState(null);
  const [loaded, setLoaded] = useState(null);
  const [confirmCancel, setConfirmCancel] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    audienceApi
      .get(id)
      .then((data) => {
        const mapped = fromAudience(data);
        form.setValues(mapped);
        setLoaded(mapped);
      })
      .catch(setLoadError)
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, isEdit]);

  const isDirty = () => {
    if (!isEdit) {
      return Object.keys(EMPTY_AUDIENCE).some((k) => form.values[k] !== EMPTY_AUDIENCE[k]);
    }
    if (!loaded) return false;
    return Object.keys(EMPTY_AUDIENCE).some((k) => form.values[k] !== loaded[k]);
  };

  const hasChanges = () => {
    if (!loaded) return true;
    return Object.keys(EMPTY_AUDIENCE).some((k) => form.values[k] !== loaded[k]);
  };

  const handleCancel = () => {
    if (isDirty()) {
      setConfirmCancel(true);
    } else {
      navigate(-1);
    }
  };

  const reloadAudience = async () => {
    try {
      const data = await audienceApi.get(id);
      const mapped = fromAudience(data);
      form.setValues(mapped);
      form.setErrors({});
      setLoaded(mapped);
      toast.success('Audience reloaded');
    } catch (err) {
      toast.error(err.message);
    }
  };

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) {
      document.querySelector('[aria-invalid="true"]')?.focus();
      toast.error('Please correct the highlighted fields');
      return;
    }
    form.setSubmitting(true);
    const payload = toAudiencePayload(form.values, isEdit);
    try {
      if (isEdit) {
        await audienceApi.update(id, payload);
        toast.success('Audience updated');
      } else {
        const created = await audienceApi.create(payload);
        toast.success(`Audience "${created.name}" created`);
      }
      navigate('/audience');
    } catch (err) {
      if (err.status === 409) {
        toast.error(err.message);
        reloadAudience();
      } else {
        form.setErrors(err.fieldErrors || {});
        toast.error(err.message);
      }
      form.setSubmitting(false);
    }
  };

  if (loading) return <Spinner />;
  if (loadError) {
    return (
      <>
        <PageHeader title="Edit Audience" backTo="/audience" />
        {loadError.status === 404 ? (
          <EmptyState title="Audience not found" message="The audience you're looking for doesn't exist." />
        ) : (
          <EmptyState title="Could not load audience" message={loadError.message} />
        )}
      </>
    );
  }

  const bind = (name) => ({
    name,
    value: form.values[name],
    onChange: form.handleChange,
    onBlur: form.handleBlur,
    error: form.errors[name],
  });

  const estimatedSizeNum = Number(form.values.estimatedSize);
  const sizeHelp =
    !Number.isNaN(estimatedSizeNum) && form.values.estimatedSize !== ''
      ? `${formatNumber(estimatedSizeNum)} users`
      : 'Users, 1,000 – 10,000,000,000';

  const placementCount = loaded?.placementCount || 0;

  return (
    <>
      <PageHeader title={isEdit ? 'Edit Audience' : 'Create Audience'} backTo="/audience" />
      <form className="card form-card" onSubmit={submit} noValidate>
        {isEdit && placementCount > 0 && (
          <div className="info-banner">
            <Info size={16} />
            <span>
              <strong>Used by {placementCount} placement{placementCount !== 1 ? 's' : ''}.</strong>{' '}
              Changes apply to all of them.
              {form.values.status === 'INACTIVE' &&
                " Inactive audiences can't be picked for new placements."}
            </span>
          </div>
        )}
        <h2 className="section-title">Audience Details</h2>
        <div className="form-grid">
          <TextField
            label="Audience Name"
            required
            placeholder="e.g. Tech Enthusiasts"
            maxLength={120}
            {...bind('name')}
          />
          <SelectField
            label="Type"
            required
            placeholder="Select type"
            options={lookups.audienceTypes}
            {...bind('type')}
          />
          <TextField
            label="Estimated Reach"
            required
            type="number"
            min={1000}
            step={1}
            inputMode="numeric"
            {...bind('estimatedSize')}
            help={sizeHelp}
          />
          {isAdmin ? (
            <RadioGroup
              label="Status"
              required
              name="status"
              value={form.values.status}
              options={lookups.audienceStatuses}
              onChange={form.handleChange}
              error={form.errors.status}
            />
          ) : isEdit && loaded ? (
            <div className="field">
              <label>Status</label>
              <StatusBadge
                status={loaded.status}
                label={lookups.label('audienceStatuses', loaded.status)}
              />
            </div>
          ) : null}
          <TextAreaField
            label="Description"
            placeholder={'Who this segment targets, e.g. "Ages 18-25, all genders"'}
            maxLength={500}
            className="span-2"
            {...bind('description')}
            help={`${(form.values.description || '').length} / 500`}
          />
        </div>
        <div className="form-actions">
          <button type="button" className="btn btn-outline-primary" onClick={handleCancel}>
            Cancel
          </button>
          <button
            type="submit"
            className="btn btn-primary"
            disabled={form.submitting || (isEdit && !hasChanges())}
          >
            {form.submitting
              ? isEdit
                ? 'Saving…'
                : 'Creating…'
              : isEdit
                ? 'Save Changes'
                : 'Create Audience'}
          </button>
        </div>
      </form>
      {confirmCancel && (
        <ConfirmDialog
          title="Discard unsaved changes?"
          message="You have unsaved changes that will be lost."
          confirmLabel="Discard"
          danger
          onConfirm={() => navigate(-1)}
          onCancel={() => setConfirmCancel(false)}
        />
      )}
    </>
  );
}
