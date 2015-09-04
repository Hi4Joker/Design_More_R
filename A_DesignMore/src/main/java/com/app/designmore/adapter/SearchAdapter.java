package com.app.designmore.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.app.designmore.R;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import java.util.List;
import rx.Observer;

/**
 * Created by Administrator on 2015/9/4.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder>
    implements Observer<List<SearchItemEntity>> {

  private Callback callback;

  private Context context;

  private List<SearchItemEntity> items;

  public SearchAdapter(Context context) {
    this.context = context;
  }

  @Override public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(context).inflate(R.layout.search_item, parent, false));
  }

  @Override public void onBindViewHolder(SearchAdapter.ViewHolder holder, int position) {

    holder.button.setTag(position);
    holder.button.setText(items.get(position).getText());
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.search_item_btn) Button button;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.search_item_btn) void onButtonClick(Button button) {

      if (callback != null) {
        int pos = (int) button.getTag();
        callback.onItemClick(pos);
      }
    }
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {

    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<SearchItemEntity> searchItemEntities) {
    this.items = searchItemEntities;
    SearchAdapter.this.notifyDataSetChanged();
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    /*条目点击事件*/
    void onItemClick(int position);

    void onError(Throwable error);
  }
}
