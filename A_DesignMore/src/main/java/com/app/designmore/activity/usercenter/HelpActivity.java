package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.adapter.HelpAdapter;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.HelpEntity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.Collections;
import java.util.List;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/8/26.
 */
public class HelpActivity extends BaseActivity implements HelpAdapter.Callback {

  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.help_layout_root) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.help_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.help_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.help_layout_srl) SwipeRefreshLayout swipeRefreshLayout;

  private HelpAdapter helpAdapter;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HelpActivity.this.loadData();
    }
  };

  public static void startFromLocation(SettingActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, HelpActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_setting_help_layout);
    ButterKnife.bind(this);

    HelpActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    HelpActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(HelpActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("帮助中心");

     /*创建Adapter*/
    HelpActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          HelpActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      HelpActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        HelpActivity.this.loadData();
      }
    });

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HelpActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    helpAdapter = new HelpAdapter(this);
    helpAdapter.setCallback(HelpActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(helpAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
  }

  private void loadData() {

    /*Action=GetQAList*/
    LoginRetrofit.getInstance()
        .getHelpList(Collections.singletonMap("Action", "GetQAList"))
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            if (!progressLayout.isContent()) progressLayout.showContent();
          }
        })
        .finallyDo(new Action0() {
          @Override public void call() {
            if (swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
          }
        })
        .compose(HelpActivity.this.<List<HelpEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(helpAdapter);
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            HelpActivity.this.loadData();
          }
        });
    ;
  }

  @Override public void onError(Throwable error) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
        getResources().getString(R.string.six_word_title),
        getResources().getString(R.string.six_word_content),
        getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(HelpActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            HelpActivity.this.finish();
          }
        });
  }
}
