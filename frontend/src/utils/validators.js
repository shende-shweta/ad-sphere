/** Pure validation helpers shared by all forms. Each returns an error message or undefined. */

export const required = (value, label) =>
  value === undefined || value === null || String(value).trim() === ''
    ? `${label} is required`
    : undefined;

export const maxLength = (value, max, label) =>
  value && String(value).length > max ? `${label} must be at most ${max} characters` : undefined;

export const minLength = (value, min, label) =>
  value && String(value).length < min ? `${label} must be at least ${min} characters` : undefined;

export const moneyRange = (value, min, max, label) => {
  if (value === '' || value == null) return undefined;
  const n = Number(value);
  if (Number.isNaN(n)) return `${label} must be a number`;
  if (!/^\d+(\.\d{1,2})?$/.test(String(value).trim()))
    return `${label} may have at most 2 decimal places`;
  if (n < min) return `${label} must be at least ${min.toLocaleString('en-US')}`;
  if (n > max) return `${label} must not exceed ${max.toLocaleString('en-US')}`;
  return undefined;
};

export const integerRange = (value, min, max, label) => {
  if (value === '' || value == null) return undefined;
  const n = Number(value);
  if (Number.isNaN(n) || !Number.isInteger(n) || n < min || n > max)
    return `${label} must be between ${min.toLocaleString('en-US')} and ${max.toLocaleString('en-US')}`;
  return undefined;
};

export const dateOrder = (start, end) =>
  start && end && end < start ? 'End date must be on or after the start date' : undefined;

export const email = (value) =>
  value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value) ? 'Enter a valid email address' : undefined;

export const phone = (value) =>
  value && !/^\+?[0-9 ()-]{7,20}$/.test(value) ? 'Enter a valid phone number' : undefined;

export const frequencyCap = (value) =>
  value && !/^[1-9]\d{0,3}\/(hour|day|week|month)$/.test(value.trim())
    ? 'Use the form 3/day (hour, day, week or month)'
    : undefined;

export const httpsUrl = (value) =>
  value && !/^https:\/\/\S+$/.test(value) ? 'URL must start with https://' : undefined;

/** Builds an errors object, dropping fields without errors. */
export const collect = (entries) =>
  Object.fromEntries(Object.entries(entries).filter(([, v]) => v));
