package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import butterknife.Bind;
import butterknife.ButterKnife;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.HelpEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;

/**
 * Created by Joker on 2015/10/8.
 */
public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.ViewHolder> {

  private Context context;
  private List<String> items;

  public DetailAdapter(Context context, List<String> items) {
    this.context = context;
    this.items = items;
  }

  @Override public DetailAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_detail_item, parent, false));
  }

  @Override public void onBindViewHolder(DetailAdapter.ViewHolder holder, int position) {

    Glide.with(context)
        .load(Constants.THUMB_URL + items.get(position))
        .fitCenter()
        .crossFade()
        .placeholder(R.drawable.ic_default_1080_icon)
        .error(R.drawable.ic_default_1080_icon)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(holder.productImage);
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.detail_item_iv) ImageView productImage;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }
  }
}
