export const durations = {
  fast: '150ms',
  normal: '250ms',
  slow: '350ms',
};

export const easings = {
  standard: 'cubic-bezier(0.4, 0, 0.2, 1)',
  enter: 'cubic-bezier(0, 0, 0.2, 1)',
  exit: 'cubic-bezier(0.4, 0, 1, 1)',
};

export const transitions = {
  default: `all ${durations.normal} ${easings.standard}`,
  colors: `color ${durations.fast} ${easings.standard}, background-color ${durations.fast} ${easings.standard}, border-color ${durations.fast} ${easings.standard}`,
  shadow: `box-shadow ${durations.normal} ${easings.standard}`,
  transform: `transform ${durations.fast} ${easings.standard}`,
};
