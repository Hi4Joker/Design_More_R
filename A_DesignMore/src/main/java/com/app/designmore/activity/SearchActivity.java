package com.app.designmore.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.Utils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/30.
 */
public class SearchActivity extends RxAppCompatActivity {

  private static final String TAG = SearchActivity.class.getSimpleName();

  @Nullable @Bind(R.id.search_layout_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.search_layout_toolbar_rfl) RevealFrameLayout revealRootView;
  @Nullable @Bind(R.id.search_layout_et) AppCompatEditText searchEt;
  @Nullable @Bind(R.id.search_layout_btn) ImageButton searchBtn;
  @Nullable @Bind(R.id.search_layout_tip_ll) LinearLayout tipLl;
  private SupportAnimator revealAnimator;

  /*键盘*/
  private InputMethodManager inputMethodManager;

  public static void navigateToSearch(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, SearchActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.search_layout);
    ButterKnife.bind(SearchActivity.this);

    SearchActivity.this.initView(savedInstanceState);
    SearchActivity.this.setListener();
  }

  private void initView(Bundle savedInstanceState) {

    SearchActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    inputMethodManager =
        (InputMethodManager) SearchActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);

    if (savedInstanceState == null) {
      toolbar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          toolbar.getViewTreeObserver().removeOnPreDrawListener(this);
          SearchActivity.this.startEnterAnim();
          return true;
        }
      });
    }
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

    ViewCompat.setAlpha(tipLl, 0.0f);
    ViewCompat.setTranslationY(tipLl, tipLl.getHeight());

    final Rect bounds = new Rect();
    revealRootView.getHitRect(bounds);

    revealAnimator = ViewAnimationUtils.createCircularReveal(revealRootView.getChildAt(0),
        bounds.right - bounds.left, 0, 0, Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.REVEAL_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {

        if (tipLl != null) {
          ViewCompat.animate(tipLl)
              .alpha(1.0f)
              .translationY(0.0f)
              .setDuration(Constants.REVEAL_DURATION);
        }
      }
    });
    revealAnimator.start();
  }

  @Nullable @OnClick(R.id.search_layout_btn) void onSearchClick() {

     /*关闭键盘*/
    inputMethodManager.hideSoftInputFromWindow(revealRootView.getApplicationWindowToken(), 0);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        SearchActivity.this.finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (revealAnimator != null && revealAnimator.isRunning()) revealAnimator.cancel();
    ButterKnife.unbind(SearchActivity.this);
  }
}
