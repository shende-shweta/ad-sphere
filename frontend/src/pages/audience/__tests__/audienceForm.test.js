import {
  validateAudience,
  fromAudience,
  toAudiencePayload,
  EMPTY_AUDIENCE,
} from '../audienceForm.js';
import { integerRange } from '../../../utils/validators.js';

// ---------------------------------------------------------------------------
// EMPTY_AUDIENCE
// ---------------------------------------------------------------------------

describe('EMPTY_AUDIENCE', () => {
  it('defaults status to ACTIVE', () => {
    expect(EMPTY_AUDIENCE.status).toBe('ACTIVE');
  });

  it('has all input fields as empty strings', () => {
    expect(EMPTY_AUDIENCE.name).toBe('');
    expect(EMPTY_AUDIENCE.type).toBe('');
    expect(EMPTY_AUDIENCE.estimatedSize).toBe('');
    expect(EMPTY_AUDIENCE.description).toBe('');
  });
});

// ---------------------------------------------------------------------------
// validateAudience
// ---------------------------------------------------------------------------

describe('validateAudience', () => {
  const valid = {
    name: 'Valid Audience Name',
    type: 'DEMOGRAPHIC',
    estimatedSize: '5000000',
    description: 'A short description',
    status: 'ACTIVE',
  };

  it('returns empty object for valid input', () => {
    expect(Object.keys(validateAudience(valid))).toHaveLength(0);
  });

  // --- name ---

  it('requires name', () => {
    expect(validateAudience({ ...valid, name: '' }).name).toBe(
      'Audience name is required',
    );
  });

  it('rejects trimmed name shorter than 3 characters', () => {
    expect(validateAudience({ ...valid, name: '  ab  ' }).name).toBe(
      'Audience name must be 3-120 characters',
    );
  });

  it('rejects name longer than 120 characters', () => {
    expect(validateAudience({ ...valid, name: 'a'.repeat(121) }).name).toBe(
      'Audience name must be 3-120 characters',
    );
  });

  it('accepts name exactly 3 characters after trim', () => {
    expect(validateAudience({ ...valid, name: 'abc' }).name).toBeUndefined();
  });

  it('accepts name exactly 120 characters', () => {
    expect(validateAudience({ ...valid, name: 'a'.repeat(120) }).name).toBeUndefined();
  });

  // --- type ---

  it('requires type', () => {
    expect(validateAudience({ ...valid, type: '' }).type).toBe('Type is required');
  });

  // --- estimatedSize ---

  it('requires estimatedSize', () => {
    expect(validateAudience({ ...valid, estimatedSize: '' }).estimatedSize).toBe(
      'Estimated reach is required',
    );
  });

  it('rejects estimatedSize below 1,000', () => {
    expect(
      validateAudience({ ...valid, estimatedSize: '999' }).estimatedSize,
    ).toBe('Estimated reach must be between 1,000 and 10,000,000,000');
  });

  it('rejects estimatedSize above 10 billion', () => {
    expect(
      validateAudience({ ...valid, estimatedSize: '10000000001' }).estimatedSize,
    ).toBe('Estimated reach must be between 1,000 and 10,000,000,000');
  });

  it('rejects decimal estimatedSize', () => {
    expect(
      validateAudience({ ...valid, estimatedSize: '5000.5' }).estimatedSize,
    ).toBe('Estimated reach must be between 1,000 and 10,000,000,000');
  });

  it('accepts estimatedSize at minimum boundary', () => {
    expect(
      validateAudience({ ...valid, estimatedSize: '1000' }).estimatedSize,
    ).toBeUndefined();
  });

  it('accepts estimatedSize at maximum boundary', () => {
    expect(
      validateAudience({ ...valid, estimatedSize: '10000000000' }).estimatedSize,
    ).toBeUndefined();
  });

  // --- description ---

  it('rejects description over 500 characters', () => {
    expect(
      validateAudience({ ...valid, description: 'x'.repeat(501) }).description,
    ).toBe('Description must be at most 500 characters');
  });

  it('accepts description of exactly 500 characters', () => {
    expect(
      validateAudience({ ...valid, description: 'x'.repeat(500) }).description,
    ).toBeUndefined();
  });

  it('accepts empty description', () => {
    expect(
      validateAudience({ ...valid, description: '' }).description,
    ).toBeUndefined();
  });
});

// ---------------------------------------------------------------------------
// integerRange
// ---------------------------------------------------------------------------

describe('integerRange', () => {
  const label = 'Estimated reach';
  const min = 1000;
  const max = 10000000000;
  const msg = 'Estimated reach must be between 1,000 and 10,000,000,000';

  it('rejects decimal values', () => {
    expect(integerRange(1500.5, min, max, label)).toBe(msg);
  });

  it('rejects non-numeric strings', () => {
    expect(integerRange('abc', min, max, label)).toBe(msg);
  });

  it('rejects values below minimum', () => {
    expect(integerRange(999, min, max, label)).toBe(msg);
  });

  it('rejects values above maximum', () => {
    expect(integerRange(10000000001, min, max, label)).toBe(msg);
  });

  it('accepts valid integer within range', () => {
    expect(integerRange(5000, min, max, label)).toBeUndefined();
  });

  it('accepts minimum boundary', () => {
    expect(integerRange(1000, min, max, label)).toBeUndefined();
  });

  it('accepts maximum boundary', () => {
    expect(integerRange(10000000000, min, max, label)).toBeUndefined();
  });

  it('returns undefined for empty string', () => {
    expect(integerRange('', min, max, label)).toBeUndefined();
  });

  it('returns undefined for null', () => {
    expect(integerRange(null, min, max, label)).toBeUndefined();
  });
});

// ---------------------------------------------------------------------------
// toAudiencePayload
// ---------------------------------------------------------------------------

describe('toAudiencePayload', () => {
  const base = {
    name: '  Test Name  ',
    type: 'INTEREST',
    description: 'A description',
    estimatedSize: '5000000',
    status: 'ACTIVE',
    version: 2,
  };

  it('trims the name', () => {
    expect(toAudiencePayload(base, false).name).toBe('Test Name');
  });

  it('converts estimatedSize to number', () => {
    const p = toAudiencePayload(base, false);
    expect(p.estimatedSize).toBe(5000000);
    expect(typeof p.estimatedSize).toBe('number');
  });

  it('preserves non-blank description', () => {
    expect(toAudiencePayload(base, false).description).toBe('A description');
  });

  it('converts blank description to null', () => {
    expect(
      toAudiencePayload({ ...base, description: '   ' }, false).description,
    ).toBeNull();
  });

  it('converts empty description to null', () => {
    expect(
      toAudiencePayload({ ...base, description: '' }, false).description,
    ).toBeNull();
  });

  it('includes version when isEdit is true', () => {
    expect(toAudiencePayload(base, true).version).toBe(2);
  });

  it('omits version when isEdit is false', () => {
    expect(toAudiencePayload(base, false)).not.toHaveProperty('version');
  });

  it('passes status through', () => {
    expect(toAudiencePayload(base, false).status).toBe('ACTIVE');
  });

  it('sends null status when status is empty', () => {
    expect(
      toAudiencePayload({ ...base, status: '' }, false).status,
    ).toBeNull();
  });
});

// ---------------------------------------------------------------------------
// fromAudience
// ---------------------------------------------------------------------------

describe('fromAudience', () => {
  it('maps API response to form values with string estimatedSize', () => {
    const data = {
      id: 2,
      name: 'Tech Enthusiasts',
      type: 'INTEREST',
      description: 'Technology, Gadgets, Gaming',
      status: 'ACTIVE',
      estimatedSize: 2750000,
      version: 0,
      placementCount: 3,
      createdAt: '2026-09-23T09:15:02.114Z',
      updatedAt: '2026-09-23T09:15:02.114Z',
    };
    const result = fromAudience(data);
    expect(result.name).toBe('Tech Enthusiasts');
    expect(result.type).toBe('INTEREST');
    expect(result.description).toBe('Technology, Gadgets, Gaming');
    expect(result.estimatedSize).toBe('2750000');
    expect(result.status).toBe('ACTIVE');
    expect(result.version).toBe(0);
    expect(result.placementCount).toBe(3);
  });

  it('defaults null description to empty string', () => {
    const result = fromAudience({
      name: 'No Desc',
      type: 'LOCATION',
      description: null,
      status: 'ACTIVE',
      estimatedSize: 1000,
      version: 0,
      placementCount: 0,
    });
    expect(result.description).toBe('');
  });

  it('preserves version and placementCount for edit mode', () => {
    const result = fromAudience({
      name: 'Test',
      type: 'DEVICE',
      description: '',
      status: 'INACTIVE',
      estimatedSize: 99000,
      version: 5,
      placementCount: 12,
    });
    expect(result.version).toBe(5);
    expect(result.placementCount).toBe(12);
  });
});
