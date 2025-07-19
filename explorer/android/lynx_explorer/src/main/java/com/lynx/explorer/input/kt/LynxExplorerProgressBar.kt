package com.lynx.explorer.input.kt

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ProgressBar
import com.lynx.react.bridge.Callback
import com.lynx.react.bridge.ReadableMap
import com.lynx.tasm.behavior.LynxContext
import com.lynx.tasm.behavior.LynxProp
import com.lynx.tasm.behavior.LynxUIMethod
import com.lynx.tasm.behavior.LynxUIMethodConstants
import com.lynx.tasm.behavior.ui.LynxUI
import com.lynx.tasm.event.LynxCustomEvent
import androidx.core.graphics.ColorUtils
import com.lynx.explorer.input.kt.HSLParser.parseHslColor


object HSLParser {
  fun parseHslColor(hslString: String): Int? {
    val clean = hslString
      .lowercase()
      .removePrefix("hsl(")
      .removeSuffix(")")
      .trim()

    val parts = clean.split(Regex("\\s+"))

    if (parts.size != 3) return null

    val hue = parts[0].toFloatOrNull()?.let { (it % 360 + 360) % 360 } ?: return null
    val sat = parsePercentage(parts[1]) ?: return null
    val light = parsePercentage(parts[2]) ?: return null

    val hsl = floatArrayOf(hue, sat.coerceIn(0f, 1f), light.coerceIn(0f, 1f))
    return ColorUtils.HSLToColor(hsl)
  }

  private fun parsePercentage(value: String): Float? {
    return if (value.endsWith("%")) {
      value.removeSuffix("%").toFloatOrNull()?.div(100f)
    } else {
      null
    }
  }
}

class LynxExplorerProgressBar(context: LynxContext) : LynxUI<ProgressBar>(context) {

  override fun createView(context: Context): ProgressBar {
    return ProgressBar(context).apply {
      isIndeterminate = false // By default, set it as determinate progress bar.
    }
  }

  @LynxProp(name = "progress")
  fun setProgress(value: Int) {
    if (mView.progress != value) {
      mView.progress = value
    }
  }

  @LynxProp(name = "max")
  fun setMax(value: Int) {
    if (mView.max != value) {
      mView.max = value
    }
  }

  @LynxProp(name = "indeterminate")
  fun setIndeterminate(value: Boolean) {
    mView.isIndeterminate = value
  }

  @LynxProp(name = "color")
  fun setColor(value: String) {
    val hsl = parseHslColor(value)
    val c = hsl ?: Color.parseColor(value)
    mView.indeterminateTintList = ColorStateList.valueOf(c);
  }

  @LynxUIMethod
  fun setProgressValue(params: ReadableMap, callback: Callback) {
    val progress = params.getInt("progress")
    mView.progress = progress
    emitEvent("progressChange", mapOf("progress" to progress))
    callback.invoke(LynxUIMethodConstants.SUCCESS)
  }

  private fun emitEvent(name: String, value: Map<String, Any>?) {
    val detail = LynxCustomEvent(sign, name)
    value?.forEach { (key, v) -> detail.addDetail(key, v) }
    lynxContext.eventEmitter.sendCustomEvent(detail)
  }
}
