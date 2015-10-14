package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.manager.DividerDecoration;
import com.app.designmore.manager.WrappingLinearLayoutManager;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements Observer<List<ProductEntity>> {

  private static final int ITEM_VIEW_HEADER = 0;
  private static final int ITEM_VIEW_ITEM = 1;

  private Context context;
  private List<ProductEntity> productEntities;
  private List<FashionEntity> fashionEntities;
  private Callback callback;

  private LayoutInflater layoutInflater;

  public HomeProductAdapter(Context context) {
    this.context = context;
    this.layoutInflater = LayoutInflater.from(context);
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    if (viewType == ITEM_VIEW_HEADER) {
      return new HeaderHolder(layoutInflater.inflate(R.layout.i_home_header_item, parent, false));
    } else {
      return new ItemHolder(layoutInflater.inflate(R.layout.i_product_item, parent, false));
    }
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    int viewType = this.getItemViewType(position);
    if (viewType == ITEM_VIEW_HEADER) {
      HomeProductAdapter.this.bindValue((HeaderHolder) holder);
    } else {
      HomeProductAdapter.this.bindValue((ItemHolder) holder, position - 1);
    }
  }

  private void bindValue(HeaderHolder holder) {

    FashionEntity fashionEntity1 = fashionEntities.get(0);
    FashionEntity fashionEntity2 = fashionEntities.get(1);

    holder.relativeLayout1.setTag(fashionEntity1.getGoodId());
    holder.textView1.setText(
        fashionEntity1.getGoodName() + "\n" + fashionEntity1.getGoodDiscount());

    holder.relativeLayout2.setTag(fashionEntity2.getGoodId());
    holder.textView2.setText(
        fashionEntity2.getGoodName() + "\n" + fashionEntity2.getGoodDiscount());

    Glide.with(context)
        .load(Constants.THUMB_URL + fashionEntity1.getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.imageView1);

    Glide.with(context)
        .load(Constants.THUMB_URL + fashionEntity2.getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.imageView2);
  }

  private void bindValue(ItemHolder holder, int position) {

    holder.rootView.setTag(productEntities.get(position).getGoodId());
    holder.desTv.setText(productEntities.get(position).getGoodName());
    holder.priceTv.setText(productEntities.get(position).getGoodPrice());

    Glide.with(context)
        .load(Constants.THUMB_URL + productEntities.get(position).getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_300_icon)
        .error(R.drawable.ic_default_300_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.thumbIv);
  }

  @Override public int getItemCount() {
    return (this.productEntities != null) ? this.productEntities.size() + 1 : 0;
  }

  @Override public int getItemViewType(int position) {
    return position == 0 ? ITEM_VIEW_HEADER : ITEM_VIEW_ITEM;
  }

  public void updateItems(List<FashionEntity> fashionEntities,
      List<ProductEntity> productEntities) {

    this.fashionEntities = fashionEntities;
    this.productEntities = productEntities;

    HomeProductAdapter.this.notifyDataSetChanged();
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<ProductEntity> productEntities) {
    if (productEntities != null && productEntities.size() == 0) {
      if (callback != null) callback.onNoData();
    } else {
      this.productEntities.addAll(productEntities);

      HomeProductAdapter.this.notifyDataSetChanged();
      //HomeProductAdapter.this.notifyItemInserted(productEntities.size() - productEntities.size() - 1);
    }
  }

  public class HeaderHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.home_header_fashion_rl_1) RelativeLayout relativeLayout1;
    @Nullable @Bind(R.id.home_header_fashion_thumb_iv_1) ImageView imageView1;
    @Nullable @Bind(R.id.home_header_fashion_title_tv_1) TextView textView1;

    @Nullable @Bind(R.id.home_header_fashion_rl_2) RelativeLayout relativeLayout2;
    @Nullable @Bind(R.id.home_header_fashion_thumb_iv_2) ImageView imageView2;
    @Nullable @Bind(R.id.home_header_fashion_title_tv_2) TextView textView2;

    public HeaderHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(HeaderHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.home_header_fashion_rl_1) void onItemClick(
        RelativeLayout relativeLayout) {

      String goodId = (String) relativeLayout.getTag();
      if (callback != null) callback.onDiscountItemClick(goodId);
    }
  }

  public class ItemHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.price_item_root_view) LinearLayout rootView;
    @Nullable @Bind(R.id.product_item_thumb_iv) ImageView thumbIv;
    @Nullable @Bind(R.id.product_item_des_tv) TextView desTv;
    @Nullable @Bind(R.id.product_item_price_tv) TextView priceTv;

    public ItemHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ItemHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.price_item_root_view) void onItemClick(LinearLayout linearLayout) {

      String productId = (String) linearLayout.getTag();

      if (callback != null) {
        callback.onProductItemClick(productId);
      }
    }
  }

  public boolean isHeader(int position) {
    return position == 0;
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件 */
    void onDiscountItemClick(String goodId);

    void onProductItemClick(String productId);

    /*无更多数据*/
    void onNoData();

    void onError(Throwable error);
  }
}
