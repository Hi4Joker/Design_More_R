package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/24.
 */
public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ViewHolder>
    implements Observer<List<ProductEntity>> {

  private Context context;
  private List<ProductEntity> items;
  private Callback callback;

  public HomeProductAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_product_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.rootView.setTag(items.get(position).getGoodId());
    holder.desTv.setText(items.get(position).getGoodDes());
    holder.priceTv.setText(items.get(position).getGoodPrice());

    Glide.with(context)
        .load(Constants.THUMB_URL + items.get(position).getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_300_icon)
        .error(R.drawable.ic_default_300_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.thumbIv);
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public void updateItems(List<ProductEntity> items) {
    this.items = items;
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
      this.items.addAll(productEntities);

      HomeProductAdapter.this.notifyDataSetChanged();
      //HomeProductAdapter.this.notifyItemInserted(items.size() - productEntities.size() - 1);
    }
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.price_item_root_view) LinearLayout rootView;
    @Nullable @Bind(R.id.product_item_thumb_iv) ImageView thumbIv;
    @Nullable @Bind(R.id.product_item_des_tv) TextView desTv;
    @Nullable @Bind(R.id.product_item_price_tv) TextView priceTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.price_item_root_view) void onItemClick(LinearLayout linearLayout) {

      String productId = (String) linearLayout.getTag();

      if (callback != null) {
        callback.onProductItemClick(productId);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件 */
    void onProductItemClick(String productId);

    /*无更多数据*/
    void onNoData();

    void onError(Throwable error);
  }
}
