package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.CollectionResponse;
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
public class CollectionRetrofit {

  interface CollectionService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<CollectionResponse> getMyCollectionList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestDeleteCollection(@FieldMap Map<String, String> params);
  }

  private final CollectionService collectionService;

  private CollectionRetrofit() {
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
    private static CollectionRetrofit instance = new CollectionRetrofit();
  }

  public static CollectionRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取收藏列表
   */
  public Observable<List<CollectionEntity>> getCollectionList(final Map<String, String> params) {

    Observable<List<CollectionEntity>> observable =
        Observable.defer(new Func0<Observable<CollectionResponse>>() {
          @Override public Observable<CollectionResponse> call() {

             /*获取热搜列表，超时8秒*/
            return collectionService.getMyCollectionList(params)
                .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
          }
        }).retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        }).concatMap(new Func1<CollectionResponse, Observable<CollectionResponse>>() {
          @Override
          public Observable<CollectionResponse> call(CollectionResponse collectionResponse) {
            return collectionResponse.filterWebServiceErrors();
          }
        }).map(new Func1<CollectionResponse, List<CollectionEntity>>() {
          @Override public List<CollectionEntity> call(CollectionResponse collectionResponse) {

            final ArrayList<CollectionEntity> collectionEntities = new ArrayList<>();
            CollectionEntity instance = new CollectionEntity();

            for (CollectionResponse.Collect collect : collectionResponse.getCollections()) {

              CollectionEntity clone = instance.newInstance();

              clone.setGoodId(collect.goodInfo.goodId);
              clone.setGoodName(collect.goodInfo.goodName);
              clone.setGoodPrice(collect.goodInfo.goodPrice);
              clone.setGoodThumb(collect.goodInfo.goodThumb);
              clone.setCollectionId(collectionResponse.collectionId);

              collectionEntities.add(clone);
            }

            return collectionEntities;
          }
        }).compose(SchedulersCompat.<List<CollectionEntity>>applyExecutorSchedulers());

    return observable;
  }

  public Observable<BaseResponse> requestDeleteCollection(final Map<String, String> params) {

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
