package com.app.designmore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by Joker on 2015/10/14.
 */
public class AutoFitRecyclerView extends RecyclerView {

  private static final String TAG = AutoFitRecyclerView.class.getSimpleName();

  private GridLayoutManager gridLayoutManager;
  private int columnWidth;

  public AutoFitRecyclerView(Context context) {
    super(context);
    AutoFitRecyclerView.this.init(context, null);
  }

  public AutoFitRecyclerView(Context context, AttributeSet attrs) {
    super(context, attrs);
    AutoFitRecyclerView.this.init(context, null);
  }

  public AutoFitRecyclerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    AutoFitRecyclerView.this.init(context, null);
  }

  private void init(Context context, AttributeSet attributeSet) {

    if (attributeSet != null) {
      int[] attrsArray = { android.R.attr.columnWidth };

      TypedArray array = null;
      try {
        array = context.obtainStyledAttributes(attributeSet, attrsArray);
        columnWidth = array.getDimensionPixelSize(0, -1);
      } finally {
        array.recycle();
      }
    }
  }

  @Override protected void onMeasure(int widthSpec, int heightSpec) {
    super.onMeasure(widthSpec, heightSpec);
    if (columnWidth > 0) {
      int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
      gridLayoutManager.setSpanCount(spanCount);
    }
  }
}
