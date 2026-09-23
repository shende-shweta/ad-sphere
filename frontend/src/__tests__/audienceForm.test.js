import { describe, expect, it } from 'vitest';
import {
  validateAudience,
  fromAudience,
  toAudiencePayload,
} from '../pages/audience/audienceForm.js';
import { integerRange } from '../utils/validators.js';

describe('validateAudience', () => {
  it('returns errors for all required fields when empty', () => {
    const errors = validateAudience({
      name: '',
      type: '',
      estimatedSize: '',
      description: '',
      status: 'ACTIVE',
    });
    expect(errors.name).toBe('Audience name is required');
    expect(errors.type).toBe('Type is required');
    expect(errors.estimatedSize).toBe('Estimated reach is required');
    expect(errors.description).toBeUndefined();
  });

  it('validates name minimum length', () => {
    const errors = validateAudience({
      name: 'Ab',
      type: 'INTEREST',
      estimatedSize: '5000',
      description: '',
    });
    expect(errors.name).toBe('Audience name must be at least 3 characters');
  });

  it('validates name maximum length', () => {
    const errors = validateAudience({
      name: 'A'.repeat(121),
      type: 'INTEREST',
      estimatedSize: '5000',
      description: '',
    });
    expect(errors.name).toBe('Audience name must be at most 120 characters');
  });

  it('validates estimatedSize range', () => {
    const errors = validateAudience({
      name: 'Valid Name',
      type: 'INTEREST',
      estimatedSize: '500',
      description: '',
    });
    expect(errors.estimatedSize).toContain('must be between');
  });

  it('validates description max length', () => {
    const errors = validateAudience({
      name: 'Valid Name',
      type: 'INTEREST',
      estimatedSize: '5000000',
      description: 'x'.repeat(501),
    });
    expect(errors.description).toBe(
      'Description must be at most 500 characters',
    );
  });

  it('passes with valid values', () => {
    const errors = validateAudience({
      name: 'Valid Name',
      type: 'INTEREST',
      estimatedSize: '5000000',
      description: 'A description',
    });
    expect(Object.keys(errors).length).toBe(0);
  });
});

describe('integerRange', () => {
  it('returns undefined for empty values', () => {
    expect(integerRange('', 1000, 10000000000, 'Test')).toBeUndefined();
    expect(integerRange(null, 1000, 10000000000, 'Test')).toBeUndefined();
  });

  it('rejects decimals', () => {
    expect(integerRange('1000.5', 1000, 10000000000, 'Test')).toBe(
      'Test must be a whole number',
    );
  });

  it('rejects non-numbers', () => {
    expect(integerRange('abc', 1000, 10000000000, 'Test')).toBe(
      'Test must be a number',
    );
  });

  it('rejects values below minimum', () => {
    expect(
      integerRange('999', 1000, 10000000000, 'Test'),
    ).toContain('must be between');
  });

  it('rejects values above maximum', () => {
    expect(
      integerRange('10000000001', 1000, 10000000000, 'Test'),
    ).toContain('must be between');
  });

  it('accepts valid values at boundaries and mid-range', () => {
    expect(integerRange('1000', 1000, 10000000000, 'Test')).toBeUndefined();
    expect(
      integerRange('5000000', 1000, 10000000000, 'Test'),
    ).toBeUndefined();
    expect(
      integerRange('10000000000', 1000, 10000000000, 'Test'),
    ).toBeUndefined();
  });
});

describe('toAudiencePayload', () => {
  it('trims name and nulls blank description', () => {
    const payload = toAudiencePayload({
      name: '  Test Name  ',
      type: 'INTEREST',
      estimatedSize: '5000000',
      description: '   ',
      status: 'ACTIVE',
    });
    expect(payload.name).toBe('Test Name');
    expect(payload.description).toBeNull();
    expect(payload.estimatedSize).toBe(5000000);
  });

  it('preserves non-blank description', () => {
    const payload = toAudiencePayload({
      name: 'Test',
      type: 'INTEREST',
      estimatedSize: '5000000',
      description: 'A real description',
      status: 'ACTIVE',
    });
    expect(payload.description).toBe('A real description');
  });

  it('includes version when present', () => {
    const payload = toAudiencePayload({
      name: 'Test',
      type: 'INTEREST',
      estimatedSize: '5000000',
      description: '',
      status: 'ACTIVE',
      version: 3,
    });
    expect(payload.version).toBe(3);
  });
});

describe('fromAudience', () => {
  it('maps response to form values', () => {
    const response = {
      id: 1,
      name: 'Tech Enthusiasts',
      type: 'INTEREST',
      description: 'Technology fans',
      status: 'ACTIVE',
      estimatedSize: 2750000,
      version: 0,
      placementCount: 3,
      createdAt: '2026-09-23T09:15:02.114Z',
      updatedAt: '2026-09-23T09:15:02.114Z',
    };
    const values = fromAudience(response);
    expect(values.name).toBe('Tech Enthusiasts');
    expect(values.type).toBe('INTEREST');
    expect(values.estimatedSize).toBe('2750000');
    expect(values.description).toBe('Technology fans');
    expect(values.status).toBe('ACTIVE');
    expect(values.version).toBe(0);
  });

  it('converts null description to empty string', () => {
    const response = {
      name: 'Test',
      type: 'INTEREST',
      description: null,
      status: 'ACTIVE',
      estimatedSize: 1000,
      version: 0,
    };
    expect(fromAudience(response).description).toBe('');
  });
});
