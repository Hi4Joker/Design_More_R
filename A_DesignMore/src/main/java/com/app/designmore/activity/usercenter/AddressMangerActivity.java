package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressMangerActivity extends RxAppCompatActivity implements AddressAdapter.Callback {

  private static final String TAG = AddressMangerActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.address_manager_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_manager_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.address_manager_layout_rv) RecyclerView recyclerView;

  private ProgressDialog progressDialog;
  private AddressAdapter addressAdapter;
  private List<AddressEntity> items;

  /*默认地址*/
  private int defaultPosition = -1;
  /*编辑地址*/
  private int editorPosition = -1;
  /*删除地址*/
  //private int deletePosition = -1;
  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      AddressMangerActivity.this.loadData();
    }
  };
  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {

    Intent intent = new Intent(startingActivity, AddressMangerActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_manager_layout);
    ButterKnife.bind(AddressMangerActivity.this);
    EventBusInstance.getDefault().register(AddressMangerActivity.this);

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

    addressAdapter = new AddressAdapter(this);
    addressAdapter.setCallback(AddressMangerActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(addressAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
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

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    TextView textView = (TextView) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    textView.setText(getText(R.string.action_add));
    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressAddActivity.navigateToAddressEditor(AddressMangerActivity.this);
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

  /**
   * 刷新数据
   */
  private void loadData() {

    /* Action=GetUserByAddress&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetUserByAddress");
    params.put("uid", "1");

    AddressRetrofit.getInstance()
        .getAddressList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .compose(AddressMangerActivity.this.<List<AddressEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<AddressEntity>>() {
          @Override public void onCompleted() {
            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              progressLayout.showContent();
            } else if (items != null && items.size() == 0) {
              progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您还没有收货地址", null);
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            AddressMangerActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<AddressEntity> addresses) {

            AddressMangerActivity.this.items = addresses;
            addressAdapter.updateItems(items);
          }
        });
  }

  private void showError(String errorTitle, String errorContent) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
        errorContent, getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  private void checkAddress() {
    if (this.defaultPosition != -1) {//更改默认地址
      DialogManager.showAddressChangeDialog(AddressMangerActivity.this, onConfirmClick);
    }
  }

  private DialogInterface.OnClickListener onConfirmClick = new DialogInterface.OnClickListener() {
    @Override public void onClick(DialogInterface dialog, int which) {
      // TODO: 2015/9/2 请求修改默认地址接口
      AddressEntity currentAddress = items.get(defaultPosition);
    }
  };

  /**
   * ************************************** AddressAdapter回调
   */
  /*点击删除按钮*/
  @Override public void onDeleteClick(final int position) {

    final AddressEntity deleteAddress = items.get(position);

    /* Action=DelUserByAddress&address_id=1&uid=2*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "DelUserByAddress");
    params.put("address_id", deleteAddress.getAddressId());
    params.put("uid", "1");

    subscription =
        AddressRetrofit.getInstance()
            .requestDeleteAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
            /*加载数据，显示进度条*/
                progressDialog = DialogManager.
                    getInstance()
                    .showProgressDialog(AddressMangerActivity.this, null, cancelListener);
              }
            })
            .map(new Func1<BaseResponse, Integer>() {
              @Override public Integer call(BaseResponse baseResponse) {
                return position;
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
            /*隐藏进度条*/
                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
              }
            })
            .filter(new Func1<Integer, Boolean>() {
              @Override public Boolean call(Integer position) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(AddressMangerActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(addressAdapter);
  }

  /*点击编辑按钮*/
  @Override public void onEditorClick(int position) {
    this.editorPosition = position;
    AddressEditorActivity.navigateToAddressEditor(AddressMangerActivity.this, items.get(position));
    overridePendingTransition(0, 0);
  }

  /*点击RadioButton*/
  @Override public void onCheckChange(int position) {
    this.defaultPosition = position;
  }

  /*发生错误回调*/
  @Override public void onError(Throwable error) {

    Snackbar.make(rootView, "删除失败，请稍后重试", Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
            /*do nothing*/
          }
        });
  }

  private void showErrorLayout(Throwable error) {

    if (error instanceof TimeoutException) {
      AddressMangerActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {

      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());

      AddressMangerActivity.this.showError("网络连接异常", ((RetrofitError) error).getKind() + "");
    } else if (error instanceof WebServiceException) {

      AddressMangerActivity.this.showError(
          getResources().getString(R.string.service_exception_title),
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  /**
   * 新增地址 -> 刷新界面
   */
  public void onEventMainThread(RefreshAddressEvent event) {

    AddressMangerActivity.this.loadData();
  }

  /**
   * 编辑地址 -> 刷新界面
   */
  public void onEventMainThread(EditorAddressEvent event) {

    AddressEntity address = items.get(editorPosition);

    address.setAddressId(event.getAddressId());
    address.setUserName(event.getUserName());
    address.setProvince(event.getProvince());
    address.setCity(event.getCity());
    address.setAddress(event.getAddress());
    address.setMobile(event.getMobile());
    address.setZipcode(event.getZipcode());
    address.setIsChecked(event.isChecked());

    this.addressAdapter.updateItem(address, editorPosition);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressMangerActivity.this.startExitAnim();
    }
    return false;
  }

  private void startExitAnim() {

    AddressMangerActivity.this.checkAddress();

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

  @Override protected void onDestroy() {
    super.onDestroy();

    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
    EventBusInstance.getDefault().unregister(AddressMangerActivity.this);
    ButterKnife.unbind(AddressMangerActivity.this);
  }
}
