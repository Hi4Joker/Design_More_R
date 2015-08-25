package com.joker.supportdesign.mvp.domain.event;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Joker on 2015/7/2.
 */
public class FragmentBindEvent {

  private View view;

  public FragmentBindEvent(View view) {
    this.view = view;
  }

  public View getView() {
    return view;
  }
}
