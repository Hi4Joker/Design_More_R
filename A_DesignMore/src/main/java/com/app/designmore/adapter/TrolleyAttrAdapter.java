package com.app.designmore.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Administrator on 2015/9/24.
 */
public class TrolleyAttrAdapter extends RecyclerView.Adapter<TrolleyAttrAdapter.ViewHolder> {

  private Context context;
  private List<ProductAttrEntity> items;
  private Callback callback;

  private int backgroundColor;

  public TrolleyAttrAdapter(Context context, List<ProductAttrEntity> productAttrs) {
    this.context = context;
    this.items = productAttrs;
    this.backgroundColor = context.getResources().getColor(R.color.design_more_red);
  }

  @Override public TrolleyAttrAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_product_attr_item, parent, false));
  }

  @Override public void onBindViewHolder(TrolleyAttrAdapter.ViewHolder holder, int position) {

    Glide.with(context)
        .load(items.get(position).getAttrThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_256_icon)
        .error(R.drawable.ic_default_256_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.imageView);

    holder.imageView.setTag(items.get(position));
    if (items.get(position).isChecked()) {
      holder.rootView.setBackgroundColor(backgroundColor);
    } else {
      holder.rootView.setBackgroundColor(Color.TRANSPARENT);
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.product_attr_item_root_view) LinearLayout rootView;
    @Nullable @Bind(R.id.product_attr_item_iv) ImageView imageView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.product_attr_item_iv) void onItemClick(ImageView imageView) {

      ProductAttrEntity productAttrEntity = (ProductAttrEntity) imageView.getTag();
      if (callback != null && !productAttrEntity.isChecked()) {
        productAttrEntity.setIsChecked(true);
        callback.onItemClick(productAttrEntity);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件 */
    void onItemClick(ProductAttrEntity productAttr);
  }
}
