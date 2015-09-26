package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/5.
 */
public class CommitOrderAdapter extends RecyclerView.Adapter<CommitOrderAdapter.ViewHolder> {

  private Context context;
  private List<TrolleyEntity> items;

  public CommitOrderAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_commit_order_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    CommitOrderAdapter.this.bindToValue(holder, items.get(position));
  }

  private void bindToValue(ViewHolder holder, TrolleyEntity trolleyEntity) {

    Glide.with(context)
        .load(trolleyEntity.getGoodThumb())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_256_icon)
        .error(R.drawable.ic_default_256_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.goodIv);

    holder.goodNameTv.setText(trolleyEntity.getGoodName());
    holder.goodAttrTv.setText("颜色分类：" + trolleyEntity.getGoodAttrValue());
    holder.goodCountTv.setText("数量：X" + trolleyEntity.getGoodCount());
    holder.goodPriceTv.setText("￥" + trolleyEntity.getGoodPrice());
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  /**
   * 刷新整个列表
   */
  public void updateItems(List<TrolleyEntity> trolleyEntities) {

    this.items = trolleyEntities;
    CommitOrderAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.commit_order_item_good_iv) ImageView goodIv;
    @Nullable @Bind(R.id.commit_order_item_good_name_tv) TextView goodNameTv;
    @Nullable @Bind(R.id.commit_order_item_good_attr_tv) TextView goodAttrTv;
    @Nullable @Bind(R.id.commit_order_item_good_count_tv) TextView goodCountTv;
    @Nullable @Bind(R.id.commit_order_item_price_tv) TextView goodPriceTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }
  }
}
