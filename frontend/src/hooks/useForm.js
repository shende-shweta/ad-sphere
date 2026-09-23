import { useCallback, useRef, useState } from 'react';

/**
 * Minimal form state manager.
 *
 * - validate(values) returns { field: message } for invalid fields.
 * - A field is validated on blur, then live on every change once it was touched or has an error.
 * - dependencies maps a field to other fields to re-validate when it changes
 *   (e.g. { startDate: ['endDate'] }).
 */
export function useForm(initialValues, validate, dependencies = {}) {
  const [values, setValuesState] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const valuesRef = useRef(initialValues);
  const touchedRef = useRef({});

  const setValues = useCallback((next) => {
    valuesRef.current = next;
    setValuesState(next);
  }, []);

  const setField = useCallback(
    (name, value) => {
      const next = { ...valuesRef.current, [name]: value };
      setValues(next);
      const fieldErrors = validate(next);
      setErrors((prev) => {
        const updated = { ...prev };
        [name, ...(dependencies[name] || [])].forEach((field) => {
          if (touchedRef.current[field] || prev[field]) updated[field] = fieldErrors[field];
        });
        return updated;
      });
    },
    [validate, dependencies, setValues],
  );

  const handleChange = useCallback(
    (e) => {
      const { name, type, checked, value } = e.target;
      setField(name, type === 'checkbox' ? checked : value);
    },
    [setField],
  );

  const handleBlur = useCallback(
    (e) => {
      const { name } = e.target;
      touchedRef.current = { ...touchedRef.current, [name]: true };
      const message = validate(valuesRef.current)[name];
      setErrors((prev) => (prev[name] === message ? prev : { ...prev, [name]: message }));
    },
    [validate],
  );

  /** Validates every field; returns true when the form can be submitted. */
  const validateAll = useCallback(() => {
    const all = validate(valuesRef.current);
    setErrors(all);
    touchedRef.current = Object.fromEntries(Object.keys(valuesRef.current).map((k) => [k, true]));
    return Object.values(all).every((v) => !v);
  }, [validate]);

  return {
    values,
    setValues,
    errors,
    setErrors,
    setField,
    handleChange,
    handleBlur,
    validateAll,
    submitting,
    setSubmitting,
  };
}
