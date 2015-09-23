package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.DetailEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.DetailResponse;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
 * Created by Joker on 2015/9/20.
 */
public class DetailRetrofit {

  interface DetailService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<DetailResponse> getGoodDetail(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestBuyGood(@FieldMap Map<String, String> params);
  }

  private final DetailService detailService;

  private DetailRetrofit() {
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

    detailService = restAdapter.create(DetailService.class);
  }

  private static class SingletonHolder {
    private static DetailRetrofit instance = new DetailRetrofit();
  }

  public static DetailRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取商品详情
   */
  public Observable<DetailEntity> getGoodDetail(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<DetailResponse>>() {
      @Override public Observable<DetailResponse> call() {
        return detailService.getGoodDetail(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<DetailResponse, Observable<DetailResponse>>() {
      @Override public Observable<DetailResponse> call(DetailResponse detailResponse) {
        return detailResponse.filterWebServiceErrors();
      }
    }).map(new Func1<DetailResponse, DetailResponse.Detail>() {
      @Override public DetailResponse.Detail call(DetailResponse detailResponse) {
        return detailResponse.getDetail();
      }
    }).map(new Func1<DetailResponse.Detail, DetailEntity>() {
      @Override public DetailEntity call(DetailResponse.Detail detail) {

        return new DetailEntity(detail.goodId, detail.goodName, detail.goodMarketPrice,
            detail.goodShopPrice, detail.goodDes, detail.goodDesUrl, detail.goodRepertory,
            detail.productImages, detail.productAttrs);
      }
    }).compose(SchedulersCompat.<DetailEntity>applyExecutorSchedulers());
  }

  /**
   * 添加购物车
   */
  public Observable<BaseResponse> requestBuyGood(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return detailService.requestBuyGood(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(BaseResponse BaseResponse) {
        return BaseResponse.filterWebServiceErrors();
      }
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }
}
