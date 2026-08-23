/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        paper: 'var(--paper)',
        card: 'var(--card)',
        ink: 'var(--ink)',
        'ink-2': 'var(--ink-2)',
        'ink-3': 'var(--ink-3)',
        rule: 'var(--rule)',
        'rule-2': 'var(--rule-2)',
        aprovado: 'var(--aprovado)',
        reprovado: 'var(--reprovado)',
        falta: 'var(--falta)',
        trancado: 'var(--trancado)',
        acento: 'var(--acento)',
        'acento-contraste': 'var(--acento-contraste)',
      },
      fontFamily: {
        sans: ['IBM Plex Sans Variable', 'ui-sans-serif', 'system-ui', 'sans-serif'],
        mono: ['IBM Plex Mono', 'ui-monospace', 'monospace'],
        condensed: ['IBM Plex Sans Condensed', 'IBM Plex Sans Variable', 'sans-serif'],
      },
      fontSize: {
        meta: '12.5px',
        label: '12px',
        apoio: '13.5px',
        corpo: '15px',
        numero: '17px',
        titulo: '27px',
        'titulo-lg': '34px',
      },
      transitionProperty: {
        control: 'background-color, border-color',
      },
    },
  },
  plugins: [],
};
