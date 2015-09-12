package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.event.EditorAddressEvent;
import com.app.designmore.event.RefreshAddressEvent;
import com.app.designmore.retrofit.entity.AddressEntity;
import com.app.designmore.retrofit.response.AddressResponse;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/9/1.
 */
public class AddressRetrofit {

  private static final String TAG = AddressRetrofit.class.getSimpleName();

  private AddressEntity addressInstance = new AddressEntity();

  interface AddressService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<AddressResponse> getAddressList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<AddressResponse> requestEditorAddress(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestAddAddress(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestDeleteAddress(@FieldMap Map<String, String> params);
  }

  private final AddressService addressService;

  private AddressRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept-Encoding", "application/json");
        //request.addHeader("Content-Type", "application/json");
      }
    };

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
        .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
        .serializeNulls().create();

    // TODO: 2015/9/1  每次创建OkHttp，待优化
    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Constants.BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setLog(new AndroidLog("Joker_DesignMore"))
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
  public Observable<List<AddressEntity>> getAddressList(final Map<String, String> params) {

    Observable<List<AddressEntity>> observable =
        Observable.defer(new Func0<Observable<AddressResponse>>() {
          @Override public Observable<AddressResponse> call() {
             /*获取地址列表，超时8秒*/
            return addressService.getAddressList(params)
                .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
          }
        }).retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
          @Override public Observable<AddressResponse> call(AddressResponse addressResponse) {
            return addressResponse.filterWebServiceErrors();
          }
        })/*.map(new Func1<AddressResponse, List<AddressEntity>>() {
          @Override public List<AddressEntity> call(AddressResponse addressResponse) {

            final ArrayList<AddressEntity> addressArrayList =
                new ArrayList<>(addressResponse.getAddressList().size());
            AddressEntity addressInstance = new AddressEntity();

            for (AddressResponse.Address entity : addressResponse.getAddressList()) {
              AddressEntity clone = addressInstance.newInstance();

              clone.setAddressId(entity.addressId);
              clone.setUserName(entity.userName);
              clone.setMobile(entity.mobile);
              clone.setZipcode(entity.zipcode);

              clone.setProvince(entity.province);
              clone.setCity(entity.city);
              clone.setAddress(entity.address);

              *//*默认选项*//*
              clone.setDefault(entity.isDefault);

              addressArrayList.add(clone);
            }
            return addressArrayList;
          }
        })*/.flatMap(new Func1<AddressResponse, Observable<AddressResponse.Address>>() {
          @Override
          public Observable<AddressResponse.Address> call(AddressResponse addressResponse) {
            return Observable.from(addressResponse.getAddressList());
          }
        }).map(new Func1<AddressResponse.Address, AddressEntity>() {
          @Override public AddressEntity call(AddressResponse.Address address) {

            AddressEntity addressEntity = addressInstance.newInstance();
            addressEntity.setAddressId(address.addressId);
            addressEntity.setUserName(address.userName);
            addressEntity.setMobile(address.mobile);
            addressEntity.setZipcode(address.zipcode);

            addressEntity.setProvince(address.province);
            addressEntity.setCity(address.city);
            addressEntity.setAddress(address.address);

            /*默认选项*/
            addressEntity.setDefault(address.isDefault);

            return addressEntity;
          }
        }).toList().compose(SchedulersCompat.<List<AddressEntity>>applyExecutorSchedulers());

    return observable;
  }

  /**
   * 编辑地址
   */
  public Observable<EditorAddressEvent> requestEditorAddress(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call() {
        return addressService.requestEditorAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
      @Override public Observable<AddressResponse> call(AddressResponse addressResponse) {
        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<AddressResponse, EditorAddressEvent>() {
      @Override public EditorAddressEvent call(AddressResponse addressResponse) {
        AddressResponse.Address address = addressResponse.getAddressList().get(0);
        EditorAddressEvent editorAddressEvent =
            new EditorAddressEvent(address.addressId, address.userName, address.province,
                address.city, address.address, address.mobile, address.zipcode, address.isDefault);

        return editorAddressEvent;
      }
    }).compose(SchedulersCompat.<EditorAddressEvent>applyExecutorSchedulers());
  }

  /**
   * 添加地址
   */
  public Observable<RefreshAddressEvent> requestAddAddress(final Map<String, String> params) {
    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return addressService.requestAddAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(BaseResponse baseResponse) {
        return baseResponse.filterWebServiceErrors();
      }
    }).map(new Func1<BaseResponse, RefreshAddressEvent>() {
      @Override public RefreshAddressEvent call(BaseResponse baseResponse) {
        /*添加成功*/
        return new RefreshAddressEvent();
      }
    }).compose(SchedulersCompat.<RefreshAddressEvent>applyExecutorSchedulers());
  }

  /**
   * 删除地址
   */
  public Observable<BaseResponse> requestDeleteAddress(final Map<String, String> params) {
    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return addressService.requestDeleteAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(BaseResponse addressResponse) {

        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<BaseResponse, BaseResponse>() {
      @Override public BaseResponse call(BaseResponse baseResponse) {
        /*删除成功*/
        return baseResponse;
      }
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }

  /**
   * 设置默认地址
   */
  public Observable<BaseResponse> requestSetDefaultAddress(final Map<String, String> params) {
    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return addressService.requestDeleteAddress(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(BaseResponse addressResponse) {
        return addressResponse.filterWebServiceErrors();
      }
    }).map(new Func1<BaseResponse, BaseResponse>() {
      @Override public BaseResponse call(BaseResponse baseResponse) {
        /*设置成功*/
        return baseResponse;
      }
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }
}
