export default function Logo({ size = 28 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 64 64" aria-hidden="true" style={{ flexShrink: 0 }}>
      <defs>
        <linearGradient id="kd-grad" x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor="#2f9c8f" />
          <stop offset="1" stopColor="#1f6f65" />
        </linearGradient>
      </defs>
      <rect width="64" height="64" rx="16" fill="url(#kd-grad)" />
      <path
        d="M18 33.5 L28 43.5 L47 22"
        fill="none"
        stroke="#fff"
        strokeWidth="7"
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      <circle cx="48" cy="16" r="5" fill="#f2b84b" />
    </svg>
  )
}
