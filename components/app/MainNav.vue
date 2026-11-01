<template>
  <nav id="main-nav" class="main-nav" aria-label="Main">
    <nuxt-link to="/" class="main-nav-logo" aria-label="Home">
      <img src="/Logo.png" alt="" class="h-10 w-10" />
    </nuxt-link>

    <div class="main-nav-items">
      <nuxt-link v-for="item in items" :key="item.to" :to="item.to" class="main-nav-item" :class="{ active: routeName === item.routeName }" :aria-current="routeName === item.routeName ? 'page' : null">
        <span class="main-nav-indicator">
          <span v-if="item.iconPack === 'abs-icons'" class="abs-icons" :class="`icon-${item.icon} ${item.iconClass || ''}`" />
          <span v-else class="material-symbols" :class="[item.iconClass || '', { fill: routeName === item.routeName }]">{{ item.icon }}</span>
        </span>
        <span class="main-nav-label">{{ item.text }}</span>
      </nuxt-link>
    </div>

    <button type="button" class="main-nav-item main-nav-more" aria-label="More" @click="showSideDrawer">
      <span class="main-nav-indicator">
        <span class="material-symbols text-2xl">menu</span>
      </span>
      <span class="main-nav-label">More</span>
    </button>
  </nav>
</template>

<script>
export default {
  computed: {
    currentLibrary() {
      return this.$store.getters['libraries/getCurrentLibrary']
    },
    currentLibraryIcon() {
      return this.currentLibrary?.icon || 'database'
    },
    userHasPlaylists() {
      return this.$store.state.libraries.numUserPlaylists
    },
    userIsAdminOrUp() {
      return this.$store.getters['user/getIsAdminOrUp']
    },
    isPodcast() {
      return this.$store.getters['libraries/getCurrentLibraryMediaType'] === 'podcast'
    },
    routeName() {
      return this.$route.name
    },
    items() {
      const home = { to: '/bookshelf', routeName: 'bookshelf', iconPack: 'abs-icons', icon: 'home', iconClass: 'text-xl', text: this.$strings.ButtonHome }
      const library = { to: '/bookshelf/library', routeName: 'bookshelf-library', iconPack: 'abs-icons', icon: this.currentLibraryIcon, iconClass: 'text-lg', text: this.$strings.ButtonLibrary }

      const items = [home]
      if (this.isPodcast) {
        items.push({ to: '/bookshelf/latest', routeName: 'bookshelf-latest', iconPack: 'abs-icons', icon: 'list', iconClass: 'text-xl', text: this.$strings.ButtonLatest }, library)
        if (this.userIsAdminOrUp) {
          items.push({ to: '/bookshelf/add-podcast', routeName: 'bookshelf-add-podcast', iconPack: 'material-symbols', icon: 'podcasts', iconClass: 'text-2xl', text: this.$strings.ButtonAdd })
        }
      } else {
        items.push(
          library,
          { to: '/bookshelf/series', routeName: 'bookshelf-series', iconPack: 'abs-icons', icon: 'columns', iconClass: 'text-lg', text: this.$strings.ButtonSeries },
          { to: '/bookshelf/collections', routeName: 'bookshelf-collections', iconPack: 'material-symbols', icon: 'collections_bookmark', iconClass: 'text-2xl', text: this.$strings.ButtonCollections },
          { to: '/bookshelf/authors', routeName: 'bookshelf-authors', iconPack: 'abs-icons', icon: 'authors', iconClass: 'text-2xl', text: this.$strings.ButtonAuthors }
        )
      }
      if (this.userHasPlaylists) {
        items.push({ to: '/bookshelf/playlists', routeName: 'bookshelf-playlists', iconPack: 'material-symbols', icon: 'queue_music', iconClass: 'text-2xl', text: this.$strings.ButtonPlaylists })
      }
      return items
    }
  },
  methods: {
    showSideDrawer() {
      this.$store.commit('setShowSideDrawer', true)
    }
  }
}
</script>

<style>
.main-nav {
  order: 2;
  flex-shrink: 0;
  display: flex;
  height: var(--shell-bottom);
  background-color: rgb(var(--color-primary));
  border-top: 1px solid rgb(var(--color-border) / 0.5);
}
.main-nav > .main-nav-logo,
.main-nav > .main-nav-more {
  display: none;
}
.main-nav-items {
  display: flex;
  flex: 1;
  min-width: 0;
}
.main-nav-item {
  flex: 1 1 0;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  color: rgb(var(--color-fg-muted));
}
.main-nav-item.active {
  color: rgb(var(--color-fg));
}
.main-nav-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 30px;
  border-radius: 15px;
  max-width: 100%;
}
.main-nav-item.active .main-nav-indicator {
  background-color: rgb(var(--color-brass) / 0.22);
  color: rgb(var(--color-brass));
}
.main-nav-label {
  font-size: 0.6875rem;
  line-height: 1rem;
  max-width: 100%;
  padding: 0 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

@media (min-width: 600px) {
  .main-nav {
    order: 0;
    flex-direction: column;
    align-items: center;
    width: var(--shell-left);
    height: 100%;
    padding: 8px 0;
    border-top: 0;
    border-right: 1px solid rgb(var(--color-border) / 0.5);
  }
  .main-nav > .main-nav-logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 48px;
    margin-bottom: 8px;
    flex-shrink: 0;
  }
  .main-nav-items {
    flex-direction: column;
    width: 100%;
    overflow-y: auto;
    gap: 4px;
  }
  .main-nav-item {
    flex: 0 0 auto;
    width: 100%;
    min-height: 56px;
  }
  .main-nav > .main-nav-more {
    display: flex;
    flex-shrink: 0;
    margin-top: auto;
  }
}
</style>
