package com.app.designmore.view.dialogplus;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.app.designmore.R;


/**
 * @author Orhan Obut
 */
public class ListHolder implements HolderAdapter, AdapterView.OnItemClickListener {

  private int backgroundColor;

  private ListView listView;
  private OnHolderListener listener;
  private View.OnKeyListener keyListener;
  private View headerView;
  private View footerView;

  @Override
  public void addHeader(View view) {
    if (view == null) {
      return;
    }
    listView.addHeaderView(view);
    headerView = view;
  }

  @Override
  public void addFooter(View view) {
    if (view == null) {
      return;
    }
    listView.addFooterView(view);
    footerView = view;
  }

  @Override
  public void setAdapter(BaseAdapter adapter) {
    listView.setAdapter(adapter);
  }

  @Override
  public void setBackgroundColor(int colorResource) {
    this.backgroundColor = colorResource;
  }

  @Override
  public View getView(LayoutInflater inflater, ViewGroup parent) {
    View view = inflater.inflate(R.layout.dialog_plus_list, parent, false);
    listView = (ListView) view.findViewById(R.id.list);
    if (backgroundColor == 0) {
      backgroundColor = android.R.color.white;
    }
    listView.setBackgroundColor(parent.getResources().getColor(backgroundColor));
    listView.setOnItemClickListener(this);
    listView.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyListener == null) {
          throw new NullPointerException("keyListener should not be null");
        }
        return keyListener.onKey(v, keyCode, event);
      }
    });
    return view;
  }

  @Override
  public void setOnItemClickListener(OnHolderListener listener) {
    this.listener = listener;
  }

  @Override
  public void setOnKeyListener(View.OnKeyListener keyListener) {
    this.keyListener = keyListener;
  }

  @Override
  public View getInflatedView() {
    return listView;
  }

  @Override
  public View getHeader() {
    return headerView;
  }

  @Override
  public View getFooter() {
    return footerView;
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (listener == null) {
      return;
    }
    listener.onItemClick(parent.getItemAtPosition(position), view, position);
  }
}
