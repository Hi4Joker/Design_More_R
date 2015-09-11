package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.TrolleyResponse;
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
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/9/4.
 */
public class TrolleyRetrofit {

  interface CollectionService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<TrolleyResponse> getTrolleyList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestDeleteCollection(@FieldMap Map<String, String> params);
  }

  private final CollectionService collectionService;

  private TrolleyRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestFacade request) {
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

    collectionService = restAdapter.create(CollectionService.class);
  }

  private static class SingletonHolder {
    private static TrolleyRetrofit instance = new TrolleyRetrofit();
  }

  public static TrolleyRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取购物车列表
   */
  public Observable<List<TrolleyEntity>> getTrolleyList(final Map<String, String> params) {

    Observable<List<TrolleyEntity>> observable =
        Observable.defer(new Func0<Observable<TrolleyResponse>>() {
          @Override public Observable<TrolleyResponse> call() {
             /*获取热搜列表，超时8秒*/
            return collectionService.getTrolleyList(params)
                .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
          }
        }).retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        }).concatMap(new Func1<TrolleyResponse, Observable<TrolleyResponse>>() {
          @Override public Observable<TrolleyResponse> call(TrolleyResponse trolleyResponse) {
            return trolleyResponse.filterWebServiceErrors();
          }
        }).map(new Func1<TrolleyResponse, List<TrolleyEntity>>() {
          @Override public List<TrolleyEntity> call(TrolleyResponse trolleyResponse) {

            final ArrayList<TrolleyEntity> trolleyEntities = new ArrayList<>();
            TrolleyEntity instance = new TrolleyEntity();

            for (TrolleyResponse.Trolley trolley : trolleyResponse.getTrolleyList()) {

              TrolleyEntity clone = instance.newInstance();

              clone.setGoodId(trolley.goodId);
              clone.setGoodName(trolley.goodName);
              clone.setGoodAttr(trolley.goodAttr);
              clone.setGoodCount(trolley.goodCount);
              clone.setGoodPrice(trolley.goodPrice);

              trolleyEntities.add(clone);
            }
            return trolleyEntities;
          }
        }).compose(SchedulersCompat.<List<TrolleyEntity>>applyExecutorSchedulers());

    return observable;
  }

  public Observable<BaseResponse> requestDeleteTrolley(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {

        return collectionService.requestDeleteCollection(params)
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
}
