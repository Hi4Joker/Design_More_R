package com.app.designmore.view.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.adapter.AccountAdapter;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.app.designmore.retrofit.response.DetailResponse;
import com.app.designmore.utils.DensityUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Joker on 2015/9/20.
 */
public class CustomAccountDialog extends Dialog implements AccountAdapter.Callback {

  public static final String PRICE = "PRICE";
  public static final String DES = "DES";
  public static final String ATTRS = "ATTRS";
  public static final String MAX = "MAX";

  @Nullable @Bind(R.id.custom_account_thumb_iv) ImageView thumbIv;
  @Nullable @Bind(R.id.custom_account_price_tv) TextView priceTv;
  @Nullable @Bind(R.id.custom_account_discount_tv) TextView discountTv;
  @Nullable @Bind(R.id.custom_account_layout_rl) RecyclerView recyclerView;
  @Nullable @Bind(R.id.custom_account_count_tv) TextView countTv;
  @Nullable @Bind(R.id.custom_account_btn) Button accountBtn;

  private String price;
  private String des;
  private String max;
  private List<DetailResponse.Detail.ProductAttr> productAttrs;

  private Activity activity;
  private final Callback callback;
  private int count = 1;

  private List<ProductAttrEntity> productEntities;
  private ProductAttrEntity currentProductAttrEntity;
  private int backgroundColor;

  public CustomAccountDialog(Activity activity, Map map, Callback callback) {
    super(activity);
    getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    getWindow().setGravity(Gravity.BOTTOM);
    getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
    getWindow().setWindowAnimations(R.style.AnimBottom);
    View rootView = getLayoutInflater().inflate(R.layout.custom_account_layout, null);
    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(DensityUtil.getScreenWidth(activity),
        ViewGroup.LayoutParams.MATCH_PARENT);
    super.setContentView(rootView, params);

    this.activity = activity;
    this.callback = callback;
    CustomAccountDialog.this.setCancelable(false);
    CustomAccountDialog.this.setCanceledOnTouchOutside(false);

    this.price = (String) map.get(PRICE);
    this.des = (String) map.get(DES);
    this.max = (String) map.get(MAX);
    this.productAttrs = (List<DetailResponse.Detail.ProductAttr>) map.get(ATTRS);

    this.productEntities = new ArrayList<>(productAttrs.size());
    for (DetailResponse.Detail.ProductAttr productAttr : productAttrs) {
      productEntities.add(
          new ProductAttrEntity(productAttr.attrId, productAttr.attrThumbUrl, false));
    }
    this.backgroundColor = activity.getResources().getColor(R.color.design_more_red);
    this.currentProductAttrEntity = productEntities.get(0);
    this.currentProductAttrEntity.setIsChecked(true);
  }

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();
    ButterKnife.bind(CustomAccountDialog.this);
    CustomAccountDialog.this.bindValue();
  }

  private void bindValue() {

    Glide.with(activity)
        .load(currentProductAttrEntity.getUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(thumbIv);

    this.priceTv.setText(price);
    this.discountTv.setText(des);
    this.countTv.setText(count + "");

    CustomAccountDialog.this.setupAdapter();
  }

  private void setupAdapter() {

    final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
    linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    AccountAdapter accountAdapter = new AccountAdapter(activity, productEntities);
    accountAdapter.setCallback(CustomAccountDialog.this);

    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(accountAdapter);
    recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
  }

  @Nullable @OnClick(R.id.custom_account_count_add_btn) void onAddCountClick() {
    if (++count <= Integer.parseInt(max)) {
      this.countTv.setText(count + "");
    } else {
      count--;
      Toast.makeText(activity, "超过最大购买数量", Toast.LENGTH_LONG).show();
    }
  }

  @Nullable @OnClick(R.id.custom_account_count_subtract_btn) void onSubtractCountClick() {
    if (--count > 0) {
      this.countTv.setText(count + "");
    } else {
      count++;
    }
  }

  @Nullable @OnClick(R.id.custom_account_cancel_btn) void onCancelClick() {
    CustomAccountDialog.this.dismiss();
    if (callback != null) callback.onDialogDismiss();
  }

  @Nullable @OnClick(R.id.custom_account_btn) void onAccountClick() {
    CustomAccountDialog.this.dismiss();
    if (callback != null) {
      callback.onAccountClick(currentProductAttrEntity,count);
      callback.onDialogDismiss();
    }
  }

  @Override public void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    ButterKnife.unbind(CustomAccountDialog.this);
  }

  @Override public void onItemClick(ProductAttrEntity productAttrEntity) {

    if (this.currentProductAttrEntity != productAttrEntity) {

      final LinearLayout oldLayout = (LinearLayout) recyclerView.getLayoutManager()
          .findViewByPosition(productEntities.indexOf(currentProductAttrEntity))
          .findViewById(R.id.account_root_view);
      oldLayout.setBackgroundColor(Color.TRANSPARENT);

      final LinearLayout newLayout = (LinearLayout) recyclerView.getLayoutManager()
          .findViewByPosition(productEntities.indexOf(productAttrEntity))
          .findViewById(R.id.account_root_view);
      newLayout.setBackgroundColor(backgroundColor);

      this.currentProductAttrEntity.setIsChecked(false);
      this.currentProductAttrEntity = productAttrEntity;
    }
  }

  public interface Callback {
    /*点击确定回调*/
    void onAccountClick(ProductAttrEntity productAttrEntity, int count);

    /*dismiss dialog*/
    void onDialogDismiss();
  }
}
