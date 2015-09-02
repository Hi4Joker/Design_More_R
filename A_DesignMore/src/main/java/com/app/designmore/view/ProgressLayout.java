package com.app.designmore.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.app.designmore.R;

/**
 * @author Vlonjat Gashi (vlonjatg)
 */
public class ProgressLayout extends RelativeLayout {

  private static final String TAG_LOADING = "ProgressActivity.TAG_LOADING";
  private static final String TAG_EMPTY = "ProgressActivity.TAG_EMPTY";
  private static final String TAG_ERROR = "ProgressActivity.TAG_ERROR";

  private LayoutInflater inflater;
  private View view;
  private LayoutParams layoutParams;

  private List<View> contentViews = new ArrayList<>();

  private RelativeLayout loadingStateRelativeLayout;

  private RelativeLayout emptyStateRelativeLayout;
  private ImageView emptyStateImageView;
  private TextView emptyStateTitleTextView;
  private TextView emptyStateContentTextView;

  private RelativeLayout errorStateRelativeLayout;
  private ImageView errorStateImageView;
  private TextView errorStateTitleTextView;
  private TextView errorStateContentTextView;
  private Button errorStateButton;

  int loadingStateBackgroundColor;

  int emptyStateImageWidth;
  int emptyStateImageHeight;
  int emptyStateTitleTextSize;
  int emptyStateContentTextSize;
  int emptyStateTitleTextColor;
  int emptyStateContentTextColor;
  int emptyStateBackgroundColor;

  int errorStateImageWidth;
  int errorStateImageHeight;
  int errorStateTitleTextSize;
  int errorStateContentTextSize;
  int errorStateTitleTextColor;
  int errorStateContentTextColor;
  int errorStateButtonTextColor;
  int errorStateBackgroundColor;

  public enum State {
    CONTENT, LOADING, EMPTY, ERROR
  }

  private State currentState = State.CONTENT;

  public ProgressLayout(Context context) {
    super(context);
  }

  public ProgressLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  public ProgressLayout(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressLayout);

    if (typedArray != null) {

      try {
        //Loading currentState attrs
        loadingStateBackgroundColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressLoadingStateBackgroundColor,
                Color.TRANSPARENT);

        //Empty currentState attrs
        emptyStateImageWidth = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressEmptyStateImageWidth, 308);

        emptyStateImageHeight = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressEmptyStateImageHeight, 308);

        emptyStateTitleTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressEmptyStateTitleTextSize, 15);

        emptyStateContentTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressEmptyStateContentTextSize, 14);

        emptyStateTitleTextColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressEmptyStateTitleTextColor,
                Color.BLACK);

        emptyStateContentTextColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressEmptyStateContentTextColor,
                Color.BLACK);

        emptyStateBackgroundColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressEmptyStateBackgroundColor,
                Color.TRANSPARENT);

        //Error currentState attrs
        errorStateImageWidth = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressErrorStateImageWidth, 308);

        errorStateImageHeight = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressErrorStateImageHeight, 308);

        errorStateTitleTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressErrorStateTitleTextSize, 15);

        errorStateContentTextSize = typedArray.getDimensionPixelSize(
            R.styleable.ProgressLayout_progressErrorStateContentTextSize, 14);

        errorStateTitleTextColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressErrorStateTitleTextColor,
                Color.BLACK);

        errorStateContentTextColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressErrorStateContentTextColor,
                Color.BLACK);

        errorStateButtonTextColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressErrorStateButtonTextColor,
                Color.BLACK);

        errorStateBackgroundColor =
            typedArray.getColor(R.styleable.ProgressLayout_progressErrorStateBackgroundColor,
                Color.TRANSPARENT);
      } catch (Exception ignored) {
      } finally {
        typedArray.recycle();
      }
    }
  }

  @Override public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
    super.addView(child, index, params);

    if (child.getTag() == null || (!child.getTag().equals(TAG_LOADING) && !child.getTag()
        .equals(TAG_EMPTY) && !child.getTag().equals(TAG_ERROR))) {
      contentViews.add(child);
    }
  }

  /**
   * Hide all other states and show content
   */
  public void showContent() {
    switchState(State.CONTENT, null, null, null, null, null, Collections.<Integer>emptyList());
  }

  /**
   * Hide all other states and show content
   *
   * @param skipIds Ids of views not to show
   */
  public void showContent(List<Integer> skipIds) {
    switchState(State.CONTENT, null, null, null, null, null, skipIds);
  }

  /**
   * Hide content and show the progress bar
   */
  public void showLoading() {
    switchState(State.LOADING, null, null, null, null, null, Collections.<Integer>emptyList());
  }

  /**
   * Hide content and show the progress bar
   *
   * @param skipIds Ids of views to not hide
   */
  public void showLoading(List<Integer> skipIds) {
    switchState(State.LOADING, null, null, null, null, null, skipIds);
  }

  /**
   * Show empty view when there are not data to show
   *
   * @param emptyImageDrawable Drawable to show
   * @param emptyTextTitle Title of the empty view to show
   * @param emptyTextContent Content of the empty view to show
   */
  public void showEmpty(Drawable emptyImageDrawable, String emptyTextTitle,
      String emptyTextContent) {
    switchState(State.EMPTY, emptyImageDrawable, emptyTextTitle, emptyTextContent, null, null,
        Collections.<Integer>emptyList());
  }

  /**
   * Show empty view when there are not data to show
   *
   * @param emptyImageDrawable Drawable to show
   * @param emptyTextTitle Title of the empty view to show
   * @param emptyTextContent Content of the empty view to show
   * @param skipIds Ids of views to not hide
   */
  public void showEmpty(Drawable emptyImageDrawable, String emptyTextTitle, String emptyTextContent,
      List<Integer> skipIds) {
    switchState(State.EMPTY, emptyImageDrawable, emptyTextTitle, emptyTextContent, null, null,
        skipIds);
  }

  /**
   * Show error view with a button when something goes wrong and prompting the user to try again
   *
   * @param errorImageDrawable Drawable to show
   * @param errorTextTitle Title of the error view to show
   * @param errorTextContent Content of the error view to show
   * @param errorButtonText Text on the error view button to show
   * @param onClickListener Listener of the error view button
   */
  public void showError(Drawable errorImageDrawable, String errorTextTitle, String errorTextContent,
      String errorButtonText, OnClickListener onClickListener) {
    switchState(State.ERROR, errorImageDrawable, errorTextTitle, errorTextContent, errorButtonText,
        onClickListener, Collections.<Integer>emptyList());
  }

  /**
   * Show error view with a button when something goes wrong and prompting the user to try again
   *
   * @param errorImageDrawable Drawable to show
   * @param errorTextTitle Title of the error view to show
   * @param errorTextContent Content of the error view to show
   * @param errorButtonText Text on the error view button to show
   * @param onClickListener Listener of the error view button
   * @param skipIds Ids of views to not hide
   */
  public void showError(Drawable errorImageDrawable, String errorTextTitle, String errorTextContent,
      String errorButtonText, OnClickListener onClickListener, List<Integer> skipIds) {
    switchState(State.ERROR, errorImageDrawable, errorTextTitle, errorTextContent, errorButtonText,
        onClickListener, skipIds);
  }

  /**
   * Get which currentState is set
   *
   * @return State
   */
  public State getCurrentState() {
    return currentState;
  }

  /**
   * Check if content is shown
   *
   * @return boolean
   */
  public boolean isContent() {
    return currentState == State.CONTENT;
  }

  /**
   * Check if loading currentState is shown
   *
   * @return boolean
   */
  public boolean isLoading() {
    return currentState == State.LOADING;
  }

  /**
   * Check if empty currentState is shown
   *
   * @return boolean
   */
  public boolean isEmpty() {
    return currentState == State.EMPTY;
  }

  /**
   * Check if error currentState is shown
   *
   * @return boolean
   */
  public boolean isError() {
    return currentState == State.ERROR;
  }

  private void switchState(State state, Drawable drawable, String errorText,
      String errorTextContent, String errorButtonText, OnClickListener onClickListener,
      List<Integer> skipIds) {
    this.currentState = state;

    switch (state) {
      case CONTENT:
        //Hide all currentState views to display content
        ProgressLayout.this.hideLoadingView();
        ProgressLayout.this.hideEmptyView();
        ProgressLayout.this.hideErrorView();

        ProgressLayout.this.setContentVisibility(true, skipIds);
        break;
      case LOADING:
        ProgressLayout.this.hideEmptyView();
        ProgressLayout.this.hideErrorView();

        ProgressLayout.this.setLoadingView();
        ProgressLayout.this.setContentVisibility(false, skipIds);
        break;
      case EMPTY:
        ProgressLayout.this.hideLoadingView();
        ProgressLayout.this.hideErrorView();

        ProgressLayout.this.setEmptyView();
        emptyStateImageView.setImageDrawable(drawable);
        emptyStateTitleTextView.setText(errorText);
        emptyStateContentTextView.setText(errorTextContent);
        ProgressLayout.this.setContentVisibility(false, skipIds);
        break;
      case ERROR:
        ProgressLayout.this.hideLoadingView();
        ProgressLayout.this.hideEmptyView();

        ProgressLayout.this.setErrorView();
        errorStateImageView.setImageDrawable(drawable);
        errorStateTitleTextView.setText(errorText);
        errorStateContentTextView.setText(errorTextContent);
        errorStateButton.setText(errorButtonText);
        errorStateButton.setOnClickListener(onClickListener);
        ProgressLayout.this.setContentVisibility(false, skipIds);
        break;
    }
  }

  private void setLoadingView() {

    view = inflater.inflate(R.layout.progress_loading_view, null);
    loadingStateRelativeLayout =
        (RelativeLayout) view.findViewById(R.id.loadingStateRelativeLayout);
    loadingStateRelativeLayout.setTag(TAG_LOADING);

    //Set background color if not TRANSPARENT
    if (loadingStateBackgroundColor != Color.TRANSPARENT) {
      loadingStateRelativeLayout.setBackgroundColor(loadingStateBackgroundColor);
    }

    layoutParams =
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    layoutParams.addRule(CENTER_IN_PARENT);

    this.addView(loadingStateRelativeLayout, layoutParams);
  }

  private void setEmptyView() {

    view = inflater.inflate(R.layout.progress_empty_view, null);
    emptyStateRelativeLayout = (RelativeLayout) view.findViewById(R.id.emptyStateRelativeLayout);
    emptyStateRelativeLayout.setTag(TAG_EMPTY);

    emptyStateImageView = (ImageView) view.findViewById(R.id.emptyStateImageView);
    emptyStateTitleTextView = (TextView) view.findViewById(R.id.emptyStateTitleTextView);
    emptyStateContentTextView = (TextView) view.findViewById(R.id.emptyStateContentTextView);

    //Set empty currentState image width and height
    emptyStateImageView.getLayoutParams().width = emptyStateImageWidth;
    emptyStateImageView.getLayoutParams().height = emptyStateImageHeight;
    emptyStateImageView.requestLayout();

    emptyStateTitleTextView.setTextSize(emptyStateTitleTextSize);
    emptyStateContentTextView.setTextSize(emptyStateContentTextSize);
    emptyStateTitleTextView.setTextColor(emptyStateTitleTextColor);
    emptyStateContentTextView.setTextColor(emptyStateContentTextColor);

    //Set background color if not TRANSPARENT
    if (emptyStateBackgroundColor != Color.TRANSPARENT) {
      emptyStateRelativeLayout.setBackgroundColor(emptyStateBackgroundColor);
    }

    layoutParams =
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    layoutParams.addRule(CENTER_IN_PARENT);

    this.addView(emptyStateRelativeLayout, layoutParams);
  }

  private void setErrorView() {

    view = inflater.inflate(R.layout.progress_error_view, null);
    errorStateRelativeLayout = (RelativeLayout) view.findViewById(R.id.errorStateRelativeLayout);
    errorStateRelativeLayout.setTag(TAG_ERROR);

    errorStateImageView = (ImageView) view.findViewById(R.id.errorStateImageView);
    errorStateTitleTextView = (TextView) view.findViewById(R.id.errorStateTitleTextView);
    errorStateContentTextView = (TextView) view.findViewById(R.id.errorStateContentTextView);
    errorStateButton = (Button) view.findViewById(R.id.errorStateButton);

    //Set error currentState image width and height
    errorStateImageView.getLayoutParams().width = errorStateImageWidth;
    errorStateImageView.getLayoutParams().height = errorStateImageHeight;
    errorStateImageView.requestLayout();

    errorStateTitleTextView.setTextSize(errorStateTitleTextSize);
    errorStateContentTextView.setTextSize(errorStateContentTextSize);
    errorStateTitleTextView.setTextColor(errorStateTitleTextColor);
    errorStateContentTextView.setTextColor(errorStateContentTextColor);
    errorStateButton.setTextColor(errorStateButtonTextColor);

    //Set background color if not TRANSPARENT
    if (errorStateBackgroundColor != Color.TRANSPARENT) {
      errorStateRelativeLayout.setBackgroundColor(errorStateBackgroundColor);
    }

    layoutParams =
        new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    layoutParams.addRule(CENTER_IN_PARENT);

    this.addView(errorStateRelativeLayout, layoutParams);
  }

  private void setContentVisibility(boolean visible, List<Integer> skipIds) {
    for (View contentView : contentViews) {
      if (!skipIds.contains(contentView.getId())) {
        contentView.setVisibility(visible ? View.VISIBLE : View.GONE);
      }
    }
  }

  private void hideLoadingView() {
    if (loadingStateRelativeLayout != null) {
      loadingStateRelativeLayout.setVisibility(GONE);
    }
  }

  private void hideEmptyView() {
    if (emptyStateRelativeLayout != null) {
      emptyStateRelativeLayout.setVisibility(GONE);
    }
  }

  private void hideErrorView() {
    if (errorStateRelativeLayout != null) {
      errorStateRelativeLayout.setVisibility(GONE);
    }
  }
}