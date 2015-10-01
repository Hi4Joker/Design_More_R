package com.app.designmore.activity.usercenter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
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
import com.app.designmore.helper.DBHelper;
import com.app.designmore.retrofit.TrolleyRetrofit;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
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
import rx.functions.Action1;
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
  @Nullable @Bind(R.id.trolley_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.trolley_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.trolley_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.trolley_layout_radio_btn) ImageButton radioBtn;
  @Nullable @Bind(R.id.trolley_layout_total_tv) TextView totalTv;
  @Nullable @Bind(R.id.trolley_layout_freight_tv) TextView freightTv;
  @Nullable @Bind(R.id.trolley_layout_pay_btn) Button payBtn;

  private Button actionButton;
  private TrolleyAdapter trolleyAdapter;
  private List<TrolleyEntity> items = new ArrayList<>();
  private List<TrolleyEntity> orderEntities = new ArrayList<>();

  private CompositeSubscription compositeSubscription = new CompositeSubscription();

  public enum Type {
    EXTEND,
    UP
  }

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      TrolleyActivity.this.loadData();
    }
  };

  private View.OnClickListener goHomeClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HomeActivity.navigateToHome(TrolleyActivity.this);
      overridePendingTransition(0, 0);
    }
  };

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
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    TrolleyActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    TrolleyActivity.this.toolbarTitleTv.setText("我的购物车");
    TrolleyActivity.this.payBtn.setEnabled(false);

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

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        TrolleyActivity.this.loadData();
      }
    });

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TrolleyActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    trolleyAdapter = new TrolleyAdapter(this);
    trolleyAdapter.setCallback(TrolleyActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new DividerDecoration(TrolleyActivity.this, R.dimen.material_1dp));
    recyclerView.setAdapter(trolleyAdapter);
  }

  private void observableListenerWrapper(Observable<TrolleyEntity> observable) {

    compositeSubscription.add(observable.subscribeOn(Schedulers.immediate())
        .compose(TrolleyActivity.this.<TrolleyEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<TrolleyEntity>() {
          @Override public void onCompleted() {

            if (orderEntities.size() == items.size()) {
              radioBtn.setImageDrawable(
                  getResources().getDrawable(R.drawable.ic_radio_selected_icon));
            } else {
              radioBtn.setImageDrawable(
                  getResources().getDrawable(R.drawable.ic_radio_normal_icon));
            }

            if (orderEntities.size() != 0) {//has trolley
              freightTv.setText("不含运费");
              /*计算总价钱*/
              float totalPrice = 0;
              for (TrolleyEntity trolleyEntity : orderEntities) {
                totalPrice += Float.parseFloat(trolleyEntity.getGoodPrice());
              }

              SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
              spannableStringBuilder.append("合计: ");
              spannableStringBuilder.append("￥");
              spannableStringBuilder.append(totalPrice + "");

              spannableStringBuilder.setSpan(
                  new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_11)), 0, 2,
                  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
              spannableStringBuilder.setSpan(
                  new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_8)), 3, 3,
                  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
              spannableStringBuilder.setSpan(
                  new ForegroundColorSpan(getResources().getColor(R.color.design_more_red)), 3, 3,
                  Spanned.SPAN_INCLUSIVE_INCLUSIVE);
              spannableStringBuilder.setSpan(
                  new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_16)), 4,
                  spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
              spannableStringBuilder.setSpan(
                  new ForegroundColorSpan(getResources().getColor(R.color.design_more_red)), 4,
                  spannableStringBuilder.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

              totalTv.setText(spannableStringBuilder);
              payBtn.setText("结算 ( " + String.valueOf(orderEntities.size()) + " )");
              totalTv.setText(spannableStringBuilder);
              TrolleyActivity.this.payBtn.setEnabled(true);
            } else {//no trolley
              totalTv.setText("");
              payBtn.setText("结算 ( 0 )");
              freightTv.setText("");
              TrolleyActivity.this.payBtn.setEnabled(false);
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
              radioIv.setImageDrawable(
                  getResources().getDrawable(R.drawable.ic_radio_selected_icon));
              orderEntities.add(trolleyEntity);
            } else {
              radioIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_normal_icon));
              orderEntities.remove(trolleyEntity);
            }
          }
        }));
  }

  private void loadData() {

    /*Action=GoodsCartList&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GoodsCartList");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(TrolleyActivity.this));

    TrolleyRetrofit.getInstance()
        .getTrolleyList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            TrolleyActivity.this.payBtn.setEnabled(false);
          }
        })
        .compose(TrolleyActivity.this.<List<TrolleyEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<TrolleyEntity>>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              TrolleyActivity.this.actionButton.setEnabled(true);
              if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
              } else if (!progressLayout.isContent()) {
                progressLayout.showContent();
              }
            } else if (items != null && items.size() == 0) {
              progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您的购物车空空如也，快去购物吧", null, "去首页逛逛", goHomeClickListener);
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            TrolleyActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<TrolleyEntity> trolleyEntities) {

            TrolleyActivity.this.orderEntities.clear();

            TrolleyActivity.this.items.clear();
            TrolleyActivity.this.items.addAll(trolleyEntities);
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
      TrolleyActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
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
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setEnabled(false);
    actionButton.setText(getText(R.string.action_editor));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        TrolleyActivity.this.totalTv.setText("");
        TrolleyActivity.this.freightTv.setText("");
        TrolleyActivity.this.payBtn.setText("结算 ( 0 )");

        TrolleyEditorActivity.navigateToTrolleyEditor(TrolleyActivity.this,
            (ArrayList<TrolleyEntity>) items);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Nullable @OnClick(R.id.trolley_layout_radio_btn) void onRadioClick(ImageButton imageButton) {

    if (orderEntities.size() == items.size()) {/*全选 -> 清空*/
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

  @Nullable @OnClick(R.id.trolley_layout_pay_btn) void onPayClick() {

    OrderCommitActivity.navigateToOrderCommit(TrolleyActivity.this,
        (ArrayList<TrolleyEntity>) orderEntities);
    overridePendingTransition(0, 0);
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
          .withLayer()
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
          .withLayer()
          .setListener(new ViewPropertyAnimatorListenerAdapter() {
            @Override public void onAnimationEnd(View view) {
              TrolleyActivity.this.loadData();
            }
          });
    }
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

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(TrolleyActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            TrolleyActivity.this.finish();
          }
        });
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == Constants.ACTIVITY_CODE && resultCode == RESULT_OK) {
      TrolleyActivity.this.loadData();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (!compositeSubscription.isUnsubscribed() && compositeSubscription.hasSubscriptions()) {
      compositeSubscription.clear();
    }
  }
}
