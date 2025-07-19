package com.lynx.explorer.modules

// import com.lynx.jsbridge.Callback
import android.content.Context
import com.lynx.jsbridge.LynxMethod
import com.lynx.jsbridge.LynxModule
import com.lynx.tasm.behavior.LynxContext

class NativeLocalStorageModule(context: Context) : LynxModule(context) {
  private val PREF_NAME = "MyLocalStorage"

  private fun getContext(): Context {
    val lynxContext = mContext as LynxContext
    return lynxContext.getContext()
  }

  @LynxMethod
  fun setItem(key: String, value: String) {
    val sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.commit()
  }

  @LynxMethod
  fun getItem(key: String): String? {
    val sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, "")
  }

  @LynxMethod
  fun removeItem(key: String) {
    val sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove(key)
    editor.commit()
  }

  @LynxMethod
  fun clear() {
    val sharedPreferences = getContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.clear()
    editor.commit()
  }
}


