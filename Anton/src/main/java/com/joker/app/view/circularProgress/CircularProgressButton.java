package com.joker.app.view.circularProgress;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.StateSet;
import android.widget.Button;

import com.joker.app.R;

public class CircularProgressButton extends Button implements StateManager.StateInterface {

  private static final String TAG = CircularProgressButton.class.getSimpleName();

  public static final int IDLE_STATE_PROGRESS = 0;
  public static final int ERROR_STATE_PROGRESS = -1;

  public static final int STATE_ENABLED = android.R.attr.state_enabled;
  public static final int STATE_PRESSED = android.R.attr.state_pressed;
  public static final int STATE_FOCUSED = android.R.attr.state_focused;
  public static final int STATE_WINDOW_FOCUSED = android.R.attr.state_window_focused;
  public static final int STATE_SELECTED = android.R.attr.state_selected;

  /*背景Drawable*/
  private StrokeGradientDrawable background;

  private CircularAnimatedDrawable mAnimatedDrawable;
  private CircularProgressDrawable mProgressDrawable;

  private ColorStateList idleColorSelector;
  private ColorStateList completeColorSelector;
  private ColorStateList errorColorSelector;

  private StateListDrawable mIdleStateDrawable;
  private StateListDrawable mCompleteStateDrawable;
  private StateListDrawable mErrorStateDrawable;

  private String idleText;
  private String completeText;
  private String errorText;
  private int textColor;

  private int completeIcon;
  private int errorIcon;
  private boolean isStroke = false;

  /**
   * progressInternalColor：Progress内部颜色
   * indicatorColor：Stroke颜色
   * $BackgroundStrokeColor：Stroke背景颜色
   */
  private int idleBackgroundStrokeColor;
  private int completeBackgroundStrokeColor;
  private int errorBackgroundStrokeColor;
  private int progressInternalColor;
  private int indicatorColor;

  private StateManager mStateManager;
  private State currentState = State.IDLE;

  private int backgroundStrokeWidth;
  private int indicatorWidth;
  private int progressPadding;
  private float radiusCorner;

  private boolean mIndeterminateProgressMode;
  private boolean mConfigurationChanged;
  private boolean mFrameToFinish = false;

  private enum State {
    PROGRESS, IDLE, COMPLETE, ERROR
  }

  private static final int mMaxProgress = 100;
  private float currentProgress;

  /*是否处在向progress过度的状态*/
  private boolean mMorphingInProgress;

  public CircularProgressButton(Context context) {
    super(context);
    CircularProgressButton.this.init(context, null);
  }

  public CircularProgressButton(Context context, AttributeSet attrs) {
    super(context, attrs);
    CircularProgressButton.this.init(context, attrs);
  }

  public CircularProgressButton(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    CircularProgressButton.this.init(context, attrs);
  }

  private void init(Context context, AttributeSet attributeSet) {

    /*initAttributes*/
    CircularProgressButton.this.initAttributes(context, attributeSet);

    int value = (int) getContext().getResources().getDimension(R.dimen.stroke_width);

    CircularProgressButton.this.backgroundStrokeWidth = isStroke ? value : 0;
    CircularProgressButton.this.indicatorWidth = value;

    CircularProgressButton.this.currentState = State.IDLE;
    CircularProgressButton.this.mStateManager = new StateManager(CircularProgressButton.this);

    CircularProgressButton.this.setText(idleText);

    CircularProgressButton.this.initIdleStateDrawable();
    CircularProgressButton.this.setBackgroundCompat(mIdleStateDrawable);
  }

  private void initAttributes(Context context, AttributeSet attributeSet) {

    TypedArray attr =
        context.obtainStyledAttributes(attributeSet, R.styleable.CircularProgressButton, 0, 0);
    if (attr == null) {
      return;
    }

    try {

      isStroke = attr.getBoolean(R.styleable.CircularProgressButton_cpb_is_stroke, false);
      textColor = attr.getColor(R.styleable.CircularProgressButton_cpb_text_color,
          Color.parseColor("#FFFFFCFD"));

      idleText = attr.getString(R.styleable.CircularProgressButton_cpb_idle_text);
      completeText = attr.getString(R.styleable.CircularProgressButton_cpb_complete_text);
      errorText = attr.getString(R.styleable.CircularProgressButton_cpb_error_text);

      idleBackgroundStrokeColor =
          attr.getColor(R.styleable.CircularProgressButton_cpb_idle_backgroundStroke_color,
              Color.BLACK);
      completeBackgroundStrokeColor =
          attr.getColor(R.styleable.CircularProgressButton_cpb_complete_backgroundStroke_color,
              Color.YELLOW);
      errorBackgroundStrokeColor =
          attr.getColor(R.styleable.CircularProgressButton_cpb_error_backgroundStroke_color,
              Color.GRAY);
      indicatorColor =
          attr.getColor(R.styleable.CircularProgressButton_cpb_indicator_color, Color.BLUE);
      progressInternalColor =
          attr.getColor(R.styleable.CircularProgressButton_cpb_progress_internal_color, Color.RED);

      completeIcon = attr.getResourceId(R.styleable.CircularProgressButton_cpb_complete_icon, 0);
      errorIcon = attr.getResourceId(R.styleable.CircularProgressButton_cpb_error_icon, 0);

      radiusCorner = attr.getDimension(R.styleable.CircularProgressButton_cpb_radius_corner, 0);
      progressPadding =
          attr.getDimensionPixelSize(R.styleable.CircularProgressButton_cpb_progress_padding, 0);

      idleColorSelector = getResources().getColorStateList(
          attr.getResourceId(R.styleable.CircularProgressButton_cpb_idle_selector,
              R.drawable.idle_state_selector));
      completeColorSelector = getResources().getColorStateList(
          attr.getResourceId(R.styleable.CircularProgressButton_cpb_complete_selector,
              R.drawable.complete_state_selector));
      errorColorSelector = getResources().getColorStateList(
          attr.getResourceId(R.styleable.CircularProgressButton_cpb_error_selector,
              R.drawable.error_state_selector));
    } finally {
      attr.recycle();
    }
  }

  private void initIdleStateDrawable() {

    int normalColor = CircularProgressButton.this.getNormalColor(idleColorSelector);
    int pressedColor = CircularProgressButton.this.getPressedColor(idleColorSelector);
    int colorFocused = CircularProgressButton.this.getFocusedColor(idleColorSelector);
    int disabledColor = CircularProgressButton.this.getDisabledColor(idleColorSelector);

    if (background == null) {
      background = CircularProgressButton.this.createBackgroundDrawable(normalColor);
    }

    StrokeGradientDrawable pressedDrawable =
        CircularProgressButton.this.createBackgroundDrawable(pressedColor);
    StrokeGradientDrawable focusedDrawable =
        CircularProgressButton.this.createBackgroundDrawable(colorFocused);
    StrokeGradientDrawable disabledDrawable =
        CircularProgressButton.this.createBackgroundDrawable(disabledColor);

    mIdleStateDrawable = new StateListDrawable();

    mIdleStateDrawable.addState(new int[] { STATE_PRESSED, STATE_WINDOW_FOCUSED },
        pressedDrawable.getGradientDrawable());
    mIdleStateDrawable.addState(new int[] { STATE_FOCUSED }, focusedDrawable.getGradientDrawable());
    mIdleStateDrawable.addState(new int[] { -STATE_ENABLED },
        disabledDrawable.getGradientDrawable());
    //没有任何状态时显示的图片,colorNormal生成的Drawable
    mIdleStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
  }

  private void initCompleteStateDrawable() {

    int pressedColor = CircularProgressButton.this.getPressedColor(completeColorSelector);

    StrokeGradientDrawable drawablePressed =
        CircularProgressButton.this.createBackgroundDrawable(pressedColor);

    mCompleteStateDrawable = new StateListDrawable();
    mCompleteStateDrawable.addState(new int[] { STATE_PRESSED, STATE_WINDOW_FOCUSED },
        drawablePressed.getGradientDrawable());
    mCompleteStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
  }

  private void initErrorStateDrawable() {

    int pressedColor = CircularProgressButton.this.getPressedColor(errorColorSelector);

    StrokeGradientDrawable drawablePressed =
        CircularProgressButton.this.createBackgroundDrawable(pressedColor);

    mErrorStateDrawable = new StateListDrawable();
    mErrorStateDrawable.addState(new int[] { STATE_PRESSED, STATE_WINDOW_FOCUSED },
        drawablePressed.getGradientDrawable());
    mErrorStateDrawable.addState(StateSet.WILD_CARD, background.getGradientDrawable());
  }

  private int getNormalColor(ColorStateList colorStateList) {
    return colorStateList.getColorForState(new int[] { STATE_ENABLED }, 0);
  }

  private int getFocusedColor(ColorStateList colorStateList) {
    return colorStateList.getColorForState(new int[] { STATE_FOCUSED }, 0);
  }

  private int getPressedColor(ColorStateList colorStateList) {
    return colorStateList.getColorForState(new int[] { STATE_PRESSED, STATE_WINDOW_FOCUSED }, 0);
  }

  private int getDisabledColor(ColorStateList colorStateList) {
    return colorStateList.getColorForState(new int[] { -STATE_ENABLED }, 0);
  }

  private StrokeGradientDrawable createBackgroundDrawable(int normalColor) {

    /*此处注意避免“constant state”，所以我们用mutate来生成一个drawable*/
    GradientDrawable gradientDrawable =
        (GradientDrawable) getResources().getDrawable(R.drawable.background).mutate();
    gradientDrawable.setColor(normalColor);
    gradientDrawable.setCornerRadius(radiusCorner);

    StrokeGradientDrawable strokeGradientDrawable = new StrokeGradientDrawable(gradientDrawable);
    strokeGradientDrawable.initStrokeAndColor(backgroundStrokeWidth, idleBackgroundStrokeColor);

    return strokeGradientDrawable;
  }

  @Override protected void drawableStateChanged() {

    if (currentState == State.COMPLETE) {
      initCompleteStateDrawable();
      setBackgroundCompat(mCompleteStateDrawable);
    } else if (currentState == State.IDLE) {
      initIdleStateDrawable();
      setBackgroundCompat(mIdleStateDrawable);
    } else if (currentState == State.ERROR) {
      initErrorStateDrawable();
      setBackgroundCompat(mErrorStateDrawable);
    }

    if (currentState != State.PROGRESS) {
      super.drawableStateChanged();
    }
  }

  @Override protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (currentProgress > 0 && currentState == State.PROGRESS && !mMorphingInProgress) {
      if (mIndeterminateProgressMode && !mFrameToFinish) {
        CircularProgressButton.this.drawIndeterminateProgress(canvas, false);
      } else if (mIndeterminateProgressMode && mFrameToFinish) {
        CircularProgressButton.this.drawIndeterminateProgress(canvas, true);
      } else if (!mIndeterminateProgressMode && !mFrameToFinish) {
        CircularProgressButton.this.drawProgress(canvas);
      }
    }
  }

  private void drawIndeterminateProgress(Canvas canvas, boolean isFinish) {

    if (this.mAnimatedDrawable == null) {

      int offset = (getWidth() - getHeight()) / 2;
      if (!isFinish) {
        Shader linearGradient = new LinearGradient(0, 0, 100, 100, new int[] {
            Color.WHITE, Color.parseColor("#F7F7F7")
        }, null, Shader.TileMode.REPEAT);

        mAnimatedDrawable = new CircularAnimatedDrawable(linearGradient, indicatorWidth);
      } else {
        mAnimatedDrawable = new CircularAnimatedDrawable(progressInternalColor, indicatorWidth);
      }

      int top = progressPadding;
      int left = offset + progressPadding;
      int right = getWidth() - offset - progressPadding;
      int bottom = getHeight() - progressPadding;
      mAnimatedDrawable.setBounds(left, top, right, bottom);
      CircularProgressButton.this.setDrawableBounds(mAnimatedDrawable.getBounds());
      mAnimatedDrawable.setCallback(this);
      mAnimatedDrawable.start();
    } else {
      mAnimatedDrawable.draw(canvas);
    }
  }

  private Rect bounds;

  public void setDrawableBounds(Rect bounds) {
    this.bounds = bounds;
  }

  public Rect getDrawableBounds() {
    return bounds;
  }

  private void drawProgress(Canvas canvas) {

    if (mProgressDrawable == null) {
      int offset = (getWidth() - getHeight()) / 2;
      int size = getHeight() - progressPadding * 2;
      mProgressDrawable = new CircularProgressDrawable(size, indicatorWidth, indicatorColor);
      int left = offset + progressPadding;
      mProgressDrawable.setBounds(left, progressPadding, left, progressPadding);
    }
    float sweepAngle = (360f / mMaxProgress) * currentProgress;
    mProgressDrawable.setSweepAngle(sweepAngle);
    mProgressDrawable.draw(canvas);
  }

  public boolean isIndeterminateProgressMode() {
    return mIndeterminateProgressMode;
  }

  public void setIndeterminateProgressMode(boolean indeterminateProgressMode) {
    this.mIndeterminateProgressMode = indeterminateProgressMode;
  }

  public void setFrameToFinish(boolean frameToFinish) {

    CircularProgressButton.this.mAnimatedDrawable = null;
    this.mFrameToFinish = frameToFinish;
  }

  public boolean isFrameToFinish() {
    return mFrameToFinish;
  }

  @Override protected boolean verifyDrawable(Drawable who) {
    return who == mAnimatedDrawable || super.verifyDrawable(who);
  }

  private MorphingAnimation createMorphing() {
    mMorphingInProgress = true;

    MorphingAnimation animation = new MorphingAnimation(this, background);
    animation.setFromCornerRadius(radiusCorner);
    animation.setToCornerRadius(radiusCorner);

    animation.setFromWidth(getWidth());
    animation.setToWidth(getWidth());

    if (mConfigurationChanged) {
      animation.setDuration(MorphingAnimation.DURATION_INSTANT);
    } else {
      animation.setDuration(MorphingAnimation.DURATION_NORMAL);
    }

    mConfigurationChanged = false;

    return animation;
  }

  private MorphingAnimation createProgressMorphing(float fromRadius, float toRadius, int fromWidth,
      int toWidth) {

    CircularProgressButton.this.mMorphingInProgress = true;

    MorphingAnimation animation = new MorphingAnimation(CircularProgressButton.this, background);
    animation.setFromCornerRadius(fromRadius);
    animation.setToCornerRadius(toRadius);

    animation.setFromWidth(fromWidth);
    animation.setToWidth(toWidth);

    animation.setPadding(progressPadding);

    if (mConfigurationChanged) {
      animation.setDuration(MorphingAnimation.DURATION_INSTANT);
    } else {
      animation.setDuration(MorphingAnimation.DURATION_NORMAL);
    }

    mConfigurationChanged = false;

    return animation;
  }

  /**********************************************************
   * END PROGRESS
   * /**
   * Idle --> Progress
   */
  private void morphIdleToProgress() {

    super.setWidth(getWidth());
    super.setText(null);

    MorphingAnimation animation =
        CircularProgressButton.this.createProgressMorphing(radiusCorner, getHeight(), getWidth(),
            getHeight());
    animation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

    animation.setFromColor(CircularProgressButton.this.getNormalColor(idleColorSelector));
    animation.setToColor(progressInternalColor);

    animation.setFromStrokeColor(idleBackgroundStrokeColor);
    animation.setToStrokeColor(progressInternalColor);

    animation.setListener(mProgressStateListener);

    animation.start();
  }

  private AnimatorListenerAdapter mProgressStateListener = new AnimatorListenerAdapter() {
    @Override public void onAnimationEnd(Animator animator) {

      CircularProgressButton.this.mAnimatedDrawable = null;
      CircularProgressButton.this.mFrameToFinish = false;
      CircularProgressButton.this.mMorphingInProgress = false;
      CircularProgressButton.this.currentState = State.PROGRESS;

      mStateManager.checkState(CircularProgressButton.this);
    }
  };

  /**********************************************************
   * END COMPLETE
   * /**
   * Idle --> Complete
   */
  private void morphIdleToComplete() {

    MorphingAnimation animation = CircularProgressButton.this.createMorphing();

    animation.setFromColor(getNormalColor(idleColorSelector));
    animation.setToColor(getNormalColor(completeColorSelector));

    animation.setFromStrokeColor(idleBackgroundStrokeColor);
    animation.setToStrokeColor(completeBackgroundStrokeColor);

    animation.setListener(mCompleteStateListener);

    animation.start();
  }

  /**
   * Progress --> Complete
   */
  private void morphProgressToComplete() {

    MorphingAnimation animation =
        CircularProgressButton.this.createProgressMorphing(getHeight(), radiusCorner, getHeight(),
            getWidth());

    animation.setFromColor(progressInternalColor);
    animation.setToColor(CircularProgressButton.this.getNormalColor(completeColorSelector));

    animation.setFromStrokeColor(progressInternalColor);
    animation.setToStrokeColor(completeBackgroundStrokeColor);

    animation.setListener(mCompleteStateListener);

    animation.start();
  }

  private AnimatorListenerAdapter mCompleteStateListener = new AnimatorListenerAdapter() {
    @Override public void onAnimationEnd(Animator animator) {

      CircularProgressButton.this.mAnimatedDrawable = null;
      CircularProgressButton.this.mFrameToFinish = false;
      CircularProgressButton.this.mMorphingInProgress = false;
      CircularProgressButton.this.currentState = State.COMPLETE;

      if (completeIcon != 0) {
        CircularProgressButton.super.setText(null);
        CircularProgressButton.this.setIcon(completeIcon);
      } else {
        CircularProgressButton.super.setText(completeText);
        CircularProgressButton.super.setTextColor(textColor);
      }

      mStateManager.checkState(CircularProgressButton.this);
    }
  };

  /**********************************************************
   * END ERROR
   * /**
   * Idle --> Error
   */
  private void morphIdleToError() {
    MorphingAnimation animation = CircularProgressButton.this.createMorphing();

    animation.setFromColor(getNormalColor(idleColorSelector));
    animation.setToColor(getNormalColor(errorColorSelector));

    animation.setFromStrokeColor(idleBackgroundStrokeColor);
    animation.setToStrokeColor(errorBackgroundStrokeColor);

    animation.setListener(mErrorStateListener);

    animation.start();
  }

  /**
   * Progress --> Error
   */
  private void morphProgressToError() {
    MorphingAnimation animation =
        CircularProgressButton.this.createProgressMorphing(getHeight(), radiusCorner, getHeight(),
            getWidth());
    animation.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

    animation.setFromColor(progressInternalColor);
    animation.setToColor(getNormalColor(errorColorSelector));

    animation.setFromStrokeColor(progressInternalColor);
    animation.setToStrokeColor(errorBackgroundStrokeColor);

    animation.setListener(mErrorStateListener);

    animation.start();
  }

  private AnimatorListenerAdapter mErrorStateListener = new AnimatorListenerAdapter() {
    @Override public void onAnimationEnd(Animator animator) {

      CircularProgressButton.this.mAnimatedDrawable = null;
      CircularProgressButton.this.mFrameToFinish = false;
      CircularProgressButton.this.mMorphingInProgress = false;
      CircularProgressButton.this.currentState = State.ERROR;

      if (errorIcon != 0) {
        CircularProgressButton.super.setText(null);
        CircularProgressButton.this.setIcon(errorIcon);
      } else {
        CircularProgressButton.super.setText(errorText);
        CircularProgressButton.super.setTextColor(textColor);
      }

      mStateManager.checkState(CircularProgressButton.this);
    }
  };

  /**********************************************************
   * END IDLE
   * /**
   * Complete --> Idle
   */
  private void morphCompleteToIdle() {
    MorphingAnimation animation = CircularProgressButton.this.createMorphing();

    animation.setFromColor(getNormalColor(completeColorSelector));
    animation.setToColor(getNormalColor(idleColorSelector));

    animation.setFromStrokeColor(completeBackgroundStrokeColor);
    animation.setToStrokeColor(idleBackgroundStrokeColor);

    animation.setListener(mIdleStateListener);

    animation.start();
  }

  /**
   * Error --> Idle
   */
  private void morphErrorToIdle() {
    MorphingAnimation animation = CircularProgressButton.this.createMorphing();
    animation.setDuration(getResources().getInteger(android.R.integer.config_longAnimTime));

    animation.setFromColor(getNormalColor(errorColorSelector));
    animation.setToColor(getNormalColor(idleColorSelector));

    animation.setFromStrokeColor(errorBackgroundStrokeColor);
    animation.setToStrokeColor(idleBackgroundStrokeColor);

    animation.setListener(mIdleStateListener);

    animation.start();
  }

  /**
   * Progress --> Idle
   */
  private void morphProgressToIdle() {
    MorphingAnimation animation =
        CircularProgressButton.this.createProgressMorphing(getHeight(), radiusCorner, getHeight(),
            getWidth());

    animation.setFromColor(progressInternalColor);
    animation.setToColor(getNormalColor(idleColorSelector));

    animation.setFromStrokeColor(progressInternalColor);
    animation.setToStrokeColor(idleBackgroundStrokeColor);
    animation.setListener(mIdleStateListener);

    animation.start();
  }

  private AnimatorListenerAdapter mIdleStateListener = new AnimatorListenerAdapter() {
    @Override public void onAnimationEnd(Animator animator) {

      CircularProgressButton.this.mMorphingInProgress = false;
      CircularProgressButton.this.currentState = State.IDLE;

      CircularProgressButton.this.removeIcon();
      CircularProgressButton.super.setText(idleText);
      CircularProgressButton.super.setTextColor(textColor);

      mStateManager.checkState(CircularProgressButton.this);
    }
  };

  private void setIcon(int icon) {
    Drawable drawable = getResources().getDrawable(icon);
    if (drawable != null) {
      int padding = (getWidth() / 2) - (drawable.getIntrinsicWidth() / 2);
      setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);
      setPadding(padding, 0, 0, 0);
    }
  }

  protected void removeIcon() {
    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    setPadding(0, 0, 0, 0);
  }

  /**
   * Set the View's background. Masks the API changes made in Jelly Bean.
   */
  @SuppressWarnings("deprecation") @SuppressLint("NewApi") public void setBackgroundCompat(
      Drawable drawable) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      setBackground(drawable);
    } else {
      setBackgroundDrawable(drawable);
    }
  }

  @Override public boolean getEnabled() {
    return super.isEnabled();
  }

  @Override public void setEnabled(boolean isEnable) {
    super.setEnabled(isEnable);
  }

  @Override public float getProgress() {
    return CircularProgressButton.this.currentProgress;
  }

  @Override public void setProgress(float progress) {

    CircularProgressButton.this.currentProgress = progress;

    if (mMorphingInProgress || getWidth() == 0) {
      return;
    }

    mStateManager.saveProgress(CircularProgressButton.this);

    if (currentProgress > IDLE_STATE_PROGRESS) {
      if (currentState == State.IDLE) {

       /*Idle --> Progress*/
        CircularProgressButton.this.morphIdleToProgress();
      } else if (currentState == State.PROGRESS) {
        invalidate();
      }
    }

    if (currentProgress >= mMaxProgress) {
      if (currentState == State.IDLE) {

        /*Idle --> Complete*/
        CircularProgressButton.this.morphIdleToComplete();
      } else if (currentState == State.PROGRESS) {

         /*Progress --> Complete*/
        CircularProgressButton.this.morphProgressToComplete();
      }
    }

    if (currentProgress == ERROR_STATE_PROGRESS) {
      if (currentState == State.PROGRESS) {

        /*Progress --> Error*/
        CircularProgressButton.this.morphProgressToError();
      } else if (currentState == State.IDLE) {

        /*Idle --> Error*/
        CircularProgressButton.this.morphIdleToError();
      }
    }

    if (currentProgress == IDLE_STATE_PROGRESS) {
      if (currentState == State.COMPLETE) {

        /*Complete --> Idle*/
        CircularProgressButton.this.morphCompleteToIdle();
      } else if (currentState == State.PROGRESS) {

        /*Progress --> Idle*/
        CircularProgressButton.this.morphProgressToIdle();
      } else if (currentState == State.ERROR) {

        /*Error --> Idle*/
        CircularProgressButton.this.morphErrorToIdle();
      }
    }
  }

  public void setBackgroundColor(int color) {
    background.getGradientDrawable().setColor(color);
  }

  public void setBackgroundRadius(float radius) {
    background.getGradientDrawable().setCornerRadius(radius);
  }

  public void setIdleBackgroundStrokeColor(int color) {
    background.setStrokeColor(color);
  }

  public void setBackgroundStrokeWidth(int width) {
    background.setStrokeWidth(width);
  }

  public String getIdleText() {
    return idleText;
  }

  public String getCompleteText() {
    return completeText;
  }

  public String getErrorText() {
    return errorText;
  }

  public void setIdleText(String text) {
    idleText = text;
  }

  public void setCompleteText(String text) {
    completeText = text;
  }

  public void setErrorText(String text) {
    errorText = text;
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      CircularProgressButton.this.setProgress(currentProgress);
    }
  }

  @Override public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.mProgress = currentProgress;
    savedState.mIndeterminateProgressMode = mIndeterminateProgressMode;
    savedState.mFrameToFinish = mFrameToFinish;
    savedState.mConfigurationChanged = true;

    return savedState;
  }

  @Override public void onRestoreInstanceState(Parcelable state) {
    if (state instanceof SavedState) {
      SavedState savedState = (SavedState) state;
      currentProgress = savedState.mProgress;
      mIndeterminateProgressMode = savedState.mIndeterminateProgressMode;
      mFrameToFinish = savedState.mFrameToFinish;
      mConfigurationChanged = savedState.mConfigurationChanged;
      super.onRestoreInstanceState(savedState.getSuperState());
      setProgress(currentProgress);
    } else {
      super.onRestoreInstanceState(state);
    }
  }

  static class SavedState extends BaseSavedState {

    private boolean mIndeterminateProgressMode;
    private boolean mConfigurationChanged;
    private boolean mFrameToFinish;
    private float mProgress;

    public SavedState(Parcelable parcel) {
      super(parcel);
    }

    private SavedState(Parcel in) {
      super(in);
      mProgress = in.readInt();
      mIndeterminateProgressMode = in.readInt() == 1;
      mFrameToFinish = in.readInt() == 1;
      mConfigurationChanged = in.readInt() == 1;
    }

    @Override public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeFloat(mProgress);
      out.writeInt(mIndeterminateProgressMode ? 1 : 0);
      out.writeInt(mFrameToFinish ? 1 : 0);
      out.writeInt(mConfigurationChanged ? 1 : 0);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

      @Override public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      @Override public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }
}
