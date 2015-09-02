package com.app.designmore.retrofit;

import android.util.Log;
import com.app.designmore.Constants;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.retrofit.entity.Address;
import com.app.designmore.retrofit.result.AddressResponse;
import com.app.designmore.rx.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.security.auth.login.LoginException;
import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.FieldMap;
import retrofit.http.POST;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/9/1.
 */
public class AddressRetrofit {

  private static final String TAG = AddressRetrofit.class.getSimpleName();

  interface AddressService {

    //@Headers("Accept-Encoding: application/json")
    @POST("/mobile/api/client/interface.php") Observable<AddressResponse> getAddressList(
        @FieldMap Map<String, String> params);

    @POST("/mobile/api/client/interface.php") Observable<AddressResponse> requestEditorAddress(
        @FieldMap Map<String, String> params);

    @POST("/mobile/api/client/interface.php") Observable<AddressResponse> requestAddAddress(
        @FieldMap Map<String, String> params);

    @POST("/mobile/api/client/interface.php") Observable<AddressResponse> requestDeleteAddress(
        @FieldMap Map<String, String> params);
  }

  private final AddressService addressService;

  private AddressRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept-Encoding", "application/json");
      }
    };

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
        .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
        .create();

    // TODO: 2015/9/1  每次创建OkHttp，待优化
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Constants.BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setClient(new OkClient())
        .setConverter(new GsonConverter(gson))
        .build();

    addressService = restAdapter.create(AddressService.class);
  }

  private static class SingletonHolder {
    private static AddressRetrofit instance = new AddressRetrofit();
  }

  public static AddressRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取地址列表
   */
  public Observable<List<Address>> getAddressList(final Map<String, String> params) {

    Observable<List<Address>> observable =
        Observable.defer(new Func0<Observable<AddressResponse>>() {
          @Override public Observable<AddressResponse> call() {

            /*获取地址列表，超时8秒*/
            return addressService.getAddressList(params)
                .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
          }
        }).retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {

            if (throwable instanceof TimeoutException && integer < 1) {//可重试一次
              return true;
            }
            return false;
          }
        }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
          @Override public Observable<AddressResponse> call(AddressResponse addressManagerEntity) {
            return addressManagerEntity.filterWebServiceErrors();
          }
        }).map(new Func1<AddressResponse, List<Address>>() {
          @Override public List<Address> call(AddressResponse addressResponse) {

            final ArrayList<Address> addressArrayList = new ArrayList<>();

            Address addressInstance = new Address();

            for (AddressResponse.Address entity : addressResponse.getAddressList()) {
              Address clone = addressInstance.newInstance();

              clone.setAddressId(entity.addressId);
              clone.setUserName(entity.userName);
              clone.setMobile(entity.mobile);
              clone.setZipcode(entity.zipcode);

              clone.setProvince(entity.province);
              clone.setCity(entity.city);
              clone.setAddress(entity.address);

              /*默认选项*/
              clone.setIsChecked(entity.isChecked);

              addressArrayList.add(clone);
            }
            return addressArrayList;
          }
        }).compose(SchedulersCompat.<List<Address>>applyExecutorSchedulers());

    return observable;
  }

  /**
   * 编辑地址
   */
  public Observable<Address> requestEditorAddress(final HashMap<String, String> params) {

    return Observable.defer(new Func0<Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call() {

        return addressService.requestEditorAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {

        if (throwable instanceof TimeoutException && integer < 1) {//可重试一次
          return true;
        }
        return false;
      }
    }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call(AddressResponse addressResponse) {

        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<AddressResponse, Address>() {
      @Override public Address call(AddressResponse addressResponse) {

        AddressResponse.Address address = addressResponse.getAddressList().get(0);

        EditorAddressEvent editorAddressEvent =
            new EditorAddressEvent(address.addressId, address.userName, address.province,
                address.city, address.address, address.mobile, address.zipcode, address.isChecked);

        return editorAddressEvent;
      }
    }).compose(SchedulersCompat.<Address>applyExecutorSchedulers());
  }

  /**
   * 添加地址
   */
  public Observable<RefreshAddressEvent> requestAddAddress(final HashMap<String, String> params) {

    return Observable.defer(new Func0<Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call() {

        return addressService.requestAddAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {

        if (throwable instanceof TimeoutException && integer < 1) {//可重试一次
          return true;
        }
        return false;
      }
    }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call(AddressResponse addressResponse) {

        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<AddressResponse, RefreshAddressEvent>() {
      @Override public RefreshAddressEvent call(AddressResponse addressResponse) {

        /*添加成功*/
        return new RefreshAddressEvent();
      }
    }).compose(SchedulersCompat.<RefreshAddressEvent>applyExecutorSchedulers());
  }

  /**
   * 删除地址
   */
  public Observable<AddressResponse> requestDeleteAddress(final HashMap<String, String> params) {

    return Observable.defer(new Func0<Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call() {

        return addressService.requestDeleteAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {

        if (throwable instanceof TimeoutException && integer < 1) {//可重试一次
          return true;
        }
        return false;
      }
    }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call(AddressResponse addressResponse) {

        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<AddressResponse, AddressResponse>() {
      @Override public AddressResponse call(AddressResponse addressResponse) {

        /*添加成功*/
        return addressResponse;
      }
    }).compose(SchedulersCompat.<AddressResponse>applyExecutorSchedulers());
  }
}
