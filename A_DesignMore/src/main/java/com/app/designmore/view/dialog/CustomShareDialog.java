package com.app.designmore.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/14.
 */
public class CustomShareDialog extends Dialog {

  private static final String TAG = CustomShareDialog.class.getCanonicalName();

  @Nullable @Bind(R.id.custom_share_layout_ll_) LinearLayout _linearLayout_;
  @Nullable @Bind(R.id.custom_share_layout_content_et) EditText contentEt;

  @Nullable @Bind(R.id.custom_share_layout_ll) LinearLayout linearLayout;

  private Callback callback;
  private Activity activity;

  private Type currentType;

  private enum Type {
    weibo,
    wechat

  }

  public CustomShareDialog(Activity activity, Callback callback) {
    super(activity);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.BOTTOM);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimBottom);
    View rootView = getLayoutInflater().inflate(R.layout.custom_share_layout, null);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(DensityUtil.getScreenWidth(activity),
        ViewGroup.LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.activity = activity;
    this.callback = callback;
    CustomShareDialog.this.setCancelable(false);
    CustomShareDialog.this.setCanceledOnTouchOutside(false);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(CustomShareDialog.this);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    ButterKnife.unbind(CustomShareDialog.this);
  }

  @Nullable @OnClick(R.id.custom_share_layout_weibo_btn) void onWeiboClick() {
    this.currentType = Type.weibo;
    CustomShareDialog.this.startTranslate();
  }

  @Nullable @OnClick(R.id.custom_share_layout_wechat_btn) void onWechatClick() {
    this.currentType = Type.wechat;
    CustomShareDialog.this.startTranslate();
  }

  @Nullable @OnClick(R.id.custom_share_layout_send_btn) void onSendClick() {
    if (callback != null) {
      if (this.currentType == Type.weibo) {
        callback.onWeiboClick(contentEt.getText().toString());
      } else {
        callback.onWechatClick(contentEt.getText().toString());
      }
      CustomShareDialog.this.dismiss();
    }
  }

  @Nullable @OnClick(R.id.custom_share_layout_cancel_btn) void onCancelClick() {
    CustomShareDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.custom_share_layout_cancel_btn_) void onBackClick() {
    CustomShareDialog.this.resumeTranslate();
  }

  private void startTranslate() {

    ViewCompat.setTranslationY(_linearLayout_, DensityUtil.hideFromBottom(_linearLayout_));

    ViewCompat.animate(linearLayout)
        .translationY(DensityUtil.hideFromBottom(linearLayout))
        .setDuration(Constants.MILLISECONDS_300)
        .setInterpolator(new FastOutLinearInInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {

            ViewCompat.animate(_linearLayout_)
                .translationY(0.0f)
                .setDuration(Constants.MILLISECONDS_300)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(null);
          }
        });
  }

  private void resumeTranslate() {

    ViewCompat.animate(_linearLayout_)
        .translationY(DensityUtil.hideFromBottom(_linearLayout_))
        .setDuration(Constants.MILLISECONDS_300)
        .setInterpolator(new FastOutLinearInInterpolator())
        .setListener(new ViewPropertyAnimatorListenerAdapter() {
          @Override public void onAnimationEnd(View view) {

            ViewCompat.animate(linearLayout)
                .translationY(0.0F)
                .setDuration(Constants.MILLISECONDS_300)
                .setInterpolator(new FastOutSlowInInterpolator())
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                  @Override public void onAnimationEnd(View view) {
                    ViewCompat.setTranslationY(_linearLayout_, 0.0f);
                  }
                });
          }
        });
  }

  public interface Callback {

    void onWeiboClick(String content);

    void onWechatClick(String content);
  }
}
