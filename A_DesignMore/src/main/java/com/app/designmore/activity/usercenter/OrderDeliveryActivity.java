package com.app.designmore.activity.usercenter;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.DetailActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.adapter.DeliveryAdapter;
import com.app.designmore.adapter.SimpleAddressAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.retrofit.OrderRetrofit;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.entity.DeliveryEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
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
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by Joker on 2015/10/3.
 */
public class OrderDeliveryActivity extends BaseActivity implements DeliveryAdapter.Callback {

  private static final String TAG = OrderDeliveryActivity.class.getSimpleName();
  public static final String DEFAULT_DELIVERY = "DEFAULT_DELIVERY";
  private static final String START_LOCATION_Y = "START_LOCATION_Y";
  private static final String DEFAULT_ENTITY = "DEFAULT_ENTITY";
  public static final int ACTIVITY_CODE = 2;

  @Nullable @Bind(R.id.order_delivery_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;

  @Nullable @Bind(R.id.order_delivery_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.order_delivery_layout_rl) RecyclerView recyclerView;

  private List<DeliveryEntity> items = new ArrayList<>();

  private DeliveryEntity defaultDelivery = null;
  private LinearLayoutManager linearLayoutManager;
  private DeliveryAdapter deliveryAdapter;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      OrderDeliveryActivity.this.loadData();
    }
  };

  public static void startFromLocation(OrderCommitActivity startingActivity,
      DeliveryEntity defaultDelivery, int startingLocationY) {
    Intent intent = new Intent(startingActivity, OrderDeliveryActivity.class);
    intent.putExtra(DEFAULT_ENTITY, defaultDelivery);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivityForResult(intent, OrderDeliveryActivity.ACTIVITY_CODE);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.order_delivery_layout);

    OrderDeliveryActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    OrderDeliveryActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(OrderDeliveryActivity.this);

    OrderDeliveryActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    OrderDeliveryActivity.this.toolbarTitleTv.setText("配送方式");

    OrderDeliveryActivity.this.defaultDelivery =
        (DeliveryEntity) getIntent().getSerializableExtra(DEFAULT_ENTITY);

    /*创建Adapter*/
    OrderDeliveryActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          OrderDeliveryActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      OrderDeliveryActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    linearLayoutManager = new LinearLayoutManager(OrderDeliveryActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    deliveryAdapter = new DeliveryAdapter(this);
    deliveryAdapter.setCallback(OrderDeliveryActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(deliveryAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new DividerDecoration(OrderDeliveryActivity.this, R.dimen.material_1dp));
  }

  private void loadData() {

    /* Action=GetShippingList*/
    Map<String, String> params = new HashMap<>(1);
    params.put("Action", "GetShippingList");

    OrderRetrofit.getInstance()
        .getDeliveryList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .doOnNext(new Action1<List<DeliveryEntity>>() {
          @Override public void call(List<DeliveryEntity> deliveryEntities) {

            OrderDeliveryActivity.this.items.clear();
            OrderDeliveryActivity.this.items.addAll(deliveryEntities);

            if (defaultDelivery != null) {

              Observable.timer(Constants.MILLISECONDS_100, TimeUnit.MILLISECONDS,
                  AndroidSchedulers.mainThread()).forEach(new Action1<Long>() {
                @Override public void call(Long aLong) {

                  ((ImageView) linearLayoutManager.findViewByPosition(
                      items.indexOf(defaultDelivery))
                      .findViewById(R.id.order_delivery_item_radio_btn)).setImageDrawable(
                      getResources().getDrawable(R.drawable.ic_radio_selected_icon));
                }
              });
            }
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            if (!progressLayout.isContent()) {
              progressLayout.showContent();
            }
          }
        })
        .compose(
            OrderDeliveryActivity.this.<List<DeliveryEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(deliveryAdapter);
  }

  @Override public void onItemClick(DeliveryEntity deliveryEntity) {

    if (defaultDelivery != null) {

      if (defaultDelivery.equals(deliveryEntity)) {
        OrderDeliveryActivity.this.exit();
      } else {

        int oldIndex = items.indexOf(defaultDelivery);
        int newIndex = items.indexOf(deliveryEntity);

        OrderDeliveryActivity.this.defaultDelivery = deliveryEntity;

        ((ImageView) linearLayoutManager.findViewByPosition(oldIndex)
            .findViewById(R.id.order_delivery_item_radio_btn)).setImageDrawable(null);

        ((ImageView) linearLayoutManager.findViewByPosition(newIndex)
            .findViewById(R.id.order_delivery_item_radio_btn)).setImageDrawable(
            getResources().getDrawable(R.drawable.ic_radio_selected_icon));

        Observable.timer(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS,
            AndroidSchedulers.mainThread()).forEach(new Action1<Long>() {
          @Override public void call(Long aLong) {
            OrderDeliveryActivity.this.exit();
          }
        });
      }
    } else {

      int index = items.indexOf(deliveryEntity);
      OrderDeliveryActivity.this.defaultDelivery = deliveryEntity;

      ((ImageView) linearLayoutManager.findViewByPosition(index)
          .findViewById(R.id.order_delivery_item_radio_btn)).setImageDrawable(
          getResources().getDrawable(R.drawable.ic_radio_selected_icon));

      Observable.timer(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS,
          AndroidSchedulers.mainThread()).forEach(new Action1<Long>() {
        @Override public void call(Long aLong) {
          OrderDeliveryActivity.this.exit();
        }
      });
    }
  }

  @Override public void onError(Throwable e) {
    OrderDeliveryActivity.this.showErrorLayout(e);
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      OrderDeliveryActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      OrderDeliveryActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      OrderDeliveryActivity.this.showError(
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

  private void startEnterAnim(int startLocationY) {

    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            if (progressLayout != null) OrderDeliveryActivity.this.loadData();
          }
        });
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(OrderDeliveryActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {

            if (defaultDelivery != null) {
              Intent intent = new Intent();
              intent.putExtra(DEFAULT_DELIVERY, OrderDeliveryActivity.this.defaultDelivery);
              setResult(RESULT_OK, intent);
            }

            OrderDeliveryActivity.this.finish();
          }
        });
  }
}
