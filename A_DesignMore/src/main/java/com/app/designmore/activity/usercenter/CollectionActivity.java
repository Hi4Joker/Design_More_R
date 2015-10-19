package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.DetailActivity;
import com.app.designmore.activity.HomeActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.adapter.CollectionAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.CollectionRetrofit;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.view.dialog.CustomShareDialog;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
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
 * Created by Joker on 2015/9/4.
 */
public class CollectionActivity extends BaseActivity
    implements CollectionAdapter.Callback, CustomShareDialog.Callback {

  private static final String TAG = CollectionActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.collection_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.collection_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.collection_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.collection_layout_rv) RecyclerView recyclerView;

  private CollectionAdapter collectionAdapter;
  private List<CollectionEntity> items = new ArrayList<>();
  private Subscription subscription = Subscriptions.empty();

  private ProgressDialog progressDialog;
  private CustomShareDialog customShareDialog;
  private ViewGroup toast;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      CollectionActivity.this.loadData();
    }
  };

  private View.OnClickListener goHomeClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      HomeActivity.navigateToHome(CollectionActivity.this);
      overridePendingTransition(0, 0);
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  public static void startFromLocation(MineActivity startingActivity, int startingLocationY) {
    Intent intent = new Intent(startingActivity, CollectionActivity.class);
    intent.putExtra(START_LOCATION_Y, startingLocationY);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_collection_layout);

    CollectionActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    CollectionActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) toolbarTitleTv.getLayoutParams();
    params.rightMargin = DensityUtil.getActionBarSize(CollectionActivity.this);
    toolbarTitleTv.setVisibility(View.VISIBLE);
    toolbarTitleTv.setText("我的收藏");

    /*创建Adapter*/
    CollectionActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          CollectionActivity.this.startEnterAnim(getIntent().getIntExtra(START_LOCATION_Y, 0));
          return true;
        }
      });
    } else {
      CollectionActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .compose(CollectionActivity.this.<Void>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Void>() {
          @Override public void call(Void aVoid) {
            CollectionActivity.this.loadData();
          }
        });

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CollectionActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    collectionAdapter = new CollectionAdapter(CollectionActivity.this);
    collectionAdapter.setCallback(CollectionActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(collectionAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(
        new DividerDecoration(CollectionActivity.this, R.dimen.material_1dp));
    recyclerView.setItemAnimator(new DefaultItemAnimator());
  }

  private void startEnterAnim(int startLocationY) {

    ViewCompat.setLayerType(rootView, ViewCompat.LAYER_TYPE_HARDWARE, null);
    rootView.setPivotY(startLocationY);
    ViewCompat.setScaleY(rootView, 0.0f);

    ViewCompat.animate(rootView)
        .scaleY(1.0f)
        .setDuration(Constants.MILLISECONDS_400 / 2)
        .setInterpolator(new AccelerateInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CollectionActivity.this.loadData();
          }
        });
  }

  /**
   * 刷新数据
   */
  private void loadData() {

    /*Action=GetCollectByGoods&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetCollectByGoods");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(CollectionActivity.this));


    CollectionRetrofit.getInstance()
        .getCollectionList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
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
            CollectionActivity.this.<List<CollectionEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<CollectionEntity>>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0 && !progressLayout.isContent()) {
              progressLayout.showContent();
            } else if (items != null && items.size() == 0) {
              progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您还没有收藏", null, "去首页看看", goHomeClickListener);
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            CollectionActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<CollectionEntity> collectionEntities) {

            CollectionActivity.this.items.clear();
            CollectionActivity.this.items.addAll(collectionEntities);
            collectionAdapter.updateItems(items);
          }
        });
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      CollectionActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      CollectionActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      CollectionActivity.this.showError(getResources().getString(R.string.service_exception_title),
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

  //CollectionAdapter回调
  /*点击条目，跳转商品详情*/
  @Override public void onItemClick(CollectionEntity entity) {

    DetailActivity.navigateToDetail(CollectionActivity.this, entity.getGoodId());
    overridePendingTransition(0, 0);
  }

  /*点击更多按钮，弹出列表对话框，删除操作*/
  @Override public void onDeleteClick(final CollectionEntity entity) {
    DialogManager.getInstance()
        .showNormalDialog(CollectionActivity.this, "删除收藏", new DialogInterface.OnClickListener() {
          @Override public void onClick(DialogInterface dialog, int which) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
              CollectionActivity.this.requestDeleteCollection(entity);
            }
          }
        });
  }

  /*弹出列表对话框，分享操作*/
  @Override public void onShareClick(final CollectionEntity entity) {
    if (customShareDialog == null) {
      customShareDialog =
          DialogManager.getInstance().showShareDialog(CollectionActivity.this, this);
    }
    customShareDialog.show();
  }

  /**
   * 删除收藏
   */
  private void requestDeleteCollection(final CollectionEntity deleteCollection) {

    /*Action=DelCollectByGoods&uid=1&rec_id=1*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "DelCollectByGoods");
    params.put("rec_id", deleteCollection.getCollectionId());
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(CollectionActivity.this));

    subscription = CollectionRetrofit.getInstance()
        .requestDeleteCollection(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.
                  getInstance().showSimpleProgressDialog(CollectionActivity.this, cancelListener);
            } else {
              progressDialog.show();
            }
          }
        })
        .map(new Func1<BaseResponse, Integer>() {
          @Override public Integer call(BaseResponse baseResponse) {
            return items.indexOf(deleteCollection);
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            /*隐藏进度条*/
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
          }
        })
        .doOnCompleted(new Action0() {
          @Override public void call() {

            toast = DialogManager.getInstance()
                .showNoMoreDialog(CollectionActivity.this, Gravity.TOP, "删除成功，O(∩_∩)O~~");

            if (items.size() == 0) {
              progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您还没有收藏", null, "去首页看看", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                      HomeActivity.navigateToHome(CollectionActivity.this);
                      overridePendingTransition(0, 0);
                    }
                  });
            }
          }
        })
        .filter(new Func1<Integer, Boolean>() {
          @Override public Boolean call(Integer integer) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(CollectionActivity.this.<Integer>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(collectionAdapter);
  }

  @Override public void onError(Throwable error) {
    Snackbar.make(rootView, "删除失败，请稍后重试", Snackbar.LENGTH_LONG)
        .setAction("确定", new View.OnClickListener() {
          @Override public void onClick(View v) {
            /*do nothing*/
          }
        });
  }

  @Override public void exit() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(CollectionActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .withLayer()
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CollectionActivity.this.finish();
          }
        });
  }

  @Override public void onWeiboClick(String content) {
    // TODO: 2015/9/15 新浪分享

  }

  @Override public void onWechatClick(String content) {
    // TODO: 2015/9/15 微信分享

  }

  @Override protected void onDestroy() {
    super.onDestroy();

    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    this.customShareDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
