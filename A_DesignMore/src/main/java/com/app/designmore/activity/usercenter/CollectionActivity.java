package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.activity.HomeActivity;
import com.app.designmore.activity.MineActivity;
import com.app.designmore.adapter.CollectionAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.CollectionRetrofit;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.view.ProgressLayout;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/4.
 */
public class CollectionActivity extends BaseActivity implements CollectionAdapter.Callback {

  private static final String TAG = CollectionActivity.class.getSimpleName();
  private static final String START_LOCATION_Y = "START_LOCATION_Y";

  @Nullable @Bind(R.id.collection_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.collection_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;
  @Nullable @Bind(R.id.collection_layout_rv) RecyclerView recyclerView;

  private CollectionAdapter collectionAdapter;
  private List<CollectionEntity> items;

  private Subscription subscription = Subscriptions.empty();

  private ProgressDialog progressDialog;

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      CollectionActivity.this.loadData();
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
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back));

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

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CollectionActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    collectionAdapter = new CollectionAdapter(this);
    collectionAdapter.setCallback(CollectionActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(collectionAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
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
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CollectionActivity.this.loadData();
          }
        });
  }

  private void loadData() {

    /*Action=GetCollectByGoods&uid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetCollectByGoods");
    params.put("uid", "1");

    CollectionRetrofit.getInstance()
        .getCollectionList(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .compose(
            CollectionActivity.this.<List<CollectionEntity>>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<List<CollectionEntity>>() {
          @Override public void onCompleted() {

            /*加载完毕，显示内容界面*/
            if (items != null && items.size() != 0) {
              progressLayout.showContent();
            } else if (items != null && items.size() == 0) {
              progressLayout.showError(getResources().getDrawable(R.drawable.ic_grey_logo_icon),
                  "您还没有收藏", null, "去首页看看", new View.OnClickListener() {
                    @Override public void onClick(View v) {
                      HomeActivity.navigateToHome(CollectionActivity.this);
                      overridePendingTransition(0, 0);
                    }
                  });
            }
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            CollectionActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(List<CollectionEntity> collectionEntities) {

            CollectionActivity.this.items = collectionEntities;
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
      CollectionActivity.this.showError("网络连接异常", ((RetrofitError) error).getKind() + "");
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

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        CollectionActivity.this.startExitAnim();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      CollectionActivity.this.startExitAnim();
    }
    return false;
  }

  private void startExitAnim() {

    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(CollectionActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            CollectionActivity.super.onBackPressed();
            overridePendingTransition(0, 0);
          }
        });
  }

  //CollectionAdapter回调
  /*点击条目，跳转商品详情*/
  @Override public void onItemClick(int position) {

  }

  /*点击更多按钮，弹出列表对话框，删除操作*/
  @Override public void onMoreClick(final int position) {

    DialogManager.getInstance()
        .showNormalDialog(CollectionActivity.this, "请确认删除收藏",
            new DialogInterface.OnClickListener() {
              @Override public void onClick(DialogInterface dialog, int which) {
                CollectionActivity.this.requestDeleteCollection(position);
              }
            });
  }

  private void requestDeleteCollection(final int position) {

    CollectionEntity deleteCollection = items.get(position);

    /*Action=DelCollectByGoods&uid=1&rec_id=1*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "DelCollectByGoods");
    params.put("rec_id", deleteCollection.getCollectionId());
    params.put("uid", "1");

    subscription = CollectionRetrofit.getInstance()
        .requestDeleteCollection(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressDialog = DialogManager.
                getInstance().showProgressDialog(CollectionActivity.this, null, cancelListener);
          }
        })
        .map(new Func1<BaseResponse, Integer>() {
          @Override public Integer call(BaseResponse baseResponse) {

            /*卧槽，什么鬼啊，这里给提示*/
            Toast.makeText(CollectionActivity.this, baseResponse.message, Toast.LENGTH_LONG).show();
            return position;
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            /*隐藏进度条*/
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
          }
        })
        .filter(new Func1<Integer, Boolean>() {
          @Override public Boolean call(Integer position) {
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

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
