package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.R;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.adapter.AddressAdapter;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.HttpException;
import com.app.designmore.retrofit.entity.Address;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.MaterialCheckBox;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.functions.Action0;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressMangerActivity extends RxAppCompatActivity implements AddressAdapter.Callback {

  private static final String TAG = AddressMangerActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.address_manager_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_manager_layout_pl) ProgressLayout progresslayout;
  @Nullable @Bind(R.id.address_manager_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.address_manager_item_radio_btn) MaterialCheckBox checkBox;

  private List<Address> items = new ArrayList<>();
  private AddressAdapter addressAdapter;

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

    /*创建Adapter*/
    AddressMangerActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AddressMangerActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      AddressMangerActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(AddressMangerActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    addressAdapter = new AddressAdapter(this, items);
    addressAdapter.setCallback(AddressMangerActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(addressAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
          addressAdapter.setAnimationsLocked(false);
        }
      }
    });
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

    final AddressRetrofit addressRetrofit = AddressRetrofit.getInstance();

    /* Action=GetUserByAddress&uid=2*/
    HashMap<String, String> params = new HashMap<>(2);
    params.put("Action", "GetUserByAddress");
    params.put("uid", "2");

    addressRetrofit.getAddressList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progresslayout.showLoading();
          }
        })
        .doOnUnsubscribe(new Action0() {
          @Override public void call() {
            /*清理操作*/
            items.clear();
            addressAdapter = null;
          }
        })
        .compose(AddressMangerActivity.this.<List<Address>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<Address>>() {
          @Override public void onCompleted() {
            /*加载完毕，显示内容界面*/
            progresslayout.showContent();
          }

          @Override public void onError(Throwable error) {

            if (error instanceof TimeoutException) {

            } else if (error instanceof RetrofitError) {
              Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());

              //progresslayout.showError();
            } else if (error instanceof HttpException) {

              Log.e(TAG, "HttpException");
            } else {
              Log.e(TAG, error.getMessage());
              error.printStackTrace();
              throw new RuntimeException("See inner exception");
            }
          }

          @Override public void onNext(List<Address> addresses) {

            items.clear();
            items.addAll(addresses);
            addressAdapter.notifyDataSetChanged();
          }
        });
  }

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {

    }
  };

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

  //AddressAdapter回调
  /*点击删除按钮*/
  @Override public void onDeleteClick(int position) {

  }

  /*点击编辑按钮*/
  @Override public void onEditorClick(int position) {

  }

  /*发生错误回调*/
  @Override public void onError(Throwable error) {

  }
}
