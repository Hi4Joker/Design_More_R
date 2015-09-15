package com.app.designmore.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.utils.DensityUtil;

/**
 * Created by Joker on 2015/9/14.
 */
public class CustomShareDialog extends Dialog {

  private Activity activity;
  private Callback callback;

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
    if (callback != null) callback.onWeiboClick();
    CustomShareDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.custom_share_layout_wechat_btn) void onWechatClick() {
    if (callback != null) callback.onWechatClick();
    CustomShareDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.custom_share_layout_cancle_btn) void onCancelClick() {
    CustomShareDialog.this.dismiss();
  }

  public interface Callback {

    void onWeiboClick();

    void onWechatClick();
  }
}
