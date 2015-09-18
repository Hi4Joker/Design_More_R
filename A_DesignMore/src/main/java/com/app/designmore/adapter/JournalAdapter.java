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
import com.app.designmore.retrofit.entity.JournalEntity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.util.List;
import rx.Observer;

/**
 * Created by Joker on 2015/9/15.
 */
public class JournalAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements Observer<List<JournalEntity>> {

  private static final String TAG = JournalAdapter.class.getCanonicalName();
  public static final int TYPE_CONTENT = 0;
  public static final int TYPE_TEXT = 1;

  private Callback callback;
  private Context context;

  /*数据*/
  private List<JournalEntity> items;

  public JournalAdapter(Context context) {
    this.context = context;
  }

  @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    if (viewType == TYPE_CONTENT) {
      return new ViewHolder(
          LayoutInflater.from(context).inflate(R.layout.i_journal_item, parent, false));
    } else {
      return new TextViewHolder(
          LayoutInflater.from(context).inflate(R.layout.i_journal_text_item, parent, false));
    }
  }

  @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    //Log.e(TAG, "position: " + position);
    int viewType = this.getItemViewType(position);
    if (viewType == TYPE_CONTENT) {
      if (position > 1) {
        this.bindValue((ViewHolder) holder, items.get(position - 1));
      } else {
        this.bindValue((ViewHolder) holder, items.get(position));
      }
    }
  }

  private void bindValue(ViewHolder holder, JournalEntity journalEntity) {

    holder.rootView.setTag(journalEntity);
    holder.title.setText(journalEntity.getJournalTitle());
    holder.content.setText(journalEntity.getJournalContent());

    Glide.with(context)
        .load(journalEntity.getJournalThumbUrl())
        .placeholder(R.drawable.ic_default_1080)
        .error(R.drawable.ic_default_1080)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(holder.thumbIv);
  }

  @Override public int getItemViewType(int position) {
    if (position == 1) {//往期回顾，四个大字
      return TYPE_TEXT;
    } else {//普通item
      return TYPE_CONTENT;
    }
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() + 1 : 0;
  }

  /**
   * 更新整张列表
   */
  public void updateItems(List<JournalEntity> journalEntities) {
    this.items = journalEntities;
    JournalAdapter.this.notifyDataSetChanged();
  }

  @Override public void onCompleted() {
    /*never invoked*/
  }

  @Override public void onError(Throwable e) {
    if (callback != null) callback.onError(e);
  }

  @Override public void onNext(List<JournalEntity> journalEntities) {

    if (journalEntities != null && journalEntities.size() == 0) {
      if (callback != null) callback.onNoData();
    } else {

      this.items.addAll(journalEntities);
      JournalAdapter.this.notifyItemInserted(items.size() - journalEntities.size() + 1);
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.journal_item_root_view) RelativeLayout rootView;
    @Nullable @Bind(R.id.journal_item_thumb_iv) ImageView thumbIv;
    @Nullable @Bind(R.id.journal_item_title_tv) TextView title;
    @Nullable @Bind(R.id.journal_item_content_tv) TextView content;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.journal_item_root_view) void onItemClick(RelativeLayout rootView) {
      JournalEntity journalEntity = (JournalEntity) rootView.getTag();
      if (callback != null) {
        callback.onItemClick(journalEntity);
      }
    }
  }

  public class TextViewHolder extends RecyclerView.ViewHolder {
    public TextViewHolder(View itemView) {
      super(itemView);
    }
  }

  public interface Callback {

    /*条目点击事件 */
    void onItemClick(JournalEntity entity);

    /*无更多数据*/
    void onNoData();

    void onError(Throwable error);
  }
}
