package com.app.designmore.manager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.app.designmore.R;

public class DividerDecoration extends RecyclerView.ItemDecoration {
  private int margin;

  public DividerDecoration(Context context, @DimenRes int dimen) {
    margin = context.getResources().getDimensionPixelSize(dimen);
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.set(0, 0, 0, margin);
  }
}