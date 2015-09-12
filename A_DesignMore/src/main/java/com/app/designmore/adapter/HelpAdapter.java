package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.activity.usercenter.HelpActivity;
import com.app.designmore.retrofit.entity.HelpEntity;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import java.util.List;
import rx.Observer;

/**
 * Created by Administrator on 2015/9/13.
 */
public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.ViewHolder>
    implements Observer<List<HelpEntity>> {

  private Callback callback;
  private Context context;
  private List<HelpEntity> items;

  public HelpAdapter(Context context) {
    this.context = context;
  }

  @Override public HelpAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.i_help_item, parent, false));
  }

  @Override public void onBindViewHolder(HelpAdapter.ViewHolder holder, int position) {

    holder.title.setText(items.get(position).getTitle());
    holder.content.setText(items.get(position).getContent());
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.help_item_title) TextView title;
    @Nullable @Bind(R.id.help_item_content) TextView content;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<HelpEntity> searchItemEntities) {
    this.items = searchItemEntities;
    HelpAdapter.this.notifyDataSetChanged();
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {
    void onError(Throwable error);
  }
}
