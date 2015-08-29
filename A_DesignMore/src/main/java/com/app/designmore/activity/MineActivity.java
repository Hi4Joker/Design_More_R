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
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.ProfileActivity;
import com.app.designmore.activity.usercenter.SettingActivity;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.app.designmore.view.ProgressLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Joker on 2015/8/25.
 */
public class MineActivity extends RxAppCompatActivity {

  private static final String TAG = MineActivity.class.getSimpleName();

  @Nullable @Bind(R.id.mine_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.mine_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.transparent_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.mine_layout_avatar_iv) ImageView avatarIv;
  @Nullable @Bind(R.id.bottom_bar_mine_iv) ImageView mineIv;
  @Nullable @Bind(R.id.bottom_bar_mine_tv) TextView mineTv;
  @Nullable @Bind(R.id.bottom_bar_home_rl) RelativeLayout bottomBarHomeRl;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;

  private List<Integer> skipIds = Arrays.asList(R.id.mine_layout_bar_layout);
  private SupportAnimator revealAnimator;

  public static void navigateToUserCenter(AppCompatActivity startingActivity) {

    Intent intent = new Intent(startingActivity, MineActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mine_layout);
    ButterKnife.bind(this);
    EventBusInstance.getDefault().register(MineActivity.this);

    MineActivity.this.initView(savedInstanceState);
    MineActivity.this.setListener();
  }

  private void initView(Bundle savedInstanceState) {

    MineActivity.this.setSupportActionBar(toolbar);
    MineActivity.this.getSupportActionBar().setTitle("");

    if (savedInstanceState == null) {

      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          MineActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      MineActivity.this.loadData();
    }
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), bounds.left, bounds.top, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.REVEAL_DURATION);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
        MineActivity.this.loadData();
      }
    });
    revealAnimator.start();
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
  }

  private void loadData() {

    //progressLayout.showLoading(skipIds);

    /*Drawable emptyDrawable =
        new IconDrawable(this, Iconify.IconValue.zmdi_shopping_cart_plus).colorRes(
            android.R.color.white);*/

    /*Drawable errorDrawable = new IconDrawable(this, Iconify.IconValue.zmdi_wifi_off).colorRes(
        android.R.color.white);*/

    /*progressLayout.showEmpty(emptyDrawable, "Empty Shopping Cart",
        "Please add things in the cart to continue.", skipIds);*/

   /* progressLayout.showError(errorDrawable, "No Connection",
        "We could not establish a connection with our servers. Please try again when you are connected to the internet.",
        "Try Again", new View.OnClickListener() {
          @Override public void onClick(View v) {

          }
        }, skipIds);*/

    BitmapPool bitmapPool = Glide.get(MineActivity.this).getBitmapPool();
    Glide.with(MineActivity.this)
        .load(R.drawable.test_background)
        .centerCrop()
        .crossFade()
        .bitmapTransform(new CropCircleTransformation(bitmapPool))
        .placeholder(R.drawable.center_profile_default_icon)
        .error(R.drawable.center_profile_default_icon)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(avatarIv);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(mineIv.getDrawable().mutate()), Color.RED);
    mineTv.setTextColor(Color.RED);

    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton imageButton =
        (ImageButton) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_icon));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SettingActivity.navigateToSetting(MineActivity.this);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  /**
   * 购物车
   */
  @Nullable @OnClick(R.id.mine_layout_trolley_ll) void onTrolleyClick(View view) {

    TrolleyActivity.startFromLocation(MineActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的订单
   */
  @Nullable @OnClick(R.id.mine_layout_order_ll) void onOrderClick(View view) {
    TrolleyActivity.startFromLocation(MineActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的收藏
   */
  @Nullable @OnClick(R.id.mine_layout_favorite_ll) void onFavoriteClick(View view) {
    TrolleyActivity.startFromLocation(MineActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 个人资料
   */
  @Nullable @OnClick(R.id.mine_layout_information_ll) void onInfoClick(View view) {
    ProfileActivity.startFromLocation(MineActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 地址管理
   */
  @Nullable @OnClick(R.id.mine_layout_address_ll) void onAddressClick(View view) {
    TrolleyActivity.startFromLocation(MineActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 消息中心
   */
  @Nullable @OnClick(R.id.mine_layout_message_ll) void onMessageClick(View view) {

  }

  /**
   * 主页
   */
  @Nullable @OnClick(R.id.bottom_bar_home_rl) void onMineClick() {

    HomeActivity.navigateToUserCenter(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 上新
   */
  @Nullable @OnClick(R.id.bottom_bar_fashion_rl) void onFashionClick() {

    FashionActivity.navigateToUserCenter(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 杂志
   */
  @Nullable @OnClick(R.id.bottom_bar_journal_rl) void onJournalClick() {

    JournalActivity.navigateToUserCenter(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      MineActivity.this.showExitDialog();
    }
    return false;
  }

  protected void showExitDialog() {

    DialogManager.getInstance()
        .showExitDialog(MineActivity.this, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (EventBusInstance.getDefault().hasSubscriberForEvent(FinishEvent.class)) {
              EventBusInstance.getDefault().post(new FinishEvent());
            }
          }
        });
  }

  public void onEventMainThread(FinishEvent event) {
    MineActivity.this.finish();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    MineActivity.this.setIntent(intent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    EventBusInstance.getDefault().unregister(MineActivity.this);
    ButterKnife.unbind(MineActivity.this);
  }
}
