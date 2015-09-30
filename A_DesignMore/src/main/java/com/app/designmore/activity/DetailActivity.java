package com.app.designmore.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.adapter.DetailBannerAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.CollectionRetrofit;
import com.app.designmore.retrofit.DetailRetrofit;
import com.app.designmore.retrofit.entity.DetailEntity;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.DetailResponse;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.view.dialog.CustomAccountDialog;
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
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Joker on 2015/9/19.
 */
public class DetailActivity extends BaseActivity
    implements CustomShareDialog.Callback, CustomAccountDialog.Callback,
    DetailBannerAdapter.Callback {

  private static final String TAG = DetailActivity.class.getCanonicalName();
  private static final String GOOD_ID = "GOOD_ID";

  @Nullable @Bind(R.id.detail_layout_root_view) FrameLayout rootView;
  @Nullable @Bind(R.id.detail_layout_reveal_fl) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.detail_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.detail_layout_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.detail_layout_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.detail_layout_collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
  @Nullable @Bind(R.id.detail_layout_parallax_viewpager) ViewPager viewPager;
  @Nullable @Bind(R.id.detail_layout_banner_page_tv) TextView bannerPageTv;
  @Nullable @Bind(R.id.detail_layout_title_tv) TextView titleTv;
  @Nullable @Bind(R.id.detail_layout_price_tv) TextView priceTv;
  @Nullable @Bind(R.id.detail_layout_discount_tv) TextView discountTv;
  @Nullable @Bind(R.id.detail_layout_heart_iv) ImageView heartIv;
  @Nullable @Bind(R.id.detail_layout_collection_count_tv) TextView collectionCountTv;
  @Nullable @Bind(R.id.detail_layout_iv) ImageView detailIv;

  @Nullable @Bind(R.id.detail_layout_trolley_expanded_iv) ImageView expandedIv;

  private SupportAnimator revealAnimator;
  private String goodId;
  private ProgressDialog progressDialog;
  private CustomShareDialog customShareDialog;
  private CustomAccountDialog customAccountDialog;
  private ViewGroup toast;

  private Animator expandAnimator;
  private Rect startBounds;
  private float thumbScale;

  private PhotoViewAttacher photoViewAttacher;

  //private ViewPager viewPager;
  private List<DetailResponse.Detail.ProductImage> productImages;
  private DetailEntity currentEntity;

  private Subscription subscription = Subscriptions.empty();

  private View.OnClickListener retryClickListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      DetailActivity.this.loadData();
    }
  };

  private DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
    @Override public void onCancel(DialogInterface dialog) {
      subscription.unsubscribe();
    }
  };

  private ViewPager.SimpleOnPageChangeListener simpleOnPageChangeListener =
      new ViewPager.SimpleOnPageChangeListener() {
        @Override public void onPageSelected(int position) {
          DetailActivity.this.bannerPageTv.setText(++position + "/" + productImages.size());
        }
      };

  private PhotoViewAttacher.OnPhotoTapListener photoTapListener =
      new PhotoViewAttacher.OnPhotoTapListener() {
        @Override public void onPhotoTap(View view, float x, float y) {
          DetailActivity.this.collapsingThumb();
        }
      };

  private PhotoViewAttacher.OnViewTapListener viewTapListener =
      new PhotoViewAttacher.OnViewTapListener() {
        @Override public void onViewTap(View view, float x, float y) {
          DetailActivity.this.collapsingThumb();
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

    //viewPager = (ViewPager) findViewById(R.id.detail_layout_parallax_viewpager);
    DetailActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    DetailActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back_icon);

    this.goodId = getIntent().getStringExtra(GOOD_ID);

    this.photoViewAttacher = new PhotoViewAttacher(expandedIv);
    this.photoViewAttacher.setAllowParentInterceptOnEdge(false);
    this.photoViewAttacher.setScaleType(ImageView.ScaleType.FIT_CENTER);
    this.photoViewAttacher.setOnViewTapListener(viewTapListener);
    this.photoViewAttacher.setOnPhotoTapListener(photoTapListener);

    if (savedInstanceState == null) {
      revealFrameLayout.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealFrameLayout.getViewTreeObserver().removeOnPreDrawListener(this);
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
    detailBannerAdapter.setCallback(DetailActivity.this);
    viewPager.setAdapter(detailBannerAdapter);
    viewPager.addOnPageChangeListener(simpleOnPageChangeListener);
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), 0, bounds.left, 0,
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

            DetailActivity.this.currentEntity = detailEntity;

            titleTv.setText(detailEntity.getGoodName());

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append("￥");
            spannableStringBuilder.append(detailEntity.getShopPrice());
            spannableStringBuilder.append("  ￥");
            spannableStringBuilder.append(detailEntity.getMarketPrice());

            /*店价*/
            spannableStringBuilder.setSpan(new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_13)),
                0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spannableStringBuilder.setSpan(new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_18)),
                1, detailEntity.getShopPrice().length() + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.design_more_red)), 0,
                detailEntity.getShopPrice().length() + 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            /*较价*/
            spannableStringBuilder.setSpan(new AbsoluteSizeSpan(DensityUtil.sp2px(Constants.SP_13)),
                detailEntity.getShopPrice().length() + 3, spannableStringBuilder.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(
                new ForegroundColorSpan(getResources().getColor(R.color.darker_gray)),
                detailEntity.getShopPrice().length() + 3, spannableStringBuilder.length(),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableStringBuilder.setSpan(new StrikethroughSpan(),
                detailEntity.getShopPrice().length() + 3, spannableStringBuilder.length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            priceTv.setText(spannableStringBuilder);
            discountTv.setText(detailEntity.getGoodDes());

            Glide.with(DetailActivity.this)
                .load(detailEntity.getGoodDesUrl())
                .centerCrop()
                .crossFade()
                .placeholder(R.drawable.ic_default_1080_icon)
                .error(R.drawable.ic_default_1080_icon)
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(detailIv);

            DetailActivity.this.productImages = detailEntity.getProductImages();
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

  private void requestCollectionGood() {

    /*Action=AddCollectByGoods&gid=1&uid=1*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "AddCollectByGoods");
    params.put("gid", goodId);
    params.put("uid", DBHelper.getInstance(getApplicationContext()).getUserID(DetailActivity.this));

    subscription =
        CollectionRetrofit.getInstance()
            .requestCollectionGood(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*加载数据，显示进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.
                      getInstance().showSimpleProgressDialog(DetailActivity.this, cancelListener);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*隐藏进度条*/
                if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
              }
            })
            .filter(new Func1<BaseResponse, Boolean>() {
              @Override public Boolean call(BaseResponse baseResponse) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(DetailActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(new SimpleObserver<BaseResponse>() {
              @Override public void onCompleted() {
                toast = DialogManager.getInstance()
                    .showNoMoreDialog(DetailActivity.this, Gravity.TOP, "收藏成功，O(∩_∩)O~~");
              }

              @Override public void onError(Throwable e) {

                Log.e(TAG, e.getMessage());

                toast = DialogManager.getInstance()
                    .showNoMoreDialog(DetailActivity.this, Gravity.TOP, "收藏失败，请重试，O__O …");
              }
            });
  }

  @Nullable @OnClick(R.id.detail_layout_collection_rl) void onCollectionClick() {
    DetailActivity.this.requestCollectionGood();
  }

  @Nullable @OnClick(R.id.detail_layout_share_rl) void onShareClick() {
    if (customShareDialog == null) {
      customShareDialog = DialogManager.getInstance().showShareDialog(DetailActivity.this, this);
    }
    customShareDialog.show();
  }

  @Nullable @OnClick(R.id.detail_layout_trolley_rl) void onTrylleyClick() {

    if (customAccountDialog == null) {
      Map map = new HashMap();
      map.put(CustomAccountDialog.PRICE, currentEntity.getShopPrice());
      map.put(CustomAccountDialog.DES, currentEntity.getGoodDes());
      map.put(CustomAccountDialog.MAX, currentEntity.getGoodRepertory());
      map.put(CustomAccountDialog.ATTRS, currentEntity.getProductAttrs());
      customAccountDialog =
          DialogManager.getInstance().showDetailDialog(DetailActivity.this, map, this);
    }
    DetailActivity.this.showAnim();
  }

  @Override public void onConfirmClick(ProductAttrEntity productAttrEntity, int count) {

    /*Action=AddGoodsCart&uid=1&gid=1&goods_attr=0&count=0*/
    Map<String, String> params = new HashMap<>(5);
    params.put("Action", "AddGoodsCart");
    params.put("uid", DBHelper.getInstance(getApplicationContext()).getUserID(DetailActivity.this));
    params.put("gid", goodId);
    params.put("goods_attr", productAttrEntity.getAttrId());
    params.put("count", count + "");

    subscription = DetailRetrofit.getInstance()
        .requestBuyGood(params)
        .doOnSubscribe(new Action0() {
          @Override public void call() {
            /*加载数据，显示进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.
                  getInstance().showSimpleProgressDialog(DetailActivity.this, cancelListener);
            } else {
              progressDialog.show();
            }
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            /*隐藏进度条*/
            if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
          }
        })
        .filter(new Func1<BaseResponse, Boolean>() {
          @Override public Boolean call(BaseResponse baseResponse) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(DetailActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new SimpleObserver<BaseResponse>() {
          @Override public void onCompleted() {
            toast = DialogManager.getInstance()
                .showNoMoreDialog(DetailActivity.this, Gravity.TOP, "成功加入购物车,(≧∇≦)ﾉ");
          }

          @Override public void onError(Throwable e) {
            toast = DialogManager.getInstance()
                .showNoMoreDialog(DetailActivity.this, Gravity.TOP, "收藏失败，请重试，O__O …");
          }
        });
  }

  @Override public void onDialogDismiss() {
    DetailActivity.this.hiddenAnim();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    this.toolbarTitleTv.setText("商品详情");
    this.collapsingToolbar.setTitle("");

    /*collapsingToolbar.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
    collapsingToolbar.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);*/

    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem trolleyItem = menu.findItem(R.id.action_inbox);
    trolleyItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton trolleyButton =
        (ImageButton) trolleyItem.getActionView().findViewById(R.id.action_inbox_btn);
    trolleyButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_trolley_black_icon));

    trolleyItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TrolleyActivity.startFromLocation(DetailActivity.this, 0, TrolleyActivity.Type.UP);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  @Override public void onWeiboClick(String content) {

  }

  @Override public void onWechatClick(String content) {

  }

  @Override public void onItemClick(String thumbUrl) {

    /*expand  thumb*/
    if (expandAnimator != null) {
      expandAnimator.cancel();
    }

    Glide.with(DetailActivity.this)
        .load(thumbUrl)
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(expandedIv);

    startBounds = new Rect();
    final Rect finalBounds = new Rect();
    viewPager.getGlobalVisibleRect(startBounds);
    rootView.getGlobalVisibleRect(finalBounds);

    thumbScale = DensityUtil.calculateScale(startBounds, finalBounds);

    this.expandedIv.setVisibility(View.VISIBLE);
    this.expandedIv.setPivotX(0.0f);
    this.expandedIv.setPivotY(0.0f);

    AnimatorSet set = new AnimatorSet();
    set.play(ObjectAnimator.ofFloat(this.expandedIv, View.X, startBounds.left, finalBounds.left))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.Y, startBounds.top, finalBounds.top))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.SCALE_X, thumbScale, 1.0f))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.SCALE_Y, thumbScale, 1.0f))
        .with(ObjectAnimator.ofFloat(this.viewPager, View.ALPHA, 0.8f, 0.1f));
    set.setDuration(Constants.MILLISECONDS_400);
    set.setInterpolator(new DecelerateInterpolator());
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        DetailActivity.this.expandAnimator = null;
      }

      @Override public void onAnimationCancel(Animator animation) {
        DetailActivity.this.expandAnimator = null;
      }
    });
    set.start();
    DetailActivity.this.expandAnimator = set;
  }

  private void collapsingThumb() {

    final float startScaleFinal = thumbScale;
    if (expandAnimator != null) {
      expandAnimator.cancel();
    }

    AnimatorSet set = new AnimatorSet();
    set.play(ObjectAnimator.ofFloat(this.expandedIv, View.X, startBounds.left))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.Y, startBounds.top))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.SCALE_X, startScaleFinal))
        .with(ObjectAnimator.ofFloat(this.expandedIv, View.SCALE_Y, startScaleFinal))
        .with(ObjectAnimator.ofFloat(this.viewPager, View.ALPHA, 1.0f));
    set.setDuration(Constants.MILLISECONDS_300);
    set.setInterpolator(new DecelerateInterpolator());
    set.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {

        ViewCompat.animate(viewPager).alpha(1.0f);
        DetailActivity.this.expandedIv.setVisibility(View.GONE);
        DetailActivity.this.expandAnimator = null;
      }

      @Override public void onAnimationCancel(Animator animation) {

        ViewCompat.animate(viewPager).alpha(1.0f);
        DetailActivity.this.expandedIv.setVisibility(View.GONE);
        DetailActivity.this.expandAnimator = null;
      }
    });
    set.start();
    DetailActivity.this.expandAnimator = set;
  }

  private void showAnim() {

    this.revealFrameLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.SCALE_X, 1.0f, 0.9f);
    scaleXAnim.setDuration(Constants.MILLISECONDS_400);
    ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.SCALE_Y, 1.0f, 0.9f);
    scaleYAnim.setDuration(Constants.MILLISECONDS_400);

    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.ALPHA, 1.0f, 0.5f);
    alphaAnim.setDuration(Constants.MILLISECONDS_400);

    ObjectAnimator rotationXAnim =
        ObjectAnimator.ofFloat(revealFrameLayout, View.ROTATION_X, 0.0f, 10.0f);
    rotationXAnim.setDuration(Constants.MILLISECONDS_300);

    ObjectAnimator resumeAnim =
        ObjectAnimator.ofFloat(revealFrameLayout, View.ROTATION_X, 10.0f, 0.0f);
    resumeAnim.setDuration(Constants.MILLISECONDS_400);
    resumeAnim.setStartDelay(Constants.MILLISECONDS_300);

    ObjectAnimator transYAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.TRANSLATION_Y, 0.0f,
        -0.05f * revealFrameLayout.getHeight());
    transYAnim.setDuration(Constants.MILLISECONDS_400);

    AnimatorSet showAnim = new AnimatorSet();
    showAnim.playTogether(scaleXAnim, rotationXAnim, resumeAnim, transYAnim, alphaAnim, scaleYAnim);
    showAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        DetailActivity.this.customAccountDialog.show();
      }

      @Override public void onAnimationEnd(Animator animation) {
        DetailActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_NONE, null);
      }
    });
    showAnim.start();
  }

  private void hiddenAnim() {

    this.revealFrameLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.SCALE_X, 0.9f, 1.0f);
    scaleXAnim.setDuration(Constants.MILLISECONDS_400);
    ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.SCALE_Y, 0.9f, 1.0f);
    scaleYAnim.setDuration(Constants.MILLISECONDS_400);

    ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.ALPHA, 0.5f, 1.0f);
    alphaAnim.setDuration(Constants.MILLISECONDS_400);

    ObjectAnimator rotationXAnim =
        ObjectAnimator.ofFloat(revealFrameLayout, View.ROTATION_X, 0.0f, 10.0f);
    rotationXAnim.setDuration(Constants.MILLISECONDS_200);

    ObjectAnimator resumeAnim =
        ObjectAnimator.ofFloat(revealFrameLayout, View.ROTATION_X, 10.0f, 0.0f);
    resumeAnim.setDuration(Constants.MILLISECONDS_300);
    resumeAnim.setStartDelay(Constants.MILLISECONDS_200);

    ObjectAnimator transYAnim = ObjectAnimator.ofFloat(revealFrameLayout, View.TRANSLATION_Y,
        -0.05f * revealFrameLayout.getHeight(), 0.0f);
    transYAnim.setDuration(Constants.MILLISECONDS_400);

    AnimatorSet hideAnim = new AnimatorSet();
    hideAnim.playTogether(scaleXAnim, rotationXAnim, resumeAnim, transYAnim, alphaAnim, scaleYAnim);
    hideAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        DetailActivity.this.revealFrameLayout.setLayerType(View.LAYER_TYPE_NONE, null);
      }
    });

    hideAnim.start();
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {

    if (keyCode == KeyEvent.KEYCODE_BACK
        && event.getRepeatCount() == 0
        && this.expandedIv.getVisibility() == View.VISIBLE) {
      DetailActivity.this.collapsingThumb();
      return false;
    }

    return super.onKeyDown(keyCode, event);
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

    this.viewPager.removeOnPageChangeListener(simpleOnPageChangeListener);
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    this.customShareDialog = null;
    this.customAccountDialog = null;
    this.photoViewAttacher.cleanup();
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
  }
}
