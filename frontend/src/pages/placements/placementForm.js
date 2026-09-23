import { collect, frequencyCap, maxLength, required } from '../../utils/validators.js';

export const EMPTY_PLACEMENT = {
  name: '',
  country: '',
  audienceId: '',
  videoTargeting: '',
  traffic: 'APP',
  adPosition: '',
  dealType: '',
  frequencyCap: '',
  deviceTargeting: '',
  adFormat: '',
  notes: '',
};

export const validatePlacement = (v) =>
  collect({
    name: required(v.name, 'Placement name') || maxLength(v.name, 120, 'Placement name'),
    country: required(v.country, 'Country'),
    audienceId: required(v.audienceId, 'Audience group'),
    traffic: required(v.traffic, 'Traffic'),
    adPosition: required(v.adPosition, 'Position of ad'),
    frequencyCap: frequencyCap(v.frequencyCap),
    notes: maxLength(v.notes, 1000, 'Notes'),
  });

/** Converts form values to the API payload (empty strings become null). */
export const toPlacementPayload = (v) => {
  const out = {};
  Object.entries(v).forEach(([k, val]) => {
    const trimmed = typeof val === 'string' ? val.trim() : val;
    out[k] = trimmed === '' ? null : trimmed;
  });
  out.audienceId = v.audienceId ? Number(v.audienceId) : null;
  return out;
};

/** Extracts "newPlacement.x" server errors into { x: message }. */
export const nestedErrors = (fieldErrors, prefix) =>
  Object.fromEntries(
    Object.entries(fieldErrors || {})
      .filter(([k]) => k.startsWith(`${prefix}.`))
      .map(([k, v]) => [k.slice(prefix.length + 1), v]),
  );
