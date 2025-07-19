package com.lynx.explorer.modules

import android.content.Context
import android.graphics.Rect
import android.view.inputmethod.InputMethodManager
import com.lynx.jsbridge.LynxMethod
import com.lynx.jsbridge.LynxModule
import com.lynx.tasm.behavior.LynxContext

class KeyboardModule(context: Context) : LynxModule(context) {
  @LynxMethod
  fun dismiss() {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity ?: return
    val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
      inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
    }
  }

  @LynxMethod
  fun isVisible(): Boolean {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity ?: return false
    val rootView = activity.window.decorView
    val rect = Rect()
    rootView.getWindowVisibleDisplayFrame(rect)
    val screenHeight = rootView.height
    val keypadHeight = screenHeight - rect.bottom
    return keypadHeight > screenHeight * 0.15 // 0.15 ratio is perhaps enough to determine keypad height.
  }

  @LynxMethod
  fun metrics(): Map<String, Any> {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity ?: return emptyMap()
    val displayMetrics = activity.resources.displayMetrics
    return mapOf(
      "width" to displayMetrics.widthPixels / displayMetrics.density,
      "height" to displayMetrics.heightPixels / displayMetrics.density,
      "scale" to displayMetrics.density,
      "fontScale" to activity.resources.configuration.fontScale
    )
  }
}
