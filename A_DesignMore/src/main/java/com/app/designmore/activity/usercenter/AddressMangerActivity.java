package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.adapter.AddressAdapter;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressManagerEvent;
import com.app.designmore.event.RefreshOrderAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.Collection;
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
  public static final Object DEFAULT_ADDRESS = "DEFAULT_ADDRESS";
  public static final Object ADDRESS_LIST = "ADDRESS_LIST";

  @Nullable @Bind(R.id.address_manager_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.address_manager_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.address_manager_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.address_manager_layout_rv) RecyclerView recyclerView;

  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private AddressAdapter addressAdapter;
  private List<AddressEntity> items = new ArrayList<>();

  /*默认地址*/
  private AddressEntity defaultAddress = null;
  /*编辑地址*/
  private AddressEntity editorAddress = null;
  private Subscription subscription = Subscriptions.empty();

  public enum Type {
    EXTEND,
    UP
  }

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

  public static void startFromLocation(BaseActivity startingActivity, int startingLocationY,
      Type type) {

    Intent intent = new Intent(startingActivity, AddressMangerActivity.class);
    if (type == Type.EXTEND) {
      intent.putExtra(START_LOCATION_Y, startingLocationY);
    }
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_address_manager_layout);

    AddressMangerActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    AddressMangerActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

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

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .compose(AddressMangerActivity.this.<Void>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Void>() {
          @Override public void call(Void aVoid) {
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
    recyclerView.addItemDecoration(
        new DividerDecoration(AddressMangerActivity.this, R.dimen.material_16dp));

    RxRecyclerView.scrollStateChanges(recyclerView)
        .compose(AddressMangerActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Integer>() {
          @Override public void call(Integer recyclerViewScrollStateChangeEvent) {
            if (recyclerViewScrollStateChangeEvent == RecyclerView.SCROLL_STATE_DRAGGING) {
              addressAdapter.setAnimationsLocked(true);
            }
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
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(AddressMangerActivity.this));

    AddressRetrofit.getInstance()
        .getAddressList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            if (swipeRefreshLayout.isRefreshing()) {
              RxSwipeRefreshLayout.refreshing(swipeRefreshLayout).call(false);
            }
          }
        })
        .compose(AddressMangerActivity.this.<HashMap>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<HashMap>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0 && !progressLayout.isContent()) {
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

          @Override public void onNext(HashMap hashMap) {

            AddressMangerActivity.this.defaultAddress =
                (AddressEntity) hashMap.get(DEFAULT_ADDRESS);

            AddressMangerActivity.this.items.clear();
            AddressMangerActivity.this.items.addAll(
                (Collection<? extends AddressEntity>) hashMap.get(ADDRESS_LIST));
            addressAdapter.updateItems(items);
          }
        });
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
                if (which == DialogInterface.BUTTON_POSITIVE) {
                  AddressMangerActivity.this.requestDeleteAddress(addressEntity);
                }
              }
            });
  }

  /**
   * 删除地址
   */
  private void requestDeleteAddress(final AddressEntity addressEntity) {

    /*Action=DelUserByAddress&address_id=1&uid=2*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "DelUserByAddress");
    params.put("address_id", addressEntity.getAddressId());
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(AddressMangerActivity.this));

    subscription =
        AddressRetrofit.getInstance()
            .requestDeleteAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.
                      getInstance()
                      .showSimpleProgressDialog(AddressMangerActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
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

                if (defaultAddress == addressEntity) defaultAddress = null;

                toast = DialogManager.getInstance()
                    .showNoMoreDialog(AddressMangerActivity.this, Gravity.TOP, "删除成功，O(∩_∩)O~~");

                if (items.size() == 0) {
                  progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                      "您还没有收货地址", null);
                }
              }
            })
            .filter(new Func1<Integer, Boolean>() {
              @Override public Boolean call(Integer integer) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(AddressMangerActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(addressAdapter);
  }

  /*点击编辑按钮*/
  @Override public void onEditorClick(AddressEntity entity) {
    this.editorAddress = entity;
    AddressEditorActivity.navigateToAddressEditor(AddressMangerActivity.this, editorAddress);
    overridePendingTransition(0, 0);
  }

  /*点击RadioButton*/
  @Override public void onDefaultChange(AddressEntity addressEntity) {

    if (defaultAddress == addressEntity) return;

    AddressMangerActivity.this.requestSetDefaultAddress(addressEntity);
  }

  /**
   * 设置默认地址
   */
  private void requestSetDefaultAddress(final AddressEntity addressEntity) {

    /* Action=SetDefaultAddress&uid=2&address_id=113*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "SetDefaultAddress");
    params.put("address_id", addressEntity.getAddressId());
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(AddressMangerActivity.this));

    subscription =
        AddressRetrofit.getInstance()
            .requestSetDefaultAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.
                      getInstance()
                      .showCancelableProgressDialog(AddressMangerActivity.this, null,
                          cancelListener, false);
                } else {
                  progressDialog.show();
                }
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
            .filter(new Func1<BaseResponse, Boolean>() {
              @Override public Boolean call(BaseResponse baseResponse) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(AddressMangerActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new SimpleObserver<BaseResponse>() {

              @Override public void onCompleted() {

                toast = DialogManager.getInstance()
                    .showNoMoreDialog(AddressMangerActivity.this, Gravity.TOP, "设置成功，O(∩_∩)O~~");
              }

              @Override public void onError(Throwable e) {

                toast = DialogManager.getInstance()
                    .showNoMoreDialog(AddressMangerActivity.this, Gravity.TOP, "设置失败，请重试，O__O …");
              }

              @Override public void onNext(BaseResponse baseResponse) {

                if (defaultAddress != null) {
                  int oldIndex = items.indexOf(defaultAddress);
                  ((ImageButton) recyclerView.getLayoutManager()
                      .findViewByPosition(oldIndex)
                      .findViewById(R.id.address_manager_item_radio_btn)).setImageDrawable(
                      getResources().getDrawable(R.drawable.ic_radio_normal_icon));
                }

                int newIndex = items.indexOf(addressEntity);
                ((ImageButton) recyclerView.getLayoutManager()
                    .findViewByPosition(newIndex)
                    .findViewById(R.id.address_manager_item_radio_btn)).setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_radio_selected_icon));

                AddressMangerActivity.this.defaultAddress = addressEntity;
              }
            });
  }

  /*发生错误回调*/
  @Override public void onError(Throwable error) {
    toast = DialogManager.getInstance()
        .showNoMoreDialog(AddressMangerActivity.this, Gravity.TOP, "删除失败，请重试，O__O …");
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
      AddressMangerActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
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

  private void showError(String errorTitle, String errorContent) {
    progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
        errorContent, getResources().getString(R.string.retry_button_text), retryClickListener);
  }

  /**
   * 新增地址 -> 刷新界面
   */
  public void onEventMainThread(RefreshAddressManagerEvent event) {
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

  private void startEnterAnim(int startLocationY) {

    if (startLocationY != 0) {
      rootView.setPivotY(startLocationY);
      rootView.setScaleY(0.0f);
      ViewCompat.animate(rootView)
          .scaleY(1.0f)
          .setDuration(Constants.MILLISECONDS_400 / 2)
          .setInterpolator(new AccelerateInterpolator())
          .withLayer()
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              AddressMangerActivity.this.loadData();
            }
          });
    } else {
      ViewCompat.setTranslationY(rootView, rootView.getHeight());
      ViewCompat.animate(rootView)
          .translationY(0.0f)
          .setDuration(Constants.MILLISECONDS_400)
          .setInterpolator(new LinearInterpolator())
          .withLayer()
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              AddressMangerActivity.this.loadData();
            }
          });
    }
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    Button actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_add));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        AddressAddActivity.navigateToAddressEditor(AddressMangerActivity.this);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public void exit() {
    AddressMangerActivity.this.checkAddress();
  }

  private void checkAddress() {

    if (items != null && items.size() == 0) {
      AddressMangerActivity.this.exitWithoutDialog();
    } else {
      if (defaultAddress != null && items.contains(defaultAddress)) {
        AddressMangerActivity.this.exitWithoutDialog();
      } else {
        DialogManager.getInstance().showConfirmDialog(AddressMangerActivity.this, "请选择默认地址");
      }
    }
  }

  private void exitWithoutDialog() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AddressMangerActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {

            if (EventBusInstance.getDefault()
                .hasSubscriberForEvent(RefreshOrderAddressEvent.class)) {
              EventBusInstance.getDefault().post(new RefreshOrderAddressEvent());
            }

            AddressMangerActivity.this.finish();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
