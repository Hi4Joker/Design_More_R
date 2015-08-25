package com.joker.app.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Joker on 2015/7/26.
 */
public class AutoFocusView extends View {

  public AutoFocusView(Context context) {
    super(context);
  }

  public AutoFocusView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoFocusView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public AutoFocusView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override public boolean hasFocus() {
    return true;
  }

  @Override public boolean hasFocusable() {
    return true;
  }
}
