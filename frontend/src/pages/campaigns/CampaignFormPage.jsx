import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { campaignApi, placementApi } from '../../api/services.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import MultiSelect from '../../components/MultiSelect.jsx';
import Spinner from '../../components/Spinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import { Field, RadioGroup, SelectField, TextAreaField, TextField } from '../../components/FormField.jsx';
import PlacementFields from '../placements/PlacementFields.jsx';
import { EMPTY_PLACEMENT, nestedErrors, toPlacementPayload, validatePlacement } from '../placements/placementForm.js';
import { CAMPAIGN_DEPENDENCIES, EMPTY_CAMPAIGN, fromCampaign, validateCampaign } from './campaignForm.js';

const PLACEMENT_MODES = [
  { value: 'existing', label: 'Select Existing Placements' },
  { value: 'new', label: 'Create New Placement' },
];

export default function CampaignFormPage() {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const toast = useToast();
  const lookups = useLookups();

  const form = useForm(EMPTY_CAMPAIGN, validateCampaign, CAMPAIGN_DEPENDENCIES);
  const placementForm = useForm(EMPTY_PLACEMENT, validatePlacement);
  const [mode, setMode] = useState('existing');
  const [placementIds, setPlacementIds] = useState([]);
  const [placements, setPlacements] = useState([]);
  const [placementsLoading, setPlacementsLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);
  const [loading, setLoading] = useState(isEdit);

  useEffect(() => {
    placementApi
      .list({ size: 100, sort: 'name,asc' })
      .then((page) =>
        setPlacements(
          page.content.map((p) => ({
            value: p.id,
            label: p.name,
            hint: `${lookups.label('countries', p.country)} · ${lookups.label('trafficTypes', p.traffic)} · ${p.audienceName}`,
          })),
        ),
      )
      .catch((err) => toast.error(err.message))
      .finally(() => setPlacementsLoading(false));
  }, [lookups, toast]);

  useEffect(() => {
    if (!isEdit) return;
    campaignApi
      .get(id)
      .then((c) => {
        form.setValues(fromCampaign(c));
        setPlacementIds(c.placements.map((p) => p.id));
      })
      .catch(setLoadError)
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id, isEdit]);

  const submit = async (e) => {
    e.preventDefault();
    const campaignOk = form.validateAll();
    const placementOk = mode === 'new' ? placementForm.validateAll() : true;
    if (!campaignOk || !placementOk) {
      document.querySelector('[aria-invalid="true"]')?.focus();
      return;
    }
    form.setSubmitting(true);
    const payload = {
      ...form.values,
      name: form.values.name.trim(),
      budget: Number(form.values.budget),
      description: form.values.description.trim() || null,
      placementIds,
      newPlacement: mode === 'new' ? toPlacementPayload(placementForm.values) : null,
    };
    try {
      const saved = isEdit ? await campaignApi.update(id, payload) : await campaignApi.create(payload);
      toast.success(isEdit ? 'Campaign updated' : `Campaign "${saved.name}" created (${saved.code})`);
      navigate('/campaigns');
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      placementForm.setErrors(nestedErrors(err.fieldErrors, 'newPlacement'));
      toast.error(err.message);
      form.setSubmitting(false);
    }
  };

  if (loading) return <Spinner />;
  if (loadError) return <EmptyState title="Could not load campaign" message={loadError.message} />;

  const bind = (name) => ({
    name,
    value: form.values[name],
    onChange: form.handleChange,
    onBlur: form.handleBlur,
    error: form.errors[name],
  });

  return (
    <>
      <PageHeader title={isEdit ? 'Edit Campaign' : 'Create Campaign'} backTo="/campaigns" />
      <form className="card form-card" onSubmit={submit} noValidate>
        <h2 className="section-title">Campaign Details</h2>
        <div className="form-grid">
          <TextField label="Campaign Name" required placeholder="Enter campaign name" maxLength={120} {...bind('name')} />
          <SelectField label="Objective" required placeholder="Select objective" options={lookups.objectives} {...bind('objective')} />
          <TextField label="Start Date" required type="date" {...bind('startDate')} />
          <TextField label="End Date" required type="date" min={form.values.startDate || undefined} {...bind('endDate')} />
          <TextField label="Budget" required type="number" min="100" step="0.01" inputMode="decimal" placeholder="Enter budget" help="USD, minimum 100" {...bind('budget')} />
          <SelectField label="Status" required options={lookups.campaignStatuses} {...bind('status')} />
          <TextAreaField label="Description" placeholder="Optional notes about this campaign" maxLength={1000} className="span-2" {...bind('description')} />
        </div>

        <h2 className="section-title">Assign Placements</h2>
        <RadioGroup name="placementMode" value={mode} options={PLACEMENT_MODES} onChange={(e) => setMode(e.target.value)} />

        {mode === 'existing' ? (
          <Field label="Select Placements" error={form.errors.placementIds} help="You can assign multiple placements">
            {(a11y) => (
              <MultiSelect
                id={a11y.id}
                options={placements}
                value={placementIds}
                onChange={setPlacementIds}
                loading={placementsLoading}
                placeholder="Search and select placements"
                error={form.errors.placementIds}
              />
            )}
          </Field>
        ) : (
          <div className="nested-form">
            {placementIds.length > 0 && (
              <p className="field-help">
                The new placement will be assigned together with the {placementIds.length} selected existing placement(s).
              </p>
            )}
            <PlacementFields form={placementForm} />
          </div>
        )}

        <div className="form-actions">
          <button type="button" className="btn btn-outline-primary" onClick={() => navigate(-1)}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Saving…' : isEdit ? 'Save Changes' : 'Save Campaign'}
          </button>
        </div>
      </form>
    </>
  );
}
