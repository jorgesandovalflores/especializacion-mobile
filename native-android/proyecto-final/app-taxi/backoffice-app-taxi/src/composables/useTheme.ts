import { ref, watch } from 'vue'

type Theme = 'light' | 'dark'

const THEME_KEY = 'apptaxi.backoffice.theme'

function initialTheme(): Theme {
  const saved = localStorage.getItem(THEME_KEY)
  if (saved === 'light' || saved === 'dark') return saved
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

const theme = ref<Theme>(initialTheme())

function apply(value: Theme) {
  document.documentElement.dataset.theme = value
}

apply(theme.value)

watch(theme, (value) => {
  apply(value)
  localStorage.setItem(THEME_KEY, value)
})

export function useTheme() {
  return {
    theme,
    toggle: () => {
      theme.value = theme.value === 'dark' ? 'light' : 'dark'
    }
  }
}
