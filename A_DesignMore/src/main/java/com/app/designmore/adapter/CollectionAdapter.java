package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Administrator on 2015/9/4.
 */
public class CollectionAdapter extends RecyclerView.Adapter<CollectionAdapter.ViewHolder>
    implements Observer<Integer> {

  private Context context;
  private Callback callback;
  /*数据*/
  private List<CollectionEntity> items;

  public CollectionAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_collection_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.itemRootView.setTag(items.get(position));
    holder.goodMoreBtn.setTag(items.get(position));

    /*绑定数据*/
    CollectionAdapter.this.bindToValue(holder, items.get(position));
  }

  private void bindToValue(ViewHolder holder, CollectionEntity collectionEntity) {

    Glide.with(context)
        .load(Constants.THUMB_URL + collectionEntity.getGoodThumb())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_256_icon)
        .error(R.drawable.ic_default_256_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.goodIv);

    holder.goodNameTv.setText(collectionEntity.getGoodName());
    holder.goodPriceTv.setText("¥ " + collectionEntity.getGoodPrice());
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  @Override public void onCompleted() {
  /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(Integer deletePosition) {

    this.items.remove((int) deletePosition);
    CollectionAdapter.this.notifyItemRemoved(deletePosition);
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<CollectionEntity> collectionEntities) {
    this.items = collectionEntities;
    CollectionAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.collection_item_root_view) RelativeLayout itemRootView;
    @Nullable @Bind(R.id.collection_item_good_iv) ImageView goodIv;
    @Nullable @Bind(R.id.collection_item_good_name_tv) TextView goodNameTv;
    @Nullable @Bind(R.id.collection_item_good_price_tv) TextView goodPriceTv;
    @Nullable @Bind(R.id.collection_item_good_more_btn) ImageButton goodMoreBtn;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.collection_item_root_view) void onItemClick(RelativeLayout rootView) {
      CollectionEntity entity = (CollectionEntity) rootView.getTag();
      if (callback != null) {
        callback.onItemClick(entity);
      }
    }

    @Nullable @OnClick(R.id.collection_item_good_more_btn) void onDeleteClick(
        ImageButton imageButton) {
      CollectionEntity entity = (CollectionEntity) imageButton.getTag();
      if (callback != null) {
        callback.onDeleteClick(entity);
      }
    }

    @Nullable @OnClick(R.id.collection_item_good_share_btn) void onShareClick(
        ImageButton imageButton) {
      CollectionEntity entity = (CollectionEntity) imageButton.getTag();
      if (callback != null) {
        callback.onShareClick(entity);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*点击条目*/
    void onItemClick(CollectionEntity entity);

    /*点击删除*/
    void onDeleteClick(CollectionEntity entity);

    /*点击分享*/
    void onShareClick(CollectionEntity entity);

    void onError(Throwable error);
  }
}
