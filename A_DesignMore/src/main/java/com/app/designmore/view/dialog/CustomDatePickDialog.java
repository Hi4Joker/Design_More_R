package com.app.designmore.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.app.designmore.R;

public class CustomDatePickDialog implements OnDateChangedListener, OnTimeChangedListener {
  private static final String TAG = "ProfileActivity";
  private DatePicker datePicker;
  private String dateTime;
  private String initDateTime;
  private AppCompatDialog alertDialog;
  private Context context;

  public CustomDatePickDialog(Context context) {

    this.context = context;
  }

  public void showPickerDialog(final TextView textView, final String initDateTime) {

    LinearLayout dateTimeLayout =
        (LinearLayout) View.inflate(context, R.layout.center_profile_data_dailaog_layout, null);
    datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.profile_data_dialog_date_picker);

    CustomDatePickDialog.this.init(initDateTime, datePicker);

    alertDialog = new AlertDialog.Builder(context).setTitle(initDateTime)
        .setView(dateTimeLayout)
        .setCancelable(false)
        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            textView.setText(dateTime);
          }
        })
        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            textView.setText(initDateTime);
          }
        })
        .create();
    alertDialog.getWindow().setWindowAnimations(R.style.AnimCenter);
    alertDialog.show();

    CustomDatePickDialog.this.onDateChanged(null, 0, 0, 0);
  }

  public void init(String currentDate, DatePicker datePicker) {

    this.initDateTime = currentDate;

    Calendar calendar = Calendar.getInstance();
    if (!(null == this.initDateTime || "".equals(this.initDateTime))) {
      calendar = this.getCalendarByInitData(initDateTime);
    } else {
      this.initDateTime =
          calendar.get(Calendar.YEAR) + "年" + calendar.get(Calendar.MONTH) + "月" + calendar.get(
              Calendar.DAY_OF_MONTH) + "日";
    }

    datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH), this);
  }

  private Calendar getCalendarByInitData(String initDateTime) {
    Calendar calendar = Calendar.getInstance();

    String date = CustomDatePickDialog.this.splitDate(initDateTime, "日", "index", "front");

    String yearStr = CustomDatePickDialog.this.splitDate(date, "年", "index", "front");
    String monthAndDay = CustomDatePickDialog.this.splitDate(date, "年", "index", "back");

    String monthStr = CustomDatePickDialog.this.splitDate(monthAndDay, "月", "index", "front");
    String dayStr = CustomDatePickDialog.this.splitDate(monthAndDay, "月", "index", "back");

    int currentYear = Integer.valueOf(yearStr.trim()).intValue();
    int currentMonth = Integer.valueOf(monthStr.trim()).intValue() - 1;
    int currentDay = Integer.valueOf(dayStr.trim()).intValue();

    calendar.set(currentYear, currentMonth, currentDay);
    return calendar;
  }

  private String splitDate(String srcStr, String pattern, String indexOrLast, String frontOrBack) {

    String result = "";
    int loc = -1;
    if (indexOrLast.equalsIgnoreCase("index")) {
      loc = srcStr.indexOf(pattern);
    } else {
      loc = srcStr.lastIndexOf(pattern);
    }
    if (frontOrBack.equalsIgnoreCase("front")) {
      if (loc != -1) result = srcStr.substring(0, loc);
    } else {
      if (loc != -1) result = srcStr.substring(loc + 1, srcStr.length());
    }
    return result;
  }

  @Override public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    Calendar calendar = Calendar.getInstance();

    calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日");

    this.dateTime = simpleDateFormat.format(calendar.getTime());
    alertDialog.setTitle(dateTime);
  }

  @Override public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
    onDateChanged(null, 0, 0, 0);
  }
}
