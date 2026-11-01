<template>
  <header class="series-header flex gap-4 medium:gap-6 px-4 pt-4 pb-2 items-center">
    <covers-group-cover class="flex-shrink-0" :id="series.id" :name="series.name" :book-items="books" :width="stackWidth" :height="stackHeight" :book-cover-aspect-ratio="bookCoverAspectRatio" />
    <div class="min-w-0 flex-grow">
      <h1 class="font-book font-semibold text-xl medium:text-3xl leading-tight line-clamp-2">{{ series.name }}</h1>
      <p v-if="total" class="text-fg-muted text-sm mt-1">{{ $formatNumber(total) }} {{ $strings.LabelBooks }}</p>
      <div v-if="finishedPercent !== null" class="mt-3 max-w-sm">
        <div class="h-1 rounded-full bg-fg/20 overflow-hidden" role="progressbar" :aria-valuenow="finishedPercent" aria-valuemin="0" aria-valuemax="100">
          <div class="h-full rounded-full bg-brass" :style="{ width: finishedPercent + '%' }" />
        </div>
        <p class="text-fg-muted text-xs mt-1.5 tabular-nums">{{ finishedCount }}/{{ total }} {{ $strings.LabelFinished }}</p>
      </div>
      <p v-if="series.description" class="text-sm text-fg/80 mt-2 line-clamp-2 max-w-prose">{{ series.description }}</p>
    </div>
  </header>
</template>

<script>
export default {
  props: {
    series: {
      type: Object,
      required: true
    }
  },
  data() {
    return {
      books: [],
      total: 0
    }
  },
  computed: {
    bookCoverAspectRatio() {
      return this.$store.getters['libraries/getBookCoverAspectRatio']
    },
    isWide() {
      return this.$store.state.globals.windowWidth >= 600
    },
    stackHeight() {
      return this.isWide ? 168 : 112
    },
    stackWidth() {
      return Math.round((this.stackHeight / this.bookCoverAspectRatio) * 1.45)
    },
    finishedCount() {
      return this.series.progress?.libraryItemIdsFinished?.length || 0
    },
    finishedPercent() {
      if (!this.series.progress || !this.total) return null
      return Math.round((this.finishedCount / this.total) * 100)
    },
    libraryId() {
      return this.$store.state.libraries.currentLibraryId
    }
  },
  async mounted() {
    const filter = `series.${this.$encode(this.series.id)}`
    const payload = await this.$nativeHttp.get(`/api/libraries/${this.libraryId}/items?limit=5&page=0&minified=1&filter=${filter}`).catch((error) => {
      console.error('Failed to load series books', error)
      return null
    })
    if (payload?.results) {
      this.books = payload.results
      this.total = payload.total
    }
  }
}
</script>
