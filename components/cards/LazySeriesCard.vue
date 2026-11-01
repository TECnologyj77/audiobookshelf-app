<template>
  <div ref="card" :id="`series-card-${index}`" :style="{ width: width + 'px', height: height + 'px' }" class="cursor-pointer z-30" @click="clickCard">
    <covers-group-cover v-if="series" ref="cover" :id="seriesId" :name="title" :book-items="books" :width="width" :height="height" :book-cover-aspect-ratio="bookCoverAspectRatio" />

    <!-- Clean view: details below the covers -->
    <div v-if="isAltViewEnabled" class="absolute z-30 left-0 right-0 top-full pt-2">
      <p class="font-book font-medium truncate" :style="{ fontSize: labelFontSize + 'rem' }">{{ title }}</p>
      <div class="flex items-center gap-2 mt-0.5">
        <p class="text-fg-muted text-xs flex-shrink-0 tabular-nums">{{ progressLabel }}</p>
        <div v-if="seriesPercentInProgress > 0" class="flex-grow h-1 rounded-full bg-fg/20 overflow-hidden">
          <div class="h-full rounded-full" :class="isSeriesFinished ? 'bg-success' : 'bg-brass'" :style="{ width: seriesPercentInProgress * 100 + '%' }" />
        </div>
      </div>
    </div>

    <!-- Bookshelf view: progress along the base of the stack and a nameplate on the shelf -->
    <template v-else>
      <div v-if="seriesPercentInProgress > 0" class="absolute bottom-0 left-0 h-1 z-10 rounded-b" :class="isSeriesFinished ? 'bg-success' : 'bg-brass'" :style="{ width: seriesPercentInProgress * 100 + '%' }" />
      <div class="absolute z-40 left-0 right-0 flex justify-center" style="bottom: -24px">
        <div class="shelf-plaque"><span>{{ title }}</span></div>
      </div>
    </template>
  </div>
</template>

<script>
export default {
  props: {
    index: Number,
    width: Number,
    height: Number,
    bookCoverAspectRatio: Number,
    seriesMount: {
      type: Object,
      default: () => null
    },
    isAltViewEnabled: Boolean,
    isCategorized: Boolean
  },
  data() {
    return {
      series: null,
      isSelectionMode: false,
      selected: false,
      imageReady: false
    }
  },
  computed: {
    labelFontSize() {
      if (this.width < 160) return 0.8125
      return 1
    },
    progressLabel() {
      const total = this.$formatNumber ? this.$formatNumber(this.books.length) : this.books.length
      if (!this.seriesBooksFinished.length) return `${total} ${this.$strings.LabelBooks}`
      return `${this.seriesBooksFinished.length}/${total} ${this.$strings.LabelFinished}`
    },
    sizeMultiplier() {
      if (this.bookCoverAspectRatio === 1) return this.width / (120 * 1.6 * 2)
      return this.width / 240
    },
    title() {
      return this.series ? this.series.name : ''
    },
    books() {
      return this.series ? this.series.books || [] : []
    },
    seriesBookProgress() {
      return this.books
        .map((libraryItem) => {
          return this.store.getters['user/getUserMediaProgress'](libraryItem.id)
        })
        .filter((p) => !!p)
    },
    seriesBooksFinished() {
      return this.seriesBookProgress.filter((p) => p.isFinished)
    },
    hasSeriesBookInProgress() {
      return this.seriesBookProgress.some((p) => !p.isFinished && p.progress > 0)
    },
    seriesPercentInProgress() {
      let totalFinishedAndInProgress = this.seriesBooksFinished.length
      if (this.hasSeriesBookInProgress) totalFinishedAndInProgress += 1
      return Math.min(1, Math.max(0, totalFinishedAndInProgress / this.books.length))
    },
    isSeriesFinished() {
      return this.books.length === this.seriesBooksFinished.length
    },
    store() {
      return this.$store || this.$nuxt.$store
    },
    currentLibraryId() {
      return this.store.state.libraries.currentLibraryId
    },
    seriesId() {
      return this.series ? this.series.id : null
    }
  },
  methods: {
    setEntity(_series) {
      this.series = _series
    },
    setSelectionMode(val) {
      this.isSelectionMode = val
    },
    clickCard() {
      if (!this.series) return
      var router = this.$router || this.$nuxt.$router
      router.push(`/bookshelf/series/${this.seriesId}`)
    },
    imageLoaded() {
      this.imageReady = true
    },
    destroy() {
      // destroy the vue listeners, etc
      this.$destroy()

      // remove the element from the DOM
      if (this.$el && this.$el.parentNode) {
        this.$el.parentNode.removeChild(this.$el)
      } else if (this.$el && this.$el.remove) {
        this.$el.remove()
      }
    }
  },
  mounted() {
    if (this.seriesMount) {
      this.setEntity(this.seriesMount)
    }
  },
  beforeDestroy() {}
}
</script>
