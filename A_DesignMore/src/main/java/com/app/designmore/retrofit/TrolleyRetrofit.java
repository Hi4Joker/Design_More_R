package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.CollectionEntity;
import com.app.designmore.retrofit.entity.ProductAttrEntity;
import com.app.designmore.retrofit.entity.SimpleTrolleyEntity;
import com.app.designmore.retrofit.entity.TrolleyEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.ProductAttrResponse;
import com.app.designmore.retrofit.response.TrolleyResponse;
import com.app.designmore.rxAndroid.SchedulersCompat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.net.URLDecoder;
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

  private final Gson gson;
  private TrolleyEntity trolleyEntityInstance = new TrolleyEntity();
  private ProductAttrEntity productAttrInstance = new ProductAttrEntity();

  interface TrolleyService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<TrolleyResponse> getTrolleyList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestChangeTrolley(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestDeleteTrolley(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<ProductAttrResponse> getTrolleyAttrList(@FieldMap Map<String, String> params);
  }

  private final TrolleyService trolleyService;

  private TrolleyRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestFacade request) {
        request.addHeader("Accept-Encoding", "application/json");
        //request.addHeader("Content-Type", "application/json");
      }
    };

    gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
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

    trolleyService = restAdapter.create(TrolleyService.class);
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

    return Observable.defer(new Func0<Observable<TrolleyResponse>>() {
      @Override public Observable<TrolleyResponse> call() {
        return trolleyService.getTrolleyList(params)
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
    }).flatMap(new Func1<TrolleyResponse, Observable<TrolleyResponse.Trolley>>() {
      @Override public Observable<TrolleyResponse.Trolley> call(TrolleyResponse trolleyResponse) {
        return Observable.from(trolleyResponse.getTrolleyList());
      }
    }).map(new Func1<TrolleyResponse.Trolley, TrolleyEntity>() {
      @Override public TrolleyEntity call(TrolleyResponse.Trolley trolley) {

        TrolleyEntity clone = trolleyEntityInstance.newInstance();

        clone.setRecId(trolley.recId);
        clone.setGoodId(trolley.goodId);
        clone.setGoodName(trolley.goodName);
        clone.setGoodAttrId(trolley.goodAttrId);
        clone.setGoodCount(trolley.goodCount);
        clone.setGoodPrice(trolley.goodPrice);
        clone.setGoodThumb(trolley.goodThumb);
        clone.setGoodAttrValue(trolley.goodAttrValue);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<TrolleyEntity>>applyExecutorSchedulers());
  }

  /**
   * 修改购物车
   */
  public Observable<BaseResponse> requestChangeTrolley(final Map<String, String> params,
      final SimpleTrolleyEntity simpleTrolleyEntity) {

    List<SimpleTrolleyEntity> list = new ArrayList<>(1);
    list.add(simpleTrolleyEntity);
    params.put("item_list", URLDecoder.decode(gson.toJson(list)));

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return trolleyService.requestChangeTrolley(params)
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
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }

  /**
   * 批量删除购物车
   */
  public Observable<BaseResponse> requestDeleteTrolley(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return trolleyService.requestDeleteTrolley(params)
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
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }

  /**
   * 获取商品属性
   */
  public Observable<List<ProductAttrEntity>> getTrolleyAttrList(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<ProductAttrResponse>>() {
      @Override public Observable<ProductAttrResponse> call() {
        return trolleyService.getTrolleyAttrList(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<ProductAttrResponse, Observable<ProductAttrResponse>>() {
      @Override
      public Observable<ProductAttrResponse> call(ProductAttrResponse productAttrResponse) {
        return productAttrResponse.filterWebServiceErrors();
      }
    }).flatMap(new Func1<ProductAttrResponse, Observable<ProductAttrResponse.Attr>>() {
      @Override
      public Observable<ProductAttrResponse.Attr> call(ProductAttrResponse productAttrResponse) {
        return Observable.from(productAttrResponse.getAttrs());
      }
    }).map(new Func1<ProductAttrResponse.Attr, ProductAttrEntity>() {
      @Override public ProductAttrEntity call(ProductAttrResponse.Attr attr) {

        ProductAttrEntity clone = productAttrInstance.newInstance();

        clone.setAttrId(attr.goodsAttrId);
        clone.setAttrValue(attr.goodsAttrValue);
        clone.setAttrPrice(attr.goodsAttrPrice);
        clone.setAttrThumbUrl(attr.goodsAttrThumb);
        clone.setIsChecked(false);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<ProductAttrEntity>>applyExecutorSchedulers());
  }
}
