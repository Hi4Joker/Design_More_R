package com.app.designmore.retrofit;

import android.support.annotation.CheckResult;
import com.app.designmore.Constants;
import com.app.designmore.manager.OkClientInstance;
import com.app.designmore.retrofit.entity.DeliveryEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.DeliveryResponse;
import com.app.designmore.retrofit.response.ProductResponse;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/10/8.
 */
public class OrderRetrofit {

  private DeliveryEntity instance = new DeliveryEntity();

  interface OrderService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<DeliveryResponse> getDeliveryList(@FieldMap Map<String, String> params);
  }

  private final OrderService orderService;

  private OrderRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestFacade request) {
        request.addHeader("Accept-Encoding", "application/json");
        //request.addHeader("Content-Type", "application/json");
      }
    };

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
        .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
        .serializeNulls().create();

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(Constants.BASE_URL)
        .setRequestInterceptor(requestInterceptor)
        .setLogLevel(RestAdapter.LogLevel.FULL)
        .setLog(new AndroidLog("Joker_DesignMore"))
        .setClient(OkClientInstance.getInstance())
        .setConverter(new GsonConverter(gson))
        .build();

    orderService = restAdapter.create(OrderService.class);
  }

  private static class SingletonHolder {
    private static OrderRetrofit instance = new OrderRetrofit();
  }

  public static OrderRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取配送方式
   */
  @CheckResult public Observable<List<DeliveryEntity>> getDeliveryList(
      final Map<String, String> params) {

    return orderService.getDeliveryList(params)
        .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        })
        .concatMap(new Func1<DeliveryResponse, Observable<DeliveryResponse>>() {
          @Override public Observable<DeliveryResponse> call(DeliveryResponse deliveryResponse) {
            return deliveryResponse.filterWebServiceErrors();
          }
        })
        .flatMap(new Func1<DeliveryResponse, Observable<DeliveryResponse.Delivery>>() {
          @Override
          public Observable<DeliveryResponse.Delivery> call(DeliveryResponse deliveryResponse) {
            return Observable.from(deliveryResponse.getDeliveries());
          }
        })
        .map(new Func1<DeliveryResponse.Delivery, DeliveryEntity>() {
          @Override public DeliveryEntity call(DeliveryResponse.Delivery delivery) {

            DeliveryEntity clone = instance.newInstance();

            clone.setDeliveryId(delivery.DeliveryId);
            clone.setDeliveryName(delivery.DeliveryName);
            clone.setDeliveryBaseFee(delivery.DeliveryFee);

            return clone;
          }
        })
        .toList()
        .compose(SchedulersCompat.<List<DeliveryEntity>>applyExecutorSchedulers());
  }
}
