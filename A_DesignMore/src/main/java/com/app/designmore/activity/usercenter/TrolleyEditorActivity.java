package com.app.designmore.activity.usercenter;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.OnClick;
import com.app.designmore.Constants;
import com.app.designmore.R;
import com.app.designmore.activity.BaseActivity;
import com.app.designmore.adapter.TrolleyEditorAdapter;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.helper.DBHelper;
import com.app.designmore.manager.DialogManager;
import com.app.designmore.retrofit.TrolleyRetrofit;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.app.designmore.retrofit.entity.SimpleTrolleyEntity;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.revealLib.animation.SupportAnimator;
import com.app.designmore.revealLib.animation.ViewAnimationUtils;
import com.app.designmore.revealLib.widget.RevealFrameLayout;
import com.app.designmore.rxAndroid.SimpleObserver;
import com.app.designmore.utils.MarginDecoration;
import com.app.designmore.utils.Utils;
import com.app.designmore.view.ProgressLayout;
import com.app.designmore.view.dialog.CustomAccountDialog;
import com.app.designmore.view.dialog.CustomTrolleyDialog;
import com.google.gson.Gson;
import com.trello.rxlifecycle.ActivityEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/8/25.
 */
public class TrolleyEditorActivity extends BaseActivity
    implements TrolleyEditorAdapter.Callback, CustomTrolleyDialog.Callback {

  private static final String TAG = TrolleyEditorActivity.class.getSimpleName();
  private static final String ITEMS = "ITEMS";

  @Nullable @Bind(R.id.trolley_editor_layout_root_view) LinearLayout rootView;
  @Nullable @Bind(R.id.trolley_editor_layout_rfl) RevealFrameLayout revealFrameLayout;
  @Nullable @Bind(R.id.white_toolbar_root_view) Toolbar toolbar;
  @Nullable @Bind(R.id.white_toolbar_title_tv) TextView toolbarTitleTv;

  @Nullable @Bind(R.id.trolley_editor_layout_pl) ProgressLayout progressLayout;
  @Nullable @Bind(R.id.trolley_editor_layout_rv) RecyclerView recyclerView;
  @Nullable @Bind(R.id.trolley_editor_layout_radio_btn) ImageButton radioBtn;
  @Nullable @Bind(R.id.trolley_editor_layout_delete_btn) Button deleteBtn;

  private Button actionButton;
  private SupportAnimator revealAnimator;
  private TrolleyEditorAdapter trolleyEditorAdapter;
  private List<TrolleyEntity> items = new ArrayList<>();
  private List<TrolleyEntity> deleteEntities = new ArrayList<>();
  private TrolleyEntity currentChangeEntity;

  private Subscription subscription = Subscriptions.empty();
  private CompositeSubscription compositeSubscription = new CompositeSubscription();
  private ProgressDialog progressDialog;
  private CustomTrolleyDialog customTrolleyDialog;
  private View toast;

  enum Type {
    ADD,
    SUBTRACT,
    ATTR
  }

  public static void navigateToTrolleyEditor(AppCompatActivity startingActivity,
      ArrayList<TrolleyEntity> trolleyEntities) {

    Intent intent = new Intent(startingActivity, TrolleyEditorActivity.class);
    intent.putParcelableArrayListExtra(ITEMS, trolleyEntities);
    startingActivity.startActivityForResult(intent, Constants.ACTIVITY_CODE);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.center_trolley_editor_layout);

    TrolleyEditorActivity.this.initView(savedInstanceState);
  }

  @Override public void initView(Bundle savedInstanceState) {

    TrolleyEditorActivity.this.setSupportActionBar(toolbar);
    toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_arrow_back_icon));

    TrolleyEditorActivity.this.toolbarTitleTv.setVisibility(View.VISIBLE);
    TrolleyEditorActivity.this.toolbarTitleTv.setText("购物车");

    this.items.clear();
    this.items.addAll(getIntent().getExtras().<TrolleyEntity>getParcelableArrayList(ITEMS));
    for (Iterator<TrolleyEntity> iterator = items.iterator(); iterator.hasNext(); ) {
      iterator.next().isChecked = false;
    }

    /*创建Adapter*/
    TrolleyEditorActivity.this.setupAdapter();

    if (savedInstanceState == null) {
      rootView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
        @Override public boolean onPreDraw() {
          rootView.getViewTreeObserver().removeOnPreDrawListener(this);
          TrolleyEditorActivity.this.startEnterAnim();
          return true;
        }
      });
    } else {
      TrolleyEditorActivity.this.loadData();
    }
  }

  private void startEnterAnim() {
    final Rect bounds = new Rect();
    rootView.getHitRect(bounds);

    revealAnimator =
        ViewAnimationUtils.createCircularReveal(revealFrameLayout.getChildAt(0), bounds.right, 0, 0,
            Utils.pythagorean(bounds.width(), bounds.height()));
    revealAnimator.setDuration(Constants.MILLISECONDS_400);
    revealAnimator.setInterpolator(new AccelerateInterpolator());
    revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {
      @Override public void onAnimationEnd() {
        TrolleyEditorActivity.this.loadData();
      }
    });
    revealAnimator.start();
  }

  private void setupAdapter() {

    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(TrolleyEditorActivity.this);
    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
    linearLayoutManager.setSmoothScrollbarEnabled(true);

    trolleyEditorAdapter = new TrolleyEditorAdapter(this);
    trolleyEditorAdapter.setCallback(TrolleyEditorActivity.this);

    recyclerView.setAdapter(trolleyEditorAdapter);
    recyclerView.setLayoutManager(linearLayoutManager);
    recyclerView.setHasFixedSize(true);
    recyclerView.addItemDecoration(
        new MarginDecoration(TrolleyEditorActivity.this, R.dimen.material_1dp));
  }

  private void observableListenerWrapper(Observable<TrolleyEntity> observable) {

    compositeSubscription.add(observable.subscribeOn(Schedulers.immediate())
        .compose(TrolleyEditorActivity.this.<TrolleyEntity>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new Subscriber<TrolleyEntity>() {
          @Override public void onCompleted() {

            if (deleteEntities.size() == items.size()) {
              radioBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_selected));
            } else {
              radioBtn.setImageDrawable(
                  getResources().getDrawable(R.drawable.ic_radio_normal_icon_icon));
            }

            if (deleteEntities.size() != 0) {
              TrolleyEditorActivity.this.deleteBtn.setEnabled(true);
            } else {
              TrolleyEditorActivity.this.deleteBtn.setEnabled(false);
            }

            compositeSubscription.remove(this);
          }

          @Override public void onError(Throwable e) {
            e.printStackTrace();
          }

          @Override public void onNext(TrolleyEntity trolleyEntity) {

            final ImageView radioIv = (ImageView) recyclerView.getLayoutManager()
                .findViewByPosition(items.indexOf(trolleyEntity))
                .findViewById(R.id.trolley_editor_item_radio_iv);

            if (trolleyEntity.isChecked) {
              radioIv.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_selected));
              deleteEntities.add(trolleyEntity);
            } else {
              radioIv.setImageDrawable(
                  getResources().getDrawable(R.drawable.ic_radio_normal_icon_icon));
              deleteEntities.remove(trolleyEntity);
            }
          }
        }));
  }

  private void loadData() {

    this.progressLayout.showContent();
    this.trolleyEditorAdapter.updateItems(items);
  }

  @Nullable @OnClick(R.id.trolley_editor_layout_radio_btn) void onRadioClick(
      ImageButton imageButton) {

    if (deleteEntities.size() == items.size()) {/*全选 -> 清空*/
      TrolleyEditorActivity.this.observableListenerWrapper(
          Observable.defer(new Func0<Observable<TrolleyEntity>>() {
            @Override public Observable<TrolleyEntity> call() {
              return Observable.from(items);
            }
          }).map(new Func1<TrolleyEntity, TrolleyEntity>() {
            @Override public TrolleyEntity call(TrolleyEntity trolleyEntity) {
              trolleyEntity.isChecked = false;
              return trolleyEntity;
            }
          }));
    } else {/*未全选 -> 全选*/

      TrolleyEditorActivity.this.observableListenerWrapper(
          Observable.defer(new Func0<Observable<TrolleyEntity>>() {
            @Override public Observable<TrolleyEntity> call() {
              return Observable.from(items);
            }
          }).filter(new Func1<TrolleyEntity, Boolean>() {
            @Override public Boolean call(TrolleyEntity trolleyEntity) {
              return !trolleyEntity.isChecked;
            }
          }).map(new Func1<TrolleyEntity, TrolleyEntity>() {
            @Override public TrolleyEntity call(TrolleyEntity trolleyEntity) {
              trolleyEntity.isChecked = true;
              return trolleyEntity;
            }
          }));
    }
  }

  @Nullable @OnClick(R.id.trolley_editor_layout_delete_btn) void onDeletdClick() {

    /* Action=DelCart&uid=10&item_list=[1,2,3]*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "DelCart");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(TrolleyEditorActivity.this));
    final List<String> itemList =
        Observable.from(deleteEntities).map(new Func1<TrolleyEntity, String>() {
          @Override public String call(TrolleyEntity trolleyEntity) {
            return trolleyEntity.getRecId();
          }
        }).toList().toBlocking().first();
    params.put("item_list", itemList.toString());

    Log.e(TAG, params.toString());

    subscription =
        TrolleyRetrofit.getInstance()
            .requestDeleteTrolley(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
                /*显示加载进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(TrolleyEditorActivity.this, null);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*隐藏进度条*/
                progressDialog.dismiss();
              }
            })
            .doOnCompleted(new Action0() {
              @Override public void call() {

                toast = DialogManager.getInstance()
                    .showNoMoreDialog(TrolleyEditorActivity.this, Gravity.TOP, "删除成功，请重试，(≧∇≦)ﾉ");
                TrolleyEditorActivity.this.deleteEntities.clear();
              }
            })
            .filter(new Func1<BaseResponse, Boolean>() {
              @Override public Boolean call(BaseResponse baseResponse) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(TrolleyEditorActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
            .subscribe(trolleyEditorAdapter);
  }

  /**
   * ********************Adapter回调
   */
  @Override public void onRadioClick(final TrolleyEntity trolleyEntity) {

    TrolleyEditorActivity.this.observableListenerWrapper(
        Observable.defer(new Func0<Observable<TrolleyEntity>>() {
          @Override public Observable<TrolleyEntity> call() {
            return Observable.just(trolleyEntity);
          }
        }));
  }

  @Override public void onAddCountClick(TrolleyEntity trolleyEntity) {
    TrolleyEditorActivity.this.upDateEntity(trolleyEntity, null, Type.ADD);
  }

  @Override public void onSubtractCountClick(TrolleyEntity trolleyEntity) {
    TrolleyEditorActivity.this.upDateEntity(trolleyEntity, null, Type.SUBTRACT);
  }

  @Override public void onArrowClick(TrolleyEntity trolleyEntity) {

    this.currentChangeEntity = trolleyEntity;

    /*Action=GetProductByAttr&gid=59*/
    Map<String, String> params = new HashMap<>(2);
    params.put("Action", "GetProductByAttr");
    params.put("gid", trolleyEntity.getGoodId());

    subscription =
        TrolleyRetrofit.getInstance()
            .getTrolleyAttrList(params)
            .doOnSubscribe(new Action0() {
              @Override public void call() {
            /*显示加载进度条*/
                if (progressDialog == null) {
                  progressDialog = DialogManager.getInstance()
                      .showSimpleProgressDialog(TrolleyEditorActivity.this, null);
                } else {
                  progressDialog.show();
                }
              }
            })
            .doOnTerminate(new Action0() {
              @Override public void call() {
                /*隐藏进度条*/
                progressDialog.dismiss();
              }
            })
            .filter(new Func1<List<ProductAttrEntity>, Boolean>() {
              @Override public Boolean call(List<ProductAttrEntity> productAttrEntities) {
                return !subscription.isUnsubscribed();
              }
            })
            .compose(TrolleyEditorActivity.this.<List<ProductAttrEntity>>bindUntilEvent(
                ActivityEvent.DESTROY))
            .subscribe(new SimpleObserver<List<ProductAttrEntity>>() {

              @Override public void onError(Throwable e) {
                toast = DialogManager.getInstance()
                    .showNoMoreDialog(TrolleyEditorActivity.this, Gravity.TOP, "操作失败，请重试，O__O …");
              }

              @Override public void onNext(List<ProductAttrEntity> productAttrEntities) {

                Map map = new HashMap();
                map.put(CustomAccountDialog.PRICE, currentChangeEntity.getGoodPrice());
                map.put(CustomAccountDialog.DES, currentChangeEntity.getGoodAttrValue());
                map.put(CustomAccountDialog.ATTRS, productAttrEntities);
                customTrolleyDialog = DialogManager.getInstance()
                    .showTrolleyDialog(TrolleyEditorActivity.this, map, TrolleyEditorActivity.this);
                customTrolleyDialog.show();
              }
            });
  }

  @Override public void onError(Throwable error) {

    error.printStackTrace();
    Log.e(TAG, error.getMessage());

    toast = DialogManager.getInstance()
        .showNoMoreDialog(TrolleyEditorActivity.this, Gravity.TOP, "操作失败，请重试，O__O …");
  }

  /**
   * 选择属窗口，回调
   */
  @Override public void onConfirmClick(ProductAttrEntity productAttrEntity) {

    TrolleyEditorActivity.this.upDateEntity(this.currentChangeEntity, productAttrEntity, Type.ATTR);
  }

  /**
   * 修改商品数量或者属性
   */
  private void upDateEntity(TrolleyEntity trolleyEntity, final ProductAttrEntity productAttrEntity,
      final Type type) {

    this.currentChangeEntity = trolleyEntity;
    int count = Integer.parseInt(trolleyEntity.getGoodCount());
    SimpleTrolleyEntity simpleTrolleyEntity = null;

    if (type == Type.ADD) {
      simpleTrolleyEntity = new SimpleTrolleyEntity(trolleyEntity.getGoodAttrId(), ++count + "",
          trolleyEntity.getGoodId(), "0");
    } else if (type == Type.SUBTRACT) {
      simpleTrolleyEntity = new SimpleTrolleyEntity(trolleyEntity.getGoodAttrId(), --count + "",
          trolleyEntity.getGoodId(), "0");
    } else if (type == Type.ATTR && productAttrEntity != null) {
      simpleTrolleyEntity = new SimpleTrolleyEntity(productAttrEntity.getAttrId(), count + "",
          trolleyEntity.getGoodId(), "0");
    }

    /*Action=SetCartGoodsInfo&uid=10&item_list=[{count:1,attr_id:1,gid:产品ID,is_del:1}]*/
    Map<String, String> params = new HashMap<>(3);
    params.put("Action", "SetCartGoodsInfo");
    params.put("uid",
        DBHelper.getInstance(getApplicationContext()).getUserID(TrolleyEditorActivity.this));

    subscription = TrolleyRetrofit.getInstance()
        .requestChangeTrolley(params, simpleTrolleyEntity)
        .doOnSubscribe(new Action0() {
          @Override public void call() {

            /*显示加载进度条*/
            if (progressDialog == null) {
              progressDialog = DialogManager.getInstance()
                  .showSimpleProgressDialog(TrolleyEditorActivity.this, null);
            } else {
              progressDialog.show();
            }
          }
        })
        .doOnTerminate(new Action0() {
          @Override public void call() {
            /*隐藏进度条*/
            progressDialog.dismiss();
          }
        })
        .filter(new Func1<BaseResponse, Boolean>() {
          @Override public Boolean call(BaseResponse baseResponse) {
            return !subscription.isUnsubscribed();
          }
        })
        .compose(TrolleyEditorActivity.this.<BaseResponse>bindUntilEvent(ActivityEvent.DESTROY))
        .subscribe(new SimpleObserver<BaseResponse>() {
          @Override public void onCompleted() {

            int count = Integer.parseInt(currentChangeEntity.getGoodCount());

            if (type == Type.ADD) {
              currentChangeEntity.setGoodCount(++count + "");
            } else if (type == Type.SUBTRACT) {
              currentChangeEntity.setGoodCount(--count + "");
            } else if (type == Type.ATTR) {
              currentChangeEntity.setGoodAttrValue(productAttrEntity.getAttrValue());
            }

            TrolleyEditorActivity.this.trolleyEditorAdapter.updateItem(
                items.indexOf(currentChangeEntity), currentChangeEntity);
          }

          @Override public void onError(Throwable e) {
            if (e instanceof WebServiceException) {
              toast = DialogManager.getInstance()
                  .showNoMoreDialog(TrolleyEditorActivity.this, Gravity.TOP,
                      "超过最大购买数量，请修改，o(╯□╰)o");
            } else {
              toast = DialogManager.getInstance()
                  .showNoMoreDialog(TrolleyEditorActivity.this, Gravity.TOP, "操作失败，请重试，O__O …");
            }
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_single, menu);

    MenuItem menuItem = menu.findItem(R.id.action_inbox);
    menuItem.setActionView(R.layout.menu_inbox_tv_item);
    actionButton = (Button) menuItem.getActionView().findViewById(R.id.action_inbox_btn);
    actionButton.setText(getText(R.string.action_done));
    actionButton.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        TrolleyEditorActivity.this.exit();
      }
    });
    return true;
  }

  @Override public void exit() {
    if (revealAnimator != null && !revealAnimator.isRunning()) {
      revealAnimator = revealAnimator.reverse();
      revealAnimator.setDuration(Constants.MILLISECONDS_400);
      revealAnimator.setInterpolator(new AccelerateInterpolator());
      revealAnimator.addListener(new SupportAnimator.SimpleAnimatorListener() {

        @Override public void onAnimationEnd() {

          rootView.setVisibility(View.GONE);
          TrolleyEditorActivity.this.setResult(RESULT_OK, null);
          TrolleyEditorActivity.this.finish();
        }

        @Override public void onAnimationCancel() {
          TrolleyEditorActivity.this.finish();
        }
      });
      revealAnimator.start();
    } else if (revealAnimator != null && revealAnimator.isRunning()) {
      revealAnimator.cancel();
    } else if (revealAnimator == null) {
      TrolleyEditorActivity.this.finish();
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    if (toast != null && toast.getParent() != null) {
      getWindowManager().removeViewImmediate(toast);
    }
    this.toast = null;
    this.progressDialog = null;
    this.customTrolleyDialog = null;
    if (!subscription.isUnsubscribed()) subscription.unsubscribe();
    if (!compositeSubscription.isUnsubscribed() && compositeSubscription.hasSubscriptions()) {
      compositeSubscription.clear();
    }
  }
}
