import { useEffect, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { audienceApi } from '../../api/services.js';
import { useAuth } from '../../auth/AuthContext.jsx';
import { ROLES } from '../../auth/roles.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import StatusBadge from '../../components/StatusBadge.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';
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
  const [placementCount, setPlacementCount] = useState(0);
  const [showConfirm, setShowConfirm] = useState(false);
  const loadedRef = useRef(null);

  useEffect(() => {
    if (!isEdit) return;
    audienceApi
      .get(id)
      .then((a) => {
        const formValues = fromAudience(a);
        form.setValues(formValues);
        loadedRef.current = formValues;
        setPlacementCount(a.placementCount);
      })
      .catch(setLoadError)
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, isEdit]);

  const isDirty = () => {
    const initial = isEdit ? loadedRef.current : EMPTY_AUDIENCE;
    if (!initial) return false;
    return Object.keys(initial).some((k) => form.values[k] !== initial[k]);
  };

  const hasChanges = () => {
    if (!isEdit || !loadedRef.current) return true;
    return Object.keys(loadedRef.current).some((k) => form.values[k] !== loadedRef.current[k]);
  };

  const handleCancel = () => {
    if (isDirty()) {
      setShowConfirm(true);
    } else {
      navigate(-1);
    }
  };

  const reloadAudience = async () => {
    try {
      const a = await audienceApi.get(id);
      const formValues = fromAudience(a);
      form.setValues(formValues);
      form.setErrors({});
      loadedRef.current = formValues;
      setPlacementCount(a.placementCount);
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
    try {
      if (isEdit) {
        await audienceApi.update(id, toAudiencePayload(form.values));
        toast.success('Audience updated');
      } else {
        const saved = await audienceApi.create(toAudiencePayload(form.values));
        toast.success(`Audience "${saved.name}" created`);
      }
      navigate('/audience');
    } catch (err) {
      if (err.status === 409) {
        toast.error(err.message);
      } else {
        form.setErrors(err.fieldErrors || {});
        toast.error(err.message);
      }
      form.setSubmitting(false);
    }
  };

  if (loading) return <Spinner />;
  if (loadError)
    return <EmptyState title="Could not load audience" message={loadError.message} />;

  const bind = (name) => ({
    name,
    value: form.values[name],
    onChange: form.handleChange,
    onBlur: form.handleBlur,
    error: form.errors[name],
  });

  const reachValue = form.values.estimatedSize;
  const reachNum = reachValue ? Number(reachValue) : null;
  const reachFormatted =
    reachNum && Number.isFinite(reachNum) && reachNum > 0
      ? `${formatNumber(reachNum)} users`
      : null;

  return (
    <>
      <PageHeader
        title={isEdit ? 'Edit Audience' : 'Create Audience'}
        backTo="/audience"
      />
      <form className="card form-card" onSubmit={submit} noValidate>
        {isEdit && placementCount > 0 && (
          <div className="alert alert-info">
            <strong>Used by {placementCount} placement(s).</strong> Changes apply
            to all of them.
            {isAdmin && form.values.status === 'INACTIVE' && (
              <> Inactive audiences can&apos;t be picked for new placements.</>
            )}
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
            help={
              reachFormatted
                ? <><strong>{reachFormatted}</strong><br />Users, 1,000 &ndash; 10,000,000,000</>
                : 'Users, 1,000 – 10,000,000,000'
            }
            {...bind('estimatedSize')}
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
          ) : isEdit ? (
            <div className="field">
              <label>Status</label>
              <StatusBadge
                status={form.values.status}
                label={lookups.label('audienceStatuses', form.values.status)}
              />
            </div>
          ) : null}
          <TextAreaField
            label="Description"
            placeholder='Who this segment targets, e.g. "Ages 18-25, all genders"'
            maxLength={500}
            className="span-2"
            {...bind('description')}
          />
        </div>
        <div className="form-actions">
          <button
            type="button"
            className="btn btn-outline-primary"
            onClick={handleCancel}
          >
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
      {showConfirm && (
        <ConfirmDialog
          title="Discard unsaved changes?"
          message="You have unsaved changes. Are you sure you want to leave?"
          confirmLabel="Discard"
          danger
          onConfirm={() => navigate(-1)}
          onCancel={() => setShowConfirm(false)}
        />
      )}
    </>
  );
}
