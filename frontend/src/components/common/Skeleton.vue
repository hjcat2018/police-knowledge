<template>
  <div class="skeleton" :class="{ 'skeleton-loaded': loaded }">
    <template v-if="!loaded">
      <div
        v-for="i in rows"
        :key="i"
        class="skeleton-row"
        :style="{ height: i === rows ? `${rowHeight * 0.6}px` : `${rowHeight}px` }"
      >
        <div
          class="skeleton-bar"
          :style="{ width: i === rows ? `${Math.random() * 40 + 30}%` : '100%' }"
        />
      </div>
    </template>
    <slot v-else />
  </div>
</template>

<script setup lang="ts">
interface Props {
  loaded?: boolean
  rows?: number
  rowHeight?: number
}

withDefaults(defineProps<Props>(), {
  loaded: false,
  rows: 3,
  rowHeight: 20
})
</script>

<style lang="scss" scoped>
.skeleton {
  width: 100%;

  &-loaded {
    width: auto;
    height: auto;
  }

  &-row {
    margin-bottom: 12px;
    display: flex;
    align-items: center;
  }

  &-bar {
    height: 100%;
    border-radius: 4px;
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
  .skeleton {
    &-bar {
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
