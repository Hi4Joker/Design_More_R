package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.Address;
import com.app.designmore.retrofit.result.AddressResponse;
import com.app.designmore.rx.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
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

  interface AddressService {

    //@Headers("Accept-Encoding: application/json")
    @POST("/mobile/api/client/interface.php") Observable<AddressResponse> getAddressList(
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
        }).flatMap(new Func1<AddressResponse, Observable<AddressResponse>>() {
          @Override public Observable<AddressResponse> call(AddressResponse addressManagerEntity) {
            return addressManagerEntity.filterWebServiceErrors();
          }
        }).map(new Func1<AddressResponse, List<Address>>() {
          @Override public List<Address> call(AddressResponse addressManagerResponse) {

            final ArrayList<Address> addressArrayList = new ArrayList<>();

            Address addressInstance = new Address();

            for (AddressResponse.Address entity : addressManagerResponse.addressList) {
              Address clone = addressInstance.newInstance();

              clone.setUserId(entity.userId);
              clone.setAddressId(entity.addressId);
              clone.setAddressName(entity.addressName);
              clone.setUserName(entity.consignee);
              clone.setAddress(entity.address);
              clone.setMobile(entity.mobile);

              addressArrayList.add(clone);
            }
            return addressArrayList;
          }
        }).compose(SchedulersCompat.<List<Address>>applyNewSchedulers());

    return observable;
  }
}
