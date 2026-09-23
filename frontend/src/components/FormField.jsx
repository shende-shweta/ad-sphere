import { useId } from 'react';

/** Label + control + error/help text, wired up with aria attributes. */
export function Field({ label, required, error, help, children, className = '' }) {
  const id = useId();
  const describedBy = error ? `${id}-error` : help ? `${id}-help` : undefined;
  return (
    <div className={`field ${error ? 'has-error' : ''} ${className}`}>
      {label && (
        <label htmlFor={id}>
          {label}
          {required && <span className="required" aria-hidden="true"> *</span>}
        </label>
      )}
      {children({ id, 'aria-invalid': !!error, 'aria-describedby': describedBy, 'aria-required': required })}
      {error ? (
        <p id={`${id}-error`} className="field-error" role="alert">
          {error}
        </p>
      ) : (
        help && (
          <p id={`${id}-help`} className="field-help">
            {help}
          </p>
        )
      )}
    </div>
  );
}

export function TextField({ label, required, error, help, className, ...props }) {
  return (
    <Field label={label} required={required} error={error} help={help} className={className}>
      {(a11y) => <input className="input" {...a11y} {...props} />}
    </Field>
  );
}

export function TextAreaField({ label, required, error, help, className, ...props }) {
  return (
    <Field label={label} required={required} error={error} help={help} className={className}>
      {(a11y) => <textarea className="input textarea" rows={3} {...a11y} {...props} />}
    </Field>
  );
}

export function SelectField({ label, required, error, help, options, placeholder, className, ...props }) {
  return (
    <Field label={label} required={required} error={error} help={help} className={className}>
      {(a11y) => (
        <select className="input select" {...a11y} {...props}>
          {placeholder !== undefined && <option value="">{placeholder}</option>}
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
      )}
    </Field>
  );
}

export function RadioGroup({ label, name, value, options, onChange, required, error }) {
  return (
    <fieldset className={`field radio-group ${error ? 'has-error' : ''}`}>
      {label && (
        <legend>
          {label}
          {required && <span className="required" aria-hidden="true"> *</span>}
        </legend>
      )}
      <div className="radio-options">
        {options.map((o) => (
          <label key={o.value} className="radio">
            <input
              type="radio"
              name={name}
              value={o.value}
              checked={value === o.value}
              onChange={onChange}
            />
            <span>{o.label}</span>
          </label>
        ))}
      </div>
      {error && (
        <p className="field-error" role="alert">
          {error}
        </p>
      )}
    </fieldset>
  );
}
