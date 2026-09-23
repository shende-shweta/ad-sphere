import { useState } from 'react';
import { helpApi } from '../../api/services.js';
import { useToast } from '../../context/ToastContext.jsx';
import { useForm } from '../../hooks/useForm.js';
import PageHeader from '../../components/PageHeader.jsx';
import { TextAreaField, TextField } from '../../components/FormField.jsx';
import { collect, maxLength, minLength, required } from '../../utils/validators.js';

const validate = (v) =>
  collect({
    subject: required(v.subject, 'Subject') || maxLength(v.subject, 160, 'Subject'),
    message:
      required(v.message, 'Message') ||
      minLength(v.message.trim(), 10, 'Message') ||
      maxLength(v.message, 4000, 'Message'),
  });

export default function ContactSupportPage() {
  const toast = useToast();
  const form = useForm({ subject: '', message: '' }, validate);
  const [ticketId, setTicketId] = useState(null);

  const submit = async (e) => {
    e.preventDefault();
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      const res = await helpApi.createTicket(form.values);
      setTicketId(res.id);
      form.setValues({ subject: '', message: '' });
      toast.success('Support request sent');
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      toast.error(err.message);
    } finally {
      form.setSubmitting(false);
    }
  };

  return (
    <>
      <PageHeader title="Contact Support" subtitle="Get in touch with our team" backTo="/help" backLabel="Help" />
      <form className="card form-card narrow" onSubmit={submit} noValidate>
        {ticketId && (
          <div className="alert alert-success" role="status">
            Thanks! Your request #{ticketId} was received. We usually reply within one business day.
          </div>
        )}
        <TextField label="Subject" required name="subject" maxLength={160} value={form.values.subject} onChange={form.handleChange} onBlur={form.handleBlur} error={form.errors.subject} />
        <TextAreaField label="Message" required name="message" rows={6} maxLength={4000} value={form.values.message} onChange={form.handleChange} onBlur={form.handleBlur} error={form.errors.message} help="Describe what you were doing and what went wrong." />
        <div className="form-actions">
          <button type="submit" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Sending…' : 'Send Request'}
          </button>
        </div>
      </form>
    </>
  );
}
