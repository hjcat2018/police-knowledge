<template>
  <el-dropdown
    trigger="click"
    @command="handleCommand"
    placement="bottom-end"
  >
    <div class="theme-toggle" :class="{ 'is-dark': isDark }">
      <el-tooltip :content="tooltipText" placement="bottom" :show-after="500">
        <el-button
          circle
          size="default"
          :type="isDark ? 'warning' : 'primary'"
          class="theme-btn"
          aria-label="切换主题"
        >
          <el-icon v-if="isDark"><Sunny /></el-icon>
          <el-icon v-else><Moon /></el-icon>
        </el-button>
      </el-tooltip>
    </div>
    <template #dropdown>
      <el-dropdown-menu class="theme-dropdown">
        <el-dropdown-item
          command="light"
          :class="{ 'is-active': themeMode === 'light' }"
        >
          <el-icon><Sunny /></el-icon>
          <span>亮色模式</span>
        </el-dropdown-item>
        <el-dropdown-item
          command="dark"
          :class="{ 'is-active': themeMode === 'dark' }"
        >
          <el-icon><Moon /></el-icon>
          <span>暗色模式</span>
        </el-dropdown-item>
        <el-dropdown-item
          command="auto"
          :class="{ 'is-active': themeMode === 'auto' }"
        >
          <el-icon><Monitor /></el-icon>
          <span>跟随系统</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useTheme, type ThemeMode } from '@/composables/useTheme'

const { isDark, themeMode, setThemeMode } = useTheme()

const tooltipText = computed(() => {
  switch (themeMode.value) {
    case 'light':
      return '当前：亮色模式，点击切换'
    case 'dark':
      return '当前：暗色模式，点击切换'
    case 'auto':
      return '当前：跟随系统，点击切换'
    default:
      return '切换主题'
  }
})

const handleCommand = (command: ThemeMode) => {
  setThemeMode(command)
}
</script>

<style lang="scss" scoped>
.theme-toggle {
  display: flex;
  align-items: center;
  justify-content: center;

  .theme-btn {
    transition: all 0.3s ease;
  }

  &.is-dark {
    .theme-btn {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.2);

      &:hover {
        background: rgba(255, 255, 255, 0.2);
      }
    }
  }
}
</style>

<style lang="scss">
.theme-dropdown {
  min-width: 160px;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 16px;

    .el-icon {
      font-size: 16px;
    }

    &.is-active {
      color: var(--el-dropdown-menu-item-active-color);
      background: var(--el-dropdown-menu-item-active-bg);
    }
  }
}
</style>
