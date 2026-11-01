import { Capacitor, registerPlugin } from '@capacitor/core'

/**
 * Keeps the Vuex window size and fold state current and mirrors them onto <html>:
 *   classes  size-compact | size-medium | size-expanded, posture-tabletop, posture-book
 *   CSS vars --fold-left, --fold-top, --fold-right, --fold-bottom (hinge edges in CSS px)
 */
export default ({ store }) => {
  const root = document.documentElement

  let frame = null
  const measure = () => {
    if (frame) return
    frame = requestAnimationFrame(() => {
      frame = null
      store.commit('globals/setWindowSize', { width: window.innerWidth, height: window.innerHeight })
    })
  }
  window.addEventListener('resize', measure)
  window.addEventListener('orientationchange', () => {
    measure()
    // iOS can report the new width up to 250ms after the orientation event
    setTimeout(measure, 250)
  })
  measure()

  const sizeClasses = ['size-compact', 'size-medium', 'size-expanded']
  store.watch(
    (state, getters) => getters['globals/getSizeClass'],
    (sizeClass) => {
      root.classList.remove(...sizeClasses)
      root.classList.add(`size-${sizeClass}`)
    },
    { immediate: true }
  )

  store.watch(
    (state) => state.globals.foldState,
    (foldState) => {
      root.classList.toggle('posture-tabletop', foldState.posture === 'tabletop')
      root.classList.toggle('posture-book', foldState.posture === 'book')
      root.style.setProperty('--fold-left', `${foldState.hingeLeft}px`)
      root.style.setProperty('--fold-top', `${foldState.hingeTop}px`)
      root.style.setProperty('--fold-right', `${foldState.hingeRight}px`)
      root.style.setProperty('--fold-bottom', `${foldState.hingeBottom}px`)
    },
    { immediate: true }
  )

  if (Capacitor.getPlatform() !== 'android') return

  // Native values are physical pixels relative to the WebView
  const applyNativeState = (nativeState) => {
    const scale = window.devicePixelRatio || 1
    store.commit('globals/setFoldState', {
      posture: nativeState.posture,
      hingeLeft: nativeState.left / scale,
      hingeTop: nativeState.top / scale,
      hingeRight: nativeState.right / scale,
      hingeBottom: nativeState.bottom / scale
    })
  }

  const AbsFoldState = registerPlugin('AbsFoldState')
  AbsFoldState.addListener('onFoldStateChanged', applyNativeState)
  AbsFoldState.getFoldState()
    .then(applyNativeState)
    .catch((error) => console.error('[foldable] Failed to get fold state', error))
}
