package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.retrofit.entity.JournalEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.JournalResponse;
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
public class JournalRetrofit {

  private JournalEntity instance = new JournalEntity();

  interface JournalService {

    //@Headers("Accept-Encoding: application/json")
    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<JournalResponse> getJournalList(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<JournalResponse> requestMoreJournalList(@FieldMap Map<String, String> params);
  }

  private final JournalService collectionService;

  private JournalRetrofit() {
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

    collectionService = restAdapter.create(JournalService.class);
  }

  private static class SingletonHolder {
    private static JournalRetrofit instance = new JournalRetrofit();
  }

  public static JournalRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取杂志列表
   */
  public Observable<List<JournalEntity>> getJournalList(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<JournalResponse>>() {
      @Override public Observable<JournalResponse> call() {
        /*获取杂志列表，超时8秒*/
        return collectionService.getJournalList(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<JournalResponse, Observable<JournalResponse>>() {
      @Override public Observable<JournalResponse> call(JournalResponse journalResponse) {
        return journalResponse.filterWebServiceErrors();
      }
    }).flatMap(new Func1<JournalResponse, Observable<JournalResponse.Journal>>() {
      @Override public Observable<JournalResponse.Journal> call(JournalResponse journalResponse) {
        return Observable.from(journalResponse.getJournals());
      }
    }).map(new Func1<JournalResponse.Journal, JournalEntity>() {
      @Override public JournalEntity call(JournalResponse.Journal journal) {

        JournalEntity clone = instance.newInstance();

        clone.setJournalId(journal.journalId);
        clone.setJournalThumbUrl(journal.journalThumb);
        clone.setJournalTitle(journal.journalTitle);
        clone.setJournalContent(journal.journalContent);
        clone.setJournalUrl(journal.journalUrl);

        return clone;
      }
    }).toList().compose(SchedulersCompat.<List<JournalEntity>>applyExecutorSchedulers());
  }

  /**
   * 获取更多杂志列表
   */
  public Observable<BaseResponse> requestDeleteCollection(final Map<String, String> params) {

   /* return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return collectionService.requestMoreJournalList(params)
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
        *//*删除成功*//*
        return baseResponse;
      }
    }).compose(SchedulersCompat.<BaseResponse>applyExecutorSchedulers());
  }*/

    return null;
  }
}
