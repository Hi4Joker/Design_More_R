package com.app.designmore.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
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
import com.app.designmore.adapter.HomeBannerAdapter;
import com.app.designmore.adapter.HomeCategoryAdapter;
import com.app.designmore.adapter.HomeDiscountAdapter;
import com.app.designmore.adapter.HomeProductAdapter;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.manager.MarginDecoration;
import com.app.designmore.retrofit.HomeRetrofit;
import com.app.designmore.retrofit.entity.CategoryEntity;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.HandlerScheduler;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.view.ViewTouchEvent;
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
import rx.functions.Func4;
import rx.observables.ConnectableObservable;
import rx.subscriptions.Subscriptions;

public class HomeActivity extends BaseActivity
    implements HomeCategoryAdapter.Callback, HomeDiscountAdapter.Callback,
    HomeProductAdapter.Callback, HomeBannerAdapter.Callback, IconAnim {

  private static final String TAG = HomeActivity.class.getSimpleName();
  private static final String BANNER = "BANNER";
  private static final String CAT = "CAT";
  private static final String DISCOUNT = "DISCOUNT";
  private static final String PRODUCT = "PRODUCT";

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_iv) ImageView toolbarTitleIv;
  @Nullable @Bind(R.id.home_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.home_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.home_layout_srl) SwipeRefreshLayout swipeRefreshLayout;

  @Nullable @Bind(R.id.home_layout_app_bar) AppBarLayout appBarLayout;
  @Nullable @Bind(R.id.home_layout_viewpager) ViewPager viewPager;
  @Nullable @Bind(R.id.home_layout_pager_indicator_ll) LinearLayout indicatorLayout;
  @Nullable @Bind(R.id.home_layout_category_rv) RecyclerView categoryRecyclerView;
  @Nullable @Bind(R.id.home_layout_product_rv) RecyclerView productRecyclerView;

  @Nullable @Bind(R.id.bottom_bar_home_iv) ImageView homeIv;
  @Nullable @Bind(R.id.bottom_bar_home_tv) TextView homeTv;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;

  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private List<ProductEntity> bannerItems = new ArrayList<>();
  private List<CategoryEntity> categoryItems = new ArrayList<>();
  private List<FashionEntity> discountItems = new ArrayList<>();
  private List<ProductEntity> productItems = new ArrayList<>();

  private HomeBannerAdapter bannerAdapter;
  private HomeCategoryAdapter categoryAdapter;
  private HomeProductAdapter productAdapter;

  private GridLayoutManager productLayoutManager;

  private int visibleItemCount;
  private int totalItemCount;
  private int pastVisibleItems;
  private volatile boolean isLoading = false;
  private volatile boolean isEndless = true;
  private volatile int count = 1;

  private TextView[] bannerIndicators;
  private Drawable indicatorNormal;
  private Drawable indicatorSelected;

  private boolean offsetEnable = false;

  private Subscription subscription = Subscriptions.empty();
  private Subscription errorSchedule;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {

      Observable.create(new Observable.OnSubscribe<Object>() {
        @Override public void call(Subscriber<? super Object> subscriber) {

        }
      });
      HomeActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {

      HomeActivity.this.isLoading = false;
      subscription.unsubscribe();
    }
  };

  private ViewPager.OnPageChangeListener simpleOnPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override public void onPageScrollStateChanged(int state) {

          /*http://blog.udinic.com/2013/09/16/viewpager-and-hardware-acceleration*/
          if (state != ViewPager.SCROLL_STATE_IDLE) {
            final int childCount = viewPager.getChildCount();
            for (int i = 0; i < childCount; i++) {
              viewPager.getChildAt(i).setLayerType(View.LAYER_TYPE_NONE, null);
            }
          }

          HomeActivity.this.swipeRefreshLayout.setEnabled(
              state == ViewPager.SCROLL_STATE_IDLE && offsetEnable);
        }
      };

  private AppBarLayout.OnOffsetChangedListener offsetChangedListener =
      new AppBarLayout.OnOffsetChangedListener() {
        @Override public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

          /*如果喜欢，可以尝试 https://yassirh.com/2014/05/how-to-use-swiperefreshlayout-the-right-way*/
          if (offset == 0 && productLayoutManager.findFirstVisibleItemPosition() == 0) {
            swipeRefreshLayout.setEnabled(offsetEnable = true);
          } else {
            swipeRefreshLayout.setEnabled(offsetEnable = false);
          }
        }
      };

  public static void navigateToHome(AppCompatActivity startingActivity) {
    Intent intent = new Intent(startingActivity, HomeActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home_layout);

    HomeActivity.this.initView(savedInstanceState);
    HomeActivity.this.setListener();
  }

  @Override public void initView(Bundle savedInstanceState) {

    HomeActivity.this.setSupportActionBar(toolbar);

    HomeActivity.this.setupAdapter();

    indicatorNormal = getResources().getDrawable(R.drawable.home_indicator_normal_background);
    indicatorSelected = getResources().getDrawable(R.drawable.home_indicator_selected_background);

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          HomeActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      HomeActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .compose(HomeActivity.this.<Void>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            HomeActivity.this.loadData();
          }
        });

    bannerAdapter = new HomeBannerAdapter(HomeActivity.this, viewPager);
    bannerAdapter.setCallback(HomeActivity.this);
    viewPager.setAdapter(bannerAdapter);
    viewPager.addOnPageChangeListener(bannerAdapter);

    LinearLayoutManager categoryLayoutManager = new LinearLayoutManager(HomeActivity.this);
    categoryLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    categoryLayoutManager.setSmoothScrollbarEnabled(true);
    categoryAdapter = new HomeCategoryAdapter(HomeActivity.this);
    categoryAdapter.setCallback(HomeActivity.this);
    categoryRecyclerView.setLayoutManager(categoryLayoutManager);
    categoryRecyclerView.setHasFixedSize(true);
    categoryRecyclerView.setAdapter(categoryAdapter);
    categoryRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    categoryRecyclerView.addItemDecoration(
        new MarginDecoration(HomeActivity.this, R.dimen.material_4dp));

    productLayoutManager = new GridLayoutManager(HomeActivity.this, 2);
    productLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    productLayoutManager.setSmoothScrollbarEnabled(true);
    productLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
      @Override public int getSpanSize(int position) {
        return productAdapter.isHeader(position) ? productLayoutManager.getSpanCount() : 1;
      }
    });

    productAdapter = new HomeProductAdapter(HomeActivity.this);
    productAdapter.setCallback(HomeActivity.this);
    productRecyclerView.setLayoutManager(productLayoutManager);
    productRecyclerView.setHasFixedSize(true);
    productRecyclerView.setAdapter(productAdapter);
    productRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    productRecyclerView.addItemDecoration(
        new DividerDecoration(HomeActivity.this, R.dimen.material_1dp));
  }

  private void loadData() {

    final HomeRetrofit homeRetrofit = HomeRetrofit.getInstance();

    /*轮播：Action:GetProductByKeyOrType type:cat  data:1  page:1 count :3   code : 0（综合排序）  order_by: 1（降序）*/
    final Map<String, String> bannerParams = new HashMap<>(7);
    bannerParams.put("Action", "GetProductByKeyOrType");
    bannerParams.put("type", "cat");
    bannerParams.put("data", "2");
    bannerParams.put("page", "1");
    bannerParams.put("count", "10");
    bannerParams.put("code", "0");
    bannerParams.put("order_by", "1");

     /*分类： Action=GetIndexCatList*/
    final Map<String, String> catParams = new HashMap<>(1);
    catParams.put("Action", "GetIndexCatList");

    /*打折：Action: GetProductByStar count:2 page:1*/
    final Map<String, String> discountParams = new HashMap<>(3);
    discountParams.put("Action", "GetProductByStar");
    discountParams.put("page", "1");
    discountParams.put("count", "2");

    /* 精选：Action: GetProductByKeyOrType  type:keyword  data:手表 count:10 page:1(下拉加载page = 2,3,4,5) code : 1(销量) order_by : 1（降序）*/
    final Map<String, String> productParams = new HashMap<>(3);
    productParams.put("Action", "GetProductByHot");
    productParams.put("page", String.valueOf(count = 1));
    productParams.put("count", "10");

    subscription = Observable.zip(homeRetrofit.getHotProduct(bannerParams),
        homeRetrofit.getCategoryList(catParams), homeRetrofit.getDiscountList(discountParams),
        homeRetrofit.getHotProduct(productParams),
        new Func4<List<ProductEntity>, List<CategoryEntity>, List<FashionEntity>, List<ProductEntity>, Map<String, List>>() {
          @Override
          public Map call(List<ProductEntity> bannerEntities, List<CategoryEntity> categoryEntities,
              List<FashionEntity> discountEntities, List<ProductEntity> productEntities) {

            Map<String, List> map = new HashMap(4);
            map.put(BANNER, bannerEntities);
            map.put(CAT, categoryEntities);
            map.put(DISCOUNT, discountEntities);
            map.put(PRODUCT, productEntities);
            return map;
          }
        })
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
        .compose(HomeActivity.this.<Map<String, List>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<Map<String, List>>() {

          @Override public void onCompleted() {

            HomeActivity.this.isEndless = true;

            /*显示内容*/
            if (!progressLayout.isContent()) {
              progressLayout.showContent();
            }

            /*设置banner*/
            HomeActivity.this.setupViewPager();
            /*设置category*/
            HomeActivity.this.categoryAdapter.updateItems(categoryItems);
            /*设置product*/
            HomeActivity.this.productAdapter.updateItems(discountItems, productItems);
          }

          @Override public void onError(Throwable e) {
            /*加载失败，显示错误界面*/
            HomeActivity.this.showErrorLayout(e);
          }

          @Override public void onNext(Map<String, List> map) {

            HomeActivity.this.bannerItems.clear();
            HomeActivity.this.bannerItems.addAll(map.get(BANNER));

            HomeActivity.this.categoryItems.clear();
            HomeActivity.this.categoryItems.addAll(map.get(CAT));

            HomeActivity.this.discountItems.clear();
            HomeActivity.this.discountItems.addAll(map.get(DISCOUNT));

            HomeActivity.this.productItems.clear();
            HomeActivity.this.productItems.addAll(map.get(PRODUCT));
          }
        });
  }

  private void setupViewPager() {

    int count = bannerItems.size();
    int size = DensityUtil.dip2px(DensityUtil.getXmlValue(HomeActivity.this, R.dimen.material_8dp));
    int margin =
        DensityUtil.dip2px(DensityUtil.getXmlValue(HomeActivity.this, R.dimen.material_8dp));

    indicatorLayout.removeAllViews();
    bannerIndicators = new TextView[count + 2];
    for (int i = 0; i < count; i++) {

      bannerIndicators[i] = new TextView(HomeActivity.this);
      bannerIndicators[i].setGravity(Gravity.CENTER);
      LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
      params.setMargins(0, 0, margin, 0);
      bannerIndicators[i].setLayoutParams(params);
      bannerIndicators[i].setBackgroundDrawable(indicatorNormal);
      indicatorLayout.addView(bannerIndicators[i]);
    }

    ConnectableObservable<Boolean> connectableObservable =
        Observable.create(new Observable.OnSubscribe<Boolean>() {
          @Override public void call(final Subscriber<? super Boolean> subscriber) {

            View.OnTouchListener listener = new View.OnTouchListener() {
              @Override public boolean onTouch(View v, @NonNull MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                  subscriber.onNext(false);
                } else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                  subscriber.onNext(true);
                }
                return false;
              }
            };
            viewPager.setOnTouchListener(listener);

            subscriber.add(new Subscription() {
              @Override public void unsubscribe() {
                viewPager.setOnTouchListener(null);
              }

              @Override public boolean isUnsubscribed() {
                return false;
              }
            });
          }
        }).publish();

    bannerAdapter.updateItems(bannerItems, connectableObservable);
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      HomeActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      HomeActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      HomeActivity.this.showError(getResources().getString(R.string.service_exception_title),
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

    MaterialRippleLayout.on(bottomBarJournalRl)
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

    RxRecyclerView.scrollEvents(productRecyclerView)
        .skip(1)
        .compose(HomeActivity.this.<RecyclerViewScrollEvent>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<RecyclerViewScrollEvent>() {
          @Override public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {

            //stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
            visibleItemCount = productLayoutManager.getChildCount();
            totalItemCount = productLayoutManager.getItemCount();
            pastVisibleItems = productLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading) {
              if ((visibleItemCount + pastVisibleItems) >= totalItemCount && isEndless) {

               /* 加载更多*/
                HomeActivity.this.loadDataMore();
              }
            }
          }
        });

   /* this.productRecyclerView.getViewTreeObserver()
        .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
          @Override public void onScrollChanged() {

           *//* if (nestedScrollView.getScrollY() == 0) {
              swipeRefreshLayout.setEnabled(true);
            } else {
              swipeRefreshLayout.setEnabled(false);
            }*//*

            visibleItemCount = productLayoutManager.getChildCount();
            totalItemCount = productLayoutManager.getItemCount();
            pastVisibleItems = productLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading) {
              if ((visibleItemCount + pastVisibleItems) >= totalItemCount && isEndless) {

               *//* 加载更多*//*
                HomeActivity.this.loadDataMore();
              }
            }
          }
        });*/

    this.viewPager.addOnPageChangeListener(simpleOnPageChangeListener);
    this.appBarLayout.addOnOffsetChangedListener(offsetChangedListener);
  }

  private void loadDataMore() {

    /* 精选：Action: GetProductByKeyOrType  type:keyword  data:手表 count:10 page:1(下拉加载page = 2,3,4,5) code : 1(销量) order_by : 1（降序）*/
    final Map<String, String> productParams = new HashMap<>(3);
    productParams.put("Action", "GetProductByHot");
    //productParams.put("page", String.valueOf(++count));
    productParams.put("page", String.valueOf(count));
    productParams.put("count", "10");

    subscription =
        HomeRetrofit.getInstance()
            .getHotProduct(productParams)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*正在加载*/
                HomeActivity.this.isLoading = true;
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(HomeActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*加载完毕*/
                HomeActivity.this.isLoading = false;
                progressDialog.dismiss();
              }
            })
            .filter(new Func1<List<ProductEntity>, Boolean>() {
              @Override public Boolean call(List<ProductEntity> productEntities) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(HomeActivity.this.<List<ProductEntity>>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(productAdapter);
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    HomeActivity.this.rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        HomeActivity.this.rootView.setLayerType(View.LAYER_TYPE_NONE, null);
        HomeActivity.this.loadData();
      }
    });
    revealAnimator.start();
  }

  /**
   * 上新
   */
  @Nullable @OnClick(R.id.bottom_bar_fashion_rl) void onFashionClick() {
    FashionActivity.navigateToFashion(HomeActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 杂志
   */
  @Nullable @OnClick(R.id.bottom_bar_journal_rl) void onJournalClick() {
    JournalActivity.navigateToJournal(HomeActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 我
   */
  @Nullable @OnClick(R.id.bottom_bar_mine_rl) void onMineClick() {
    MineActivity.navigateToUserCenter(HomeActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(homeIv.getDrawable().mutate()),
        getResources().getColor(R.color.design_more_red));
    homeTv.setTextColor(getResources().getColor(R.color.design_more_red));

    /*执行进入icon动画*/
    HomeActivity.this.startIconAnim();

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleIv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(HomeActivity.this) * 2;
    toolbarTitleIv.setVisibility(View.VISIBLE);

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_inbox_1);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        SearchActivity.navigateToSearch(HomeActivity.this);
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
        TrolleyActivity.startFromLocation(HomeActivity.this, 0, TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public void exit() {
    DialogManager.getInstance().showExitDialog(HomeActivity.this);
  }

  /**
   * BannerAdapter 回调
   */
  @Override public void onItemClick(ProductEntity entity) {
    DetailActivity.navigateToDetail(HomeActivity.this, entity.getGoodId());
    overridePendingTransition(0, 0);
  }

  private int lastIndicatorPos = -1;

  @Override public void changeIndicator(int position) {

    /*I HATE THI ANIMATION BUT IOS .......YOU KNOW*/
    if (bannerIndicators != null && bannerIndicators.length != 0) {

      if (lastIndicatorPos != -1) {

        ViewCompat.animate(bannerIndicators[lastIndicatorPos])
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(Constants.MILLISECONDS_100)
            .withLayer();

        ViewCompat.animate(bannerIndicators[position])
            .scaleX(1.38f)
            .scaleY(1.38f)
            .setDuration(Constants.MILLISECONDS_100)
            .withLayer();

        bannerIndicators[lastIndicatorPos].setBackgroundDrawable(indicatorNormal);
      } else {
        ViewCompat.animate(bannerIndicators[position])
            .scaleX(1.38f)
            .scaleY(1.38f)
            .setDuration(Constants.MILLISECONDS_100)
            .withLayer();
      }

      bannerIndicators[position].setBackgroundDrawable(indicatorSelected);
      lastIndicatorPos = position;
    }
  }

  /**
   * 分类条目被点击
   */
  @Override public void onCategoryItemClick(CategoryEntity entity) {
    ProductCatIdListActivity.navigateToProductKeyList(HomeActivity.this, entity.getCatId(),
        entity.getCatName());
    overridePendingTransition(0, 0);
  }

  @Override public void onAllCategoryClick() {
    AllProductListActivity.navigateToAllProductList(HomeActivity.this, "0", "全部商品");
    overridePendingTransition(0, 0);
  }

  /**
   * 折扣条目被点击
   */
  @Override public void onDiscountItemClick(String goodId) {
    DetailActivity.navigateToDetail(HomeActivity.this, goodId);
    overridePendingTransition(0, 0);
  }

  /**
   * 商品Adapter回调
   */
  @Override public void onProductItemClick(String productId) {
    DetailActivity.navigateToDetail(HomeActivity.this, productId);
    overridePendingTransition(0, 0);
  }

  @Override public void onNoData() {
    this.isEndless = false;
    toast = DialogManager.getInstance().showNoMoreDialog(HomeActivity.this, Gravity.TOP, null);
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
            HomeActivity.this.isEndless = true;
          }
        }, Constants.MILLISECONDS_2800, TimeUnit.MILLISECONDS);

    if (error instanceof RetrofitError) {
      toast =
          DialogManager.getInstance().showNoMoreDialog(HomeActivity.this, Gravity.TOP, "请检查您的网络设置");
    } else {
      toast = DialogManager.getInstance()
          .showNoMoreDialog(HomeActivity.this, Gravity.TOP, "加载更多失败，请重试,/(ㄒoㄒ)/~~");
    }
  }

  @Override protected void onDestroy() {

    this.viewPager.removeOnPageChangeListener(bannerAdapter);
    this.viewPager.removeOnPageChangeListener(simpleOnPageChangeListener);
    this.appBarLayout.removeOnOffsetChangedListener(offsetChangedListener);
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);

      if (toast.getTag() instanceof Subscription) {
        ((Subscription) toast.getTag()).unsubscribe();
      }
    }
    this.toast = null;
    this.progressDialog = null;

    this.bannerAdapter.detach();
    if (errorSchedule != null && !errorSchedule.isUnsubscribed()) errorSchedule.unsubscribe();
    if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    HomeActivity.this.setIntent(intent);

    HomeActivity.this.startIconAnim();
  }

  @Override public void startIconAnim() {

    homeIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    Animator iconAnim = ObjectAnimator.ofPropertyValuesHolder(homeIv,
        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.5f, 1.0f),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.5f, 1.0f));
    iconAnim.setDuration(Constants.MILLISECONDS_400);
    iconAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        homeIv.setLayerType(View.LAYER_TYPE_NONE, null);
      }
    });
    iconAnim.start();
  }
}
