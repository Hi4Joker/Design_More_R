package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.HomeActivity;
import com.app.designmore.adapter.TrolleyAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.retrofit.TrolleyRetrofit;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Joker on 2015/8/25.
 */
public class TrolleyActivity extends BaseActivity implements TrolleyAdapter.Callback {

  private static final String TAG = TrolleyActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.trolley_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.trolley_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.trolley_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.trolley_layout_radio_btn) ImageButton radioBtn;
  @Nullable @Bind(R.id.trolley_layout_total_tv) TextView totalTv;
  @Nullable @Bind(R.id.trolley_ayout_pay_btn) Button payBtn;

  private TrolleyAdapter trolleyAdapter;
  private List<TrolleyEntity> items;

  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  /*传入订单界面*/
  private List<TrolleyEntity> trolleyEntities = new ArrayList<>();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      TrolleyActivity.this.loadData();
    }
  };

  public enum Type {
    EXTEND,
    UP
  }

  public static void startFromLocation(AppCompatActivity startingActivity, int startingLocationY,
      Type type) {

    Intent intent = new Intent(startingActivity, TrolleyActivity.class);
    if (type == Type.EXTEND) {
      intent.putExtra(START_LOCATION_Y, startingLocationY);
    }
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_trolley_layout);

    TrolleyActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    TrolleyActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("我的购物车");

    /*创建Adapter*/
    TrolleyActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          TrolleyActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      TrolleyActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TrolleyActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    trolleyAdapter = new TrolleyAdapter(this);
    trolleyAdapter.setCallback(TrolleyActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(trolleyAdapter);
  }

  private void observableListenerWrapper(Observable<TrolleyEntity> observable) {

    compositeSubscription.add(observable.subscribeOn(Schedulers.immediate())
        .compose(TrolleyActivity.this.<TrolleyEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<TrolleyEntity>() {
          @Override public void onCompleted() {
            /*计算总价钱*/
            payBtn.setText(String.valueOf(trolleyEntities.size()));

            if (trolleyEntities.size() == items.size()) {
              radioBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_selected));
            } else {
              radioBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_normal));
            }

            compositeSubscription.remove(this);
          }

          @Override public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override public void onNext(TrolleyEntity trolleyEntity) {

            final ImageView radioIv = (ImageView) recyclerView.getLayoutManager()
                .findViewByPosition(items.indexOf(trolleyEntity))
                .findViewById(R.id.trolley_item_radio_iv);

            if (trolleyEntity.isChecked) {
              radioIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_selected));
              trolleyEntities.add(trolleyEntity);
            } else {
              radioIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_normal));
              trolleyEntities.remove(trolleyEntity);
            }
          }
        }));
  }

  private void loadData() {

    /*Action=GoodsCartList&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GoodsCartList");
    params.put("uid", "1");

    TrolleyRetrofit.getInstance()
        .getTrolleyList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .compose(TrolleyActivity.this.<List<TrolleyEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<TrolleyEntity>>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              progressLayout.showContent();
            } else if (items != null && items.size() == 0) {
              progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您的购物车空空如也，快去购物吧", null, "去首页看看", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                      HomeActivity.navigateToHome(TrolleyActivity.this);
                      overridePendingTransition(0, 0);
                    }
                  });
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            TrolleyActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<TrolleyEntity> trolleyEntities) {

            TrolleyActivity.this.items = trolleyEntities;
            trolleyAdapter.updateItems(items);
          }
        });
  }

  private void showErrorLayout(Throwable error) {

    if (error instanceof TimeoutException) {
      TrolleyActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {

      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      TrolleyActivity.this.showError("网络连接异常", ((RetrofitError) error).getKind() + "");
    } else if (error instanceof WebServiceException) {

      TrolleyActivity.this.showError(getResources().getString(R.string.service_exception_title),
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

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_center, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    Button actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_tv);
    actionButton.setText(getText(R.string.action_editor));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

      }
    });
    return true;
  }

  @Nullable @OnClick(R.id.trolley_layout_radio_btn) void onRadioClick(ImageButton imageButton) {

    if (trolleyEntities.size() == items.size()) {/*全选 -> 清空*/
      TrolleyActivity.this.observableListenerWrapper(
          Observable.defer(new Func0<Observable<TrolleyEntity>>() {
            @Override public Observable<TrolleyEntity> call() {
              return Observable.from(items);
            }
          }).map(new Func1<TrolleyEntity, TrolleyEntity>() {
            @Override public TrolleyEntity call(TrolleyEntity trolleyEntity) {
              trolleyEntity.isChecked = false;
              return trolleyEntity;
            }
          }));
    } else {/*未全选 -> 全选*/
      for (int pos = 0; pos < items.size(); pos++) {
        TrolleyActivity.this.observableListenerWrapper(
            Observable.defer(new Func0<Observable<TrolleyEntity>>() {
              @Override public Observable<TrolleyEntity> call() {
                return Observable.from(items);
              }
            }).filter(new Func1<TrolleyEntity, Boolean>() {
              @Override public Boolean call(TrolleyEntity trolleyEntity) {
                return !trolleyEntity.isChecked;
              }
            }).map(new Func1<TrolleyEntity, TrolleyEntity>() {
              @Override public TrolleyEntity call(TrolleyEntity trolleyEntity) {
                trolleyEntity.isChecked = true;
                return trolleyEntity;
              }
            }));
      }
    }
  }

  private void startEnterAnim(int startLocationY) {

    rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    if (startLocationY != 0) {
      rootView.setPivotY(startLocationY);
      rootView.setScaleY(0.0f);
      ViewCompat.animate(rootView)
          .scaleY(1.0f)
          .setDuration(Constants.MILLISECONDS_400 / 2)
          .setInterpolator(new AccelerateInterpolator())
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              TrolleyActivity.this.loadData();
            }
          });
    } else {
      ViewCompat.setTranslationY(rootView, rootView.getHeight());
      ViewCompat.animate(rootView)
          .translationY(0.0f)
          .setDuration(Constants.MILLISECONDS_400)
          .setInterpolator(new LinearInterpolator())
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              TrolleyActivity.this.loadData();
            }
          });
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        TrolleyActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void startExitAnim() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(TrolleyActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            TrolleyActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  /**
   * ********************Adapter回调
   */
  @Override public void onRadioClick(final TrolleyEntity trolleyEntity) {

    TrolleyActivity.this.observableListenerWrapper(
        Observable.defer(new Func0<Observable<TrolleyEntity>>() {
          @Override public Observable<TrolleyEntity> call() {
            return Observable.just(trolleyEntity);
          }
        }));
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      TrolleyActivity.this.startExitAnim();
    }
    return false;
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!compositeSubscription.isUnsubscribed() && compositeSubscription.hasSubscriptions()) {
      compositeSubscription.unsubscribe();
    }
  }
}
