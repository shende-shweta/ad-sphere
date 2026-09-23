import { useCallback, useEffect, useState } from 'react';
import { campaignApi, dealApi } from '../../api/services.js';
import { useForm } from '../../hooks/useForm.js';
import Modal from '../../components/Modal.jsx';
import { SelectField, TextField } from '../../components/FormField.jsx';
import { formatNumber, formatPrice } from '../../utils/format.js';
import { collect, moneyRange, required } from '../../utils/validators.js';

const validate = (v) =>
  collect({
    campaignId: required(v.campaignId, 'Campaign'),
    amount: required(v.amount, 'Bid amount') || moneyRange(v.amount, 0.01, 99999999, 'Bid amount'),
  });

/** Places a CPM bid on a deal. A bid at or above the floor price wins the deal immediately. */
export default function BidModal({ deal, onClose, onResult }) {
  const [campaigns, setCampaigns] = useState([]);
  const [serverError, setServerError] = useState('');
  const form = useForm({ campaignId: '', amount: String(deal.bidPrice) }, useCallback(validate, []));

  useEffect(() => {
    campaignApi
      .list({ size: 100, sort: 'name,asc' })
      .then((page) =>
        setCampaigns(
          page.content
            .filter((c) => c.status !== 'COMPLETED')
            .map((c) => ({ value: String(c.id), label: `${c.name} (${c.code})` })),
        ),
      )
      .catch((err) => setServerError(err.message));
  }, []);

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    if (!form.validateAll()) return;
    form.setSubmitting(true);
    try {
      const result = await dealApi.bid(deal.id, {
        amount: Number(form.values.amount),
        campaignId: Number(form.values.campaignId),
      });
      onResult(result);
    } catch (err) {
      form.setErrors(err.fieldErrors || {});
      setServerError(err.message);
      form.setSubmitting(false);
    }
  };

  const belowFloor = Number(form.values.amount) < Number(deal.bidPrice);

  return (
    <Modal
      title={`Bid on ${deal.name}`}
      onClose={onClose}
      footer={
        <>
          <button type="button" className="btn btn-outline" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" form="bid-form" className="btn btn-primary" disabled={form.submitting}>
            {form.submitting ? 'Placing bid…' : 'Place Bid'}
          </button>
        </>
      }
    >
      <form id="bid-form" onSubmit={submit} noValidate>
        <dl className="detail-grid compact">
          <div>
            <dt>Publisher</dt>
            <dd>{deal.publisher}</dd>
          </div>
          <div>
            <dt>Floor price (CPM)</dt>
            <dd>{formatPrice(deal.bidPrice)}</dd>
          </div>
          <div>
            <dt>Available impressions</dt>
            <dd>{formatNumber(deal.availableImpressions)}</dd>
          </div>
        </dl>
        {serverError && (
          <div className="alert alert-error" role="alert">
            {serverError}
          </div>
        )}
        <SelectField
          label="Campaign"
          required
          name="campaignId"
          placeholder="Select the campaign to serve"
          options={campaigns}
          value={form.values.campaignId}
          onChange={form.handleChange}
          onBlur={form.handleBlur}
          error={form.errors.campaignId}
        />
        <TextField
          label="Bid amount (CPM, USD)"
          required
          name="amount"
          type="number"
          min="0.01"
          step="0.01"
          inputMode="decimal"
          value={form.values.amount}
          onChange={form.handleChange}
          onBlur={form.handleBlur}
          error={form.errors.amount}
          help={
            belowFloor
              ? 'This bid is below the floor price and will be rejected.'
              : 'Bids at or above the floor price win the deal immediately.'
          }
        />
      </form>
    </Modal>
  );
}
