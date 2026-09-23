import { useEffect, useState } from 'react';
import { audienceApi } from '../../api/services.js';
import { useLookups } from '../../context/LookupsContext.jsx';
import { RadioGroup, SelectField, TextAreaField, TextField } from '../../components/FormField.jsx';

/** Placement inputs shared by the Create Placement page and the campaign form's inline option. */
export default function PlacementFields({ form }) {
  const lookups = useLookups();
  const [audiences, setAudiences] = useState([]);
  const [audienceError, setAudienceError] = useState('');
  const { values, errors, handleChange, handleBlur } = form;

  useEffect(() => {
    audienceApi
      .list({ status: 'ACTIVE', size: 100, sort: 'name,asc' })
      .then((page) => setAudiences(page.content.map((a) => ({ value: String(a.id), label: a.name }))))
      .catch((err) => setAudienceError(err.message));
  }, []);

  const bind = (name) => ({ name, value: values[name], onChange: handleChange, onBlur: handleBlur, error: errors[name] });

  return (
    <>
      <h2 className="section-title">Placement Details</h2>
      <div className="form-grid">
        <TextField label="Placement Name" required placeholder="Enter placement name" maxLength={120} {...bind('name')} />
        <SelectField label="Country" required placeholder="Select country" options={lookups.countries} {...bind('country')} />
        <SelectField
          label="Audience Group"
          required
          placeholder={audienceError ? 'Could not load audiences' : 'Select audience group'}
          options={audiences}
          {...bind('audienceId')}
        />
        <SelectField label="Video Targeting" placeholder="Select video targeting" options={lookups.videoTargeting} {...bind('videoTargeting')} />
        <RadioGroup
          label="Traffic"
          required
          name="traffic"
          value={values.traffic}
          options={lookups.trafficTypes}
          onChange={handleChange}
          error={errors.traffic}
        />
        <SelectField label="Position of Ad" required placeholder="Select position" options={lookups.adPositions} {...bind('adPosition')} />
        <SelectField label="Deal Type" placeholder="Select deal type" options={lookups.dealTypes} {...bind('dealType')} />
      </div>

      <h2 className="section-title">Additional Settings</h2>
      <div className="form-grid">
        <TextField
          label="Frequency Capping"
          placeholder="Enter frequency cap (e.g., 3/day)"
          help="Maximum impressions per user, e.g. 3/day or 10/week"
          {...bind('frequencyCap')}
        />
        <SelectField label="Device Targeting" placeholder="Select device type" options={lookups.deviceTargeting} {...bind('deviceTargeting')} />
        <SelectField label="Ad Format" placeholder="Select ad format" options={lookups.adFormats} {...bind('adFormat')} />
        <TextAreaField label="Notes" placeholder="Enter additional notes" maxLength={1000} {...bind('notes')} />
      </div>
    </>
  );
}
