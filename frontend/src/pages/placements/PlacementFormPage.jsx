import { useNavigate } from 'react-router-dom';
import { placementApi } from '../../api/services.js';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import PlacementFields from './PlacementFields.jsx';
import { EMPTY_PLACEMENT, toPlacementPayload, validatePlacement } from './placementForm.js';

export default function PlacementFormPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const form = useForm(EMPTY_PLACEMENT, validatePlacement);

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      const created = await placementApi.create(toPlacementPayload(form.values));
      toast.success(`Placement "${created.name}" created`);
      navigate('/campaigns');
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      toast.error(err.message);
      form.setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="Create Placement" backTo="/campaigns" />
      <form className="card form-card" onSubmit={submit} noValidate>
        <PlacementFields form={form} />
        <div className="form-actions">
          <button type="button" className="btn btn-outline-primary" onClick={() => navigate(-1)}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Saving…' : 'Save Placement'}
          </button>
        </div>
      </form>
    </>
  );
}
