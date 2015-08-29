package com.app.designmore.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

public class JournalActivity extends RxAppCompatActivity {

  private static final String TAG = JournalActivity.class.getSimpleName();

  @Nullable @Bind(R.id.journal_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView titleTv;
  @Nullable @Bind(R.id.white_toolbar_title_iv) ImageView titleIv;

  @Nullable @Bind(R.id.journal_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.bottom_bar_journal_iv) ImageView journalIv;
  @Nullable @Bind(R.id.bottom_bar_journal_tv) TextView journalTv;
  @Nullable @Bind(R.id.bottom_bar_home_rl) RelativeLayout bottomBarHomeRl;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;
  private SupportAnimator revealAnimator;

  public static void navigateToUserCenter(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, JournalActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.journal_layout);
    ButterKnife.bind(JournalActivity.this);
    EventBusInstance.getDefault().register(JournalActivity.this);

    JournalActivity.this.initView(savedInstanceState);

    JournalActivity.this.setListener();
  }

  private void initView(Bundle savedInstanceState) {

    JournalActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(JournalActivity.this);

    titleTv.setVisibility(View.VISIBLE);
    titleIv.setVisibility(View.GONE);
    titleTv.setText("杂 志");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          JournalActivity.this.startEnterAnim();
          return true;
        }
      });
    }
  }

  private void setListener() {

    MaterialRippleLayout.on(bottomBarHomeRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarFashionRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarMineRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.REVEAL_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  /**
   * 主页
   */
  @Nullable @OnClick(R.id.bottom_bar_home_rl) void onFashionClick() {

    HomeActivity.navigateToUserCenter(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 上新
   */
  @Nullable @OnClick(R.id.bottom_bar_fashion_rl) void onJournalClick() {

    FashionActivity.navigateToUserCenter(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 我
   */
  @Nullable @OnClick(R.id.bottom_bar_mine_rl) void onMineClick() {

    MineActivity.navigateToUserCenter(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(journalIv.getDrawable().mutate()), Color.RED);
    journalTv.setTextColor(Color.RED);

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_search);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
      }
    });

    MenuItem trolleyItem = menu.findItem(R.id.action_trolley);
    trolleyItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton trolleyButton =
        (ImageButton) trolleyItem.getActionView().findViewById(R.id.action_inbox_btn);
    trolleyButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_trolley_icon));

    trolleyItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
      }
    });
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        JournalActivity.this.showExitDialog();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      JournalActivity.this.showExitDialog();
    }
    return false;
  }

  protected void showExitDialog() {

    DialogManager.getInstance()
        .showExitDialog(JournalActivity.this, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (EventBusInstance.getDefault().hasSubscriberForEvent(FinishEvent.class)) {
              EventBusInstance.getDefault().post(new FinishEvent());
            }
          }
        });
  }

  public void onEventMainThread(FinishEvent event) {
    JournalActivity.this.finish();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JournalActivity.this.setIntent(intent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    EventBusInstance.getDefault().unregister(JournalActivity.this);
    ButterKnife.unbind(JournalActivity.this);
  }
}
