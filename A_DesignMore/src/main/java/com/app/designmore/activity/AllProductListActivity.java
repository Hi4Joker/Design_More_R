package com.app.designmore.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
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
import com.app.designmore.adapter.SearchAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.MarginDecoration;
import com.app.designmore.retrofit.ProductRetrofit;
import com.app.designmore.retrofit.SearchRetrofit;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.jakewharton.rxbinding.support.v7.widget.RecyclerViewScrollEvent;
import com.jakewharton.rxbinding.support.v7.widget.RxRecyclerView;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.Collection;
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
import rx.functions.Func2;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/21.
 */
public class AllProductListActivity extends BaseActivity
    implements ProductAdapter.Callback, SearchAdapter.Callback {

  private static final String TAG = AllProductListActivity.class.getCanonicalName();
  private static final String CAT_ID = "CAT_ID";
  private static final String TITLE = "TITLE";
  private static final Object KEY_ENTITIES = "KEY_ENTITIES";
  private static final Object PRODUCT_ENTITIES = "PRODUCT_ENTITIES";

  @Nullable @Bind(R.id.product_all_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.product_all_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.product_all_layout_app_bar) AppBarLayout appBarLayout;
  @Nullable @Bind(R.id.product_all_layout_collapsing_bar) CollapsingToolbarLayout
      collapsingToolbarLayout;
  @Nullable @Bind(R.id.product_all_layout_keyword_rl) RecyclerView collRecyclerView;
  @Nullable @Bind(R.id.product_all_layout_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.product_all_layout_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.product_all_product_layout_rl) ProgressLayout productProgressLayout;
  @Nullable @Bind(R.id.product_all_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.product_all_layout_rl) RecyclerView recyclerView;

  private int visibleItemCount;
  private int totalItemCount;
  private int pastVisibleItems;
  private boolean isLoading = false;

  private volatile int page;
  private volatile int count = 1;
  private volatile int code = 0;
  private volatile int currentCode = 0;
  private volatile int order_by = 1;/*1:降序; 0:升序*/
  private volatile int currentOrderBy = 1;/*1:降序; 0:升序*/
  private volatile String catId;
  private volatile String title;

  private List<ProductEntity> productItems = new ArrayList<>();
  private List<SearchItemEntity> searchItems = new ArrayList<>();
  private ProductAdapter productAdapter;
  private SearchAdapter searchAdapter;
  private volatile boolean isEndless = true;
  private int redTextColor;
  private int greyTextColor;

  private Dialog progressDialog;
  private ViewGroup toast;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      AllProductListActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  private AppBarLayout.OnOffsetChangedListener offsetChangedListener =
      new AppBarLayout.OnOffsetChangedListener() {
        @Override public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
          if (offset == 0) {
            swipeRefreshLayout.setEnabled(true);
          } else {
            swipeRefreshLayout.setEnabled(false);
          }
        }
      };

  public static void navigateToAllProductList(AppCompatActivity startingActivity, String catId,
      String title) {
    Intent intent = new Intent(startingActivity, AllProductListActivity.class);
    intent.putExtra(CAT_ID, catId);
    intent.putExtra(TITLE, title);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.product_all_layout);

    AllProductListActivity.this.initView(savedInstanceState);
    AllProductListActivity.this.setListener();
  }

  private void setListener() {
    this.appBarLayout.addOnOffsetChangedListener(offsetChangedListener);
  }

  @Override public void initView(Bundle savedInstanceState) {

    AllProductListActivity.this.setSupportActionBar(toolbar);
    this.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);
    this.collapsingToolbarLayout.setTitleEnabled(false);

    this.catId = getIntent().getStringExtra(CAT_ID);
    this.title = getIntent().getStringExtra(TITLE);

    /*创建Adapter*/
    AllProductListActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          AllProductListActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      AllProductListActivity.this.initData();
    }
  }

  /**
   * 加载热搜数据
   */
  private void initData() {

    /*Action=GetKeywordList*/
    final Map<String, String> keywordParams = new HashMap<>(1);
    keywordParams.put("Action", "GetKeywordList");

    /*Action=GetProductByKeyOrType&type=keyword&data=%E5%A4%A9%E9%BC%8E&page=1&count=10*/
    /*code = 综合排序== 0   销量 == 1   新品 == 2  收藏 == 3  价格 == 4
    order_by = 0升序 1降序*/
    final Map<String, String> productParams = new HashMap<>(7);
    productParams.put("Action", "GetProductByKeyOrType");
    productParams.put("type", "keyword");
    productParams.put("data", "全部商品");
    productParams.put("page", String.valueOf(page = 1));
    productParams.put("count", "10");
    productParams.put("code", String.valueOf(code));
    productParams.put("order_by", String.valueOf(order_by));

    Observable.zip(SearchRetrofit.getInstance().getHotSearchList(keywordParams),
        ProductRetrofit.getInstance().getProductByCatId(productParams),
        new Func2<List<SearchItemEntity>, List<ProductEntity>, Map>() {
          @Override public Map call(List<SearchItemEntity> searchItemEntities,
              List<ProductEntity> productEntities) {

            Map map = new HashMap(2);
            map.put(KEY_ENTITIES, searchItemEntities);
            map.put(PRODUCT_ENTITIES, productEntities);

            return map;
          }
        })
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {
            /*改变字体颜色*/
            AllProductListActivity.this.updateTextColor();
          }
        })
        .compose(AllProductListActivity.this.<Map>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<Map>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (searchItems != null && searchItems.size() != 0 && !progressLayout.isContent()) {
              progressLayout.showContent();
              if (productItems != null && productItems.size() == 0) {
                productProgressLayout.showEmpty(
                    getResources().getDrawable(R.drawable.ic_grey_logo_icon), "很抱歉，没有搜索到相关商品",
                    null);
              } else {
                productProgressLayout.showContent();
              }
            } else {
              progressLayout.showEmpty(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "很抱歉，没有搜索到相关商品", null);
            }
          }

          @Override public void onError(Throwable e) {
            /*加载失败，显示错误界面*/
            AllProductListActivity.this.showErrorLayout(progressLayout, e);
          }

          @Override public void onNext(Map map) {

            AllProductListActivity.this.searchItems.clear();
            AllProductListActivity.this.searchItems.addAll(
                (Collection<SearchItemEntity>) map.get(KEY_ENTITIES));
            searchAdapter.updateItems(searchItems);

            AllProductListActivity.this.productItems.clear();
            AllProductListActivity.this.productItems.addAll(
                (Collection<ProductEntity>) map.get(PRODUCT_ENTITIES));
            productAdapter.updateItems(productItems);
          }
        });
  }

  private void loadData() {

    /*data=0&code=0&count=10&type=cat&Action=GetProductByKeyOrType&page=1&order_by=1*/
    /*Action=GetProductByKeyOrType&type=keyword&data=%E5%A4%A9%E9%BC%8E&page=1&count=10*/
    /*code = 综合排序== 0   销量 == 1   新品 == 2  收藏 == 3  价格 == 4
    order_by = 0升序 1降序*/

    Map<String, String> params = new HashMap<>(7);
    params.put("Action", "GetProductByKeyOrType");
    params.put("type", "keyword");
    params.put("data", "全部商品");
    params.put("page", String.valueOf(page = 1));
    params.put("count", "10");
    params.put("code", String.valueOf(code));
    params.put("order_by", String.valueOf(order_by));

    subscription =
        ProductRetrofit.getInstance()
            .getProductByCatId(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {

                /*加载数据，显示进度条*/
                if (!swipeRefreshLayout.isRefreshing()) {

                  if (productProgressLayout.isContent()) {

                    AllProductListActivity.this.recyclerView.smoothScrollToPosition(0);

                    if (progressDialog == null) {
                      progressDialog = DialogManager.getInstance()
                          .showSimpleProgressDialog(AllProductListActivity.this, cancelListener);
                    } else {
                      progressDialog.show();
                    }
                  } else {
                    productProgressLayout.showLoading();
                  }
                }
              }
            })
            .doOnCompleted(new Action0() {
              @Override public void call() {
                /*改变字体颜色*/
                AllProductListActivity.this.updateTextColor();
              }
            })
            .doOnError(new Action1<Throwable>() {
              @Override public void call(Throwable throwable) {
                /*改变字体颜色*/
                AllProductListActivity.this.updateTextColor();
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
            .compose(AllProductListActivity.this.<List<ProductEntity>>bindUntilEvent(
                ActivityEvent.DESTROY))
            .subscribe(new Subscriber<List<ProductEntity>>() {
              @Override public void onCompleted() {

                /*加载完毕，显示内容界面*/
                if (productItems != null && productItems.size() != 0) {

                  Observable.timer(Constants.MILLISECONDS_300, TimeUnit.MILLISECONDS,
                      AndroidSchedulers.mainThread()).forEach(new Action1<Long>() {
                    @Override public void call(Long aLong) {
                      AllProductListActivity.this.isEndless = true;
                    }
                  });

                  if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                  } else if (!productProgressLayout.isContent()) {
                    productProgressLayout.showContent();
                  } else if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                  }
                } else {
                  productProgressLayout.showEmpty(
                      getResources().getDrawable(R.drawable.ic_grey_logo_icon), "空空如也", null);
                }
              }

              @Override public void onError(Throwable error) {
                /*加载失败，显示错误界面*/
                AllProductListActivity.this.showErrorLayout(productProgressLayout, error);
              }

              @Override public void onNext(List<ProductEntity> productEntities) {

                AllProductListActivity.this.productItems.clear();
                AllProductListActivity.this.productItems.addAll(productEntities);
                productAdapter.updateItems(productItems);
              }
            });
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout).forEach(new Action1<Void>() {
      @Override public void call(Void aVoid) {
        AllProductListActivity.this.loadData();
      }
    });

    GridLayoutManager keywordGridLayoutManager =
        new GridLayoutManager(AllProductListActivity.this, 5);
    keywordGridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    keywordGridLayoutManager.setSmoothScrollbarEnabled(true);

    searchAdapter = new SearchAdapter(this);
    searchAdapter.setCallback(AllProductListActivity.this);

    collRecyclerView.setLayoutManager(keywordGridLayoutManager);
    collRecyclerView.setHasFixedSize(true);
    collRecyclerView.setAdapter(searchAdapter);
    collRecyclerView.addItemDecoration(
        new MarginDecoration(AllProductListActivity.this, R.dimen.material_4dp));

    final GridLayoutManager gridLayoutManager =
        new GridLayoutManager(AllProductListActivity.this, 2);
    gridLayoutManager.setSmoothScrollbarEnabled(true);

    productAdapter = new ProductAdapter(AllProductListActivity.this);
    productAdapter.setCallback(AllProductListActivity.this);

    recyclerView.setLayoutManager(gridLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(productAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new MarginDecoration(AllProductListActivity.this, R.dimen.material_8dp));
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
                AllProductListActivity.this.loadDataMore();
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
    params.put("data", "全部商品");
    params.put("page", String.valueOf(++page));
    params.put("count", "10");
    params.put("code", String.valueOf(code));
    params.put("order_by", String.valueOf(order_by));

    subscription =
        ProductRetrofit.getInstance()
            .getProductByCatId(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*正在加载*/
                AllProductListActivity.this.isLoading = true;
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(AllProductListActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*加载完毕*/
                AllProductListActivity.this.isLoading = false;
                progressDialog.dismiss();
              }
            })
            .filter(new Func1<List<ProductEntity>, Boolean>() {
              @Override public Boolean call(List<ProductEntity> productEntities) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(AllProductListActivity.this.<List<ProductEntity>>bindUntilEvent(
                ActivityEvent.DESTROY))
            .subscribe(productAdapter);
  }

  private void showErrorLayout(ProgressLayout layout, Throwable error) {
    if (error instanceof TimeoutException) {
      AllProductListActivity.this.showError(layout,
          getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      AllProductListActivity.this.showError(layout,
          getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      AllProductListActivity.this.showError(layout,
          getResources().getString(R.string.service_exception_title),
          getResources().getString(R.string.service_exception_content));
    } else {
      Log.e(TAG, error.getMessage());
      error.printStackTrace();
      throw new RuntimeException("See inner exception");
    }
  }

  private void showError(ProgressLayout layout, String errorTitle, String errorContent) {
    layout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon), errorTitle,
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
      AllProductListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.sales_order_tv) void onSaleClick(TextView textView) {
    if (this.currentCode != 1) {
      this.code = 1;
      this.order_by = 1;
      AllProductListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.fashion_order_tv) void onFashionClick(TextView textView) {
    if (this.currentCode != 2) {
      this.code = 2;
      this.order_by = 1;
      AllProductListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.collection_order_tv) void onCollectionClick(TextView textView) {
    if (this.currentCode != 3) {
      this.code = 3;
      this.order_by = 1;
      AllProductListActivity.this.loadData();
    }
  }

  @Nullable @OnClick(R.id.price_item_ll) void onPriceClick(LinearLayout linearLayout) {

    if (currentCode != 4) {
      this.code = 4;
      this.order_by = 0;
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
    AllProductListActivity.this.loadData();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    Toolbar.LayoutParams params = (Toolbar.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.leftMargin = DensityUtil.getActionBarSize(AllProductListActivity.this);

    this.toolbarTitleTv.setText(title);

    this.redTextColor = getResources().getColor(R.color.design_more_red);
    this.greyTextColor = getResources().getColor(R.color.darker_gray);
    this.compositeTv.setTextColor(redTextColor);

    getMenuInflater().inflate(R.menu.menu_main, menu);

    MenuItem searchItem = menu.findItem(R.id.action_inbox_1);
    searchItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton searchButton =
        (ImageButton) searchItem.getActionView().findViewById(R.id.action_inbox_btn);
    searchButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_search_icon));
    searchItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SearchActivity.navigateToSearch(AllProductListActivity.this);
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
        TrolleyActivity.startFromLocation(AllProductListActivity.this,
            DensityUtil.getActionBarSize(AllProductListActivity.this), TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  private void startEnterAnim() {
    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    AllProductListActivity.this.rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), 0, bounds.left, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
        if (progressLayout != null) {
          AllProductListActivity.this.rootView.setLayerType(View.LAYER_TYPE_NONE, null);
          AllProductListActivity.this.initData();
        }
      }
    });
    revealAnimator.start();
  }

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(AllProductListActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            AllProductListActivity.this.finish();
          }
        });
  }

  @Override public void onItemClick(String productId) {
    DetailActivity.navigateToDetail(AllProductListActivity.this, productId);
    overridePendingTransition(0, 0);
  }

  @Override public void onNoData() {
    this.isEndless = false;
    toast = DialogManager.getInstance()
        .showNoMoreDialog(AllProductListActivity.this, Gravity.TOP, null);
  }

  @Override public void onError(Throwable error) {
    toast = DialogManager.getInstance()
        .showNoMoreDialog(AllProductListActivity.this, Gravity.TOP, "加载更多失败，请重试,/(ㄒoㄒ)/~~");
  }

  /*keyword 回调*/
  @Override public void onItemClick(int position) {

    ProductKeyListActivity.navigateToProductKeyList(AllProductListActivity.this,
        searchItems.get(position).getText(), searchItems.get(position).getText());
    overridePendingTransition(0, 0);
  }

  @Override protected void onDestroy() {
    this.appBarLayout.removeOnOffsetChangedListener(offsetChangedListener);
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
