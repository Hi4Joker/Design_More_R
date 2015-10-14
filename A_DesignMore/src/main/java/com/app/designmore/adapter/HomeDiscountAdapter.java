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
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Administrator on 2015/9/24.
 */
public class HomeDiscountAdapter extends RecyclerView.Adapter<HomeDiscountAdapter.ViewHolder> {

  private static final String TAG = HomeDiscountAdapter.class.getCanonicalName();
  private Callback callback;
  private Context context;

  /*数据*/
  private List<FashionEntity> items;

  public HomeDiscountAdapter(Context context, List<FashionEntity> fashionEntities) {
    this.context = context;
    this.items = fashionEntities;
  }

  @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_fashion_item, parent, false));
  }

  @Override public void onBindViewHolder(ViewHolder holder, int position) {

    holder.rootView.setTag(items.get(position).getGoodId());
    holder.titleTv.setText(items.get(position).getGoodName() + "\n" +
        items.get(position).getGoodDiscount());

    Glide.with(context)
        .load(Constants.THUMB_URL + items.get(position).getGoodThumbUrl())
        .centerCrop()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.thumbIv);
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public void updateItems(List<FashionEntity> items) {
    this.items = items;
    HomeDiscountAdapter.this.notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.fashion_item_root_view) RelativeLayout rootView;
    @Nullable @Bind(R.id.fashion_item_thumb_iv) ImageView thumbIv;
    @Nullable @Bind(R.id.fashion_item_tv) TextView titleTv;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.fashion_item_root_view) void onItemClick(RelativeLayout rootView) {

      String fashionEntity = (String) rootView.getTag();
      if (callback != null) {
        callback.onDiscountItemClick(fashionEntity);
      }
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件 */
    void onDiscountItemClick(String goodId);
  }
}
