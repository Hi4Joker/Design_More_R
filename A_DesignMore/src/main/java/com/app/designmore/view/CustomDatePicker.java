package com.app.designmore.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import java.lang.reflect.Field;

import com.app.designmore.R;

/**
 * Created by Joker on 2015/8/30.
 */
public class CustomDatePicker extends android.widget.DatePicker {

  public CustomDatePicker(Context context, AttributeSet attrs) {
    super(context, attrs);

    Class<?> internalRID = null;
    try {
      internalRID = Class.forName("com.android.internal.R$id");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    Field month = null;
    try {
      month = internalRID.getField("month");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    NumberPicker customMonth = null;
    try {
      customMonth = (NumberPicker) findViewById(month.getInt(null));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    Field day = null;
    try {
      day = internalRID.getField("day");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    NumberPicker customDay = null;
    try {
      customDay = (NumberPicker) findViewById(day.getInt(null));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    Field year = null;
    try {
      year = internalRID.getField("year");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    NumberPicker customYear = null;
    try {
      customYear = (NumberPicker) findViewById(year.getInt(null));
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }

    Class<?> numberPickerClass = null;
    try {
      numberPickerClass = Class.forName("android.widget.NumberPicker");
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    Field selectionDivider = null;
    try {
      selectionDivider = numberPickerClass.getDeclaredField("mSelectionDivider");
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }

    try {

      ColorDrawable colorDrawable =
          new ColorDrawable(getResources().getColor(R.color.accent_material_light));

      selectionDivider.setAccessible(true);
      selectionDivider.set(customMonth, colorDrawable);
      selectionDivider.set(customDay, colorDrawable);
      selectionDivider.set(customYear, colorDrawable);
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (Resources.NotFoundException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
  }
}
