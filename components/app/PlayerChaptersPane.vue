<template>
  <section class="player-chapters-pane absolute z-40 pointer-events-auto flex flex-col bg-secondary rounded-2xl overflow-hidden" aria-label="Chapters">
    <header class="flex items-center pl-4 pr-1 h-12 flex-shrink-0 border-b border-border/60">
      <h2 class="font-book font-semibold text-base flex-grow truncate">{{ chapters.length }} {{ $strings.LabelChapters }}</h2>
      <button type="button" class="w-11 h-11 flex items-center justify-center text-fg-muted" aria-label="Close chapters" @click="$emit('close')">
        <span class="material-symbols text-2xl">close</span>
      </button>
    </header>
    <ul ref="list" class="flex-grow overflow-y-auto overflow-x-hidden">
      <li v-for="chapter in chapters" :key="chapter.id" :data-chapter="chapter.id" class="relative cursor-pointer flex items-baseline gap-3 px-4 py-3 border-b border-border/30" :class="{ 'bg-brass/10': chapter.id === currentChapterId }" @click="$emit('select', chapter)">
        <span class="flex-grow min-w-0 line-clamp-2 text-sm" :class="chapter.id === currentChapterId ? 'text-fg font-semibold' : 'text-fg/80'">{{ chapter.title }}</span>
        <span class="flex-shrink-0 text-sm text-fg-muted tabular-nums">{{ $secondsToTimestamp(Math.max(0, chapter.end - chapter.start) / rate) }}</span>
        <span v-show="chapter.id === currentChapterId" class="absolute top-0 left-0 h-full w-1 bg-brass" />
      </li>
    </ul>
  </section>
</template>

<script>
export default {
  props: {
    chapters: {
      type: Array,
      default: () => []
    },
    currentChapter: {
      type: Object,
      default: () => null
    },
    playbackRate: Number
  },
  computed: {
    currentChapterId() {
      return this.currentChapter?.id
    },
    rate() {
      return this.playbackRate > 0 ? this.playbackRate : 1
    }
  },
  mounted() {
    const row = this.$refs.list.querySelector(`[data-chapter="${this.currentChapterId}"]`)
    if (row) this.$refs.list.scrollTop = Math.max(0, row.offsetTop - this.$refs.list.clientHeight / 2 + row.clientHeight / 2)
  }
}
</script>
