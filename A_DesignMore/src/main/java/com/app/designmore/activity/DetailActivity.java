package com.app.designmore.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.adapter.DetailBannerAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.DetailRetrofit;
import com.app.designmore.retrofit.entity.DetailEntity;
import com.app.designmore.retrofit.response.DetailResponse;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.view.dialog.CustomShareDialog;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import retrofit.RetrofitError;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/19.
 */
public class DetailActivity extends BaseActivity implements CustomShareDialog.Callback {

  private static final String TAG = DetailActivity.class.getCanonicalName();
  private static final String GOOD_ID = "GOOD_ID";

  @Nullable @Bind(R.id.detail_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.detail_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.detail_layout_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.detail_layout_collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
  @Nullable @Bind(R.id.detail_layout_banner_page_tv) TextView bannerPageTv;
  @Nullable @Bind(R.id.detail_layout_title_tv) TextView titleTv;
  @Nullable @Bind(R.id.detail_layout_price_tv) TextView priceTv;
  @Nullable @Bind(R.id.detail_layout_discount_tv) TextView discountTv;
  @Nullable @Bind(R.id.detail_layout_heart_iv) ImageView heartIv;
  @Nullable @Bind(R.id.detail_layout_collection_count_tv) TextView collectionCountTv;
  @Nullable @Bind(R.id.detail_layout_iv) ImageView detailIv;

  private SupportAnimator revealAnimator;
  private String goodId;
  private ProgressDialog progressDialog;
  private CustomShareDialog customShareDialog;

  private ViewPager viewPager;
  private List<DetailResponse.Detail.ProductImage> productImages;
  private List<DetailResponse.Detail.ProductAttr> productAttrs;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      DetailActivity.this.loadData();
    }
  };

  private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override public void onPageSelected(int position) {
          DetailActivity.this.bannerPageTv.setText(++position+"/"+productImages.size());
        }
      };

  public static void navigateToDetail(AppCompatActivity startingActivity, String goodId) {
    Intent intent = new Intent(startingActivity, DetailActivity.class);
    intent.putExtra(GOOD_ID, goodId);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.detail_layout);

    viewPager = (ViewPager) findViewById(R.id.detail_layout_parallax_viewpager);
    DetailActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    DetailActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    collapsingToolbar.setTitle("商品详情");
    collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
    collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

    this.goodId = getIntent().getStringExtra(GOOD_ID);

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          DetailActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      DetailActivity.this.loadData();
    }
  }

  private void setupViewpager() {
    DetailBannerAdapter detailBannerAdapter =
        new DetailBannerAdapter(DetailActivity.this, productImages);
    viewPager.setAdapter(detailBannerAdapter);
    viewPager.addOnPageChangeListener(simpleOnPageChangeListener);
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
        if (progressLayout != null) DetailActivity.this.loadData();
      }
    });
    revealAnimator.start();
  }

  private void loadData() {

    /*Action=GetProductByDetail&gid=1*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetProductByDetail");
    params.put("gid", goodId);

    DetailRetrofit.getInstance()
        .getGoodDetail(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            progressLayout.showLoading();
          }
        })
        .compose(DetailActivity.this.<DetailEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<DetailEntity>() {
          @Override public void onCompleted() {
            /*显示内容*/
            progressLayout.showContent();

            /*设置viewpager*/
            DetailActivity.this.setupViewpager();
          }

          @Override public void onError(Throwable error) {
            /*加载失败，显示错误界面*/
            DetailActivity.this.showErrorLayout(error);
          }

          @Override public void onNext(DetailEntity detailEntity) {

            titleTv.setText(detailEntity.getGoodName());
            priceTv.setText("设计猫价格："
                + detailEntity.getShopPrice()
                + "    市场价格："
                + detailEntity.getMarketPrice());
            discountTv.setText(detailEntity.getGoodDes());

            Glide.with(DetailActivity.this)
                .load(detailEntity.getGoodDesUrl())
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.ic_default_1080)
                .error(R.drawable.ic_default_1080)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(detailIv);

            DetailActivity.this.productImages = detailEntity.getProductImages();
            DetailActivity.this.productAttrs = detailEntity.getProductAttrs();
          }
        });
  }

  private void showErrorLayout(Throwable error) {
    if (error instanceof TimeoutException) {
      DetailActivity.this.showError(getResources().getString(R.string.timeout_title),
          getResources().getString(R.string.timeout_content));
    } else if (error instanceof RetrofitError) {
      Log.e(TAG, "Kind:  " + ((RetrofitError) error).getKind());
      DetailActivity.this.showError(getResources().getString(R.string.six_word_title),
          getResources().getString(R.string.six_word_content));
    } else if (error instanceof WebServiceException) {
      DetailActivity.this.showError(getResources().getString(R.string.service_exception_title),
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

  @Nullable @OnClick(R.id.detail_layout_collection_rl) void onCollectionClick() {

  }

  @Nullable @OnClick(R.id.detail_layout_share_rl) void onShareClick() {
    if (customShareDialog == null) {
      customShareDialog = DialogManager.getInstance().showShareDialog(DetailActivity.this, this);
    }
    customShareDialog.show();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem trolleyItem = menu.findItem(R.id.action_inbox);
    trolleyItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton trolleyButton =
        (ImageButton) trolleyItem.getActionView().findViewById(R.id.action_inbox_btn);
    trolleyButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_trolley_black_icon));

    trolleyItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TrolleyActivity.startFromLocation(DetailActivity.this,
            DensityUtil.getActionBarSize(DetailActivity.this), TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public void onWeiboClick() {

  }

  @Override public void onWechatClick() {

  }

  @Override public void exit() {
    ViewCompat.animate(rootView)
        .translationY(DensityUtil.getScreenHeight(DetailActivity.this))
        .setDuration(Constants.MILLISECONDS_400)
        .setInterpolator(new LinearInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {
            DetailActivity.this.finish();
          }
        });
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    this.progressDialog = null;
    this.customShareDialog = null;
    this.viewPager.removeOnPageChangeListener(simpleOnPageChangeListener);
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
