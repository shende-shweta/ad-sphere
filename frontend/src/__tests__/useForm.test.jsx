import { describe, expect, it } from 'vitest';
import { act, renderHook } from '@testing-library/react';
import { useForm } from '../hooks/useForm.js';
import { collect, dateOrder, required } from '../utils/validators.js';

const validate = (v) =>
  collect({ start: required(v.start, 'Start'), end: required(v.end, 'End') || dateOrder(v.start, v.end) });

describe('useForm', () => {
  it('does not show errors before a field is touched', () => {
    const { result } = renderHook(() => useForm({ start: '', end: '' }, validate));
    act(() => result.current.setField('start', ''));
    expect(result.current.errors.start).toBeUndefined();
  });

  it('validateAll flags every invalid field and returns false', () => {
    const { result } = renderHook(() => useForm({ start: '', end: '' }, validate));
    let ok;
    act(() => {
      ok = result.current.validateAll();
    });
    expect(ok).toBe(false);
    expect(result.current.errors).toEqual({ start: 'Start is required', end: 'End is required' });
  });

  it('re-validates dependent fields when a field changes', () => {
    const { result } = renderHook(() =>
      useForm({ start: '2025-02-01', end: '2025-01-01' }, validate, { start: ['end'] }),
    );
    act(() => {
      result.current.validateAll();
    });
    expect(result.current.errors.end).toMatch(/End date/);
    act(() => result.current.setField('start', '2024-12-01'));
    expect(result.current.errors.end).toBeUndefined();
  });
});
