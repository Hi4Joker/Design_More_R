package com.joker.supportdesign.mvp.view.fragment;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.joker.supportdesign.R;
import com.joker.supportdesign.mvp.domain.Animal;
import com.joker.supportdesign.mvp.domain.event.RlScrollEvent;
import com.joker.supportdesign.mvp.domain.event.RlScrollYEvent;
import com.joker.supportdesign.mvp.presenter.FragmentPresenter;
import com.joker.supportdesign.mvp.presenter.FragmentPresenterImp;
import com.joker.supportdesign.mvp.viewInterface.FragmentView;
import com.joker.supportdesign.util.EventBusInstance;
import java.util.List;

/**
 * Created by Joker on 2015/6/29.
 */
public class ListFragment extends Fragment
    implements FragmentView, ListAdapter.OnListClickListener {

  private static final String TAG = ListFragment.class.getSimpleName();

  @Nullable @Bind(R.id.list_fragment_recycler_view) RecyclerView recyclerView;
  private View rootView;
  private FragmentPresenter<FragmentView> fragmentPresenter;
  private LinearLayoutManager linearLayoutManager;
  private ListAdapter listAdapter;
  private boolean isLoaded;
  private Snackbar snackbar;

  /*ScrollManager*/
  private static final int MIN_SCROLL_TO_HIDE = 10;
  private boolean hidden;
  private int accummulatedDy;
  private int totalDy;
  private int initialOffset;

  private Handler handler = new Handler() {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);

      int dy = msg.what;

      totalDy += dy;
      if (totalDy < initialOffset) {
        return;
      }
      if (dy > 0) {
        accummulatedDy = accummulatedDy > 0 ? accummulatedDy + dy : dy;
        if (accummulatedDy > MIN_SCROLL_TO_HIDE) {
          ListFragment.this.hideFab();
        }
      } else if (dy < 0) {
        accummulatedDy = accummulatedDy < 0 ? accummulatedDy + dy : dy;
        if (accummulatedDy < -MIN_SCROLL_TO_HIDE) {
          ListFragment.this.showFab();
        }
      }
    }
  };

  private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {

    @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
      super.onScrolled(recyclerView, dx, dy);

      EventBusInstance.getDefault().post(new RlScrollYEvent(dy));

      if (snackbar != null) {
        snackbar.dismiss();
        snackbar = null;
      } else {
        handler.sendEmptyMessage(dy);
      }
    }
  };

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    fragmentPresenter = new FragmentPresenterImp();
    fragmentPresenter.attachView(ListFragment.this);
  }

  @SuppressLint("InflateParams") @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    if (rootView == null) {
      rootView = inflater.inflate(R.layout.list_fragment_layout, null);
      ListFragment.this.initView();
      ListFragment.this.setListener();
    } else {
      ViewGroup viewParent = (ViewGroup) rootView.getParent();
      if (viewParent != null) {
        viewParent.removeView(rootView);
      }
    }

    return rootView;
  }

  private void initView() {
    ButterKnife.bind(ListFragment.this, rootView);

    TypedValue typedValue = new TypedValue();
    if (getActivity().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
      initialOffset = TypedValue.complexToDimensionPixelSize(typedValue.data,
          getResources().getDisplayMetrics()) - 10;
    }
  }

  private void setListener() {
    recyclerView.setOnScrollListener(onScrollListener);
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    fragmentPresenter.inflateData();
  }

  @Override public void setItems(List<Animal> dataItems) {

    if (!isLoaded) {
      isLoaded = true;

      linearLayoutManager = new LinearLayoutManager(getActivity());
      linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
      linearLayoutManager.setSmoothScrollbarEnabled(true);

      listAdapter = new ListAdapter(getActivity(), dataItems);
      listAdapter.setOnListClickListener(this);

      recyclerView.setLayoutManager(linearLayoutManager);
      recyclerView.setAdapter(listAdapter);
      listAdapter.notifyDataSetChanged();
    }
  }

  @Override public void showProgress() {

  }

  @Override public void hideProgress() {

  }

  @Override public void showMessage(int resString) {
    snackbar = Snackbar.make(recyclerView, resString, Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
          }
        });
    snackbar.getView().setBackgroundColor(Color.YELLOW);
    snackbar.show();
  }

  @Override public void onHeaderClick(View view, Palette palette, int position) {

    if (snackbar != null) {
      snackbar.dismiss();
    }

    snackbar = Snackbar.make(recyclerView, "点击了条目:" + position, Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
          }
        });

    TextView snackTv =
        (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
    TextView snackAction =
        (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_action);

    snackTv.setTextColor(Color.RED);
    snackAction.setTextColor(Color.RED);

    if (palette != null) {
      snackbar.getView()
          .setBackgroundColor(palette.getLightMutedColor(
              getActivity().getResources().getColor(R.color.primary_dark)));
    }
    snackbar.show();
  }

  @Override public void onItemLongClick(View view, int backgroundColor, int position) {

    if (snackbar != null) {
      snackbar.dismiss();
    }

    snackbar = Snackbar.make(recyclerView, "ripple color:" + position, Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
          }
        });

    TextView snackTv =
        (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
    TextView snackAction =
        (TextView) snackbar.getView().findViewById(android.support.design.R.id.snackbar_action);

    snackTv.setTextColor(Color.RED);
    snackAction.setTextColor(Color.GREEN);
    snackbar.getView().setBackgroundColor(backgroundColor);
    snackbar.show();
  }

  private void showFab() {
    if (hidden) {
      hidden = false;
      RlScrollEvent event = new RlScrollEvent(true);
      EventBusInstance.getDefault().post(event);
    }
  }

  private void hideFab() {
    if (!hidden) {
      hidden = true;
      RlScrollEvent event = new RlScrollEvent(false);

      EventBusInstance.getDefault().post(event);
    }
  }

  @Override public void onDestroy() {
    super.onDestroy();
    handler.removeCallbacksAndMessages(null);
    fragmentPresenter.detachView(true);
  }
}
