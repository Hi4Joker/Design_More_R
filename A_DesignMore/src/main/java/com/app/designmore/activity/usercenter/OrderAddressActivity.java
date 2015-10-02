package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.adapter.SimpleAddressAdapter;
import com.app.designmore.event.RefreshOrderAddressEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.AddressRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollStateChangeEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/27.
 */
public class OrderAddressActivity extends BaseActivity implements SimpleAddressAdapter.Callback {

  private static final String TAG = OrderAddressActivity.class.getSimpleName();
  public static final String DEFAULT_ADDRESS = "DEFAULT_ADDRESS";

  @Nullable @Bind(R.id.order_address_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;

  @Nullable @Bind(R.id.order_address_layout_rfl) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.order_address_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.order_address_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.order_address_layout_rl) RecyclerView recyclerView;

  private List<AddressEntity> items = new ArrayList<>();
  private AddressEntity defaultAddress;

  private SimpleAddressAdapter simpleAddressAdapter;
  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private LinearLayoutManager linearLayoutManager;
  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      OrderAddressActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  public static void navigateToOrderAddress(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, OrderAddressActivity.class);
    startingActivity.startActivityForResult(intent, Constants.ACTIVITY_CODE);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.order_address_layout);
    ButterKnife.bind(this);

    OrderAddressActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    OrderAddressActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    OrderAddressActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    OrderAddressActivity.this.toolbarTitleTv.setText("选择收货地址");

    /*创建Adapter*/
    OrderAddressActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      revealFrameLayout.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealFrameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
              OrderAddressActivity.this.startEnterAnim();
              return true;
            }
          });
    } else {
      OrderAddressActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        OrderAddressActivity.this.loadData();
      }
    });

    linearLayoutManager = new LinearLayoutManager(OrderAddressActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    simpleAddressAdapter = new SimpleAddressAdapter(this);
    simpleAddressAdapter.setCallback(OrderAddressActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(simpleAddressAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new DividerDecoration(OrderAddressActivity.this, R.dimen.material_1dp));

    RxRecyclerView.scrollStateChangeEvents(recyclerView)
        .forEach(new Action1<RecyclerViewScrollStateChangeEvent>() {
          @Override
          public void call(RecyclerViewScrollStateChangeEvent recyclerViewScrollStateChangeEvent) {
            if (recyclerViewScrollStateChangeEvent.newState()
                == RecyclerView.SCROLL_STATE_DRAGGING) {
              simpleAddressAdapter.setAnimationsLocked(true);
            }
          }
        });
  }

  private void loadData() {

    /* Action=GetUserByAddress&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetUserByAddress");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(OrderAddressActivity.this));

    AddressRetrofit.getInstance()
        .getAddressList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            for (AddressEntity addressEntity : items) {
              if ("1".equals(addressEntity.isDefault())) {
                OrderAddressActivity.this.defaultAddress = addressEntity;
              }
            }
          }
        })
        .compose(
            OrderAddressActivity.this.<List<AddressEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<AddressEntity>>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
              } else if (!progressLayout.isContent()) {
                progressLayout.showContent();
              }
            } else if (items != null && items.size() == 0) {
              progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您还没有收货地址", null);
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            OrderAddressActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<AddressEntity> addresses) {

            OrderAddressActivity.this.items.clear();
            OrderAddressActivity.this.items.addAll(addresses);
            simpleAddressAdapter.updateItems(items);
          }
        });
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      OrderAddressActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      OrderAddressActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      OrderAddressActivity.this.showError(
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

  @Override public void onItemClick(final AddressEntity addressEntity) {

    if (defaultAddress == addressEntity) {
      Intent intent = new Intent();
      intent.putExtra(DEFAULT_ADDRESS, OrderAddressActivity.this.defaultAddress);
      setResult(RESULT_OK, intent);
      OrderAddressActivity.this.exit();
    } else {
      OrderAddressActivity.this.requestSetDefaultAddress(addressEntity);
    }
  }

  private void requestSetDefaultAddress(final AddressEntity addressEntity) {
  /* Action=SetDefaultAddress&uid=2&address_id=113*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "SetDefaultAddress");
    params.put("address_id", addressEntity.getAddressId());
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(OrderAddressActivity.this));

    subscription =
        AddressRetrofit.getInstance()
            .requestSetDefaultAddress(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.
                      getInstance()
                      .showCancelableProgressDialog(OrderAddressActivity.this, null, cancelListener,
                          false);
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
            .compose(OrderAddressActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new SimpleObserver<BaseResponse>() {

              @Override public void onCompleted() {

                Intent intent = new Intent();
                intent.putExtra(DEFAULT_ADDRESS, OrderAddressActivity.this.defaultAddress);
                setResult(RESULT_OK, intent);

                Observable.timer(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS,
                    AndroidSchedulers.mainThread()).forEach(new Action1<Long>() {
                  @Override public void call(Long aLong) {
                    OrderAddressActivity.this.exit();
                  }
                });
              }

              @Override public void onError(Throwable e) {
                toast = DialogManager.getInstance()
                    .showNoMoreDialog(OrderAddressActivity.this, Gravity.TOP,
                        "网络连接失败，请重试,/(ㄒoㄒ)/~~");
              }

              @Override public void onNext(BaseResponse baseResponse) {

                int oldIndex = items.indexOf(defaultAddress);
                int newIndex = items.indexOf(addressEntity);

                OrderAddressActivity.this.defaultAddress = addressEntity;

                ((ImageButton) linearLayoutManager.findViewByPosition(oldIndex)
                    .findViewById(R.id.order_address_item_radio_btn)).setImageDrawable(null);

                ((ImageButton) linearLayoutManager.findViewByPosition(newIndex)
                    .findViewById(R.id.order_address_item_radio_btn)).setImageDrawable(
                    getResources().getDrawable(R.drawable.ic_radio_selected_icon));
              }
            });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    Button actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_manager));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        AddressMangerActivity.startFromLocation(OrderAddressActivity.this, 0,
            AddressMangerActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    OrderAddressActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        if (progressLayout != null) {
          OrderAddressActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_NONE, null);
          OrderAddressActivity.this.loadData();
        }
      }
    });
    revealAnimator.start();
  }

  /**
   * 新增地址 -> 刷新界面
   */
  public void onEventMainThread(RefreshOrderAddressEvent event) {
    OrderAddressActivity.this.loadData();
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(OrderAddressActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            OrderAddressActivity.this.finish();
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
