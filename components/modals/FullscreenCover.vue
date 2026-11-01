<template>
  <modals-modal v-model="show" width="100%" height="100%" max-width="100%">
    <div class="w-full h-full overflow-hidden absolute top-0 left-0 flex items-center justify-center" @click="show = false">
      <covers-book-cover :library-item="libraryItem" :width="width" raw :book-cover-aspect-ratio="bookCoverAspectRatio" />
    </div>
  </modals-modal>
</template>

<script>
export default {
  props: {
    value: Boolean,
    libraryItem: {
      type: Object,
      default: () => {}
    }
  },
  computed: {
    show: {
      get() {
        return this.value
      },
      set(val) {
        this.$emit('input', val)
      }
    },
    bookCoverAspectRatio() {
      return this.$store.getters['libraries/getBookCoverAspectRatio']
    },
    width() {
      const { windowWidth, windowHeight } = this.$store.state.globals
      return Math.min(windowWidth, windowHeight / this.bookCoverAspectRatio)
    }
  }
}
</script>

<style>
.filter-modal-wrapper {
  max-height: calc(100% - 320px);
}
</style>