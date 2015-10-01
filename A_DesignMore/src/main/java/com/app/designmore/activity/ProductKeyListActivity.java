package com.app.designmore.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.adapter.ProductAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.MarginDecoration;
import com.app.designmore.retrofit.ProductRetrofit;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
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
 * Created by Joker on 2015/9/21.
 */
public class ProductKeyListActivity extends BaseActivity implements ProductAdapter.Callback {

  private static final String TAG = ProductKeyListActivity.class.getCanonicalName();
  private static final String KEYWORD = "KEYWORD";
  private static final String TITLE = "TITLE";

  @Nullable @Bind(R.id.product_key_layout_root_view) LinearLayout rootView;

  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.product_key_layout_reveal_view) RevealFrameLayout revealFrameLayout;

  @Nullable @Bind(R.id.product_key_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.product_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.product_key_layout_rl) RecyclerView recyclerView;

  private SupportAnimator revealAnimator;

  private int visibleItemCount;
  private int totalItemCount;
  private int pastVisibleItems;
  private boolean isLoading = false;

  private volatile int page;
  private volatile int count = 1;
  private volatile int code = 0;
  private volatile int currentCode = 0;
  private volatile int order_by = 1;/*1,降序 0,升序*/
  private volatile int currentOrderBy = 1;/*1,降序 0,升序*/
  private volatile String keyword;
  private volatile String title;

  private List<ProductEntity> items = new ArrayList<>();
  private ProductAdapter productAdapter;
  private volatile boolean isEndless = true;
  private int redTextColor;
  private int greyTextColor;

  private Dialog progressDialog;
  private ViewGroup toast;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      ProductKeyListActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  public static void navigateToProductKeyList(AppCompatActivity startingActivity, String keyword,
      String title) {
    Intent intent = new Intent(startingActivity, ProductKeyListActivity.class);
    intent.putExtra(KEYWORD, keyword);
    intent.putExtra(TITLE, title);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.product_key_layout);

    ProductKeyListActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    ProductKeyListActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);

    this.keyword = getIntent().getStringExtra(KEYWORD);
    this.title = getIntent().getStringExtra(TITLE);

    /*创建Adapter*/
    ProductKeyListActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      revealFrameLayout.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealFrameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
              ProductKeyListActivity.this.startEnterAnim();
              return true;
            }
          });
    } else {
      ProductKeyListActivity.this.loadData();
    }
  }

  private void loadData() {

    /*Action=GetProductByKeyOrType&type=keyword&data=%E5%A4%A9%E9%BC%8E&page=1&count=10*/
    /*code = 综合排序== 0   销量 == 1   新品 == 2  收藏 == 3  价格 == 4
    order_by = 0升序 1降序*/

    Map<String, String> params = new HashMap<>(7);
    params.put("Action", "GetProductByKeyOrType");
    params.put("type", "keyword");
    params.put("data", keyword);
    params.put("page", String.valueOf(page = 1));
    params.put("count", "10");
    params.put("code", String.valueOf(code));
    params.put("order_by", String.valueOf(order_by));

    subscription =
        ProductRetrofit.getInstance()
            .getProductByXxx(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {

                /*加载数据，显示进度条*/
                if (!swipeRefreshLayout.isRefreshing()) {

                  if (progressLayout.isContent()) {

                    if (progressDialog == null) {
                      progressDialog = DialogManager.getInstance()
                          .showSimpleProgressDialog(ProductKeyListActivity.this, cancelListener);
                    } else {
                      progressDialog.show();
                    }
                  } else {
                    progressLayout.showLoading();
                  }
                }
              }
            })
            .doOnCompleted(new Action0() {
              @Override public void call() {
                /*改变字体颜色*/
                ProductKeyListActivity.this.updateTextColor();
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                if (progressDialog != null && progressDialog.isShowing()) {
                  progressDialog.dismiss();
                }
              }
            })
            .filter(new Func1<List<ProductEntity>, Boolean>() {
              @Override public Boolean call(List<ProductEntity> productEntities) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(ProductKeyListActivity.this.<List<ProductEntity>>bindUntilEvent(
                ActivityEvent.DESTROY))
            .subscribe(new Subscriber<List<ProductEntity>>() {
              @Override public void onCompleted() {

                /*加载完毕，显示内容界面*/
                if (items != null && items.size() != 0) {

                  ProductKeyListActivity.this.isEndless = true;

                  if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                  } else if (!progressLayout.isContent()) {
                    progressLayout.showContent();
                  }
                } else if (items != null && items.size() == 0) {
                  progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                      "没有对应商品", null);
                }
              }

              @Override public void onError(Throwable error) {
                /*加载失败，显示错误界面*/
                ProductKeyListActivity.this.showErrorLayout(error);
              }

              @Override public void onNext(List<ProductEntity> productEntities) {

                ProductKeyListActivity.this.items.clear();
                ProductKeyListActivity.this.items.addAll(productEntities);
                productAdapter.updateItems(items);
              }
            });
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        ProductKeyListActivity.this.loadData();
      }
    });

    final GridLayoutManager gridLayoutManager =
        new GridLayoutManager(ProductKeyListActivity.this, 2);
    gridLayoutManager.setSmoothScrollbarEnabled(true);

    productAdapter = new ProductAdapter(ProductKeyListActivity.this);
    productAdapter.setCallback(ProductKeyListActivity.this);

    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(productAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new MarginDecoration(ProductKeyListActivity.this, R.dimen.material_8dp));
    recyclerView.setItemAnimator(new DefaultItemAnimator());

    RxRecyclerView.scrollEvents(recyclerView)
        .skip(1)
        .forEach(new Action1<RecyclerViewScrollEvent>() {
          @Override public void call(RecyclerViewScrollEvent recyclerViewScrollEvent) {

            //stackoverflow.com/questions/26543131/how-to-implement-endless-list-with-recyclerview/26561717#26561717
            visibleItemCount = gridLayoutManager.getChildCount();
            totalItemCount = gridLayoutManager.getItemCount();
            pastVisibleItems = gridLayoutManager.findFirstVisibleItemPosition();

            if (!isLoading) {
              if ((visibleItemCount + pastVisibleItems) >= totalItemCount && isEndless) {
                /*加载更多*/
                ProductKeyListActivity.this.loadDataMore();
              }
            }
          }
        });
  }

  private void loadDataMore() {

    /*Action=GetProductByKeyOrType&type=keyword&data=%E5%A4%A9%E9%BC%8E&page=1&count=10*/
    /*code = 综合排序== 0   销量 == 1   新品 == 2  收藏 == 3  价格 == 4
    order_by = 0升序 1降序*/

    Map<String, String> params = new HashMap<>(7);
    params.put("Action", "GetProductByKeyOrType");
    params.put("type", "keyword");
    params.put("data", keyword);
    params.put("page", String.valueOf(++page));
    params.put("count", "10");
    params.put("code", String.valueOf(code));
    params.put("order_by", String.valueOf(order_by));

    subscription =
        ProductRetrofit.getInstance()
            .getProductByXxx(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*正在加载*/
                ProductKeyListActivity.this.isLoading = true;
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(ProductKeyListActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*加载完毕*/
                ProductKeyListActivity.this.isLoading = false;
                progressDialog.dismiss();
              }
            })
            .filter(new Func1<List<ProductEntity>, Boolean>() {
              @Override public Boolean call(List<ProductEntity> productEntities) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(ProductKeyListActivity.this.<List<ProductEntity>>bindUntilEvent(
                ActivityEvent.DESTROY))
            .subscribe(productAdapter);
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      ProductKeyListActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      ProductKeyListActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      ProductKeyListActivity.this.showError(
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

  @Nullable @Bind(R.id.composite_order_tv) TextView compositeTv;
  @Nullable @Bind(R.id.sales_order_tv) TextView saleTv;
  @Nullable @Bind(R.id.fashion_order_tv) TextView fashionTv;
  @Nullable @Bind(R.id.collection_order_tv) TextView collectionTv;
  @Nullable @Bind(R.id.price_item_tv) TextView priceTv;
  @Nullable @Bind(R.id.price_item_iv) ImageView priceArrowIv;

  private static final int composite = 0;
  private static final int sale = 1;
  private static final int fashion = 2;
  private static final int collection = 3;
  private static final int price = 4;

  private void updateTextColor() {

    this.compositeTv.setTextColor(greyTextColor);
    this.saleTv.setTextColor(greyTextColor);
    this.fashionTv.setTextColor(greyTextColor);
    this.collectionTv.setTextColor(greyTextColor);
    this.priceTv.setTextColor(greyTextColor);

    if (code != price) {
      ViewCompat.animate(priceArrowIv)
          .rotation(0.0f)
          .setDuration(Constants.MILLISECONDS_300)
          .withLayer();
    }

    switch (code) {

      case composite:
        this.currentCode = composite;
        this.currentOrderBy = 1;
        this.compositeTv.setTextColor(redTextColor);
        break;
      case sale:
        this.currentCode = sale;
        this.currentOrderBy = 1;
        this.saleTv.setTextColor(redTextColor);
        break;
      case fashion:
        this.currentCode = fashion;
        this.currentOrderBy = 1;
        this.fashionTv.setTextColor(redTextColor);
        break;
      case collection:
        this.currentCode = collection;
        this.currentOrderBy = 1;
        this.collectionTv.setTextColor(redTextColor);
        break;
      case price:
        if (currentCode == price) {
          if (this.currentOrderBy == 1) {
            this.currentOrderBy = 0;
          } else {
            this.currentOrderBy = 1;
          }
        }
        this.currentCode = price;
        this.priceTv.setTextColor(redTextColor);
        break;
    }
  }

  /**
   * code = 综合排序== 0   销量 == 1   新品 == 2  收藏 == 3  价格 == 4
   */

  @Nullable @OnClick(R.id.composite_order_tv) void onCompositeClick(TextView textView) {
    if (this.currentCode != 0) {
      this.code = 0;
      this.order_by = 1;
      ProductKeyListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.sales_order_tv) void onSaleClick(TextView textView) {
    if (this.currentCode != 1) {
      this.code = 1;
      this.order_by = 1;
      ProductKeyListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.fashion_order_tv) void onFashionClick(TextView textView) {
    if (this.currentCode != 2) {
      this.code = 2;
      this.order_by = 1;
      ProductKeyListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.collection_order_tv) void onCollectionClick(TextView textView) {
    if (this.currentCode != 3) {
      this.code = 3;
      this.order_by = 1;
      ProductKeyListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.price_item_ll) void onPriceClick(LinearLayout linearLayout) {

    if (currentCode != 4) {
      this.code = 4;
      this.order_by = 1;
    } else {
      if (this.currentOrderBy == 1) {
        this.order_by = 0;
        ViewCompat.animate(priceArrowIv)
            .rotation(180.0f)
            .setDuration(Constants.MILLISECONDS_300)
            .withLayer();
      } else {
        this.order_by = 1;
        ViewCompat.animate(priceArrowIv)
            .rotation(0.0f)
            .setDuration(Constants.MILLISECONDS_300)
            .withLayer();
      }
    }
    ProductKeyListActivity.this.loadData();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(ProductKeyListActivity.this);

    this.toolbarTitleTv.setVisibility(View.VISIBLE);
    this.toolbarTitleTv.setText(title);

    this.redTextColor = getResources().getColor(R.color.design_more_red);
    this.greyTextColor = getResources().getColor(R.color.darker_gray);

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_inbox_1);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SearchActivity.navigateToSearch(ProductKeyListActivity.this);
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
        TrolleyActivity.startFromLocation(ProductKeyListActivity.this, 0, TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  private void startEnterAnim() {
    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    ProductKeyListActivity.this.rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        if (progressLayout != null) {
          ProductKeyListActivity.this.rootView.setLayerType(View.LAYER_TYPE_NONE, null);
          ProductKeyListActivity.this.loadData();
        }
      }
    });
    revealAnimator.start();
  }

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(ProductKeyListActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            ProductKeyListActivity.this.finish();
          }
        });
  }

  @Override public void onItemClick(String productId) {
    DetailActivity.navigateToDetail(ProductKeyListActivity.this, productId);
    overridePendingTransition(0, 0);
  }

  @Override public void onNoData() {
    this.isEndless = false;
    toast = DialogManager.getInstance()
        .showNoMoreDialog(ProductKeyListActivity.this, Gravity.TOP, null);
  }

  @Override public void onError(Throwable error) {
    toast = DialogManager.getInstance()
        .showNoMoreDialog(ProductKeyListActivity.this, Gravity.TOP, "加载更多失败，请重试,/(ㄒoㄒ)/~~");
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
