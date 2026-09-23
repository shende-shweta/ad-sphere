import { describe, expect, it } from 'vitest';
import { dateOrder, email, frequencyCap, moneyRange, required } from '../utils/validators.js';
import { validateCampaign } from '../pages/campaigns/campaignForm.js';
import { toPlacementPayload, validatePlacement, nestedErrors } from '../pages/placements/placementForm.js';

describe('validators', () => {
  it('required treats blank strings as missing', () => {
    expect(required('  ', 'Name')).toBe('Name is required');
    expect(required('x', 'Name')).toBeUndefined();
    expect(required(0, 'Count')).toBeUndefined();
  });

  it('moneyRange enforces bounds and two decimals', () => {
    expect(moneyRange('99.99', 100, 1000, 'Budget')).toMatch(/at least 100/);
    expect(moneyRange('100.123', 100, 1000, 'Budget')).toMatch(/2 decimal/);
    expect(moneyRange('abc', 100, 1000, 'Budget')).toMatch(/number/);
    expect(moneyRange('500.50', 100, 1000, 'Budget')).toBeUndefined();
  });

  it('dateOrder allows same day and rejects reversed ranges', () => {
    expect(dateOrder('2025-01-01', '2025-01-01')).toBeUndefined();
    expect(dateOrder('2025-02-01', '2025-01-01')).toMatch(/End date/);
  });

  it('email and frequencyCap patterns', () => {
    expect(email('a@b.co')).toBeUndefined();
    expect(email('nope')).toBeDefined();
    expect(frequencyCap('3/day')).toBeUndefined();
    expect(frequencyCap('0/day')).toBeDefined();
    expect(frequencyCap('3 per day')).toBeDefined();
  });
});

describe('campaign form', () => {
  const valid = {
    name: 'Summer',
    objective: 'REACH',
    startDate: '2025-05-01',
    endDate: '2025-05-31',
    budget: '5000',
    status: 'DRAFT',
    description: '',
  };

  it('accepts a complete campaign', () => {
    expect(validateCampaign(valid)).toEqual({});
  });

  it('reports every missing required field', () => {
    const errors = validateCampaign({ ...valid, name: '', objective: '', budget: '', startDate: '', endDate: '' });
    expect(Object.keys(errors).sort()).toEqual(['budget', 'endDate', 'name', 'objective', 'startDate']);
  });
});

describe('placement form', () => {
  it('requires country, audience, traffic and position', () => {
    const errors = validatePlacement({ name: 'P', country: '', audienceId: '', traffic: '', adPosition: '', frequencyCap: '', notes: '' });
    expect(Object.keys(errors).sort()).toEqual(['adPosition', 'audienceId', 'country', 'traffic']);
  });

  it('converts empty strings to null in the payload', () => {
    const payload = toPlacementPayload({ name: ' P ', audienceId: '3', dealType: '', notes: '' });
    expect(payload).toEqual({ name: 'P', audienceId: 3, dealType: null, notes: null });
  });

  it('maps nested server errors', () => {
    expect(nestedErrors({ name: 'x', 'newPlacement.country': 'Country is required' }, 'newPlacement')).toEqual({
      country: 'Country is required',
    });
  });
});
