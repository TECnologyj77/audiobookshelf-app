package com.audiobookshelf.app.plugins

import android.view.View
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Reports the device posture and hinge position to the web UI.
 *
 * posture: "none" when no fold is inside the window (cover screen, split screen, regular phones),
 * "flat" when fully open, "tabletop" when half open with a horizontal hinge and
 * "book" when half open with a vertical hinge.
 *
 * left/top/right/bottom are the hinge edges in physical pixels relative to the WebView.
 */
@CapacitorPlugin(name = "AbsFoldState")
class AbsFoldState : Plugin() {
  private var layoutJob: Job? = null
  private var foldingFeature: FoldingFeature? = null
  private var lastEmitted: String? = null

  // The WebView moves when window insets change, which shifts the hinge relative to it
  private val webViewLayoutListener = View.OnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
    emitIfChanged()
  }

  override fun load() {
    getBridge().webView.addOnLayoutChangeListener(webViewLayoutListener)
  }

  override fun handleOnStart() {
    super.handleOnStart()
    layoutJob?.cancel()
    layoutJob = CoroutineScope(Dispatchers.Main.immediate).launch {
      WindowInfoTracker.getOrCreate(activity).windowLayoutInfo(activity).collect { layoutInfo ->
        foldingFeature = layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
        emitIfChanged()
      }
    }
  }

  override fun handleOnStop() {
    super.handleOnStop()
    layoutJob?.cancel()
    layoutJob = null
  }

  override fun handleOnDestroy() {
    super.handleOnDestroy()
    getBridge().webView.removeOnLayoutChangeListener(webViewLayoutListener)
  }

  @PluginMethod
  fun getFoldState(call: PluginCall) {
    call.resolve(buildState())
  }

  private fun emitIfChanged() {
    val state = buildState()
    val serialized = state.toString()
    if (serialized == lastEmitted) return
    lastEmitted = serialized
    notifyListeners("onFoldStateChanged", state)
  }

  private fun buildState(): JSObject {
    val feature = foldingFeature
    val state = JSObject()
    state.put("posture", getPosture(feature))

    if (feature == null) {
      state.put("left", 0)
      state.put("top", 0)
      state.put("right", 0)
      state.put("bottom", 0)
      return state
    }

    val webViewLocation = IntArray(2)
    getBridge().webView.getLocationInWindow(webViewLocation)
    state.put("left", feature.bounds.left - webViewLocation[0])
    state.put("top", feature.bounds.top - webViewLocation[1])
    state.put("right", feature.bounds.right - webViewLocation[0])
    state.put("bottom", feature.bounds.bottom - webViewLocation[1])
    return state
  }

  private fun getPosture(feature: FoldingFeature?): String {
    if (feature == null) return "none"
    if (feature.state != FoldingFeature.State.HALF_OPENED) return "flat"
    return if (feature.orientation == FoldingFeature.Orientation.HORIZONTAL) "tabletop" else "book"
  }
}
