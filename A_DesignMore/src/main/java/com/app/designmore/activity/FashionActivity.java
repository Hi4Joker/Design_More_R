package com.app.designmore.activity;

import android.content.DialogInterface;
import android.content.Intent;
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
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
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

public class FashionActivity extends BaseActivity {

  private static final String TAG = FashionActivity.class.getSimpleName();

  @Nullable @Bind(R.id.fashion_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.fashion_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.bottom_bar_fashion_iv) ImageView fashionIv;
  @Nullable @Bind(R.id.bottom_bar_fashion_tv) TextView fashionTv;
  @Nullable @Bind(R.id.bottom_bar_home_rl) RelativeLayout bottomBarHomeRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;
  private SupportAnimator revealAnimator;

  public static void navigateToUserCenter(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, FashionActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.fashion_layout);

    FashionActivity.this.initView(savedInstanceState);
    FashionActivity.this.setListener();
  }

  @Override public void initView(Bundle savedInstanceState) {

    FashionActivity.this.setSupportActionBar(toolbar);
    //toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          FashionActivity.this.startEnterAnim();
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

    MaterialRippleLayout.on(bottomBarJournalRl)
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
    revealAnimator.setDuration(Constants.ANIMATION_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.start();
  }

  /**
   * 主页
   */
  @Nullable @OnClick(R.id.bottom_bar_home_rl) void onHomeClick() {

    HomeActivity.navigateToHome(FashionActivity.this);
    FashionActivity.this.finish();
    overridePendingTransition(0, 0);
  }

  /**
   * 杂志
   */
  @Nullable @OnClick(R.id.bottom_bar_journal_rl) void onJournalClick() {

    JournalActivity.navigateToUserCenter(FashionActivity.this);
    FashionActivity.this.finish();
    overridePendingTransition(0, 0);
  }

  /**
   * 我
   */
  @Nullable @OnClick(R.id.bottom_bar_mine_rl) void onMineClick() {

    MineActivity.navigateToUserCenter(FashionActivity.this);
    FashionActivity.this.finish();
    overridePendingTransition(0, 0);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(fashionIv.getDrawable().mutate()),
        getResources().getColor(R.color.design_more_red));
    fashionTv.setTextColor(getResources().getColor(R.color.design_more_red));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(FashionActivity.this) * 2;

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("上 新");

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_search);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SearchActivity.navigateToSearch(FashionActivity.this);
        overridePendingTransition(0, 0);
      }
    });

    MenuItem trolleyItem = menu.findItem(R.id.action_trolley);
    trolleyItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton trolleyButton =
        (ImageButton) trolleyItem.getActionView().findViewById(R.id.action_inbox_btn);
    trolleyButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_trolley_icon));

    trolleyItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TrolleyActivity.startFromLocation(FashionActivity.this,
            DensityUtil.getActionBarSize(FashionActivity.this), TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case android.R.id.home:
        FashionActivity.this.showExitDialog();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      FashionActivity.this.showExitDialog();
    }
    return false;
  }

  protected void showExitDialog() {

    DialogManager.getInstance()
        .showExitDialog(FashionActivity.this, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (EventBusInstance.getDefault().hasSubscriberForEvent(FinishEvent.class)) {
              EventBusInstance.getDefault().post(new FinishEvent());
            }
          }
        });
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    FashionActivity.this.setIntent(intent);
  }
}
