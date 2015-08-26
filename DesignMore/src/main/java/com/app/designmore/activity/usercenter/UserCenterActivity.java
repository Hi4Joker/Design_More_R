package com.app.designmore.activity.usercenter;

import android.content.DialogInterface;
import android.graphics.Rect;
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
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.manager.EventBusInstance;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class UserCenterActivity extends RxAppCompatActivity {

  private static final String TAG = UserCenterActivity.class.getSimpleName();

  @Nullable @Bind(R.id.transparent_toolbar_root) Toolbar toolbar;
  private AppCompatDialog dialog;
  private int statusBarHeight;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.user_center_layout);
    ButterKnife.bind(this);
    EventBusInstance.getDefault().register(UserCenterActivity.this);

    UserCenterActivity.this.initView();
  }

  private void initView() {

    UserCenterActivity.this.setSupportActionBar(toolbar);
    UserCenterActivity.this.getSupportActionBar().setTitle("");

    UserCenterActivity.this.getStatusBarHeight();
  }

  private void getStatusBarHeight() {
    statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_setting);
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

    TrolleyActivity.startFromLocation(UserCenterActivity.this,
        UserCenterActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的订单
   */
  @Nullable @OnClick(R.id.center_layout_order_ll) void onOrderClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this,
        UserCenterActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 我的收藏
   */
  @Nullable @OnClick(R.id.center_layout_favorite_ll) void onFavoriteClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this,
        UserCenterActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 个人资料
   */
  @Nullable @OnClick(R.id.center_layout_information_ll) void onInfoClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this,
        UserCenterActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 地址管理
   */
  @Nullable @OnClick(R.id.center_layout_address_ll) void onAddressClick(View view) {
    TrolleyActivity.startFromLocation(UserCenterActivity.this,
        UserCenterActivity.this.getLocationY(view));
    overridePendingTransition(0, 0);
  }

  /**
   * 消息中心
   */
  @Nullable @OnClick(R.id.center_layout_message_ll) void onMessageClick(View view) {

  }

  private int getLocationY(View item) {

    int[] startingLocation = new int[1];
    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    item.getGlobalVisibleRect(viewRect);

    startingLocation[0] = (viewRect.top - statusBarHeight) + (viewRect.bottom - statusBarHeight);

    return startingLocation[0] / 2;
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
