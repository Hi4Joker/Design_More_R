package com.app.designmore.utils;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.designmore.R;

public class MarginDecoration extends RecyclerView.ItemDecoration {
  private int margin;

  public MarginDecoration(Context context, @DimenRes int dimen) {
    margin = context.getResources().getDimensionPixelSize(dimen);
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.set(0, 0, 0, margin);
  }
}