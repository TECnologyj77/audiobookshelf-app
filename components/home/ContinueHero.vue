<template>
  <section class="continue-hero" :class="{ 'continue-hero-wood bookshelfRow shelf-shade': isWood }" :style="{ '--hero-tint': coverRgb || 'rgb(var(--color-secondary))' }" :aria-label="label">
    <h2 v-if="!isWood" class="font-book font-semibold text-lg px-4 pb-3">{{ label }}</h2>
    <div class="continue-hero-card" @click="openItem">
      <covers-book-cover class="flex-shrink-0 rounded-lg" :library-item="item" :width="coverWidth" :book-cover-aspect-ratio="bookCoverAspectRatio" @imageLoaded="coverLoaded" />

      <div class="flex-grow min-w-0 flex flex-col justify-center">
        <h3 class="font-book font-medium text-xl medium:text-2xl leading-tight line-clamp-2">{{ title }}</h3>
        <p class="text-fg-muted text-sm mt-1 truncate">{{ author }}</p>

        <div v-if="progressPercent > 0" class="mt-3 h-1 w-full rounded-full bg-fg/20 overflow-hidden" role="progressbar" :aria-valuenow="Math.round(progressPercent * 100)" aria-valuemin="0" aria-valuemax="100">
          <div class="h-full bg-brass rounded-full" :style="{ width: progressPercent * 100 + '%' }" />
        </div>
        <p v-if="timeRemaining" class="text-fg-muted text-xs mt-1.5 tabular-nums">{{ timeRemaining }}</p>

        <div class="mt-3">
          <button type="button" class="continue-hero-play" :aria-label="isPlaying ? $strings.ButtonPause : $strings.ButtonPlay" @click.stop="togglePlay">
            <span class="material-symbols fill text-2xl">{{ isPlaying ? 'pause' : 'play_arrow' }}</span>
            <span class="font-semibold text-sm pr-1">{{ isPlaying ? $strings.ButtonPause : $strings.ButtonPlay }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-if="isWood" class="relative h-5 mt-4 bookshelfDivider">
      <div class="absolute bottom-0 left-4 right-4 flex">
        <div class="shelf-plaque"><span>{{ label }}</span></div>
      </div>
    </div>
  </section>
</template>

<script>
import { getAverageColorFromCoverUrl } from '@/utils/coverAverageColor'

export default {
  props: {
    item: {
      type: Object,
      required: true
    },
    label: String
  },
  data() {
    return {
      coverRgb: null
    }
  },
  computed: {
    isWood() {
      return !this.$store.getters['getAltViewEnabled']
    },
    bookCoverAspectRatio() {
      return this.$store.getters['libraries/getBookCoverAspectRatio']
    },
    coverWidth() {
      const height = this.$store.state.globals.windowWidth >= 600 ? 184 : 136
      return height / this.bookCoverAspectRatio
    },
    metadata() {
      return this.item.media?.metadata || {}
    },
    title() {
      return this.metadata.title || ''
    },
    author() {
      return this.metadata.authorName || this.metadata.author || ''
    },
    isLocal() {
      return !!this.item.isLocal
    },
    progress() {
      if (this.isLocal) return this.$store.getters['globals/getLocalMediaProgressById'](this.item.id)
      return this.$store.getters['user/getUserMediaProgress'](this.item.id)
    },
    progressPercent() {
      return this.progress?.progress || 0
    },
    timeRemaining() {
      const duration = this.progress?.duration || this.item.media?.duration
      if (!duration || !this.progressPercent) return ''
      return this.$getString('LabelTimeRemaining', [this.$elapsedPretty(duration * (1 - this.progressPercent))])
    },
    playbackItemId() {
      return this.item.localLibraryItem?.id || this.item.id
    },
    serverItemId() {
      return this.isLocal ? this.item.libraryItemId : this.item.id
    },
    isPlaying() {
      if (!this.$store.state.playerIsPlaying) return false
      return this.$store.getters['getIsMediaStreaming'](this.playbackItemId, null) || this.$store.getters['getIsMediaStreaming'](this.serverItemId, null)
    }
  },
  methods: {
    openItem() {
      this.$router.push(`/item/${this.item.id}`)
    },
    async coverLoaded(fullCoverUrl) {
      if (!fullCoverUrl) return
      const average = await getAverageColorFromCoverUrl(this, fullCoverUrl)
      if (average) this.coverRgb = average.rgba
    },
    async togglePlay() {
      await this.$hapticsImpact()
      if (this.isPlaying) {
        this.$eventBus.$emit('pause-item')
        return
      }
      this.$store.commit('setPlayerIsStartingPlayback', this.playbackItemId)
      this.$eventBus.$emit('play-item', { libraryItemId: this.playbackItemId, serverLibraryItemId: this.serverItemId })
    }
  }
}
</script>

<style>
.continue-hero {
  padding-bottom: 8px;
}
/* Takes its color from the book's cover. This is the one bold surface on Home */
.continue-hero-card {
  display: flex;
  gap: 16px;
  margin: 0 12px;
  padding: 12px;
  border-radius: 20px;
  cursor: pointer;
  background: rgb(var(--color-secondary));
  background: linear-gradient(105deg, color-mix(in srgb, var(--hero-tint) 62%, rgb(var(--color-primary))) 0%, rgb(var(--color-secondary)) 78%);
  transition: background 0.3s;
}
.continue-hero-wood {
  padding: 20px 0 0;
}
.continue-hero-wood .continue-hero-card {
  background: linear-gradient(105deg, color-mix(in srgb, var(--hero-tint) 62%, #241f1a) 0%, #241f1aee 78%);
  box-shadow: 0 6px 16px #00000080;
}
.continue-hero-play {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 40px;
  padding: 0 14px 0 10px;
  border-radius: 20px;
  background-color: rgb(var(--color-brass));
  color: rgb(var(--color-primary));
}
@media (min-width: 600px) {
  .continue-hero-card {
    gap: 24px;
    padding: 16px;
  }
}
</style>
