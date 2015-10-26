package com.app.designmore.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
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
import com.app.designmore.activity.usercenter.AddressMangerActivity;
import com.app.designmore.activity.usercenter.CollectionActivity;
import com.app.designmore.activity.usercenter.OrderActivity;
import com.app.designmore.activity.usercenter.ProfileActivity;
import com.app.designmore.activity.usercenter.SettingActivity;
import com.app.designmore.activity.usercenter.TrolleyActivity;
import com.app.designmore.adapter.MineItemAdapter;
import com.app.designmore.event.FinishEvent;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.CropCircleTransformation;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.manager.EventBusInstance;
import com.app.designmore.retrofit.LoginRetrofit;
import com.app.designmore.retrofit.entity.MineItemEntity;
import com.app.designmore.retrofit.response.UserInfoEntity;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.app.designmore.utils.DensityUtil;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.MaterialRippleLayout;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.jakewharton.rxbinding.support.v4.widget.RxSwipeRefreshLayout;
import com.trello.rxlifecycle.ActivityEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class MineActivity extends BaseActivity implements MineItemAdapter.Callback, IconAnim {

  private static final String TAG = MineActivity.class.getSimpleName();
  private static WeakReference<AppCompatActivity> weakReference;
  public static final String FILE_URL = "FILE_URL";
  public static final String NICKNAME = "NICKNAME";

  @Nullable @Bind(R.id.mine_layout_root_view) RevealFrameLayout rootView;
  @Nullable @Bind(R.id.transparent_toolbar_root) Toolbar toolbar;

  @Nullable @Bind(R.id.mine_layout_srl) SwipeRefreshLayout swipeRefreshLayout;
  @Nullable @Bind(R.id.mine_layout_avatar_iv) ImageView avatarIv;
  @Nullable @Bind(R.id.mine_layout_nickname_tv) TextView nickNameTv;
  @Nullable @Bind(R.id.mine_layout_rv) RecyclerView recyclerView;

  @Nullable @Bind(R.id.bottom_bar_mine_iv) ImageView mineIv;
  @Nullable @Bind(R.id.bottom_bar_mine_tv) TextView mineTv;
  @Nullable @Bind(R.id.bottom_bar_home_rl) RelativeLayout bottomBarHomeRl;
  @Nullable @Bind(R.id.bottom_bar_fashion_rl) RelativeLayout bottomBarFashionRl;
  @Nullable @Bind(R.id.bottom_bar_journal_rl) RelativeLayout bottomBarJournalRl;

  private ViewGroup toast;

  private Subscription subscription = Subscriptions.empty();

  public static void navigateToUserCenter(AppCompatActivity startingActivity) {

    if (!(startingActivity instanceof HomeActivity)) {
      weakReference = new WeakReference<>(startingActivity);
    }

    Intent intent = new Intent(startingActivity, MineActivity.class);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.mine_layout);

    MineActivity.this.initView(savedInstanceState);
    MineActivity.this.setListener();
  }

  @Override public void initView(Bundle savedInstanceState) {

    MineActivity.this.setSupportActionBar(toolbar);
    MineActivity.this.getSupportActionBar().setTitle("");

    MineActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          MineActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      MineActivity.this.loadData();
    }
  }

  private void setupAdapter() {

    swipeRefreshLayout.setColorSchemeResources(Constants.colors);
    RxSwipeRefreshLayout.refreshes(swipeRefreshLayout)
        .compose(MineActivity.this.<Void>bindUntilEvent(ActivityEvent.DESTROY))
        .forEach(new Action1<Void>() {
          @Override public void call(Void aVoid) {

            MineActivity.this.loadData();
          }
        });

    List<MineItemEntity> itemEntities = new ArrayList<>(5);
    itemEntities.add(new MineItemEntity(R.drawable.mine_trolley_icon, "购物车"));
    itemEntities.add(new MineItemEntity(R.drawable.mine_order_icon, "我的订单"));
    itemEntities.add(new MineItemEntity(R.drawable.mine_favorite_icon, "我的收藏"));
    itemEntities.add(new MineItemEntity(R.drawable.mine_information_icon, "个人资料"));
    itemEntities.add(new MineItemEntity(R.drawable.mine_address_icon, "地址管理"));

    MineItemAdapter mineItemAdapter = new MineItemAdapter(MineActivity.this, itemEntities);
    mineItemAdapter.setCallback(MineActivity.this);

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MineActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setAdapter(mineItemAdapter);
    recyclerView.setHasFixedSize(true);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    recyclerView.addItemDecoration(new DividerDecoration(MineActivity.this, R.dimen.material_1dp));
  }

  private void startEnterAnim() {

    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    MineActivity.this.rootView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    SupportAnimator revealAnimator =
        ViewAnimationUtils.createCircularReveal(rootView.getChildAt(0), bounds.left, bounds.top, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        MineActivity.this.rootView.setLayerType(View.LAYER_TYPE_NONE, null);

        if (weakReference != null && weakReference.get() != null) {
          weakReference.get().finish();
          weakReference.clear();
          weakReference = null;
        }

        if (swipeRefreshLayout != null) MineActivity.this.loadData();
      }
    });
    revealAnimator.start();
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
  }

  private void loadData() {

    /*Action=GetUserInfo&uid=2*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetUserInfo");
    params.put("uid", DBHelper.getInstance(getApplicationContext()).getUserID(MineActivity.this));

    subscription = LoginRetrofit.getInstance()
        .requestUserInfo(params)
        .doOnTerminate(new Action0() {
          @Override public void call() {
            if (swipeRefreshLayout.isRefreshing()) {
              RxSwipeRefreshLayout.refreshing(swipeRefreshLayout).call(false);
            }
          }
        })
        .compose(MineActivity.this.<UserInfoEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<UserInfoEntity>() {
          @Override public void onCompleted() {

          }

          @Override public void onError(Throwable error) {

            /*加载失败，显示错误界面*/
            toast = DialogManager.getInstance()
                .showNoMoreDialog(MineActivity.this, Gravity.TOP, "加载失败，请重试,/(ㄒoㄒ)/~~");
          }

          @Override public void onNext(UserInfoEntity userInfoEntity) {

            MineActivity.this.nickNameTv.setText(userInfoEntity.getNickname());

            BitmapPool bitmapPool = Glide.get(MineActivity.this).getBitmapPool();
            Glide.with(MineActivity.this)
                .load(Constants.THUMB_URL + userInfoEntity.getHeaderUrl())
                .centerCrop()
                .crossFade()
                .bitmapTransform(new CropCircleTransformation(bitmapPool))
                .placeholder(R.drawable.center_profile_default_icon)
                .error(R.drawable.center_profile_default_icon)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(avatarIv);
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {

    DrawableCompat.setTint(DrawableCompat.wrap(mineIv.getDrawable().mutate()),
        getResources().getColor(R.color.design_more_red));
    mineTv.setTextColor(getResources().getColor(R.color.design_more_red));

    /*执行进入icon动画*/
    MineActivity.this.startIconAnim();

    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_btn_item);
    ImageButton imageButton =
        (ImageButton) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_setting_icon));

    menuItem.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        SettingActivity.navigateToSetting(MineActivity.this);
        overridePendingTransition(0, 0);
      }
    });
    return true;
  }

  /**
   * 主页
   */
  @Nullable @OnClick(R.id.bottom_bar_home_rl) void onMineClick() {
    HomeActivity.navigateToHome(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 上新
   */
  @Nullable @OnClick(R.id.bottom_bar_fashion_rl) void onFashionClick() {
    FashionActivity.navigateToFashion(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  /**
   * 杂志
   */
  @Nullable @OnClick(R.id.bottom_bar_journal_rl) void onJournalClick() {
    JournalActivity.navigateToJournal(MineActivity.this);
    overridePendingTransition(0, 0);
  }

  @Override public void exit() {
    DialogManager.getInstance().showExitDialog(MineActivity.this);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (requestCode == Constants.ACTIVITY_CODE && resultCode == RESULT_OK && null != data) {

      this.nickNameTv.setText(data.getStringExtra(NICKNAME));

      if (data.getParcelableExtra(FILE_URL) != null) {
        BitmapPool bitmapPool = Glide.get(MineActivity.this).getBitmapPool();
        Glide.with(MineActivity.this)
            .load(data.getParcelableExtra(FILE_URL))
            .centerCrop()
            .crossFade()
            .bitmapTransform(new CropCircleTransformation(bitmapPool))
            .placeholder(R.drawable.center_profile_default_icon)
            .error(R.drawable.center_profile_default_icon)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(avatarIv);
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;

    if (subscription != null && !subscription.isUnsubscribed()) subscription.unsubscribe();
  }

  @Override public void onItemClick(int position, View itemView) {

    int location = DensityUtil.getLocationY(itemView);

    switch (position) {

      case 0:
        TrolleyActivity.startFromLocation(MineActivity.this, location, TrolleyActivity.Type.EXTEND);
        overridePendingTransition(0, 0);
        break;

      case 1:
        OrderActivity.startFromLocation(MineActivity.this, location);
        overridePendingTransition(0, 0);
        break;

      case 2:
        CollectionActivity.startFromLocation(MineActivity.this, location);
        overridePendingTransition(0, 0);
        break;

      case 3:
        ProfileActivity.startFromLocation(MineActivity.this, location);
        overridePendingTransition(0, 0);
        break;

      case 4:
        AddressMangerActivity.startFromLocation(MineActivity.this, location,
            AddressMangerActivity.Type.EXTEND);
        overridePendingTransition(0, 0);
        break;
    }
  }

  @Override public void startIconAnim() {

    mineIv.setLayerType(View.LAYER_TYPE_HARDWARE, null);

    Animator iconAnim = ObjectAnimator.ofPropertyValuesHolder(mineIv,
        PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.5f, 1.0f),
        PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.5f, 1.0f));
    iconAnim.setDuration(Constants.MILLISECONDS_400);
    iconAnim.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        mineIv.setLayerType(View.LAYER_TYPE_NONE, null);
      }
    });
    iconAnim.start();
  }
}
