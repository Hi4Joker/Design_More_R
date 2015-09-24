package com.app.designmore.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.HomeRetrofit;
import com.app.designmore.retrofit.ProductRetrofit;
import com.app.designmore.retrofit.entity.CategoryEntity;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
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
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func4;
import rx.subscriptions.Subscriptions;

public class HomeActivity extends BaseActivity {

  private static final String TAG = HomeActivity.class.getSimpleName();
  private static final String BANNER = "BANNER";
  private static final String CAT = "CAT";
  private static final String DISCOUNT = "DISCOUNT";
  private static final String PRODUCT = "PRODUCT";

  @Nullable @Bind(R.id.home_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_iv) ImageView toolbarTitleIv;
  @Nullable @Bind(R.id.home_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.home_layout_srl) SwipeRefreshLayout swipeRefreshLayout;

  @Nullable @Bind(R.id.bottom_bar_home_iv) ImageView homeIv;
  @Nullable @Bind(R.id.bottom_bar_home_tv) TextView homeTv;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;
  @Nullable @Bind(R.id.bottom_bar_mine_rl) RelativeLayout bottomBarMineRl;

  private SupportAnimator revealAnimator;

  private List<ProductEntity> bannerItems = new ArrayList<>();
  private List<CategoryEntity> categoryItems = new ArrayList<>();
  private List<FashionEntity> discountItems = new ArrayList<>();
  private List<ProductEntity> productItems = new ArrayList<>();

  private int count = 1;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HomeActivity.this.loadData();
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
    //toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

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

  private void loadData() {

    final HomeRetrofit homeRetrofit = HomeRetrofit.getInstance();

    /*轮播：Action:GetProductByKeyOrType type:cat  data:1  page:1 count :3   code : 0  order_by: 1*/
    final Map<String, String> bannerParams = new HashMap<>(7);
    bannerParams.put("Action", "GetProductByKeyOrType");
    bannerParams.put("type", "cat");
    bannerParams.put("data", "1");
    bannerParams.put("page", "1");
    bannerParams.put("count", "3");
    bannerParams.put("code", "0");
    bannerParams.put("order_by", "1");

    /*打折：Action: GetProductByStar count:2 page:1*/
    final Map<String, String> discountParams = new HashMap<>(3);
    discountParams.put("Action", "GetProductByStar");
    discountParams.put("page", "1");
    discountParams.put("count", "2");

    /*分类： Action=GetIndexCatList*/
    final Map<String, String> catParams = new HashMap<>(1);
    catParams.put("Action", "GetIndexCatList");

    /* Action: GetProductByKeyOrType  type:keyword  data:手表 count:10 page:1(下拉加载page = 2,3,4,5) order_by : 1 code : 1*/
    final Map<String, String> productParams = new HashMap<>();
    productParams.put("Action", "GetProductByKeyOrType");
    productParams.put("type", "keyword");
    productParams.put("data", "手表");
    productParams.put("page", String.valueOf(count = 1));
    productParams.put("count", "10");
    productParams.put("code", "1");
    productParams.put("order_by", "1");

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

    MaterialRippleLayout.on(bottomBarJournalRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarMineRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();

    MaterialRippleLayout.on(bottomBarFashionRl)
        .rippleDiameterDp(DensityUtil.dip2px(5))
        .rippleFadeDuration(100)
        .rippleAlpha(0.4f)
        .rippleDuration(600)
        .rippleHover(true)
        .rippleOverlay(true)
        .rippleDelayClick(true)
        .rippleColor(getResources().getColor(android.R.color.darker_gray))
        .create();
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
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

  /**
   * 全部商品
   *//*
  @Nullable @OnClick(R.id.home_layout_all_product_ib) void onAllProductClick() {
    //ProductKeyListActivity.navigateToProductKeyList(HomeActivity.this, "手表","手表");
    AllProductListActivity.navigateToAllProductList(HomeActivity.this, "手表", "手表");
    overridePendingTransition(0, 0);
  }*/
  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(homeIv.getDrawable().mutate()),
        getResources().getColor(R.color.design_more_red));
    homeTv.setTextColor(getResources().getColor(R.color.design_more_red));

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
        TrolleyActivity.startFromLocation(HomeActivity.this,
            DensityUtil.getActionBarSize(HomeActivity.this), TrolleyActivity.Type.UP);
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

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    HomeActivity.this.setIntent(intent);

    /*刷新*/
    HomeActivity.this.loadData();
  }
}
