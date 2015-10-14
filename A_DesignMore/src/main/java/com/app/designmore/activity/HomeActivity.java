package com.app.designmore.activity;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import com.app.designmore.adapter.HomeBannerAdapter;
import com.app.designmore.adapter.HomeCategoryAdapter;
import com.app.designmore.adapter.HomeDiscountAdapter;
import com.app.designmore.adapter.HomeProductAdapter;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.manager.MarginDecoration;
import com.app.designmore.manager.WrappingGridLayoutManager;
import com.app.designmore.retrofit.HomeRetrofit;
import com.app.designmore.retrofit.entity.CategoryEntity;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.manager.WrappingLinearLayoutManager;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.view.ViewClickEvent;
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
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func4;
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
  @Nullable @Bind(R.id.home_layout_nest_view) NestedScrollView nestedScrollView;

  @Nullable @Bind(R.id.home_layout_viewpager) ViewPager viewPager;
  @Nullable @Bind(R.id.home_layout_category_rv) RecyclerView categoryRecyclerView;
  @Nullable @Bind(R.id.home_layout_discount_rv) RecyclerView fashionRecyclerView;
  @Nullable @Bind(R.id.home_layout_product_rv) RecyclerView productRecyclerView;

  @Nullable @Bind(R.id.bottom_bar_home_iv) ImageView homeIv;
  @Nullable @Bind(R.id.bottom_bar_home_tv) TextView homeTv;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;

  @Nullable @Bind(R.id.home_layout_banner_indicator1) ImageView bannerIndicator1;
  @Nullable @Bind(R.id.home_layout_banner_indicator2) ImageView bannerIndicator2;
  @Nullable @Bind(R.id.home_layout_banner_indicator3) ImageView bannerIndicator3;

  private ProgressDialog progressDialog;
  private ViewGroup toast;

  private List<ProductEntity> bannerItems = new ArrayList<>();
  private List<CategoryEntity> categoryItems = new ArrayList<>();
  private List<FashionEntity> discountItems = new ArrayList<>();
  private List<ProductEntity> productItems = new ArrayList<>();

  private HomeBannerAdapter bannerAdapter;
  private HomeCategoryAdapter categoryAdapter;
  private HomeDiscountAdapter discountAdapter;
  private HomeProductAdapter productAdapter;

  private LinearLayoutManager discountLayoutManager;
  private GridLayoutManager productLayoutManager;

  private int visibleItemCount;
  private int totalItemCount;
  private int pastVisibleItems;
  private volatile boolean isLoading = false;
  private volatile boolean isEndless = true;
  private volatile int count = 1;

  private ImageView[] bannerIndicators;
  private Drawable indicatorNormal;
  private Drawable indicatorSelected;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HomeActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
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

          HomeActivity.this.swipeRefreshLayout.setEnabled(state == ViewPager.SCROLL_STATE_IDLE);
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

    bannerIndicators = new ImageView[] {
        bannerIndicator1, bannerIndicator2, bannerIndicator3
    };
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
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
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
    //categoryRecyclerView.setLayoutManager(categoryLayoutManager);
    categoryRecyclerView.setHasFixedSize(true);
    categoryRecyclerView.setAdapter(categoryAdapter);
    categoryRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    categoryRecyclerView.addItemDecoration(
        new MarginDecoration(HomeActivity.this, R.dimen.material_1dp));

    discountLayoutManager = new WrappingLinearLayoutManager(HomeActivity.this);
    discountLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    discountLayoutManager.setSmoothScrollbarEnabled(true);
    discountAdapter = new HomeDiscountAdapter(HomeActivity.this);
    discountAdapter.setCallback(HomeActivity.this);
    fashionRecyclerView.setLayoutManager(discountLayoutManager);
    fashionRecyclerView.setHasFixedSize(true);
    fashionRecyclerView.setAdapter(discountAdapter);
    fashionRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    fashionRecyclerView.addItemDecoration(
        new DividerDecoration(HomeActivity.this, R.dimen.material_1dp));

    productLayoutManager = new WrappingGridLayoutManager(HomeActivity.this, 2);
    productLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    productLayoutManager.setSmoothScrollbarEnabled(true);
    productAdapter = new HomeProductAdapter(HomeActivity.this);
    productAdapter.setCallback(HomeActivity.this);
    productRecyclerView.setLayoutManager(productLayoutManager);
    productRecyclerView.setHasFixedSize(true);
    productRecyclerView.setNestedScrollingEnabled(false);
    productRecyclerView.setAdapter(productAdapter);
    productRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    productRecyclerView.setItemAnimator(new DefaultItemAnimator());
    productRecyclerView.addItemDecoration(
        new MarginDecoration(HomeActivity.this, R.dimen.material_8dp));
  }

  private void loadData() {

    final HomeRetrofit homeRetrofit = HomeRetrofit.getInstance();

    /*轮播：Action:GetProductByKeyOrType type:cat  data:1  page:1 count :3   code : 0（综合排序）  order_by: 1（降序）*/
    final Map<String, String> bannerParams = new HashMap<>(7);
    bannerParams.put("Action", "GetProductByKeyOrType");
    bannerParams.put("type", "cat");
    bannerParams.put("data", "2");
    bannerParams.put("page", "1");
    bannerParams.put("count", "3");
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

    Observable.defer(new Func0<Observable<Map<String, List>>>() {
      @Override public Observable<Map<String, List>> call() {
        return Observable.zip(homeRetrofit.getProductByXxx(bannerParams),
            homeRetrofit.getCategoryList(catParams), homeRetrofit.getDiscountList(discountParams),
            homeRetrofit.getProductByXxx(productParams),
            new Func4<List<ProductEntity>, List<CategoryEntity>, List<FashionEntity>, List<ProductEntity>, Map<String, List>>() {
              @Override public Map call(List<ProductEntity> bannerEntities,
                  List<CategoryEntity> categoryEntities, List<FashionEntity> discountEntities,
                  List<ProductEntity> productEntities) {

                Map<String, List> map = new HashMap(4);
                map.put(BANNER, bannerEntities);
                map.put(CAT, categoryEntities);
                map.put(DISCOUNT, discountEntities);
                map.put(PRODUCT, productEntities);
                return map;
              }
            });
      }
    })
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (!swipeRefreshLayout.isRefreshing()) progressLayout.showLoading();
          }
        })
        .compose(HomeActivity.this.<Map<String, List>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<Map<String, List>>() {
          @Override public void onCompleted() {

            /*显示内容*/
            if (swipeRefreshLayout.isRefreshing()) {
              swipeRefreshLayout.setRefreshing(false);
            } else if (!progressLayout.isContent()) {
              progressLayout.showContent();
            }

            /*设置banner*/
            HomeActivity.this.setupViewPager();
            /*设置category*/
            HomeActivity.this.categoryAdapter.updateItems(categoryItems);
            /*设置discount*/
            HomeActivity.this.discountAdapter.updateItems(discountItems);
            /*设置product*/
            HomeActivity.this.productAdapter.updateItems(productItems);
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

    bannerAdapter.updateItems(bannerItems);
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

    this.nestedScrollView.getViewTreeObserver()
        .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
          @Override public void onScrollChanged() {

            if (nestedScrollView.getScrollY() == 0) {
              swipeRefreshLayout.setEnabled(true);
            } else {
              swipeRefreshLayout.setEnabled(false);
            }

            visibleItemCount = productLayoutManager.getChildCount();
            totalItemCount = productLayoutManager.getItemCount();
            pastVisibleItems = productLayoutManager.findFirstVisibleItemPosition();

         /*   Log.e(TAG, "visibleItemCount:  " + visibleItemCount);
            Log.e(TAG, "totalItemCount:  " + totalItemCount);
            Log.e(TAG, "pastVisibleItems:  " + pastVisibleItems);*/

            if (!isLoading) {
              if ((visibleItemCount + pastVisibleItems) >= totalItemCount && isEndless) {

                /*加载更多*/
                //HomeActivity.this.loadDataMore();
              }
            }
          }
        });

    this.viewPager.addOnPageChangeListener(simpleOnPageChangeListener);
  }

  private void loadDataMore() {

    /* 精选：Action: GetProductByKeyOrType  type:keyword  data:手表 count:10 page:1(下拉加载page = 2,3,4,5) code : 1(销量) order_by : 1（降序）*/
    final Map<String, String> productParams = new HashMap<>();
    productParams.put("Action", "GetProductByHot");
    productParams.put("page", String.valueOf(++count));
    productParams.put("count", "10");

    subscription =
        HomeRetrofit.getInstance()
            .getProductByXxx(productParams)
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

        //AllProductListActivity.navigateToAllProductList(HomeActivity.this, "手表", "手表");
        //ProductKeyListActivity.navigateToProductKeyList(HomeActivity.this, "手表", "手表");
        //overridePendingTransition(0, 0);

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
    DialogManager.getInstance()
        .showExitDialog(HomeActivity.this, new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (EventBusInstance.getDefault().hasSubscriberForEvent(FinishEvent.class)) {
              EventBusInstance.getDefault().post(new FinishEvent());
            }
          }
        });
  }

  /**
   * BannerAdapter 回调
   */
  @Override public void onItemClick(ProductEntity entity) {
    DetailActivity.navigateToDetail(HomeActivity.this, entity.getGoodId());
    overridePendingTransition(0, 0);
  }

  @Override public void changeIndicator(int position) {

    for (ImageView indicator : bannerIndicators) {
      indicator.setBackgroundDrawable(indicatorNormal);
    }

    bannerIndicators[position].setBackgroundDrawable(indicatorSelected);
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
   * 新品条目被点击
   */
  @Override public void onDiscountItemClick(FashionEntity entity) {
    DetailActivity.navigateToDetail(HomeActivity.this, entity.getGoodId());
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
    if (count != 2) {
      toast = DialogManager.getInstance().showNoMoreDialog(HomeActivity.this, Gravity.TOP, null);
    }
  }

  @Override public void onError(Throwable error) {
    toast = DialogManager.getInstance()
        .showNoMoreDialog(HomeActivity.this, Gravity.TOP, "加载更多失败，请重试,/(ㄒoㄒ)/~~");
  }

  @Override protected void onDestroy() {

    this.viewPager.removeOnPageChangeListener(bannerAdapter);
    this.viewPager.removeOnPageChangeListener(simpleOnPageChangeListener);
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;

    this.bannerAdapter.detach();
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    HomeActivity.this.setIntent(intent);

    HomeActivity.this.startIconAnim();

    /*刷新*/
    //HomeActivity.this.loadData();
  }

  @Override public void startIconAnim() {

    Animator iconAnim = ObjectAnimator.ofPropertyValuesHolder(homeIv,
        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.5f, 1.0f),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.5f, 1.0f));
    iconAnim.setDuration(Constants.MILLISECONDS_400);
    iconAnim.start();
  }
}
