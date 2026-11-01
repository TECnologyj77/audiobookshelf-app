<template>
  <div class="w-full h-12 bg-bg relative z-20">
    <div id="bookshelf-toolbar" class="absolute top-0 left-0 w-full h-full z-20 flex items-center pl-4 pr-1">
      <div class="flex items-center w-full text-sm">
        <p v-show="!selectedSeriesName" class="text-fg-muted whitespace-nowrap" :class="{ 'hidden medium:block mr-3': showFilterChips }">{{ $formatNumber(totalEntities) }} {{ entityTitle }}</p>
        <div v-if="showFilterChips" class="filter-chips flex items-center gap-2 min-w-0 overflow-x-auto" role="group" aria-label="Filter by progress">
          <button v-for="chip in filterChips" :key="chip.value" type="button" class="filter-chip" :class="{ selected: selectedFilter === chip.value }" :aria-pressed="selectedFilter === chip.value" @click="selectFilterChip(chip.value)">{{ chip.text }}</button>
        </div>
        <p v-show="selectedSeriesName" class="text-fg-muted whitespace-nowrap">{{ $formatNumber(totalEntities) }} {{ $strings.LabelBooks }}</p>
        <div class="flex-grow min-w-2" />
        <span v-if="page == 'library' || seriesBookPage" class="toolbar-action material-symbols text-2xl" @click="changeView">{{ !bookshelfListView ? 'view_list' : 'grid_view' }}</span>
        <template v-if="page === 'library'">
          <div class="relative flex items-center flex-shrink-0">
            <span class="toolbar-action material-symbols text-2xl" @click="showFilterModal = true">filter_alt</span>
            <div v-show="hasOtherFilter" class="absolute top-2.5 right-2.5 w-2 h-2 rounded-full bg-brass z-10 pointer-events-none" />
          </div>
          <span class="toolbar-action material-symbols text-2xl" @click="showSortModal = true">sort</span>
        </template>
        <span v-if="seriesBookPage" class="toolbar-action material-symbols text-2xl" @click="downloadSeries">download</span>
        <span v-if="(page == 'library' && isBookLibrary) || seriesBookPage" class="toolbar-action material-symbols text-2xl" @click="showMoreMenuDialog = true">more_vert</span>
      </div>
    </div>

    <modals-order-modal v-model="showSortModal" :order-by.sync="settings.mobileOrderBy" :descending.sync="settings.mobileOrderDesc" @change="updateOrder" />
    <modals-filter-modal v-model="showFilterModal" :filter-by.sync="settings.mobileFilterBy" @change="updateFilter" />
    <modals-dialog v-model="showMoreMenuDialog" :items="menuItems" @action="clickMenuAction" />
  </div>
</template>

<style>
.toolbar-action {
  flex-shrink: 0;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.filter-chips {
  scrollbar-width: none;
}
.filter-chips::-webkit-scrollbar {
  display: none;
}
.filter-chip {
  flex-shrink: 0;
  height: 32px;
  padding: 0 12px;
  border-radius: 16px;
  border: 1px solid rgb(var(--color-border));
  color: rgb(var(--color-fg-muted));
  font-size: 0.8125rem;
  white-space: nowrap;
}
.filter-chip.selected {
  background-color: rgb(var(--color-brass) / 0.22);
  border-color: rgb(var(--color-brass) / 0.6);
  color: rgb(var(--color-fg));
  font-weight: 600;
}
</style>

<script>
export default {
  data() {
    return {
      showSortModal: false,
      showFilterModal: false,
      settings: {},
      totalEntities: 0,
      showMoreMenuDialog: false
    }
  },
  computed: {
    bookshelfListView: {
      get() {
        return this.$store.state.globals.bookshelfListView
      },
      set(val) {
        this.$localStore.setBookshelfListView(val)
        this.$store.commit('globals/setBookshelfListView', val)
      }
    },
    currentLibraryMediaType() {
      return this.$store.getters['libraries/getCurrentLibraryMediaType']
    },
    isBookLibrary() {
      return this.currentLibraryMediaType === 'book'
    },
    selectedFilter() {
      return this.$store.getters['user/getUserSetting']('mobileFilterBy') || 'all'
    },
    showFilterChips() {
      return this.page === 'library' && this.isBookLibrary
    },
    filterChips() {
      const progress = (id) => `progress.${this.$encode(id)}`
      return [
        { text: this.$strings.LabelAll, value: 'all' },
        { text: this.$strings.LabelInProgress, value: progress('in-progress') },
        { text: this.$strings.LabelNotStarted, value: progress('not-started') },
        { text: this.$strings.LabelFinished, value: progress('finished') }
      ]
    },
    hasOtherFilter() {
      return !this.filterChips.some((chip) => chip.value === this.selectedFilter)
    },
    page() {
      var routeName = this.$route.name || ''
      return routeName.split('-')[1]
    },
    seriesBookPage() {
      return this.$route.name == 'bookshelf-series-id'
    },
    routeQuery() {
      return this.$route.query || {}
    },
    entityTitle() {
      if (this.page === 'library') {
        return this.isPodcast ? this.$strings.LabelPodcasts : this.$strings.LabelBooks
      } else if (this.page === 'playlists') {
        return this.$strings.ButtonPlaylists
      } else if (this.page === 'series') {
        return this.$strings.LabelSeries
      } else if (this.page === 'collections') {
        return this.$strings.ButtonCollections
      } else if (this.page === 'authors') {
        return this.$strings.LabelAuthors
      }
      return ''
    },
    selectedSeriesName() {
      if (this.page === 'series' && this.$route.params.id && this.$store.state.globals.series) {
        return this.$store.state.globals.series.name
      }
      return null
    },
    isPodcast() {
      return this.$store.getters['libraries/getCurrentLibraryMediaType'] === 'podcast'
    },
    menuItems() {
      if (!this.isBookLibrary) return []

      if (this.seriesBookPage) {
        return [
          {
            text: this.$strings.LabelCollapseSeries,
            value: 'collapse_subseries',
            icon: this.settings.collapseBookSeries ? 'check_box' : 'check_box_outline_blank'
          }
        ]
      } else {
        return [
          {
            text: this.$strings.LabelCollapseSeries,
            value: 'collapse_series',
            icon: this.settings.collapseSeries ? 'check_box' : 'check_box_outline_blank'
          }
        ]
      }
    }
  },
  methods: {
    clickMenuAction(action) {
      this.showMoreMenuDialog = false
      if (action === 'collapse_series') {
        this.settings.collapseSeries = !this.settings.collapseSeries
        this.saveSettings()
      } else if (action === 'collapse_subseries') {
        this.settings.collapseBookSeries = !this.settings.collapseBookSeries
        this.saveSettings()
      }
    },
    updateOrder() {
      this.saveSettings()
    },
    updateFilter() {
      this.saveSettings()
    },
    async selectFilterChip(value) {
      if (value === this.selectedFilter) return
      await this.$hapticsImpact()
      this.settings.mobileFilterBy = value
      this.saveSettings()
    },
    saveSettings() {
      this.$store.dispatch('user/updateUserSettings', this.settings)
    },
    async init() {
      this.bookshelfListView = await this.$localStore.getBookshelfListView()
      this.settings = { ...this.$store.state.user.settings }
      this.bookshelfReady = true
    },
    settingsUpdated(settings) {
      for (const key in settings) {
        this.settings[key] = settings[key]
      }
    },
    setTotalEntities(total) {
      this.totalEntities = total
    },
    async changeView() {
      this.bookshelfListView = !this.bookshelfListView
      await this.$hapticsImpact()
    },
    downloadSeries() {
      console.log('Download Series click')
      this.$eventBus.$emit('download-series-click')
    }
  },
  mounted() {
    this.init()
    this.$eventBus.$on('bookshelf-total-entities', this.setTotalEntities)
    this.$eventBus.$on('user-settings', this.settingsUpdated)
  },
  beforeDestroy() {
    this.$eventBus.$off('bookshelf-total-entities', this.setTotalEntities)
    this.$eventBus.$off('user-settings', this.settingsUpdated)
  }
}
</script>

<style>
#bookshelf-toolbar {
  box-shadow: 0px 5px 5px #11111155;
}
</style>
