package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.manager.OkClientInstance;
import com.app.designmore.retrofit.entity.CategoryEntity;
import com.app.designmore.retrofit.entity.FashionEntity;
import com.app.designmore.retrofit.entity.ProductEntity;
import com.app.designmore.retrofit.response.CategoryResponse;
import com.app.designmore.retrofit.response.FashionResponse;
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
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * Created by Joker on 2015/9/20.
 */
public class HomeRetrofit {

  private FashionEntity discountInstance = new FashionEntity();
  private CategoryEntity categoryInstance = new CategoryEntity();

  private ProductEntity productinstance = new ProductEntity();

  interface HomeService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<FashionResponse> getDiscountList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<CategoryResponse> getCategoryList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<ProductResponse> getProductByXxx(@FieldMap Map<String, String> params);
  }

  private final HomeService homeService;

  private HomeRetrofit() {
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

    homeService = restAdapter.create(HomeService.class);
  }

  private static class SingletonHolder {
    private static HomeRetrofit instance = new HomeRetrofit();
  }

  public static HomeRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取打折列表
   */
  public Observable<List<FashionEntity>> getDiscountList(final Map<String, String> params) {

    return homeService.getDiscountList(params)
        .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        })
        .concatMap(new Func1<FashionResponse, Observable<FashionResponse>>() {
          @Override public Observable<FashionResponse> call(FashionResponse fashionResponse) {
            return fashionResponse.filterWebServiceErrors();
          }
        })
        .flatMap(new Func1<FashionResponse, Observable<FashionResponse.Fashion>>() {
          @Override
          public Observable<FashionResponse.Fashion> call(FashionResponse fashionResponse) {
            return Observable.from(fashionResponse.getFashions());
          }
        })
        .map(new Func1<FashionResponse.Fashion, FashionEntity>() {
          @Override public FashionEntity call(FashionResponse.Fashion fashion) {

            FashionEntity clone = discountInstance.newInstance();
            clone.setGoodId(fashion.goodId);
            clone.setGoodName(fashion.goodName);
            clone.setGoodDiscount(fashion.goodDiscount);
            clone.setGoodThumbUrl(fashion.goodThumbUrl);

            return clone;
          }
        })
        .toList()
        .compose(SchedulersCompat.<List<FashionEntity>>applyExecutorSchedulers());
  }

  /**
   * 获取分类列表
   */
  public Observable<List<CategoryEntity>> getCategoryList(final Map<String, String> params) {

    return homeService.getCategoryList(params)
        .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        })
        .concatMap(new Func1<CategoryResponse, Observable<CategoryResponse>>() {
          @Override public Observable<CategoryResponse> call(CategoryResponse categoryResponse) {
            return categoryResponse.filterWebServiceErrors();
          }
        })
        .flatMap(new Func1<CategoryResponse, Observable<CategoryResponse.Category>>() {
          @Override
          public Observable<CategoryResponse.Category> call(CategoryResponse categoryResponse) {
            return Observable.from(categoryResponse.getCategories());
          }
        })
        .map(new Func1<CategoryResponse.Category, CategoryEntity>() {
          @Override public CategoryEntity call(CategoryResponse.Category category) {

            CategoryEntity clone = categoryInstance.newInstance();

            clone.setCatId(category.catId);
            clone.setCatName(category.catName);
            clone.setCatThumbUrl(category.catThumbUrl);

            return clone;
          }
        })
        .toList()
        .compose(SchedulersCompat.<List<CategoryEntity>>applyExecutorSchedulers());
  }

  /**
   * 获取精选商品
   */
  public Observable<List<ProductEntity>> getHotProduct(final Map<String, String> params) {

    return homeService.getProductByXxx(params)
        .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        })
        .concatMap(new Func1<ProductResponse, Observable<ProductResponse>>() {
          @Override public Observable<ProductResponse> call(ProductResponse productResponse) {
            return productResponse.filterWebServiceErrors();
          }
        })
        .flatMap(new Func1<ProductResponse, Observable<ProductResponse.Product>>() {
          @Override
          public Observable<ProductResponse.Product> call(ProductResponse productResponse) {
            return Observable.from(productResponse.getProducts());
          }
        })
        .map(new Func1<ProductResponse.Product, ProductEntity>() {
          @Override public ProductEntity call(ProductResponse.Product product) {

            ProductEntity clone = productinstance.newInstance();

            clone.setGoodId(product.goodId);
            clone.setGoodThumbUrl(product.goodThumb);
            clone.setGoodPrice(product.goodPrice);
            clone.setGoodName(product.goodName);

            return clone;
          }
        })
        .toList()
        .compose(SchedulersCompat.<List<ProductEntity>>applyExecutorSchedulers());
  }
}
