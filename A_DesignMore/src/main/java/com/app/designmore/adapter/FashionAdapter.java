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
import com.app.designmore.retrofit.entity.FashionEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Administrator on 2015/9/18.
 */
public class FashionAdapter extends RecyclerView.Adapter<FashionAdapter.ViewHolder>
    implements Observer<List<FashionEntity>> {

  private static final String TAG = FashionAdapter.class.getCanonicalName();
  private Callback callback;
  private Context context;

  /*数据*/
  private List<FashionEntity> items;

  public FashionAdapter(Context context) {
    this.context = context;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_fashion_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.rootView.setTag(items.get(position));
    holder.titleTv.setText(items.get(position).getGoodName());
    holder.contentTv.setText(items.get(position).getDiscount());

    Glide.with(context)
        .load(items.get(position).getGoodThumbUrl())
        .placeholder(R.drawable.ic_default_1080)
        .error(R.drawable.ic_default_1080)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(holder.thumbIv);
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

  @Override public void onNext(List<FashionEntity> fashionEntities) {

    if (fashionEntities != null && fashionEntities.size() == 0) {
      if (callback != null) callback.onNoData();
    } else {
      this.items.addAll(fashionEntities);
      FashionAdapter.this.notifyItemInserted(items.size() - fashionEntities.size());
    }
  }

  public void updateItems(List<FashionEntity> items) {
    this.items = items;
    FashionAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.fashion_item_root_view) RelativeLayout rootView;
    @Nullable @Bind(R.id.fashion_item_thumb_iv) ImageView thumbIv;
    @Nullable @Bind(R.id.fashion_item_title_tv) TextView titleTv;
    @Nullable @Bind(R.id.fashion_item_content_tv) TextView contentTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.fashion_item_root_view) void onItemClick(RelativeLayout rootView) {

      FashionEntity fashionEntity = (FashionEntity) rootView.getTag();
      if (callback != null) {
        callback.onItemClick(fashionEntity);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件 */
    void onItemClick(FashionEntity entity);

    /*无更多数据*/
    void onNoData();

    void onError(Throwable error);
  }
}
