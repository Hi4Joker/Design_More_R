package com.app.designmore.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
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
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.IconAnim;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.adapter.JournalAdapter;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.JournalRetrofit;
import com.app.designmore.retrofit.entity.JournalEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.HandlerScheduler;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.ActivityEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

public class JournalActivity extends BaseActivity implements JournalAdapter.Callback, IconAnim {

  private static final String TAG = JournalActivity.class.getSimpleName();
  private static WeakReference<AppCompatActivity> weakReference;

  @Nullable @Bind(R.id.journal_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.journal_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.journal_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.journal_layout_rl) RecyclerView recyclerView;

  @Nullable @Bind(R.id.bottom_bar_journal_iv) ImageView journalIv;
  @Nullable @Bind(R.id.bottom_bar_journal_tv) TextView journalTv;
  @Nullable @Bind(R.id.bottom_bar_home_rl) RelativeLayout bottomBarHomeRl;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;

  private JournalAdapter journalAdapter;
  private List<JournalEntity> items = new ArrayList<>();
  private volatile int page = 1;

  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private int visibleItemCount;
  private int totalItemCount;
  private int pastVisibleItems;
  private boolean isLoading = false;
  private boolean isEndless = true;

  private Subscription subscription = Subscriptions.empty();

  private Subscription errorSchedule;

  private View.OnClickListener goHomeClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HomeActivity.navigateToHome(JournalActivity.this);
      overridePendingTransition(0, 0);
    }
  };

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      JournalActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {

      JournalActivity.this.isLoading = false;
      subscription.unsubscribe();
    }
  };

  public static void navigateToJournal(AppCompatActivity startingActivity) {

    if (!(startingActivity instanceof HomeActivity)) {
      weakReference = new WeakReference<>(startingActivity);
    }

    Intent intent = new Intent(startingActivity, JournalActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.journal_layout);

    JournalActivity.this.initView(savedInstanceState);
    JournalActivity.this.setListener();
  }

  @Override public void initView(Bundle savedInstanceState) {

    JournalActivity.this.setSupportActionBar(toolbar);

    /*创建Adapter*/
    JournalActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          JournalActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      JournalActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .compose(JournalActivity.this.<Void>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            JournalActivity.this.loadData();
          }
        });

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(JournalActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    journalAdapter = new JournalAdapter(this);
    journalAdapter.setCallback(JournalActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(journalAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new DividerDecoration(JournalActivity.this, R.dimen.material_8dp));

    RxRecyclerView.scrollEvents(recyclerView)
        .skip(1)
        .compose(
            JournalActivity.this.<RecyclerViewScrollEvent>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<RecyclerViewScrollEvent>() {
          @Override public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {

            //stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
            visibleItemCount = linearLayoutManager.getChildCount();
            totalItemCount = linearLayoutManager.getItemCount();
            pastVisibleItems = linearLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading) {
              if ((visibleItemCount + pastVisibleItems) >= totalItemCount && isEndless) {
                /*加载更多*/
                JournalActivity.this.loadDataMore();
              }
            }
          }
        });
  }

  private void loadData() {

    /* Action=GetMagazine&count=10&page=1*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "GetMagazine");
    params.put("count", "10");
    params.put("page", String.valueOf(page = 1));

    subscription =
        JournalRetrofit.getInstance()
            .getJournalList(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {

                /*网络错误规避*/
                if (errorSchedule != null && !errorSchedule.isUnsubscribed()) {
                  errorSchedule.unsubscribe();
                }

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
            .compose(
                JournalActivity.this.<List<JournalEntity>>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new Subscriber<List<JournalEntity>>() {
              @Override public void onCompleted() {

                JournalActivity.this.isEndless = true;

                /*加载完毕，显示内容界面*/
                if (items != null && items.size() != 0 && !progressLayout.isContent()) {
                  progressLayout.showContent();
                } else if (items != null && items.size() == 0) {
                  progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                      "当前没有杂志可看", null, "去首页看看", goHomeClickListener);
                }
              }

              @Override public void onError(Throwable error) {
                /*加载失败，显示错误界面*/
                JournalActivity.this.showErrorLayout(error);
              }

              @Override public void onNext(List<JournalEntity> journalEntities) {

                JournalActivity.this.items.clear();
                JournalActivity.this.items.addAll(journalEntities);
                journalAdapter.updateItems(items);
              }
            });
  }

  private void loadDataMore() {

    /* Action=GetMagazine&count=10&page=1*/
    final Map<String, String> params = new HashMap<>(3);
    params.put("Action", "GetMagazine");
    params.put("count", "10");
    params.put("page", String.valueOf(++page));

    subscription =
        JournalRetrofit.getInstance()
            .getJournalList(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*正在加载*/
                JournalActivity.this.isLoading = true;
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(JournalActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*加载完毕*/
                JournalActivity.this.isLoading = false;
                progressDialog.dismiss();
              }
            })
            .compose(
                JournalActivity.this.<List<JournalEntity>>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(journalAdapter);
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      JournalActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      JournalActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      JournalActivity.this.showError(getResources().getString(R.string.service_exception_title),
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

  private void setListener() {

    MaterialRippleLayout.on(bottomBarHomeRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(Constants.MILLISECONDS_100)
        .rippleAlpha(0.4f)
        .rippleDuration(Constants.MILLISECONDS_600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarFashionRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(Constants.MILLISECONDS_100)
        .rippleAlpha(0.4f)
        .rippleDuration(Constants.MILLISECONDS_600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarMineRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(Constants.MILLISECONDS_100)
        .rippleAlpha(0.4f)
        .rippleDuration(Constants.MILLISECONDS_600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    JournalActivity.this.rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        JournalActivity.this.rootView.setLayerType(View.LAYER_TYPE_NONE, null);

        if (weakReference != null && weakReference.get() != null) {
          weakReference.get().finish();
          weakReference.clear();
          weakReference = null;
        }

        if (progressLayout != null) JournalActivity.this.loadData();
      }
    });
    revealAnimator.start();
  }

  /**
   * 主页
   */
  @Nullable @OnClick(R.id.bottom_bar_home_rl) void onFashionClick() {
    HomeActivity.navigateToHome(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 上新
   */
  @Nullable @OnClick(R.id.bottom_bar_fashion_rl) void onJournalClick() {
    FashionActivity.navigateToFashion(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 我
   */
  @Nullable @OnClick(R.id.bottom_bar_mine_rl) void onMineClick() {
    MineActivity.navigateToUserCenter(JournalActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(journalIv.getDrawable().mutate()),
        getResources().getColor(R.color.design_more_red));
    journalTv.setTextColor(getResources().getColor(R.color.design_more_red));

    /*执行进入icon动画*/
    JournalActivity.this.startIconAnim();

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(JournalActivity.this) * 2;
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("杂 志");

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_inbox_1);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SearchActivity.navigateToSearch(JournalActivity.this);
        overridePendingTransition(0, 0);
      }
    });

    MenuItem trolleyItem = menu.findItem(R.id.action_inbox_2);
    trolleyItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton trolleyButton =
        (ImageButton) trolleyItem.getActionView().findViewById(R.id.action_inbox_btn);
    trolleyButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_trolley_black_icon));

    trolleyItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TrolleyActivity.startFromLocation(JournalActivity.this, 0, TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  /**
   * item点击事件回调
   */
  @Override public void onItemClick(JournalEntity entity) {

    JournalDetailActivity.navigateToJournalDetail(JournalActivity.this, entity);
    overridePendingTransition(0, 0);
  }

  @Override public void onNoData() {
    this.isEndless = false;
    toast = DialogManager.getInstance().showNoMoreDialog(JournalActivity.this, Gravity.TOP, null);
  }

  @Override public void onError(Throwable error) {

    this.isEndless = false;

    if (errorSchedule != null && !errorSchedule.isUnsubscribed()) {
      errorSchedule.unsubscribe();
    }

    errorSchedule = HandlerScheduler.from(new Handler(Looper.getMainLooper()))
        .createWorker()
        .schedule(new Action0() {
          @Override public void call() {
            JournalActivity.this.isEndless = true;
          }
        }, Constants.MILLISECONDS_2800, TimeUnit.MILLISECONDS);

    if (error instanceof RetrofitError) {
      toast = DialogManager.getInstance()
          .showNoMoreDialog(JournalActivity.this, Gravity.TOP, "请检查您的网络设置");
    } else {
      toast = DialogManager.getInstance()
          .showNoMoreDialog(JournalActivity.this, Gravity.TOP, "加载更多失败，请重试,/(ㄒoㄒ)/~~");
    }
  }

  @Override public void exit() {
    DialogManager.getInstance().showExitDialog(JournalActivity.this);
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    JournalActivity.this.setIntent(intent);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);

      if (toast.getTag() instanceof Subscription) {
        ((Subscription) toast.getTag()).unsubscribe();
      }
    }
    this.progressDialog = null;
    this.toast = null;
    if (errorSchedule != null && !errorSchedule.isUnsubscribed()) errorSchedule.unsubscribe();
    if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @Override public void startIconAnim() {

    journalIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    Animator iconAnim = ObjectAnimator.ofPropertyValuesHolder(journalIv,
        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.5f, 1.0f),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.5f, 1.0f));
    iconAnim.setDuration(Constants.MILLISECONDS_400);
    iconAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        journalIv.setLayerType(View.LAYER_TYPE_NONE, null);
      }
    });
    iconAnim.start();
  }
}
