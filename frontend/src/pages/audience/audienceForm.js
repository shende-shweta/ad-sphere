import { collect, maxLength, required } from '../../utils/validators.js';
import { integerRange } from '../../utils/validators.js';

export const EMPTY_AUDIENCE = {
  name: '',
  type: '',
  description: '',
  estimatedSize: '',
  status: 'ACTIVE',
};

export const validateAudience = (v) => {
  const trimmedName = v.name ? v.name.trim() : '';
  return collect({
    name:
      required(v.name, 'Audience name') ||
      (trimmedName.length < 3 || trimmedName.length > 120
        ? 'Audience name must be 3-120 characters'
        : undefined),
    type: required(v.type, 'Type'),
    estimatedSize:
      required(v.estimatedSize, 'Estimated reach') ||
      integerRange(v.estimatedSize, 1000, 10000000000, 'Estimated reach'),
    description: maxLength(v.description, 500, 'Description'),
  });
};

export const fromAudience = (data) => ({
  name: data.name,
  type: data.type,
  description: data.description || '',
  estimatedSize: String(data.estimatedSize),
  status: data.status,
  version: data.version,
  placementCount: data.placementCount,
});

export const toAudiencePayload = (values, isEdit) => {
  const payload = {
    name: values.name.trim(),
    type: values.type,
    description: values.description?.trim() || null,
    estimatedSize: Number(values.estimatedSize),
    status: values.status || null,
  };
  if (isEdit) {
    payload.version = values.version;
  }
  return payload;
};
