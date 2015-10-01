package com.app.designmore.manager;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

public class MarginDecoration extends RecyclerView.ItemDecoration {
  private int margin;

  public MarginDecoration(Context context, @DimenRes int dimen) {
    this.margin = context.getResources().getDimensionPixelSize(dimen);

    Log.e("joker", margin + "");
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.set(margin, margin, margin, margin);
  }
}