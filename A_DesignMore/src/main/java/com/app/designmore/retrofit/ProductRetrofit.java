package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.ProductEntity;
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
public class ProductRetrofit {

  private ProductEntity instance = new ProductEntity();

  interface ProductService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<ProductResponse> getProductByKey(@FieldMap Map<String, String> params);
  }

  private final ProductService productService;

  private ProductRetrofit() {
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

    productService = restAdapter.create(ProductService.class);
  }

  private static class SingletonHolder {
    private static ProductRetrofit instance = new ProductRetrofit();
  }

  public static ProductRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 搜索分类
   */
  public Observable<List<ProductEntity>> getProductByKey(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<ProductResponse>>() {
      @Override public Observable<ProductResponse> call() {
        return productService.getProductByKey(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<ProductResponse, Observable<ProductResponse>>() {
      @Override public Observable<ProductResponse> call(ProductResponse productResponse) {
        return productResponse.filterWebServiceErrors();
      }
    }).flatMap(new Func1<ProductResponse, Observable<ProductResponse.Product>>() {
      @Override public Observable<ProductResponse.Product> call(ProductResponse productResponse) {
        return Observable.from(productResponse.getProducts());
      }
    }).map(new Func1<ProductResponse.Product, ProductEntity>() {
      @Override public ProductEntity call(ProductResponse.Product product) {

        ProductEntity clone = instance.newInstance();

        clone.setGoodId(product.goodId);
        clone.setGoodThumbUrl(product.goodThumb);
        clone.setGoodPrice(product.goodPrice);
        clone.setGoodDes(product.goodDes);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<ProductEntity>>applyExecutorSchedulers());
  }
}
