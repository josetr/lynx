package com.lynx.explorer.modules

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.app.TimePickerDialog
import android.view.MenuItem
import android.widget.PopupMenu
import android.widget.Toast
import com.lynx.jsbridge.LynxMethod
import com.lynx.jsbridge.LynxModule
import com.lynx.tasm.behavior.LynxContext
import com.lynx.tasm.behavior.ui.LynxUI
import java.security.SecureRandom
import java.util.Calendar

class NativeUtilModule(context: Context) : LynxModule(context) {
  private fun getContext(): Context {
    val lynxContext = mContext as LynxContext
    return lynxContext.getContext()
  }

  @LynxMethod
  fun randomId(radix: Int = 16): String {
    val secureRandom = SecureRandom()
    val randomInt = secureRandom.nextInt()
    return randomInt.toUInt().toString(radix)
  }
  
  @LynxMethod
  fun alert(message: String){
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity
    AlertDialog.Builder(activity)
      .setTitle("Alert")
      .setMessage(message)
      .setPositiveButton("OK", null)
      .show()
  }

  @LynxMethod
  fun clearFocus() {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity
    val e = activity?.currentFocus
    activity?.runOnUiThread{
      e?.clearFocus()
    }
  }

  @LynxMethod
  fun showToast(message: String){
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity
    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
  }
  
  @LynxMethod
  fun showDatePicker() {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity ?: return

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        activity,
        { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            Toast.makeText(activity, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()
        }, year, month, day
    )
    datePickerDialog.show()
  }
  
  @LynxMethod
  fun showTimePicker() {
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity ?: return

    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(
        activity,
        { _, selectedHour, selectedMinute ->
            val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            Toast.makeText(activity, "Selected Time: $selectedTime", Toast.LENGTH_SHORT).show()
        },
        hour,
        minute,
        true // true for 24-hour format, false for 12-hour format
    )
    timePickerDialog.show()
  }
  
  @LynxMethod
  fun showPopupMenu() { // options: 
    val lynxContext = mContext as LynxContext
    val activity = lynxContext.activity 

    if (activity == null) {
      Toast.makeText(activity, "null activity", Toast.LENGTH_SHORT).show()
      return;
    }
    
    val uiBase = lynxContext.findLynxUIByName("popup-anchor");

    if (uiBase == null) {
      Toast.makeText(activity, "null popup-anchor", Toast.LENGTH_SHORT).show()
      return;
    }
    
    val ui = uiBase as LynxUI<*>?;
    val view = ui?.view;
    
    if (view == null) {
      Toast.makeText(activity, "null view", Toast.LENGTH_SHORT).show()
      return;
    }
    
    val popupMenu = PopupMenu(activity, view)

    val options = arrayOf("Option 1", "Option 2", "Option 3")
    options.forEachIndexed { index, title ->
      popupMenu.menu.add(0, index, index, title)
    }

    popupMenu.setOnMenuItemClickListener { item: MenuItem ->
      Toast.makeText(activity, "Selected: ${item.title}", Toast.LENGTH_SHORT).show()
      // Handle menu item clicks here
      // You can send events back to JavaScript if needed
      // callback.invoke(item.itemId)
      true
    }

    popupMenu.show()
  }
}
