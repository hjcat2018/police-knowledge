<template>
  <div class="skeleton-card" :class="{ 'skeleton-loaded': loaded }">
    <template v-if="!loaded">
      <div class="skeleton-icon" />
      <div class="skeleton-content">
        <div class="skeleton-value" />
        <div class="skeleton-label" />
      </div>
    </template>
    <slot v-else />
  </div>
</template>

<script setup lang="ts">
interface Props {
  loaded?: boolean
}

defineProps<Props>()
</script>

<style lang="scss" scoped>
.skeleton-card {
  display: flex;
  align-items: center;
  padding: 20px;
  background: var(--el-card-bg-color, #fff);
  border-radius: 8px;

  &-loaded {
    display: block;
  }

  .skeleton-icon {
    width: 56px;
    height: 56px;
    border-radius: 8px;
    margin-right: 16px;
    background: linear-gradient(
      90deg,
      var(--skeleton-bg, #f0f2f5) 25%,
      var(--skeleton-highlight, #e8e8e8) 50%,
      var(--skeleton-bg, #f0f2f5) 75%
    );
    background-size: 200% 100%;
    animation: skeleton-loading 1.5s ease-in-out infinite;
    flex-shrink: 0;
  }

  .skeleton-content {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: 8px;
  }

  .skeleton-value {
    width: 80px;
    height: 32px;
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

  .skeleton-label {
    width: 60px;
    height: 16px;
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
  .skeleton-card {
    background: var(--el-card-bg-color, #1e1e3f);

    .skeleton-icon,
    .skeleton-value,
    .skeleton-label {
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
