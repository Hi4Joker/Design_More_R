package com.app.designmore.retrofit;

import android.support.annotation.CheckResult;
import com.app.designmore.Constants;
import com.app.designmore.manager.OkClientInstance;
import com.app.designmore.retrofit.entity.SearchItemEntity;
import com.app.designmore.retrofit.response.SearchListResponse;
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
public class SearchRetrofit {

  interface SearchService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<SearchListResponse> getHotSearchList(@FieldMap Map<String, String> params);
  }

  private final SearchService searchService;

  private SearchRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
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

    searchService = restAdapter.create(SearchService.class);
  }

  private static class SingletonHolder {
    private static SearchRetrofit instance = new SearchRetrofit();
  }

  public static SearchRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取热搜列表
   */
  @CheckResult public Observable<List<SearchItemEntity>> getHotSearchList(
      final Map<String, String> params) {

    return searchService.getHotSearchList(params)
        .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS)
        .retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            return throwable instanceof TimeoutException && integer < 1;
          }
        })
        .concatMap(new Func1<SearchListResponse, Observable<SearchListResponse>>() {
          @Override
          public Observable<SearchListResponse> call(SearchListResponse searchListResponse) {
            return searchListResponse.filterWebServiceErrors();
          }
        })
        .map(new Func1<SearchListResponse, List<SearchItemEntity>>() {
          @Override public List<SearchItemEntity> call(SearchListResponse searchListResponse) {

            final ArrayList<SearchItemEntity> searchItemEntities = new ArrayList<>();
            SearchItemEntity instance = new SearchItemEntity();

            for (String text : searchListResponse.getResult()) {

              SearchItemEntity clone = instance.newInstance();
              clone.setText(text);
              searchItemEntities.add(clone);
            }

            return searchItemEntities;
          }
        })
        .compose(SchedulersCompat.<List<SearchItemEntity>>applyExecutorSchedulers());
  }
}
