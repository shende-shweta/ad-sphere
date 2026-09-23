import { collect, integerRange, maxLength, minLength, required } from '../../utils/validators.js';

export const EMPTY_AUDIENCE = {
  name: '',
  type: '',
  estimatedSize: '',
  description: '',
  status: 'ACTIVE',
};

export const validateAudience = (v) =>
  collect({
    name:
      required(v.name, 'Audience name') ||
      minLength(v.name, 3, 'Audience name') ||
      maxLength(v.name, 120, 'Audience name'),
    type: required(v.type, 'Type'),
    estimatedSize:
      required(v.estimatedSize, 'Estimated reach') ||
      integerRange(v.estimatedSize, 1000, 10000000000, 'Estimated reach'),
    description: maxLength(v.description, 500, 'Description'),
  });

export const fromAudience = (a) => ({
  name: a.name,
  type: a.type,
  estimatedSize: String(a.estimatedSize),
  description: a.description || '',
  status: a.status,
  version: a.version,
});

export const toAudiencePayload = (values) => ({
  name: values.name.trim(),
  type: values.type,
  estimatedSize: parseInt(values.estimatedSize, 10),
  description: values.description.trim() || null,
  status: values.status || null,
  version: values.version != null ? Number(values.version) : undefined,
});
