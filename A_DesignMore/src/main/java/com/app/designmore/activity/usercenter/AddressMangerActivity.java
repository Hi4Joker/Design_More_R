package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Toast;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.adapter.AddressAdapter;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollStateChangeEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class AddressMangerActivity extends BaseActivity implements AddressAdapter.Callback {

  private static final String TAG = AddressMangerActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.address_manager_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.address_manager_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.address_manager_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.address_manager_layout_rv) RecyclerView recyclerView;

  private ProgressDialog progressDialog;
  private AddressAdapter addressAdapter;
  private List<AddressEntity> items = new ArrayList<>();

  /*默认地址*/
  private AddressEntity defaultAddress = null;
  /*编辑地址*/
  private AddressEntity editorAddress = null;
  /*删除地址*/
  //private int deletePosition = -1;
  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      AddressMangerActivity.this.loadData();
    }
  };

  private DialogInterface.OnClickListener onConfirmClick = new DialogInterface.OnClickListener() {
    @Override public void onClick(DialogInterface dialog, int which) {
      // TODO: 2015/9/2 请求修改默认地址接口

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

    AddressMangerActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

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

    int[] colors = new int[] {
        R.color.design_more_red, R.color.accent_material_light, R.color.design_more_red,
        R.color.accent_material_light
    };

    swipeRefreshLayout.setColorSchemeResources(colors);
    swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override public void onRefresh() {
        AddressMangerActivity.this.loadData();
      }
    });

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

    RxRecyclerView.scrollStateChangeEvents(recyclerView)
        .compose(AddressMangerActivity.this.<RecyclerViewScrollStateChangeEvent>bindUntilEvent(
            ActivityEvent.DESTROY))
        .subscribe(new Action1<RecyclerViewScrollStateChangeEvent>() {
          @Override public void call(RecyclerViewScrollStateChangeEvent stateChangeEvent) {
            if (stateChangeEvent.newState() == RecyclerView.SCROLL_STATE_DRAGGING) {
              addressAdapter.setAnimationsLocked(true);
            }
          }
        });
  }

  private void startEnterAnim(int startLocationY) {

    ViewCompat.setLayerType(rootView, View.LAYER_TYPE_HARDWARE, null);
    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AddressMangerActivity.this.loadData();
          }
        });
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
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .compose(
            AddressMangerActivity.this.<List<AddressEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<AddressEntity>>() {
          @Override public void onCompleted() {
            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
              } else {
                progressLayout.showContent();
              }
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

            AddressMangerActivity.this.items.clear();
            AddressMangerActivity.this.items.addAll(addresses);
            addressAdapter.updateItems(items);
          }
        });
  }

  private void showError(String errorTitle, String errorContent) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
        errorContent, getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  private void checkAddress() {

    if (items != null && items.size() == 0) {
      Toast.makeText(AddressMangerActivity.this, "木有地址", Toast.LENGTH_LONG).show();
      AddressMangerActivity.this.startExitAnim();
    } else {

      if (defaultAddress == null) {
        Toast.makeText(AddressMangerActivity.this, "木有更改默认底子", Toast.LENGTH_LONG).show();
        AddressMangerActivity.this.startExitAnim();
      } else if (items.contains(defaultAddress)) {
        Toast.makeText(AddressMangerActivity.this, "默认地址已经修改", Toast.LENGTH_LONG).show();
        DialogManager.getInstance()
            .showNormalDialog(AddressMangerActivity.this, "修改默认地址",
                new DialogInterface.OnClickListener() {
                  @Override public void onClick(DialogInterface dialog, int which) {
                    // TODO: 2015/9/8  修改默认地址
                  }
                });
      } else {
        Toast.makeText(AddressMangerActivity.this, "请选择默认地址", Toast.LENGTH_LONG).show();
        DialogManager.getInstance().showConfirmDialog(AddressMangerActivity.this, "请选择默认地址");
      }
    }
  }

  /**
   * ************************************** AddressAdapter回调
   */
  /*点击删除按钮*/
  @Override public void onDeleteClick(final AddressEntity addressEntity) {

    DialogManager.getInstance()
        .showNormalDialog(AddressMangerActivity.this, "确认删除地址",
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                /* Action=DelUserByAddress&address_id=1&uid=2*/
                Map<String, String> params = new HashMap<>(3);
                params.put("Action", "DelUserByAddress");
                params.put("address_id", addressEntity.getAddressId());
                params.put("uid", "1");

                subscription = AddressRetrofit.getInstance()
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
                        return items.indexOf(addressEntity);
                      }
                    })
                    .doOnTerminate(new Action0() {
                      @Override public void call() {
                        /*隐藏进度条*/
                        if (progressDialog != null && progressDialog.isShowing()) {
                          progressDialog.dismiss();
                        }
                      }
                    })
                    .doOnCompleted(new Action0() {
                      @Override public void call() {
                        if (items.size() == 0) {
                          progressLayout.showEmpty(
                              getResources().getDrawable(R.drawable.ic_grey_logo_icon), "您还没有收货地址",
                              null);
                        }
                      }
                    })
                    .filter(new Func1<Integer, Boolean>() {
                      @Override public Boolean call(Integer position) {
                        return !subscription.isUnsubscribed();
                      }
                    })
                    .compose(
                        AddressMangerActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
                    .subscribe(addressAdapter);
              }
            });
  }

  /*点击编辑按钮*/
  @Override public void onEditorClick(AddressEntity entity) {
    this.editorAddress = entity;
    AddressEditorActivity.navigateToAddressEditor(AddressMangerActivity.this, editorAddress);
    overridePendingTransition(0, 0);
  }

  /*点击RadioButton*/
  @Override public void onDefaultChange(AddressEntity addressEntity) {
    this.defaultAddress = addressEntity;
  }

  /*发生错误回调*/
  @Override public void onError(Throwable error) {
    DialogManager.getInstance().showConfirmDialog(AddressMangerActivity.this, "删除失败，请重试");
  }

  /**
   * *******************************************
   */

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

    int editorPosition = items.indexOf(editorAddress);

    editorAddress.setAddressId(event.getAddressId());
    editorAddress.setUserName(event.getUserName());
    editorAddress.setProvince(event.getProvince());
    editorAddress.setCity(event.getCity());
    editorAddress.setAddress(event.getAddress());
    editorAddress.setMobile(event.getMobile());
    editorAddress.setZipcode(event.getZipcode());
    editorAddress.setDefault(event.isDefault());

    this.addressAdapter.updateItem(editorAddress, editorPosition);
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
        AddressMangerActivity.this.checkAddress();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      AddressMangerActivity.this.checkAddress();
    }
    return false;
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AddressMangerActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AddressMangerActivity.this.finish();
          }
        });
  }

  @Override protected void onDestroy() {
    swipeRefreshLayout.setOnRefreshListener(null);
    super.onDestroy();
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
