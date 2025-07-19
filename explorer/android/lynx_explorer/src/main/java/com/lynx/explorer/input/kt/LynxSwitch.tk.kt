package com.lynx.explorer.input.kt;

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import com.lynx.react.bridge.Callback
import com.lynx.react.bridge.ReadableMap
import com.lynx.tasm.behavior.LynxContext
import com.lynx.tasm.behavior.LynxProp
import com.lynx.tasm.behavior.LynxUIMethod
import com.lynx.tasm.behavior.LynxUIMethodConstants
import com.lynx.tasm.behavior.ui.LynxUI
import com.lynx.tasm.event.LynxCustomEvent
import androidx.appcompat.widget.SwitchCompat
import androidx.core.graphics.ColorUtils
import android.view.ViewGroup

class LynxExplorerSwitch(context: LynxContext) : LynxUI<SwitchCompat>(context) {
  override fun createView(context: Context): SwitchCompat {
    return SwitchCompat(context).apply {
      layoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT
      )
      setOnCheckedChangeListener { _, isChecked ->
        emitEvent("change", mapOf("checked" to isChecked))
      }
    }
  }

  @LynxProp(name = "checked")
  fun setChecked(value: Boolean) {
    if (mView.isChecked != value) {
      mView.isChecked = value
    }
  }

  @LynxProp(name = "enabled")
  fun setEnabled(value: Boolean) {
    mView.isEnabled = value
  }

  @LynxProp(name = "disabled")
  fun setDisabled(value: Boolean) {
    mView.isEnabled = !value
  }

  @LynxProp(name = "thumbColor")
  fun setThumbColor(color: String) {
    mView.thumbTintList = ColorStateList.valueOf(Color.parseColor(color))
  }

  @LynxProp(name = "trackColor")
  fun setTrackColor(color: String) {
    val states = arrayOf(
      intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked),     // enabled + checked
      intArrayOf(android.R.attr.state_enabled, -android.R.attr.state_checked),    // enabled + unchecked
      intArrayOf(-android.R.attr.state_enabled, android.R.attr.state_checked),    // disabled + checked
      intArrayOf(-android.R.attr.state_enabled, -android.R.attr.state_checked)    // disabled + unchecked
    )

    val colors = intArrayOf(
      Color.parseColor(color),
      ColorUtils.setAlphaComponent(Color.parseColor(color), (255*0.15).toInt()) ,
      Color.parseColor(color),
      ColorUtils.setAlphaComponent(Color.parseColor(color), (255*0.10).toInt())
    )

    mView.trackTintList = ColorStateList(states, colors)
  }
  
  @LynxUIMethod
  fun toggle(params: ReadableMap, callback: Callback) {
    mView.toggle()
    emitEvent("change", mapOf("checked" to mView.isChecked))
    callback.invoke(LynxUIMethodConstants.SUCCESS)
  }

  private fun emitEvent(name: String, value: Map<String, Any>?) {
    val detail = LynxCustomEvent(sign, name)
    value?.forEach { (key, v) -> detail.addDetail(key, v) }
    lynxContext.eventEmitter.sendCustomEvent(detail)
  }
}

// public fun String.toColorInt(): Int { return 0; }
