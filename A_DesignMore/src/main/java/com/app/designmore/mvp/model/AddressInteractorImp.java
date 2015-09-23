package com.app.designmore.mvp.model;

import android.content.Context;
import com.app.designmore.mvp.ExecutorCallback;
import com.app.designmore.retrofit.entity.Province;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/9/23.
 */
public class AddressInteractorImp implements AddressInteractor {

  private Context context;

  public AddressInteractorImp(Context context) {
    this.context = context;
  }

  @Override public void inflateAddressItems(ExecutorCallback callback) {

    callback.onDataFinish(getAddressItems());
  }

  private Observable<List<Province>> getAddressItems() {

    return Observable.create(new Observable.OnSubscribe<List<Province>>() {
      @Override public void call(final Subscriber<? super List<Province>> subscriber) {

        Schedulers.io().createWorker().schedule(new Action0() {
          @Override public void call() {
            InputStream in = null;
            List<Province> provinces = new ArrayList<>();
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
    });
  }
}
