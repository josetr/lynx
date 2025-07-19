// Copyright 2025 The Lynx Authors. All rights reserved.
// Licensed under the Apache License Version 2.0 that can be found in the
// LICENSE file in the root directory of this source tree.
package com.lynx.explorer.input;

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.autofill.HintConstants
import com.lynx.react.bridge.Callback
import com.lynx.react.bridge.ReadableMap
import com.lynx.tasm.behavior.LynxContext
import com.lynx.tasm.behavior.LynxProp
import com.lynx.tasm.behavior.LynxUIMethod
import com.lynx.tasm.behavior.LynxUIMethodConstants
import com.lynx.tasm.behavior.ui.LynxUI
import com.lynx.tasm.event.LynxCustomEvent
import android.util.TypedValue
import org.json.JSONObject
import kotlin.toString

enum class KeyboardType {
  NONE, DECIMAL, NUMERIC, TEL, TEXT, SEARCH, EMAIL, URL
}

object JsonCache {
  var cachedJson: JSONObject? = null
}

fun loadJsonFromAssets(context: Context): JSONObject {
  JsonCache.cachedJson?.let {
    return it // Return from cache
  }

  val json = context.assets.open("icons.json")
    .bufferedReader().use { it.readText() }

  val result = JSONObject(json)
  JsonCache.cachedJson = result 
  return result
}

// https://github.com/facebook/react-native/blob/main/packages/react-native/ReactAndroid/src/main/java/com/facebook/react/views/textinput/ReactTextInputManager.java
class LynxExplorerInput(context: LynxContext) : LynxUI<AppCompatEditText>(context) {
  private val REACT_PROPS_AUTOFILL_HINTS_MAP: Map<String, String> = mapOf(
    "birthdate-day" to HintConstants.AUTOFILL_HINT_BIRTH_DATE_DAY,
    "birthdate-full" to HintConstants.AUTOFILL_HINT_BIRTH_DATE_FULL,
    "birthdate-month" to HintConstants.AUTOFILL_HINT_BIRTH_DATE_MONTH,
    "birthdate-year" to HintConstants.AUTOFILL_HINT_BIRTH_DATE_YEAR,
    "cc-csc" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_SECURITY_CODE,
    "cc-exp" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DATE,
    "cc-exp-day" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_DAY,
    "cc-exp-month" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_MONTH,
    "cc-exp-year" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_EXPIRATION_YEAR,
    "cc-number" to HintConstants.AUTOFILL_HINT_CREDIT_CARD_NUMBER,
    "email" to HintConstants.AUTOFILL_HINT_EMAIL_ADDRESS,
    "gender" to HintConstants.AUTOFILL_HINT_GENDER,
    "name" to HintConstants.AUTOFILL_HINT_PERSON_NAME,
    "name-family" to HintConstants.AUTOFILL_HINT_PERSON_NAME_FAMILY,
    "name-given" to HintConstants.AUTOFILL_HINT_PERSON_NAME_GIVEN,
    "name-middle" to HintConstants.AUTOFILL_HINT_PERSON_NAME_MIDDLE,
    "name-middle-initial" to HintConstants.AUTOFILL_HINT_PERSON_NAME_MIDDLE_INITIAL,
    "name-prefix" to HintConstants.AUTOFILL_HINT_PERSON_NAME_PREFIX,
    "name-suffix" to HintConstants.AUTOFILL_HINT_PERSON_NAME_SUFFIX,
    "password" to HintConstants.AUTOFILL_HINT_PASSWORD,
    "password-new" to HintConstants.AUTOFILL_HINT_NEW_PASSWORD,
    "postal-address" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS,
    "postal-address-country" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_COUNTRY,
    "postal-address-extended" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_EXTENDED_ADDRESS,
    "postal-address-extended-postal-code" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_EXTENDED_POSTAL_CODE,
    "postal-address-locality" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_LOCALITY,
    "postal-address-region" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_REGION,
    "postal-code" to HintConstants.AUTOFILL_HINT_POSTAL_CODE,
    "street-address" to HintConstants.AUTOFILL_HINT_POSTAL_ADDRESS_STREET_ADDRESS,
    "sms-otp" to HintConstants.AUTOFILL_HINT_SMS_OTP,
    "tel" to HintConstants.AUTOFILL_HINT_PHONE_NUMBER,
    "tel-country-code" to HintConstants.AUTOFILL_HINT_PHONE_COUNTRY_CODE,
    "tel-national" to HintConstants.AUTOFILL_HINT_PHONE_NATIONAL,
    "tel-device" to HintConstants.AUTOFILL_HINT_PHONE_NUMBER_DEVICE,
    "username" to HintConstants.AUTOFILL_HINT_USERNAME,
    "username-new" to HintConstants.AUTOFILL_HINT_NEW_USERNAME
  )

  @RequiresApi(Build.VERSION_CODES.O)
  override fun createView(context: Context): AppCompatEditText {
    return AppCompatEditText(context).apply {
      setLines(1)
      setSingleLine()
      setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
      gravity = Gravity.CENTER_VERTICAL
      background = null
      imeOptions = EditorInfo.IME_ACTION_NONE
      importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
      inputType = InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
      setHorizontallyScrolling(true)
      setPadding(0, 0, 0, 0)
      addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) {
          emitEvent("input", mapOf("value" to (s?.toString() ?: "")))
        }
      })
      onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
        if (!hasFocus) {
          emitEvent("blur", null)
        }
      }
    }
  }

  override fun onLayoutUpdated() {
    super.onLayoutUpdated()
    val paddingTop = mPaddingTop + mBorderTopWidth
    val paddingBottom = mPaddingBottom + mBorderBottomWidth
    val paddingLeft = mPaddingLeft + mBorderLeftWidth
    val paddingRight = mPaddingRight + mBorderRightWidth
    mView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
  }
  var set = false;
  @LynxProp(name = "defaultValue")
  fun setDefaultValue(value: String?) {
    if (set) return;
    set = true;
    val v = value ?: "";
    if (v != mView.text.toString()) {
      mView.setText(v)
    }
  }

  @LynxProp(name = "value")
  fun setValue(value: String?) {
    val v = value ?: "";
    if (v != mView.text.toString()) {
      mView.setText(v)
    }
  }

  @LynxUIMethod
  fun clear() {
    mView.setText("")
  }

  @LynxProp(name = "placeholder")
  fun setPlaceHolder(value: String?) {
    mView.hint = value ?: ""
  }

  @LynxProp(name = "maxLength")
  fun setMaxLength(value: Int) {
    mView.filters = arrayOf(InputFilter.LengthFilter(value))
  }

  @LynxProp(name = "editable")
  fun setEditable(value: Boolean) {
    mView.isEnabled = value;
  }
  
  @LynxProp(name = "autoCorrect")
  fun setAutoCorrect(value: Boolean) {
    if (value)
      mView.inputType = mView.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
    else
      mView.inputType = mView.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS.inv();
  }

  @RequiresApi(Build.VERSION_CODES.O)
  @LynxProp(name = "autoComplete")
  fun setAutoComplete(value: String?) {
    if (value == null || value == "off") {
      view.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO;
    } else if (REACT_PROPS_AUTOFILL_HINTS_MAP.containsKey(value)) {
      view.setAutofillHints(REACT_PROPS_AUTOFILL_HINTS_MAP[value]);
    }
    else {
      throw IllegalArgumentException("Invalid autoCorrect `$value`")
    }
  }

  @RequiresApi(Build.VERSION_CODES.Q)
  @LynxProp(name = "cursorColor")
  fun setCursorColor(value: String) {
    val cursorDrawable = mView.textCursorDrawable
    cursorDrawable?.setTint(Color.parseColor(value))
    // cursorDrawable?.setColorFilter(Color.parseColor(value), PorterDuff.Mode.SRC_IN) 
  }

  @LynxProp(name = "textColor")
  fun setColor(value: String){
    mView.setTextColor(Color.parseColor(value))
  }
  
  @LynxProp(name = "placeholderTextColor")
  fun setPlaceholderTextColor(value: String) {
    mView.setHintTextColor(Color.parseColor(value))
  }

  @LynxProp(name = "textAlign")
  fun setTextAlign(value: String) {
    if (value == "center")
      mView.gravity = mView.gravity or Gravity.CENTER_HORIZONTAL;
    else
      mView.gravity = mView.gravity and Gravity.CENTER_HORIZONTAL.inv();
  }

  @LynxProp(name = "secureTextEntry")
  fun setSecureTextEntry(value: Boolean) {
    if(value)
      mView.inputType = mView.inputType or InputType.TYPE_TEXT_VARIATION_PASSWORD;
    else
      mView.inputType = mView.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD.inv()
  }

  @LynxProp(name = "inputMode")
  fun setInputMode(value: String?) {
    mView.inputType = when (enumValues<KeyboardType>().first { it.name.equals(value, ignoreCase = true) }) {
      KeyboardType.NONE -> InputType.TYPE_CLASS_TEXT
      KeyboardType.DECIMAL -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
      KeyboardType.NUMERIC -> InputType.TYPE_CLASS_NUMBER
      KeyboardType.TEL -> InputType.TYPE_CLASS_PHONE
      KeyboardType.SEARCH -> InputType.TYPE_CLASS_TEXT // or InputType.TYPE_TEXT_VARIATION_WEB_SEARCH
      KeyboardType.EMAIL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
      KeyboardType.URL -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
      else -> InputType.TYPE_CLASS_TEXT
    }
  }
  
  @LynxUIMethod
  fun focus(params: ReadableMap, callback: Callback) {
    if (mView.requestFocus()) {
      if (showSoftInput()) {
        callback.invoke(LynxUIMethodConstants.SUCCESS)
      } else {
        callback.invoke(LynxUIMethodConstants.UNKNOWN, "fail to show keyboard")
      }
    } else {
      callback.invoke(LynxUIMethodConstants.UNKNOWN, "fail to focus")
    }
  }

  private fun showSoftInput(): Boolean {
    val imm = lynxContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return imm.showSoftInput(mView, InputMethodManager.SHOW_IMPLICIT, null)
  }

  private fun emitEvent(name: String, value: Map<String, Any>?) {
    val detail = LynxCustomEvent(sign, name)
    value?.forEach { (key, v) -> detail.addDetail(key, v) }
    lynxContext.eventEmitter.sendCustomEvent(detail)
  }
}
