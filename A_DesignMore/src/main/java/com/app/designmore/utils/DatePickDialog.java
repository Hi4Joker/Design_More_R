package com.app.designmore.utils;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.app.designmore.R;

public class DatePickDialog implements OnDateChangedListener, OnTimeChangedListener {
  private static final String TAG = "ProfileActivity";
  private DatePicker datePicker;
  private String dateTime;
  private String initDateTime;
  private AppCompatDialog alertDialog;

  private DatePickDialog() {
  }

  private static class SingleTonHolder {
    private static DatePickDialog instance = new DatePickDialog();
  }

  public static DatePickDialog getInstance() {
    return SingleTonHolder.instance;
  }

  public void showPickerDialog(Context context, final TextView textView,
      final String initDateTime) {

    LinearLayout dateTimeLayout =
        (LinearLayout) View.inflate(context, R.layout.center_profile_data_dailaog_layout, null);
    datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.profile_data_dialog_date_picker);

    DatePickDialog.this.init(initDateTime, datePicker);

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
    alertDialog.show();

    DatePickDialog.this.onDateChanged(null, 0, 0, 0);
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

    String date = DatePickDialog.this.splitDate(initDateTime, "日", "index", "front"); // ����

    String yearStr = DatePickDialog.this.splitDate(date, "年", "index", "front");
    String monthAndDay = DatePickDialog.this.splitDate(date, "年", "index", "back");

    String monthStr = DatePickDialog.this.splitDate(monthAndDay, "月", "index", "front");
    String dayStr = DatePickDialog.this.splitDate(monthAndDay, "月", "index", "back");

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
