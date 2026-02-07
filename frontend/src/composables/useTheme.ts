import { ref, watch, onMounted } from 'vue'

export type ThemeMode = 'light' | 'dark' | 'auto'

export function useTheme() {
  const isDark = ref(true)
  const themeMode = ref<ThemeMode>('dark')

  const toggleTheme = () => {
    if (themeMode.value === 'auto') {
      themeMode.value = isDark.value ? 'light' : 'dark'
    } else {
      themeMode.value = themeMode.value === 'dark' ? 'light' : 'dark'
    }
  }

  const setThemeMode = (mode: ThemeMode) => {
    themeMode.value = mode
  }

  const updateDarkClass = (dark: boolean) => {
    if (dark) {
      document.documentElement.classList.add('dark')
      document.documentElement.setAttribute('data-theme', 'dark')
    } else {
      document.documentElement.classList.remove('dark')
      document.documentElement.setAttribute('data-theme', 'light')
    }
  }

  const checkSystemPreference = (): boolean => {
    if (typeof window !== 'undefined' && window.matchMedia) {
      return window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    return true
  }

  const updateIsDark = () => {
    let dark: boolean
    switch (themeMode.value) {
      case 'light':
        dark = false
        break
      case 'dark':
        dark = true
        break
      case 'auto':
      default:
        dark = checkSystemPreference()
        break
    }
    isDark.value = dark
    updateDarkClass(dark)
    localStorage.setItem('theme-mode', themeMode.value)
  }

  watch(themeMode, () => {
    updateIsDark()
  })

  onMounted(() => {
    const savedMode = localStorage.getItem('theme-mode') as ThemeMode | null
    if (savedMode && ['light', 'dark', 'auto'].includes(savedMode)) {
      themeMode.value = savedMode
    } else {
      themeMode.value = 'dark'
    }
    updateIsDark()
  })

  return {
    isDark,
    themeMode,
    toggleTheme,
    setThemeMode,
    updateIsDark
  }
}
