<template>
  <div class="skeleton-table" :class="{ 'skeleton-loaded': loaded }">
    <template v-if="!loaded">
      <div class="skeleton-header">
        <div
          v-for="i in columns"
          :key="i"
          class="skeleton-cell header-cell"
          :style="{ width: `${100 / columns}%` }"
        />
      </div>
      <div
        v-for="j in rows"
        :key="j"
        class="skeleton-row"
      >
        <div
          v-for="i in columns"
          :key="i"
          class="skeleton-cell"
          :style="{ width: `${100 / columns}%` }"
        />
      </div>
    </template>
    <slot v-else />
  </div>
</template>

<script setup lang="ts">
interface Props {
  loaded?: boolean
  columns?: number
  rows?: number
}

withDefaults(defineProps<Props>(), {
  loaded: false,
  columns: 4,
  rows: 5
})
</script>

<style lang="scss" scoped>
.skeleton-table {
  width: 100%;

  &-loaded {
    display: block;
  }

  .skeleton-header {
    display: flex;
    border-bottom: 1px solid var(--el-table-border-color, #e4e7ed);
    background: var(--el-table-header-bg-color, #fafafa);
  }

  .skeleton-row {
    display: flex;
    border-bottom: 1px solid var(--el-table-border-color, #ebeef5);

    &:last-child {
      border-bottom: none;
    }
  }

  .skeleton-cell {
    padding: 12px 16px;
    height: 48px;

    &.header-cell {
      font-weight: 600;
      height: 48px;
    }

    background: linear-gradient(
      90deg,
      var(--skeleton-bg, #f0f2f5) 25%,
      var(--skeleton-highlight, #e8e8e8) 50%,
      var(--skeleton-bg, #f0f2f5) 75%
    );
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
  }
}

html.dark {
  .skeleton-table {
    .skeleton-header {
      background: var(--el-table-header-bg-color-dark, #16213e);
    }

    .skeleton-cell {
      background: linear-gradient(
        90deg,
        var(--skeleton-bg-dark, #2a2a3e) 25%,
        var(--skeleton-highlight-dark, #3a3a4e) 50%,
        var(--skeleton-bg-dark, #2a2a3e) 75%
      );
      background-size: 200% 100%;
    }
  }
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>
