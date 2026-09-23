export default function Logo({ size = 32 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 32 32" aria-hidden="true">
      <rect width="32" height="32" rx="8" fill="#2563eb" />
      <circle cx="16" cy="16" r="8" fill="none" stroke="#fff" strokeWidth="3" />
      <circle cx="16" cy="16" r="3" fill="#fff" />
    </svg>
  );
}
