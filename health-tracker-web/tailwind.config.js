/** @type {import('tailwindcss').Config} */
export default {
  darkMode: "class",
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        "on-secondary": "#ffffff",
        "on-primary-fixed-variant": "#5a2d00",
        "error-container": "#ffdad6",
        "tertiary": "#006d37",
        "secondary-container": "#ffddb9",
        "on-primary": "#ffffff",
        "secondary-fixed-dim": "#ffb961",
        "tertiary-fixed-dim": "#4ae183",
        "secondary-fixed": "#ffddb9",
        "on-tertiary-container": "#003a1a",
        "secondary": "#865300",
        "on-error": "#ffffff",
        "outline": "#897365",
        "on-error-container": "#93000a",
        "inverse-on-surface": "#f2f1ee",
        "background": "#faf9f6",
        "surface-container-high": "#e9e8e5",
        "primary-container": "#ffdcc1",
        "on-primary-container": "#2e1500",
        "error": "#ba1a1a",
        "surface": "#faf9f6",
        "primary-fixed-dim": "#ffb682",
        "inverse-primary": "#ffb682",
        "surface-tint": "#A23F00",
        "surface-container-low": "#f4f3f1",
        "outline-variant": "#dcc1b1",
        "on-secondary-fixed": "#2b1700",
        "tertiary-fixed": "#6bfe9c",
        "surface-dim": "#dbdad7",
        "on-tertiary-fixed": "#00210c",
        "inverse-surface": "#2f312f",
        "tertiary-container": "#00b05c",
        "on-surface": "#1a1c1a",
        "surface-variant": "#e3e2e0",
        "on-surface-variant": "#564337",
        "surface-container-lowest": "#ffffff",
        "surface-container": "#efeeeb",
        "primary-fixed": "#ffdcc1",
        "on-primary-fixed": "#2e1500",
        "on-secondary-fixed-variant": "#663e00",
        "on-tertiary-fixed-variant": "#005228",
        "on-background": "#1a1c1a",
        "surface-bright": "#faf9f6",
        "on-secondary-container": "#2b1700",
        "on-tertiary": "#ffffff",
        "surface-container-highest": "#e3e2e0",
        "primary": "#A23F00"
      },
      fontFamily: {
        "headline": ["Manrope", "sans-serif"],
        "body": ["Inter", "sans-serif"],
        "label": ["Inter", "sans-serif"]
      },
      borderRadius: {
        "DEFAULT": "1.5rem",
        "lg": "2rem",
        "xl": "3rem",
        "full": "9999px"
      },
      boxShadow: {
        'amber-care': '0 24px 48px rgba(134, 83, 0, 0.08)',
      },
      backgroundImage: {
        'amber-gradient': 'linear-gradient(135deg, #A23F00 0%, #FFB682 100%)',
      }
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/container-queries'),
  ],
}