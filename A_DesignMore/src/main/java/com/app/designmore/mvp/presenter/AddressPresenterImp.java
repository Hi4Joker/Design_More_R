package com.app.designmore.mvp.presenter;

import android.content.Context;
import android.util.Log;
import com.app.designmore.mvp.viewinterface.AddressView;
import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.app.designmore.rxAndroid.schedulers.AndroidSchedulers;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * Created by Joker on 2015/9/9.
 */
public class AddressPresenterImp implements AddressPresenter {

  private static final String TAG = AddressPresenterImp.class.getSimpleName();
  private Context context;
  private AddressView addressView;
  private List<Province> provinces = new ArrayList<>();
  private Subscription subscribe = Subscriptions.empty();

  public AddressPresenterImp() {

  }

  @Override public void attach(Context context, AddressView addressView) {
    this.context = context;
    this.addressView = addressView;
  }

  /*@Override public void showPicker() {
    if (provinces.size() > 0) {
      addressView.onInflateFinish(provinces);
    } else {

      provinces = Observable.create(new Observable.OnSubscribe<List<Province>>() {
        @Override public void call(final Subscriber<? super List<Province>> subscriber) {

          subscribe = Schedulers.io().createWorker().schedule(new Action0() {
            @Override public void call() {
              InputStream inputStream = null;
              try {
                inputStream = context.getResources().getAssets().open("address.txt");
                byte[] arrayOfByte = new byte[inputStream.available()];
                inputStream.read(arrayOfByte);
                JSONArray jsonList = new JSONArray(new String(arrayOfByte, "UTF-8"));
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .serializeNulls()
                    .create();
                for (int i = 0; i < jsonList.length(); i++) {
                  provinces.add(gson.fromJson(jsonList.getString(i), Province.class));
                }
              } catch (Exception e) {
                subscriber.onError(e);
              } finally {
                if (inputStream != null) {
                  try {
                    inputStream.close();
                  } catch (IOException e) {
                    Observable.error(e);
                  }
                }
              }
              subscriber.onNext(provinces);
              subscriber.onCompleted();
            }
          });
        }
      }).toBlocking().single();

      if (provinces != null && provinces.size() > 0) {
        addressView.onInflateFinish(provinces);
      } else {
        addressView.showError();
      }
    }
  }*/

  @Override public void showPicker() {

    if (provinces.size() > 0) {
      addressView.onInflateFinish(provinces);
    } else {

      subscribe = Observable.create(new Observable.OnSubscribe<List<Province>>() {
        @Override public void call(final Subscriber<? super List<Province>> subscriber) {

          Schedulers.io().createWorker().schedule(new Action0() {
            @Override public void call() {
              InputStream in = null;
              try {
                in = context.getResources().getAssets().open("address.txt");
                byte[] arrayOfByte = new byte[in.available()];
                in.read(arrayOfByte);
                JSONArray jsonList = new JSONArray(new String(arrayOfByte, "UTF-8"));
                Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                    .serializeNulls()
                    .create();
                for (int i = 0; i < jsonList.length(); i++) {
                  provinces.add(gson.fromJson(jsonList.getString(i), Province.class));
                }
              } catch (Exception e) {
                subscriber.onError(e);
              } finally {
                if (in != null) {
                  try {
                    in.close();
                  } catch (IOException e) {
                    subscriber.onError(e);
                  }
                }
                subscriber.onNext(provinces);
                subscriber.onCompleted();
              }
            }
          });
        }
      }).doOnSubscribe(new Action0() {
        @Override public void call() {
          addressView.showProgress();
        }
      }).finallyDo(new Action0() {
        @Override public void call() {
          addressView.hideProgress();
        }
      }).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<List<Province>>() {
        @Override public void call(List<Province> provinces) {
          addressView.onInflateFinish(provinces);
        }
      }, new Action1<Throwable>() {
        @Override public void call(Throwable throwable) {
          addressView.showError();
        }
      });
    }
  }

  @Override public void detach() {
    if (subscribe != null && !subscribe.isUnsubscribed()) subscribe.unsubscribe();
  }
}
