export default function Toggle({ label, description, name, checked, onChange, disabled }) {
  return (
    <label className={`toggle-row ${disabled ? 'disabled' : ''}`}>
      <span className="toggle-text">
        <span className="toggle-label">{label}</span>
        {description && <span className="toggle-desc">{description}</span>}
      </span>
      <span className="toggle">
        <input
          type="checkbox"
          role="switch"
          name={name}
          checked={!!checked}
          onChange={onChange}
          disabled={disabled}
        />
        <span className="toggle-track" aria-hidden="true" />
      </span>
    </label>
  );
}
