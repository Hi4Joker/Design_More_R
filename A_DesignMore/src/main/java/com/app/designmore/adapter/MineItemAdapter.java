package com.app.designmore.adapter;

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
import com.app.designmore.retrofit.entity.MineItemEntity;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.trello.rxlifecycle.ActivityEvent;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import java.util.List;
import java.util.concurrent.TimeUnit;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

/**
 * Created by Joker on 2015/9/13.
 */
public class MineItemAdapter extends RecyclerView.Adapter<MineItemAdapter.ViewHolder> {

  private final PublishSubject<View> publishSubject;
  private Callback callback;
  private RxAppCompatActivity rxAppCompatActivity;
  private List<MineItemEntity> items;

  public MineItemAdapter(RxAppCompatActivity rxAppCompatActivity, List<MineItemEntity> items) {
    this.rxAppCompatActivity = rxAppCompatActivity;
    this.items = items;
    this.publishSubject = PublishSubject.create();

    publishSubject.compose(rxAppCompatActivity.<View>bindUntilEvent(ActivityEvent.DESTROY))
        .throttleFirst(Constants.MILLISECONDS_800, TimeUnit.MILLISECONDS,
            AndroidSchedulers.mainThread())
        .forEach(new Action1<View>() {
          @Override public void call(View view) {
            if (callback != null) callback.onItemClick((Integer) view.getTag(), view);
          }
        });
  }

  @Override public MineItemAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

    return new ViewHolder(
        LayoutInflater.from(rxAppCompatActivity).inflate(R.layout.i_mine_item, parent, false));
  }

  @Override
  public void onBindViewHolder(final MineItemAdapter.ViewHolder holder, final int position) {

    holder.rootView.setTag(position);

    holder.imageView.setImageDrawable(
        rxAppCompatActivity.getResources().getDrawable(items.get(position).getDrawable()));
    holder.title.setText(items.get(position).getTitle());
  }

  @Override public int getItemCount() {
    return (this.items != null) ? this.items.size() : 0;
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    @Nullable @Bind(R.id.mine_item_ll) LinearLayout rootView;
    @Nullable @Bind(R.id.mine_item_title) TextView title;
    @Nullable @Bind(R.id.mine_item_icon) ImageView imageView;

    public ViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(ViewHolder.this, itemView);
    }

    @Nullable @OnClick(R.id.mine_item_ll) void onItemClick(final LinearLayout linearLayout) {

      publishSubject.onNext(linearLayout);
    }
  }

  public void setCallback(Callback callback) {
    this.callback = callback;
  }

  public interface Callback {

    void onItemClick(int position, View itemView);
  }
}
