package com.app.designmore.mvp.presenter;

import android.content.Context;
import com.app.designmore.mvp.viewinterface.AddressView;
import com.app.designmore.retrofit.entity.Province;
import com.app.designmore.view.CustomWheelPicker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import rx.Scheduler;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

/**
 * Created by Joker on 2015/9/9.
 */
public class AddressPresenterImp implements AddressPresenter {

  private Context context;
  private AddressView addressView;
  private ArrayList<Province> provinces = new ArrayList<>();
  private Scheduler.Worker worker;

  public AddressPresenterImp() {

  }

  @Override public void attach(Context context, AddressView addressView) {
    this.context = context;
    this.addressView = addressView;
  }

  @Override public void showPicker() {

    if (provinces.size() > 0) {
      addressView.onInflateFinish(provinces);
    } else {
      if (worker == null) {
        worker = Schedulers.immediate().createWorker();
      }

      /*显示进度条*/
      addressView.showProgress();

      worker.schedule(new Action0() {
        @Override public void call() {

          String address;
          InputStream in = null;

          try {
            in = context.getResources().getAssets().open("address.txt");
            byte[] arrayOfByte = new byte[in.available()];
            in.read(arrayOfByte);
            address = new String(arrayOfByte, "UTF-8");
            JSONArray jsonList = new JSONArray(address);
            Gson gson =
                new GsonBuilder().excludeFieldsWithoutExposeAnnotation().serializeNulls().create();
            for (int i = 0; i < jsonList.length(); i++) {
              provinces.add(gson.fromJson(jsonList.getString(i), Province.class));
            }
          } catch (Exception ignored) {
          } finally {
            if (in != null) {
              try {
                in.close();
              } catch (IOException ignored) {
              }
            }
            if (provinces.size() > 0) {
              addressView.hideProgress();
              addressView.onInflateFinish(provinces);
            } else {
              addressView.showError();
            }
          }
        }
      });
    }
  }

  @Override public void detach() {
    if (!worker.isUnsubscribed()) worker.unsubscribe();
  }
}
