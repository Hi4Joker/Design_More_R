package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.manager.OkClientInstance;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.response.FashionResponse;
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
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;
import retrofit.http.FieldMap;
import retrofit.http.FormUrlEncoded;
import retrofit.http.POST;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/9/15.
 */
public class FashionRetrofit {

  private FashionEntity instance = new FashionEntity();

  interface CollectionService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<FashionResponse> getFashionList(@FieldMap Map<String, String> params);
  }

  private final CollectionService collectionService;

  private FashionRetrofit() {
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

    collectionService = restAdapter.create(CollectionService.class);
  }

  private static class SingletonHolder {
    private static FashionRetrofit instance = new FashionRetrofit();
  }

  public static FashionRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取新品列表
   */
  public Observable<List<FashionEntity>> getFashionList(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<FashionResponse>>() {
      @Override public Observable<FashionResponse> call() {
        /*获取新品列表，超时8秒*/
        return collectionService.getFashionList(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<FashionResponse, Observable<FashionResponse>>() {
      @Override public Observable<FashionResponse> call(FashionResponse fashionResponse) {
        return fashionResponse.filterWebServiceErrors();
      }
    }).flatMap(new Func1<FashionResponse, Observable<FashionResponse.Fashion>>() {
      @Override public Observable<FashionResponse.Fashion> call(FashionResponse fashionResponse) {
        return Observable.from(fashionResponse.getFashions());
      }
    }).map(new Func1<FashionResponse.Fashion, FashionEntity>() {
      @Override public FashionEntity call(FashionResponse.Fashion fashion) {

        FashionEntity clone = instance.newInstance();
        clone.setGoodId(fashion.goodId);
        clone.setGoodName(fashion.goodName);
        clone.setGoodDiscount(fashion.goodDiscount);
        clone.setGoodThumbUrl(fashion.goodThumbUrl);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<FashionEntity>>applyExecutorSchedulers());
  }
}
