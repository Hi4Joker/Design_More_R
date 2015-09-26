package com.app.designmore.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.adapter.ProductAttrAdapter;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.app.designmore.utils.DensityUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import java.util.Map;

/**
 * Created by Joker on 2015/9/20.
 */
public class CustomTrolleyDialog extends Dialog
    implements ProductAttrAdapter.Callback, DialogInterface.OnDismissListener {

  public static final String PRICE = "PRICE";
  public static final String VALUE = "VALUE";
  public static final String ATTRS = "ATTRS";

  @Nullable @Bind(R.id.custom_trolley_thumb_iv) ImageView thumbIv;
  @Nullable @Bind(R.id.custom_trolley_price_tv) TextView priceTv;
  @Nullable @Bind(R.id.custom_trolley_attr_tv) TextView valueTv;
  @Nullable @Bind(R.id.custom_trolley_layout_rl) RecyclerView recyclerView;

  private String price;
  private String value;
  private List<ProductAttrEntity> productAttrEntities;
  private ProductAttrEntity currentProductAttrEntity;

  private Activity activity;
  private final Callback callback;
  private int backgroundColor;

  public CustomTrolleyDialog(Activity activity, Map map, Callback callback) {
    super(activity);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.BOTTOM);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimBottom);
    View rootView = getLayoutInflater().inflate(R.layout.custom_trolley_layout, null);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(DensityUtil.getScreenWidth(activity),
        ViewGroup.LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.activity = activity;
    this.callback = callback;
    //CustomTrolleyDialog.this.setCancelable(false);
    CustomTrolleyDialog.this.setCanceledOnTouchOutside(true);

    this.price = (String) map.get(PRICE);
    this.value = (String) map.get(VALUE);
    this.productAttrEntities = (List<ProductAttrEntity>) map.get(ATTRS);

    this.backgroundColor = activity.getResources().getColor(R.color.design_more_red);
    this.currentProductAttrEntity = productAttrEntities.get(0);
    this.currentProductAttrEntity.setIsChecked(true);
  }

  private void bindValue() {

    Glide.with(activity)
        .load(currentProductAttrEntity.getAttrThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(thumbIv);

    this.priceTv.setText(price);
    this.valueTv.setText(value);

    CustomTrolleyDialog.this.setupAdapter();
  }

  private void setupAdapter() {

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    ProductAttrAdapter accountAdapter = new ProductAttrAdapter(activity, productAttrEntities);
    accountAdapter.setCallback(CustomTrolleyDialog.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(accountAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
  }

  @Nullable @OnClick(R.id.custom_trolley_cancel_btn) void onCancelClick() {
    CustomTrolleyDialog.this.dismiss();
  }

  @Nullable @OnClick(R.id.custom_trolley_confirm_btn) void onAccountClick() {
    CustomTrolleyDialog.this.dismiss();
    if (callback != null) {
      callback.onConfirmClick(currentProductAttrEntity);
    }
  }

  @Override public void onItemClick(ProductAttrEntity productAttrEntity) {

    if (this.currentProductAttrEntity != productAttrEntity) {

      CustomTrolleyDialog.this.priceTv.setText(productAttrEntity.getAttrPrice());
      CustomTrolleyDialog.this.valueTv.setText(productAttrEntity.getAttrValue());
      Glide.with(activity)
          .load(productAttrEntity.getAttrThumbUrl())
          .centerCrop()
          .crossFade()
          .placeholder(R.drawable.ic_default_1080_icon)
          .error(R.drawable.ic_default_1080_icon)
          .diskCacheStrategy(DiskCacheStrategy.RESULT)
          .into(thumbIv);

      final LinearLayout oldLayout = (LinearLayout) recyclerView.getLayoutManager()
          .findViewByPosition(productAttrEntities.indexOf(currentProductAttrEntity))
          .findViewById(R.id.product_attr_item_root_view);
      oldLayout.setBackgroundColor(Color.TRANSPARENT);

      final LinearLayout newLayout = (LinearLayout) recyclerView.getLayoutManager()
          .findViewByPosition(productAttrEntities.indexOf(productAttrEntity))
          .findViewById(R.id.product_attr_item_root_view);
      newLayout.setBackgroundColor(backgroundColor);

      this.currentProductAttrEntity.setIsChecked(false);
      this.currentProductAttrEntity = productAttrEntity;
    }
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(CustomTrolleyDialog.this);
    CustomTrolleyDialog.this.bindValue();
    CustomTrolleyDialog.this.setOnDismissListener(CustomTrolleyDialog.this);
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    ButterKnife.unbind(CustomTrolleyDialog.this);
  }

  @Override public void onBackPressed() {
    /*do nothing*/
  }

  @Override public void onDismiss(DialogInterface dialog) {
    if (callback != null) callback.onDialogDismiss();
  }

  public interface Callback {
    /*点击确定回调*/
    void onConfirmClick(ProductAttrEntity productAttrEntity);

    /*dismiss dialog*/
    void onDialogDismiss();
  }
}
