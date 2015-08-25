package com.joker.app;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import butterknife.Bind;
import com.joker.app.arcLib.ArcAnimator;
import com.joker.app.arcLib.Side;
import com.joker.app.revealLib.animation.SupportAnimator;
import com.joker.app.revealLib.widget.RevealFrameLayout;
import com.joker.app.view.transformation.CBorderTransformation;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.joker.app.revealLib.animation.ViewAnimationUtils;

/**
 * Created by Joker on 2015/8/11.
 */
public class ProfileActivity extends BaseActivity implements Callback {

  private static final String AVATAR_START_LOCATION = "start_location";
  private static final String TAG = ProfileActivity.class.getSimpleName();
  private static final int AVATAR_START_ALPHA = 255;

  @Nullable @Bind(R.id.profile_appBar_layout) AppBarLayout appBarLayout;
  @Nullable @Bind(R.id.profile_collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
  @Nullable @Bind(R.id.profile_avatar_rf) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.profile_avatar_iv) ImageView collapsing_avatar;
  @Nullable @Bind(R.id.profile_toolbar) Toolbar toolbar;
  @Nullable @Bind(R.id.profile_nested_scroll_view) NestedScrollView nestedScrollView;

  @Nullable @Bind(R.id.profile_avatar) ImageView avatar;

  private int url;
  private int[] startingInfos;

  /*属性动画*/
  private ArcAnimator arcAnimator;
  private SupportAnimator revealAnimator;
  private ObjectAnimator raiseAnimator;
  private int nestedStartY;
  private int nestedEndY;
  private boolean isBack = false;
  private ViewPropertyAnimatorCompat viewPropertyAnimatorCompat;

  public static void startFromLocation(DetailActivity startingActivity, int[] startingLocation) {

    Intent intent = new Intent(startingActivity, ProfileActivity.class);
    intent.putExtra(AVATAR_START_LOCATION, startingLocation);
    startingActivity.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.profile_layout);

    ProfileActivity.this.initView(savedInstanceState);
    ProfileActivity.this.setListener();
  }

  private void setListener() {
    appBarLayout.addOnOffsetChangedListener(offsetChangedListener);
  }

  private int collapsingOffset;
  private AppBarLayout.OnOffsetChangedListener offsetChangedListener =
      new AppBarLayout.OnOffsetChangedListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT) @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

          if (collapsingOffset >= 0) {
            collapsingOffset = toolbar.getHeight() - appBarLayout.getHeight();
          }

          if (collapsing_avatar.getDrawable() != null && collapsingOffset < 0) {
            if (offset <= collapsingOffset + 40) {
              collapsing_avatar.getDrawable().setAlpha(0);
            } else if (offset == collapsingOffset
                && collapsing_avatar.getDrawable().getAlpha() != 0) {
              collapsing_avatar.getDrawable().setAlpha(0);
            } else if (offset > (collapsingOffset + 40)) {
              float alpha = AVATAR_START_ALPHA * (1 - offset * 1.0f / collapsingOffset);
              collapsing_avatar.getDrawable().setAlpha((int) alpha);
            } else if (offset == 0
                && collapsing_avatar.getDrawable().getAlpha() != AVATAR_START_ALPHA) {
              collapsing_avatar.getDrawable().setAlpha(AVATAR_START_ALPHA);
            }
          }
        }
      };

  private void initView(final Bundle savedInstanceState) {

    this.startingInfos = getIntent().getIntArrayExtra(AVATAR_START_LOCATION);
    this.url = startingInfos[2];

    ProfileActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);

    avatar.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
      @Override public boolean onPreDraw() {

        avatar.getViewTreeObserver().removeOnPreDrawListener(this);
        ProfileActivity.this.setupAvatar(savedInstanceState);
        ProfileActivity.this.setupNested();
        return true;
      }
    });
  }

  private void setupAvatar(Bundle savedInstanceState) {

    if (savedInstanceState == null) {

      ViewCompat.setTranslationX(avatar, startingInfos[0]);
      ViewCompat.setTranslationY(avatar, startingInfos[1]);

      Picasso.with(ProfileActivity.this)
          .load(url)
          .noPlaceholder()
          .noFade()
          .fit()
          .centerCrop()
          .transform(new CBorderTransformation())
          .into(avatar, this);
    }
  }

  @Override public void onSuccess() {

    arcAnimator = ArcAnimator.createArcAnimator(avatar, collapsingToolbarLayout, 45, Side.RIGHT);
    arcAnimator.setInterpolator(new LinearInterpolator());
    arcAnimator.setStartDelay(50);
    arcAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
    arcAnimator.addListener(arcListener);
    arcAnimator.start();
  }

  @Override public void onError() {

  }

  private AnimatorListenerAdapter arcListener = new AnimatorListenerAdapter() {

    @Override public void onAnimationCancel(Animator animation) {
      ProfileActivity.this.isBack = true;
    }

    @Override public void onAnimationEnd(Animator animation) {

      if (!isBack) {
        Picasso.with(ProfileActivity.this)
            .load(url)
            .noFade()
            .noPlaceholder()
            .fit()
            .centerCrop()
            .transform(new CBorderTransformation())
            .into(collapsing_avatar, new EmptyCallback() {
              @Override public void onSuccess() {
                ViewGroup parent = (ViewGroup) avatar.getParent();
                if (parent != null) parent.removeView(avatar);

                ProfileActivity.this.startReveal();
              }
            });
      }
    }
  };

  private void startReveal() {

    int centerX = (int) (collapsing_avatar.getX() + collapsing_avatar.getWidth() / 2);
    int centerY = (int) (collapsing_avatar.getY() + collapsing_avatar.getHeight() / 2);

    final Rect bounds = new Rect();
    revealFrameLayout.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal((View) collapsing_avatar.getParent(), centerX,
            centerY, collapsing_avatar.getHeight() / 2, bounds.height());
    revealAnimator.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {
        revealFrameLayout.setVisibility(View.VISIBLE);
      }

      @Override public void onAnimationCancel() {
        ProfileActivity.this.isBack = true;
      }

      @Override public void onAnimationEnd() {
        if (!isBack) ProfileActivity.this.raiseNested();
      }
    });

    revealAnimator.start();
  }

  private void raiseNested() {

    /*starTop = nestedStartY*/
    //int starTop = nestedScrollView.getTop() - toolbar.getHeight();

    /*raiseAnimator = ObjectAnimator.ofInt(nestedScrollView, "top", nestedEndY, nestedStartY);
    raiseAnimator.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));
    raiseAnimator.setInterpolator(new AccelerateInterpolator());
    raiseAnimator.start();*/

    viewPropertyAnimatorCompat = ViewCompat.animate(nestedScrollView);
    viewPropertyAnimatorCompat.translationY(0.0f)
        .setDuration(getResources().getInteger(android.R.integer.config_longAnimTime) * 2)
        .setInterpolator(new LinearInterpolator())
        .start();
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

      if (arcAnimator != null && arcAnimator.isRunning()) {
        arcAnimator.removeAllListeners();
        arcAnimator.cancel();
      }
      if (revealAnimator != null && revealAnimator.isRunning()) {
        revealAnimator.cancel();
      }
      if (viewPropertyAnimatorCompat != null) viewPropertyAnimatorCompat.cancel();

       /*if (raiseAnimator != null && raiseAnimator.isRunning()) {
        raiseAnimator.removeAllListeners();
        raiseAnimator.cancel();
      }*/

      ProfileActivity.this.finish();
      overridePendingTransition(0, 0);
    }
    return false;
  }

  private void setupNested() {

    nestedStartY = appBarLayout.getHeight();
    nestedEndY = nestedStartY + nestedScrollView.getHeight();
    //nestedScrollView.setTop(nestedEndY);

    ViewCompat.setTranslationY(nestedScrollView, nestedEndY);
  }
}
