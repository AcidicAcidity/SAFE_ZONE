export type IconName =
  | "box"
  | "warehouse"
  | "grid"
  | "key"
  | "calendar"
  | "shield"
  | "users"
  | "chart"
  | "download"
  | "refresh"
  | "back"
  | "search"
  | "logout"
  | "check"
  | "info"
  | "eye"
  | "eye-off"
  | "chevron";
const paths: Record<IconName, string[]> = {
  box: ["M3 3h18v5H3z", "M5 8v13h14V8", "M9 12h6", "M10 16h4"],
  warehouse: ["m3 9 9-6 9 6v12H3Z", "M7 21V11h10v10", "M7 15h10", "M7 18h10"],
  grid: ["M3 3h7v7H3z", "M14 3h7v7h-7z", "M3 14h7v7H3z", "M14 14h7v7h-7z"],
  key: [
    "M14 3a7 7 0 0 0-6 10L2 19v3h4v-3h3v-3l1-1a7 7 0 1 0 4-12Z",
    "M16 7h.01",
  ],
  calendar: [
    "M4 5h16v16H4z",
    "M8 3v4",
    "M16 3v4",
    "M4 10h16",
    "M8 14h3",
    "M8 17h6",
  ],
  shield: ["M12 3 4 6v6c0 5 8 9 8 9s8-4 8-9V6Z", "m8 12 3 3 5-6"],
  users: [
    "M16 21v-3a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v3",
    "M9.5 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z",
    "M17 4a4 4 0 0 1 0 7",
    "M21 21v-3a4 4 0 0 0-3-4",
  ],
  chart: ["M4 3v18h17", "M9 16v-4", "M14 16V7", "M19 16v-6"],
  download: ["M12 3v12", "m7 10 5 5 5-5", "M4 16v5h16v-5"],
  refresh: [
    "M20 7v5h-5",
    "M4 17v-5h5",
    "M6 7a7 7 0 0 1 12-1l2 3",
    "M18 17a7 7 0 0 1-12 1l-2-3",
  ],
  back: ["m14 6-6 6 6 6"],
  search: ["M10.5 3a7.5 7.5 0 1 0 0 15 7.5 7.5 0 0 0 0-15Z", "m16 16 5 5"],
  logout: ["M9 4H4v16h5", "M10 12h11", "m17 8 4 4-4 4"],
  check: ["m5 12 4 4L19 6"],
  info: ["M12 3a9 9 0 1 0 0 18 9 9 0 0 0 0-18Z", "M12 11v6", "M12 7h.01"],
  eye: [
    "M2 12s4-7 10-7 10 7 10 7-4 7-10 7S2 12 2 12Z",
    "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6Z",
  ],
  "eye-off": [
    "m3 3 18 18",
    "M10 5h2c6 0 10 7 10 7a19 19 0 0 1-3 4",
    "M6 6a24 24 0 0 0-4 6s4 7 10 7a12 12 0 0 0 5-1",
  ],
  chevron: ["m8 10 4 4 4-4"],
};
export function Icon({
  name,
  className = "",
}: {
  name: IconName;
  className?: string;
}) {
  return (
    <svg
      className={`icon ${className}`}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.65"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[name].map((path, i) => (
        <path d={path} key={i} />
      ))}
    </svg>
  );
}
