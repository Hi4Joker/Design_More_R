package com.app.designmore.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.adapter.SearchAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.retrofit.SearchRetrofit;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/8/30.
 */
public class SearchActivity extends BaseActivity implements SearchAdapter.Callback {

  private static final String TAG = SearchActivity.class.getSimpleName();

  @Nullable @Bind(R.id.search_layout_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.search_layout_toolbar_rfl) RevealFrameLayout revealRootView;
  @Nullable @Bind(R.id.search_layout_et) AppCompatEditText searchEt;
  @Nullable @Bind(R.id.search_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.search_layout_recycler_root_view) LinearLayout recyclerRootView;
  @Nullable @Bind(R.id.search_layout_rv) RecyclerView recyclerView;

  private SupportAnimator revealAnimator;

  /*键盘*/
  private InputMethodManager inputMethodManager;
  /*adapter*/
  private SearchAdapter searchAdapter;
  private List<SearchItemEntity> items;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      SearchActivity.this.loadData();
    }
  };

  public static void navigateToSearch(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, SearchActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);

    SearchActivity.this.initView(savedInstanceState);
    SearchActivity.this.setListener();
  }

  @Override public void initView(Bundle savedInstanceState) {

    SearchActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    inputMethodManager =
        (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);

    /*创建Adapter*/
    SearchActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      toolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          toolbar.getViewTreeObserver().removeOnPreDrawListener(this);
          SearchActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      SearchActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    GridLayoutManager gridLayoutManager = new GridLayoutManager(SearchActivity.this, 5);
    gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    gridLayoutManager.setSmoothScrollbarEnabled(true);

    searchAdapter = new SearchAdapter(this);
    searchAdapter.setCallback(SearchActivity.this);

    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(searchAdapter);
  }

  private void setListener() {

    searchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
          /*关闭键盘*/
          inputMethodManager.hideSoftInputFromWindow(revealRootView.getApplicationWindowToken(), 0);
          return true;
        }
        return false;
      }
    });
  }

  private void startEnterAnim() {

    ViewCompat.setLayerType(progressLayout, ViewCompat.LAYER_TYPE_HARDWARE, null);
    ViewCompat.setAlpha(progressLayout, 0.0f);
    ViewCompat.setTranslationY(progressLayout, progressLayout.getHeight());

    final Rect bounds = new Rect();
    revealRootView.getHitRect(bounds);

    revealAnimator = ViewAnimationUtils.createCircularReveal(revealRootView.getChildAt(0),
        bounds.right - bounds.left, 0, 0, Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {

        if (progressLayout != null) {
          ViewCompat.animate(progressLayout)
              .alpha(1.0f)
              .translationY(0.0f)
              .setDuration(Constants.MILLISECONDS_400);
        }
      }

      @Override public void onAnimationEnd() {
        if (progressLayout != null) SearchActivity.this.loadData();
      }
    });
    revealAnimator.start();
  }

  /**
   * 加载热搜数据
   */
  private void loadData() {

    /*Action=GetKeywordList*/
    Map<String, String> params = new HashMap<>(1);
    params.put("Action", "GetKeywordList");

    SearchRetrofit.getInstance()
        .getHotSearchList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示加载界面*/
            progressLayout.showLoading();
          }
        })
        .doOnNext(new Action1<List<SearchItemEntity>>() {
          @Override public void call(List<SearchItemEntity> searchItemEntities) {
            SearchActivity.this.items = searchItemEntities;
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            /*显示内容界面*/
            if (items != null && items.size() != 0) {
              recyclerRootView.setVisibility(View.VISIBLE);
              progressLayout.showContent();
            }
          }
        })
        .compose(SearchActivity.this.<List<SearchItemEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(searchAdapter);
  }

  @Nullable @OnClick(R.id.search_layout_btn) void onSearchClick() {

     /*关闭键盘*/
    inputMethodManager.hideSoftInputFromWindow(revealRootView.getApplicationWindowToken(), 0);
  }

  @Override public void onError(Throwable error) {

    if (error instanceof TimeoutException) {
      SearchActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      SearchActivity.this.showError("网络连接异常", "请点击重试");
    } else if (error instanceof WebServiceException) {
      SearchActivity.this.showError(getResources().getString(R.string.service_exception_title),
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showError(String errorTitle, String errorContent) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
        errorContent, getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  @Override public void exit() {
    SearchActivity.this.finish();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (revealAnimator != null && revealAnimator.isRunning()) revealAnimator.cancel();
  }

  @Override public void onItemClick(int position) {
    // TODO: 2015/9/4 跳转搜索

  }
}
