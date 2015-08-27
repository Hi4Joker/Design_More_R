package com.app.designmore.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.ProfileActivity;
import com.app.designmore.activity.usercenter.SettingActivity;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.utils.DensityUtil;
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
public class UserCenterActivity extends RxAppCompatActivity {

  private static final String TAG = UserCenterActivity.class.getSimpleName();

  @Nullable @Bind(R.id.center_layout_layout_root_view) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.transparent_toolbar_root) Toolbar toolbar;
  @Nullable @Bind(R.id.user_center_layout_avatar_iv) ImageView avatarIv;

  private AppCompatDialog dialog;
  private int statusBarHeight;
  private List<Integer> skipIds = Arrays.asList(R.id.center_layout_bar_layout);

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_main_layout);
    ButterKnife.bind(this);
    EventBusInstance.getDefault().register(UserCenterActivity.this);

    UserCenterActivity.this.initView();
    UserCenterActivity.this.setListener();
    UserCenterActivity.this.loadData();
  }

  private void initView() {

    this.statusBarHeight = DensityUtil.getStatusBarHeight(UserCenterActivity.this);

    UserCenterActivity.this.setSupportActionBar(toolbar);
    UserCenterActivity.this.getSupportActionBar().setTitle("");
  }

  private void setListener() {
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

    BitmapPool bitmapPool = Glide.get(UserCenterActivity.this).getBitmapPool();
    Glide.with(UserCenterActivity.this)
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
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton imageButton =
        (ImageButton) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SettingActivity.navigateToSetting(UserCenterActivity.this);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  /**
   * 购物车
   */
  @Nullable @OnClick(R.id.center_layout_trolley_ll) void onTrolleyClick(View view) {

    TrolleyActivity.startFromLocation(UserCenterActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的订单
   */
  @Nullable @OnClick(R.id.center_layout_order_ll) void onOrderClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的收藏
   */
  @Nullable @OnClick(R.id.center_layout_favorite_ll) void onFavoriteClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 个人资料
   */
  @Nullable @OnClick(R.id.center_layout_information_ll) void onInfoClick(View view) {
    ProfileActivity.startFromLocation(UserCenterActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 地址管理
   */
  @Nullable @OnClick(R.id.center_layout_address_ll) void onAddressClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this, DensityUtil.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 消息中心
   */
  @Nullable @OnClick(R.id.center_layout_message_ll) void onMessageClick(View view) {

  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      UserCenterActivity.this.CreateDialog();
    }
    return false;
  }

  protected void CreateDialog() {

    if (dialog == null) {
      AlertDialog.Builder builder = new AlertDialog.Builder(UserCenterActivity.this);
      builder.setTitle("提示")
          .setMessage("确认退出吗？")
          .setCancelable(false)
          .setInverseBackgroundForced(false)
          .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              if (EventBusInstance.getDefault().hasSubscriberForEvent(FinishEvent.class)) {
                EventBusInstance.getDefault().post(new FinishEvent());
              }
            }
          })
          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
      dialog = builder.create();
    }
    dialog.show();
  }

  public void onEventMainThread(FinishEvent event) {
    UserCenterActivity.this.finish();
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    ButterKnife.unbind(UserCenterActivity.this);
    EventBusInstance.getDefault().unregister(UserCenterActivity.this);
  }
}
