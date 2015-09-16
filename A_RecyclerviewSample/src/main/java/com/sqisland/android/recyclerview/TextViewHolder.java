package com.sqisland.android.recyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TextViewHolder extends RecyclerView.ViewHolder {
  public TextView textView;
  public TextViewHolder(View itemView) {
    super(itemView);
    textView = (TextView) itemView.findViewById(R.id.text);
  }
}