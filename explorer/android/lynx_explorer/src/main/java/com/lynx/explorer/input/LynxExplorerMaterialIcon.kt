package com.lynx.explorer.input

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.res.ResourcesCompat
import com.lynx.explorer.R
import com.lynx.tasm.behavior.LynxContext
import com.lynx.tasm.behavior.LynxProp
import com.lynx.tasm.behavior.ui.LynxUI
import org.json.JSONObject

class LynxMaterialIcon(context: LynxContext) : LynxUI<AppCompatTextView>(context) {
  var cachedJson: JSONObject? = null
  
  override fun createView(context: Context): AppCompatTextView {
    cachedJson = loadJsonFromAssets(context)
    val textView = AppCompatTextView(context)
    textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
    textView.typeface = ResourcesCompat.getFont(context, R.font.material_community_icons)
    return textView
  }

  @LynxProp(name = "value")
  fun setValue(value: String?) {
    val codePoint = cachedJson?.optString(value)?.toIntOrNull()
    if (codePoint != null)
      mView.text = String(Character.toChars(codePoint))
    else
      mView.text = ""
  }

  @LynxProp(name = "color")
  fun setColor(value: String) {
    mView.setTextColor(Color.parseColor(value))
  }

  @LynxProp(name = "fontSize")
  fun setTextSize(value: Float) {
    mView.setTextSize(TypedValue.COMPLEX_UNIT_SP, value)
  }
}
