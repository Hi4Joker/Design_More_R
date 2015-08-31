package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.R;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.MaterialCheckBox;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressMangerActivity extends RxAppCompatActivity {

  private static final String TAG = AddressMangerActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  @Nullable @Bind(R.id.address_manager_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_manager_layout_pl) ProgressLayout progresslayout;
  @Nullable @Bind(R.id.address_manager_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.address_manager_item_radio_btn) MaterialCheckBox checkBox;

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, AddressMangerActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_manager_layout);
    ButterKnife.bind(AddressMangerActivity.this);

    AddressMangerActivity.this.initView(savedInstanceState);
  }

  private void initView(Bundle savedInstanceState) {

    AddressMangerActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("地址管理");

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AddressMangerActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    }
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    rootView.setScaleY(0.0f);

    rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(200)
        .setInterpolator(new AccelerateInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AddressMangerActivity.this.loadData();
          }
        });
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AddressMangerActivity.this))
        .setDuration(400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AddressMangerActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_add));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressEditorActivity.navigateToAddressEditor(AddressMangerActivity.this);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        AddressMangerActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadData() {

  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressMangerActivity.this.startExitAnim();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    ButterKnife.unbind(AddressMangerActivity.this);
  }
}
