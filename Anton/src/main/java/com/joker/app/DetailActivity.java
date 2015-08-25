package com.joker.app;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import com.joker.app.arcLib.ArcAnimator;
import com.joker.app.arcLib.Side;
import com.joker.app.adapter.DetailAdapter;
import com.joker.app.adapter.DetailEntity;
import com.joker.app.manager.Scroller;
import com.joker.app.manager.SheetRevealManager;
import com.joker.app.utils.Utils;
import com.joker.app.view.transformation.CircleTransformation;
import com.joker.app.view.RevealBackgroundView;
import com.joker.app.revealLib.animation.SupportAnimator;
import com.joker.app.revealLib.animation.ViewAnimationUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/7/15.
 */
public class DetailActivity extends BaseActivity
    implements RevealBackgroundView.OnStateChangeListener, PopupMenu.OnMenuItemClickListener,
    View.OnClickListener, SheetRevealManager.StateListener, DetailAdapter.Callback {

  private static final int[] avatars = new int[] {
      R.drawable.detail_icon0, R.drawable.detail_icon1, R.drawable.detail_icon2,
      R.drawable.detail_icon3, R.drawable.detail_icon4, R.drawable.detail_icon5,
      R.drawable.detail_icon6, R.drawable.detail_icon7, R.drawable.detail_icon8,
      R.drawable.detail_icon9, R.drawable.detail_icon10,
  };

  private static final String TAG = DetailActivity.class.getSimpleName();
  public static final String REVEAL_START_LOCATION = "reveal_start_location";
  private static final int ANIMATION_DURATION_SHORT = 300;
  private static final int ANIMATION_DURATION_MEDIUM = 400;
  private static final int ANIMATION_DURATION_LONG = 500;
  private static final int AVATAR_START_ALPHA = 188;
  private static final int ANTON_FAB_ID = 0x0000010;
  private static final int NORMAL_FAB_ID = 0x0000011;
  private static final int FOOTER_BAR_ID = 0x0000012;
  private static final int SHEET_BAR_ID = 0x0000013;

  public float collapsingOffset = 0;

  @Nullable @Bind(R.id.detail_root) RelativeLayout rootLayout;
  @Nullable @Bind(R.id.detail_reveal_layout) RevealBackgroundView revealBackgroundView;
  @Nullable @Bind(R.id.detail_coordinator_layout) CoordinatorLayout coordinatorLayout;

  @Nullable @Bind(R.id.detail_appBar_layout) AppBarLayout appBarLayout;
  @Nullable @Bind(R.id.detail_collapsing_toolbar) CollapsingToolbarLayout collapsingToolbarLayout;
  @Nullable @Bind(R.id.detail_avatar_iv) ImageView avatarView;
  @Nullable @Bind(R.id.detail_toolbar) Toolbar toolbar;

  @Nullable @Bind(R.id.detail_recycler_view) RecyclerView recyclerView;

  private FloatingActionButton floatingActionButton;
  private View footerBar;
  private View antonRevealLayout;
  private Snackbar snackBar;

  private Scroller scroller;
  private SheetRevealManager sheetRevealManager;

  private static WeakReference<BaseActivity> weakReference;
  private float fabX;
  private float fabY;
  private State currentState;

  public enum State {Anton, Normal, Footer, Sheet}

  private RelativeLayout.LayoutParams footerBarParams = null;
  private RelativeLayout.LayoutParams antonRevealParams = null;
  private SupportAnimator footerRevealAnim;
  private SupportAnimator antonRevealAnim;

  private int fabSizeSize;

  public static void startFromLocation(LaunchActivity startingActivity, int[] startingLocation) {

    Intent intent = new Intent(startingActivity, DetailActivity.class);
    intent.putExtra(REVEAL_START_LOCATION, startingLocation);
    startingActivity.startActivity(intent);

    weakReference = new WeakReference<BaseActivity>(startingActivity);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.detail_layout);

    DetailActivity.this.initView(savedInstanceState);
    DetailActivity.this.setListener();
  }

  private void initView(Bundle savedInstanceState) {

    fabSizeSize =
        getResources().getDimensionPixelSize(android.support.design.R.dimen.fab_size_normal);

    DetailActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(R.drawable.ic_menu);

    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppBar);
    collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);

    /*设置Reveal背景*/
    DetailActivity.this.setupRevealBackground(savedInstanceState);
  }

  private void setListener() {

    appBarLayout.addOnOffsetChangedListener(offsetChangedListener);
  }

  private AppBarLayout.OnOffsetChangedListener offsetChangedListener =
      new AppBarLayout.OnOffsetChangedListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT) @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {

          if (collapsingOffset >= 0) {
            collapsingOffset = toolbar.getHeight() - appBarLayout.getHeight();
          }

          if (avatarView.getDrawable() != null && collapsingOffset < 0) {
            if (offset <= collapsingOffset + 40) {
              avatarView.getDrawable().setAlpha(0);
            } else if (offset == collapsingOffset && avatarView.getDrawable().getAlpha() != 0) {
              avatarView.getDrawable().setAlpha(0);
            } else if (offset > (collapsingOffset + 40)) {
              float alpha = AVATAR_START_ALPHA * (1 - offset * 1.0f / collapsingOffset);
              avatarView.getDrawable().setAlpha((int) alpha);
            } else if (offset == 0 && avatarView.getDrawable().getAlpha() != AVATAR_START_ALPHA) {
              avatarView.getDrawable().setAlpha(AVATAR_START_ALPHA);
            }
          }
        }
      };

  private void setupRevealBackground(Bundle savedInstanceState) {

    revealBackgroundView.setOnStateChangeListener(this);

    if (savedInstanceState == null) {
      final int[] startingLocation = getIntent().getIntArrayExtra(REVEAL_START_LOCATION);
      revealBackgroundView.getViewTreeObserver()
          .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override public boolean onPreDraw() {
              revealBackgroundView.getViewTreeObserver().removeOnPreDrawListener(this);
              revealBackgroundView.startFromLocation(startingLocation);
              return true;
            }
          });
    } else {
      revealBackgroundView.setToFinishedFrame();
    }
  }

  @Override public void onStateChange(int state) {
    if (RevealBackgroundView.STATE_FINISHED == state) {
      if (revealBackgroundView != null) {
        revealBackgroundView.setVisibility(View.GONE);
        DetailActivity.this.startAvatarAnim();
      }
    } else {
      if (coordinatorLayout != null) {
        coordinatorLayout.setVisibility(View.INVISIBLE);
      }
    }
  }

  private ViewPropertyAnimatorCompat avatarAnimator;

  /**
   * 头像动画
   */
  private void startAvatarAnim() {

    ViewCompat.setScaleX(avatarView, 0.0f);
    ViewCompat.setScaleY(avatarView, 0.0f);

    Picasso.with(DetailActivity.this)
        .load(R.drawable.detail_avatar)
        .noFade()
        .noPlaceholder()
        .transform(new CircleTransformation())
        .into(avatarView, new Callback.EmptyCallback() {
          @Override public void onSuccess() {
            avatarView.getDrawable().setAlpha(AVATAR_START_ALPHA);

            avatarAnimator = ViewCompat.animate(avatarView)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(ANIMATION_DURATION_SHORT / 2)
                .setInterpolator(new LinearInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                  @Override public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);

                    collapsingToolbarLayout.setTitle(
                        getResources().getString(R.string.collapsing_title));
                  }

                  @Override public void onAnimationStart(View view) {
                    DetailActivity.this.initFab();
                  }
                });
          }
        });
  }

  private ViewPropertyAnimatorCompat fabAnimator;

  private void initFab() {

    int hOffset = 0;
    int vOffset = 0;

    final int margin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
    final int elevation = getResources().getDimensionPixelSize(R.dimen.fab_default_elevation);
    final int cornerRadius =
        (int) (getResources().getDimensionPixelSize(R.dimen.fab_size_normal) / 2.0f);
    int mContentPadding =
        (int) ((getResources().getDimension(android.support.design.R.dimen.fab_size_normal)
            - getResources().getDimensionPixelSize(android.support.design.R.dimen.fab_content_size))
            / 2);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      hOffset = vOffset = 0;
    } else {
     /* Rect rect = new Rect();
      DetailActivity.this.calculatePadding(elevation, rect);*/

      /*hOffset = elevation * 4;
      vOffset = (int) (hOffset * 1.3f);*/
      hOffset = elevation * 3;
      vOffset = (int) (hOffset * 1.2f);

     /* hOffset = (int) Math.ceil((1.0D - Math.cos(Math.toRadians(45.0D))) * (double) cornerRadius);
      vOffset = (int) (hOffset * 1.2f);*/


     /* hOffset = cornerRadius;
      vOffset = (int) (hOffset * 1.5f);*/
    }

    floatingActionButton = new FloatingActionButton(DetailActivity.this);
    floatingActionButton.setBackgroundTintList(
        getResources().getColorStateList(android.R.color.transparent));
    floatingActionButton.setScaleType(ImageView.ScaleType.CENTER_CROP);
    ViewCompat.setScaleX(floatingActionButton, 0.0f);
    ViewCompat.setScaleY(floatingActionButton, 0.0f);
    ViewCompat.setAlpha(floatingActionButton, 0.4f);
    floatingActionButton.setPadding(hOffset, vOffset, hOffset, vOffset);

    CoordinatorLayout.LayoutParams layoutParams =
        new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.gravity = Gravity.END | Gravity.BOTTOM;
    layoutParams.setMargins(0, 0, margin, margin);
    coordinatorLayout.addView(floatingActionButton, layoutParams);

    floatingActionButton.getViewTreeObserver()
        .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
          @Override public boolean onPreDraw() {
            floatingActionButton.getViewTreeObserver().removeOnPreDrawListener(this);

            fabX = floatingActionButton.getX() + floatingActionButton.getWidth() / 2;
            fabY = floatingActionButton.getY() + floatingActionButton.getHeight() / 2;
            return true;
          }
        });

    DetailActivity.this.createNormalFAB();
    floatingActionButton.setOnClickListener(DetailActivity.this);

    Picasso.with(DetailActivity.this)
        .load(R.drawable.detail_avatar_2)
        .noFade()
        .noPlaceholder()
        .transform(new CircleTransformation())
        .into(floatingActionButton, new Callback.EmptyCallback() {
          @Override public void onSuccess() {

            fabAnimator = ViewCompat.animate(floatingActionButton)
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setStartDelay(ANIMATION_DURATION_SHORT / 3)
                .setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime) / 2)
                .setInterpolator(new LinearInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                  @Override public void onAnimationEnd(View view) {
                    super.onAnimationEnd(view);

                    scroller = Scroller.builder()
                        .addView(floatingActionButton, Scroller.Direction.DOWN)
                        .initOffset(margin / 2)
                        .build();
                    scroller.attach(recyclerView);
                    DetailActivity.this.currentState = State.Normal;
                  }
                });
          }
        });
  }

  @Override public void onAlphaFinish() {

    if (weakReference != null && weakReference.get() != null) {
      weakReference.get().finish();
      weakReference.clear();
      weakReference = null;
    }

    rootLayout.setBackgroundColor(getResources().getColor(R.color.window_background));
    DetailActivity.this.startCoordinatorAnim();
  }

  private ViewPropertyAnimatorCompat coordinatorAnimator;

  private void startCoordinatorAnim() {

    coordinatorLayout.setVisibility(View.VISIBLE);

    coordinatorLayout.setPivotX((coordinatorLayout.getLeft() + coordinatorLayout.getRight()) / 2);
    coordinatorLayout.setPivotY(coordinatorLayout.getBottom());

    coordinatorLayout.setScaleX(1.2f);
    coordinatorLayout.setScaleY(1.2f);
    coordinatorLayout.setTranslationY(-30f);

    coordinatorAnimator = ViewCompat.animate(coordinatorLayout)
        .scaleX(1.0f)
        .scaleY(1.0f)
        .translationY(0.0f)
        .setDuration(ANIMATION_DURATION_SHORT)
        .setInterpolator(new DecelerateInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {

            DetailActivity.this.inflateAdapter();
          }
        });
  }

  private PopupMenu popupMenu;

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_detail, menu);

    MenuItem searchMenu = menu.findItem(R.id.action_search);
    searchMenu.setActionView(R.layout.menu_inbox_item);
    searchMenu.getActionView().setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {

        if (popupMenu == null) {
          popupMenu = new PopupMenu(DetailActivity.this, v);
          popupMenu.inflate(R.menu.menu_pop);
          popupMenu.setOnMenuItemClickListener(DetailActivity.this);
          popupMenu.show();
        } else {
          popupMenu.show();
        }
      }
    });

    return true;
  }

  @Override public boolean onMenuItemClick(MenuItem item) {

    switch (item.getItemId()) {

      case R.id.item_anton:

        if (currentState == State.Anton) return true;

        DetailActivity.this.resetSomething(State.Anton);
        DetailActivity.this.createAntonFAB();
        Toast.makeText(DetailActivity.this, "Anton", Toast.LENGTH_LONG).show();
        return true;

      case R.id.item_normal:

        if (currentState == State.Normal) return true;

        DetailActivity.this.resetSomething(State.Normal);
        DetailActivity.this.createNormalFAB();
        Toast.makeText(DetailActivity.this, "Normal", Toast.LENGTH_LONG).show();
        return true;
      case R.id.item_footer_bar:

        if (currentState == State.Footer) return true;

        DetailActivity.this.resetSomething(State.Footer);
        DetailActivity.this.createFooterFAB();

        Toast.makeText(DetailActivity.this, "FooterBar", Toast.LENGTH_LONG).show();
        return true;
      case R.id.item_sheet:

        if (currentState == State.Sheet) return true;

        DetailActivity.this.resetSomething(State.Sheet);
        DetailActivity.this.createSheetFAB();
        Toast.makeText(DetailActivity.this, "SheetReveal", Toast.LENGTH_LONG).show();
        return true;
    }
    return false;
  }

  private void resetSomething(State state) {

    if (this.currentState == State.Anton
        && antonRevealLayout != null
        && antonRevealLayout.getParent() != null) {
      ViewGroup parent = (ViewGroup) antonRevealLayout.getParent();
      parent.removeView(antonRevealLayout);
    } else if (this.currentState == State.Footer
        && footerBar != null
        && footerBar.getParent() != null) {
      ViewGroup parent = (ViewGroup) footerBar.getParent();
      parent.removeView(footerBar);
      this.footerBar = null;
    } else if (this.currentState == State.Sheet && sheetRevealManager != null) {
      sheetRevealManager.detachSheet2View(floatingActionButton);
      sheetRevealManager.detach2RecyclerView(recyclerView);
    }

    this.currentState = state;
    if (snackBar != null && snackBar.getView().getParent() != null) {
      snackBar.dismiss();
    } else {
      floatingActionButton.setTranslationX(0.0f);
      floatingActionButton.setTranslationY(0.0f);
    }
  }

  private void createAntonFAB() {
    floatingActionButton.setId(ANTON_FAB_ID);
    DetailActivity.this.createAntonReveal();
  }

  private void createNormalFAB() {
    floatingActionButton.setId(NORMAL_FAB_ID);
  }

  private void createFooterFAB() {
    floatingActionButton.setId(FOOTER_BAR_ID);
    DetailActivity.this.createFooterBar();
  }

  private void createSheetFAB() {
    floatingActionButton.setId(SHEET_BAR_ID);

    if (sheetRevealManager == null) {
      sheetRevealManager = new SheetRevealManager();
    }
    sheetRevealManager.attachSheet2View(floatingActionButton, DetailActivity.this);
    sheetRevealManager.attach2RecyclerView(recyclerView);
  }

  /***************** Created Something ***********************/
  private void createAntonReveal() {

    if (antonRevealLayout == null) {
      LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
      antonRevealLayout = inflater.inflate(R.layout.detail_anton_reveal_layout, null, false);
      antonRevealParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
          RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    rootLayout.addView(antonRevealLayout, antonRevealParams);
  }

  private void createFooterBar() {

    if (footerBar == null) {
      LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
      footerBar = inflater.inflate(R.layout.detail_footer_bar_layout, null, false);
      footerBarParams =
          new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, fabSizeSize);
      footerBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
      footerBar.findViewById(R.id.detail_footer_bar_voice).setOnClickListener(DetailActivity.this);
    }

    rootLayout.addView(footerBar, footerBarParams);
  }

  @Override public void onClick(View v) {

    switch (v.getId()) {

      case ANTON_FAB_ID:
        DetailActivity.this.onAntonClick();
        break;

      case NORMAL_FAB_ID:
        DetailActivity.this.onNormalClick();
        break;

      case FOOTER_BAR_ID:
        DetailActivity.this.onFooterClick();
        break;

      case SHEET_BAR_ID:
        DetailActivity.this.onSheetClick();
        break;

      case R.id.detail_footer_bar_voice:
        DetailActivity.this.disappearFooterReveal();
        break;
    }
  }

  /**
   * Anton Mode
   */
  private void onAntonClick() {

    final ArcAnimator arcAnimator =
        ArcAnimator.createArcAnimator(floatingActionButton, rootLayout, 37, Side.LEFT)
            .setDuration(ANIMATION_DURATION_LONG);
    arcAnimator.setInterpolator(new AccelerateInterpolator());
    arcAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);

        floatingActionButton.setEnabled(false);

        floatingActionButton.setImageDrawable(null);
        floatingActionButton.setBackgroundTintList(
            getResources().getColorStateList(R.color.cpb_internal_color));

        /*Scale X&Y ,and you can also ues the AnimatorSet*/
        //DetailActivity.this.ScaleFab(1.0f, 1.6f);
      }

      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        DetailActivity.this.appearAntonReveal();
      }
    });

    arcAnimator.start();
  }

  private void appearAntonReveal() {

    antonRevealLayout.setVisibility(View.VISIBLE);
    View child = antonRevealLayout.findViewById(R.id.detail_anton_view);

    int cx = (int) (child.getX() + child.getWidth() / 2);
    int cy = (int) (child.getY() + child.getHeight() / 2);
    float startRadius = Utils.pythagorean(fabSizeSize / 2, fabSizeSize / 2);
    float finalRadius = Utils.pythagorean(child.getWidth() / 2, child.getHeight() / 2);

    antonRevealAnim =
        ViewAnimationUtils.createCircularReveal(child, cx, cy, startRadius, finalRadius);
    antonRevealAnim.setDuration(ANIMATION_DURATION_SHORT);
    antonRevealAnim.setInterpolator(new AccelerateInterpolator());
    antonRevealAnim.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {
        /*隐藏FAB*/
        floatingActionButton.setVisibility(View.INVISIBLE);
      }

      @Override public void onAnimationEnd() {

        Intent intent = new Intent(DetailActivity.this, LaunchActivity.class);
        DetailActivity.this.startActivity(intent);
        DetailActivity.this.finish();
        overridePendingTransition(0, 0);
      }
    });

    antonRevealAnim.start();
  }

  private void ScaleFab(float fromScale, float finalScale) {

    ObjectAnimator scaleX =
        ObjectAnimator.ofFloat(floatingActionButton, "scaleX", fromScale, finalScale);
    ObjectAnimator scaleY =
        ObjectAnimator.ofFloat(floatingActionButton, "scaleY", fromScale, finalScale);

    AnimatorSet animatorSet = new AnimatorSet();
    animatorSet.playTogether(scaleX, scaleY);
    animatorSet.setStartDelay(ANIMATION_DURATION_SHORT / 5);
    animatorSet.setDuration(ANIMATION_DURATION_SHORT / 3);
    animatorSet.setInterpolator(new LinearInterpolator());
    animatorSet.start();
  }

  /**
   * Normal Mode
   */
  private void onNormalClick() {

    if (snackBar != null && snackBar.getView().getParent() != null) {
      snackBar.dismiss();
    } else {
      snackBar = Snackbar.make(coordinatorLayout, "GO小鄧子简书", Snackbar.LENGTH_LONG)
          .setAction("确定", new View.OnClickListener() {
            @Override public void onClick(View v) {

            }
          });
      /**********放在这里合适么。。。。************/
      floatingActionButton.setTag(snackBar);
      /*通知Scroller*/
      scroller.notifySnacking();

      TextView snackTv =
          (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_text);
      TextView snackAction =
          (TextView) snackBar.getView().findViewById(android.support.design.R.id.snackbar_action);

      snackBar.getView()
          .setBackgroundColor(getResources().getColor(R.color.snack_bar_background_color));
      snackTv.setTextColor(getResources().getColor(R.color.snack_bar_text_color));
      snackAction.setTextColor(getResources().getColor(R.color.snack_bar_action_color));
      snackBar.show();
    }
  }

  /**
   * FooterBar Mode
   */
  private void onFooterClick() {

    final ArcAnimator arcAnimator =
        ArcAnimator.createArcAnimator(floatingActionButton, footerBar, 45, Side.RIGHT)
            .setDuration(ANIMATION_DURATION_LONG);
    arcAnimator.setInterpolator(new AccelerateInterpolator());
    arcAnimator.addListener(new AnimatorListenerAdapter() {

      @Override public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);

        scroller.lockAnim(true);
      }

      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);
        DetailActivity.this.appearFooterReveal();
      }
    });

    arcAnimator.start();
  }

  private void appearFooterReveal() {

    footerBar.setVisibility(View.VISIBLE);
    RelativeLayout voice = (RelativeLayout) footerBar.findViewById(R.id.detail_footer_bar_rl);

    int cx = (int) (voice.getX() + voice.getWidth() / 2);
    int cy = (int) (voice.getY() + voice.getHeight() / 2);
    float finalRadius = Utils.pythagorean(voice.getWidth() / 2, voice.getHeight() / 2);

    footerRevealAnim = ViewAnimationUtils.createCircularReveal(voice, cx, cy, 0, finalRadius);
    footerRevealAnim.setDuration(ANIMATION_DURATION_SHORT);
    footerRevealAnim.setInterpolator(new AccelerateInterpolator());
    footerRevealAnim.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationStart() {
        /*隐藏FAB*/
        floatingActionButton.setVisibility(View.INVISIBLE);
      }
    });
    footerRevealAnim.start();
  }

  private void disappearFooterReveal() {

    final ArcAnimator reArcAnimator =
        ArcAnimator.createArcAnimator(floatingActionButton, fabX, fabY, 45, Side.RIGHT)
            .setDuration(ANIMATION_DURATION_SHORT);
    reArcAnimator.setInterpolator(new AccelerateInterpolator());
    reArcAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        scroller.lockAnim(false);
      }
    });

    footerRevealAnim = footerRevealAnim.reverse();
    footerRevealAnim.setDuration(ANIMATION_DURATION_SHORT);
    footerRevealAnim.setInterpolator(new AccelerateInterpolator());
    footerRevealAnim.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {

        footerBar.setVisibility(View.INVISIBLE);
        floatingActionButton.setVisibility(View.VISIBLE);

        reArcAnimator.start();
      }
    });
    footerRevealAnim.start();
  }

  /**
   * Sheet Mode
   */
  private void onSheetClick() {

    Rect rect = new Rect();
    sheetRevealManager.getSheetRect(rect);

    int endX = rect.centerX();
    int endY = rect.centerY();

    final ArcAnimator arcAnimator =
        ArcAnimator.createArcAnimator(floatingActionButton, endX, endY, 45, Side.LEFT)
            .setDuration(ANIMATION_DURATION_LONG);
    arcAnimator.setInterpolator(new AccelerateInterpolator());
    arcAnimator.addListener(new AnimatorListenerAdapter() {

      @Override public void onAnimationStart(Animator animation) {
        super.onAnimationStart(animation);

        scroller.lockAnim(true);
      }

      @Override public void onAnimationEnd(Animator animation) {
        super.onAnimationEnd(animation);

        /*隐藏Fab*/
        floatingActionButton.setVisibility(View.INVISIBLE);
        sheetRevealManager.appearSheetReveal(floatingActionButton.getWidth(),
            floatingActionButton.getHeight());
      }
    });

    arcAnimator.start();
  }

  /*************************************/
  @Override public void onSheetHidden() {

    floatingActionButton.setVisibility(View.VISIBLE);
    final ArcAnimator reArcAnimator =
        ArcAnimator.createArcAnimator(floatingActionButton, fabX, fabY, 45, Side.LEFT)
            .setDuration(ANIMATION_DURATION_SHORT);
    reArcAnimator.setInterpolator(new AccelerateInterpolator());
    reArcAnimator.addListener(new AnimatorListenerAdapter() {
      @Override public void onAnimationEnd(Animator animation) {
        scroller.lockAnim(false);
      }
    });
    reArcAnimator.start();
  }

  /*************************************/

  private void inflateAdapter() {

    List<DetailEntity> dataItems = new ArrayList<>();
    DetailEntity detailEntity = new DetailEntity();

    String[] imageDescriptions = getResources().getStringArray(R.array.detail_description);

    for (int i = 0; i < imageDescriptions.length; i++) {

      DetailEntity clone = detailEntity.newInstance();
      clone.setAvatar(avatars[i]);
      clone.setDescription(imageDescriptions[i]);
      dataItems.add(clone);
    }

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DetailActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    DetailAdapter detailAdapter = new DetailAdapter(DetailActivity.this, dataItems);
    detailAdapter.setCallback(DetailActivity.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setAdapter(detailAdapter);
    detailAdapter.notifyDataSetChanged();
  }

  @Override public void onAvatarClick(ImageView avatarItem) {

    ProfileActivity.startFromLocation(DetailActivity.this, getLocation(avatarItem));
    overridePendingTransition(0, 0);
  }

  private int[] getLocation(View avatarItem) {

    int[] startingLocation = new int[3];
    // 得到相对于整个屏幕的区域坐标（左上角坐标——右下角坐标）
    Rect viewRect = new Rect();
    avatarItem.getGlobalVisibleRect(viewRect);

    int statusBarHeight = 0;
    int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      statusBarHeight = getResources().getDimensionPixelSize(resourceId);
    }

    startingLocation[0] = viewRect.left;
    startingLocation[1] = viewRect.top - statusBarHeight;
    startingLocation[2] = avatars[((int) avatarItem.getTag())];

    return startingLocation;
  }

  @Override protected void onDestroy() {

    if (coordinatorAnimator != null) coordinatorAnimator.cancel();
    if (avatarAnimator != null) avatarAnimator.cancel();
    if (fabAnimator != null) fabAnimator.cancel();

    if (scroller != null) {
      scroller.detach(recyclerView);
    }
    appBarLayout.removeOnOffsetChangedListener(offsetChangedListener);

    super.onDestroy();
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
      DetailActivity.this.CreateDialog();
    }
    return false;
  }

  protected AppCompatDialog dialog;

  protected void CreateDialog() {

    if (dialog == null) {
      AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
      builder.setTitle("提示")
          .setMessage("确认退出吗？")
          .setCancelable(false)
          .setInverseBackgroundForced(false)
          .setPositiveButton("确认", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
              DetailActivity.this.finish();
            }
          })
          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          });
      dialog = builder.create();
    }

    dialog.show();
  }
}