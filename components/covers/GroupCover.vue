<template>
  <div :style="{ height: height + 'px', width: width + 'px' }" class="relative">
    <div v-if="!covers.length" class="absolute top-0 left-0 w-full h-full flex items-center justify-center rounded-lg bg-primary box-shadow-book p-3">
      <p class="font-book text-center">{{ name }}</p>
    </div>

    <!-- Fanned stack: the first book is in front and later books recede behind it -->
    <div v-for="cover in covers" :key="cover.id" class="group-cover-book absolute rounded-lg overflow-hidden bg-primary" :style="cover.style">
      <img :src="cover.url" alt="" loading="lazy" class="w-full h-full object-cover" />
    </div>
  </div>
</template>

<script>
const MAX_COVERS = 5
const SCALE_STEP = 0.07

export default {
  props: {
    id: String,
    name: String,
    bookItems: {
      type: Array,
      default: () => []
    },
    width: Number,
    height: Number,
    groupTo: String,
    bookCoverAspectRatio: Number
  },
  computed: {
    store() {
      return this.$store || this.$nuxt.$store
    },
    covers() {
      const books = this.bookItems
        .map((book) => ({ id: book.id, url: this.store.getters['globals/getLibraryItemCoverSrc'](book, '') }))
        .filter((book) => book.url)
        .slice(0, MAX_COVERS)

      const coverHeight = this.height
      const coverWidth = Math.min(this.width, coverHeight / (this.bookCoverAspectRatio || 1))
      const offset = books.length > 1 ? (this.width - coverWidth) / (books.length - 1) : 0

      return books.map((book, index) => {
        const scale = 1 - index * SCALE_STEP
        return {
          ...book,
          style: {
            width: coverWidth + 'px',
            height: coverHeight + 'px',
            left: index * offset + 'px',
            zIndex: books.length - index,
            transform: `scale(${scale})`,
            transformOrigin: 'right center',
            filter: index ? `brightness(${1 - index * 0.14})` : null
          }
        }
      })
    }
  }
}
</script>

<style>
.group-cover-book {
  box-shadow: -6px 2px 12px #00000066;
}
</style>
