package com.app.designmore.retrofit;

import com.app.designmore.Constants;
import com.app.designmore.exception.WebServiceException;
import com.app.designmore.retrofit.entity.HelpEntity;
import com.app.designmore.retrofit.entity.LoginCodeEntity;
import com.app.designmore.retrofit.entity.LoginEntity;
import com.app.designmore.retrofit.entity.RegisterEntity;
import com.app.designmore.retrofit.entity.RetrieveEntity;
import com.app.designmore.retrofit.response.BaseResponse;
import com.app.designmore.retrofit.response.HelpResponse;
import com.app.designmore.retrofit.response.LoginCodeResponse;
import com.app.designmore.retrofit.response.LoginResponse;
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
 * Created by Joker on 2015/9/8.
 */
public class LoginRetrofit {

  private HelpEntity helpInstance = new HelpEntity();

  interface LoginService {

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<LoginCodeResponse> getLoginCode(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestRegister(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<BaseResponse> requestRetrieve(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php")
    Observable<LoginResponse> requestLogin(@FieldMap Map<String, String> params);

    @FormUrlEncoded @POST("/mobile/api/client/interface.php") Observable<HelpResponse> getHelpList(
        @FieldMap Map<String, String> params);
  }

  private final LoginService loginService;

  private LoginRetrofit() {
    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestFacade request) {
        request.addHeader("Accept-Encoding", "application/json");
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

    loginService = restAdapter.create(LoginService.class);
  }

  private static class SingletonHolder {
    private static LoginRetrofit instance = new LoginRetrofit();
  }

  public static LoginRetrofit getInstance() {
    return SingletonHolder.instance;
  }

  /**
   * 获取验证码
   */
  public Observable<LoginCodeEntity> getAuthCode(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<LoginCodeResponse>>() {
      @Override public Observable<LoginCodeResponse> call() {
        return loginService.getLoginCode(params).timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<LoginCodeResponse, Observable<LoginCodeResponse>>() {
      @Override public Observable<LoginCodeResponse> call(LoginCodeResponse loginCodeResponse) {
        return loginCodeResponse.filterWebServiceErrors();
      }
    }).map(new Func1<LoginCodeResponse, LoginCodeEntity>() {
      @Override public LoginCodeEntity call(LoginCodeResponse loginCodeResponse) {

        LoginCodeEntity loginCodeEntity = new LoginCodeEntity(loginCodeResponse.getCode());

        return loginCodeEntity;
      }
    }).compose(SchedulersCompat.<LoginCodeEntity>applyExecutorSchedulers());
  }

  /**
   * 注册
   */
  public Observable<RegisterEntity> requestRegister(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return loginService.requestRegister(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(final BaseResponse baseResponse) {
        return baseResponse.filterWebServiceErrors()
            .onErrorResumeNext(new Func1<Throwable, Observable>() {
              @Override public Observable call(Throwable throwable) {
                if (throwable instanceof WebServiceException
                    && baseResponse.resultCode == Constants.RESULT_FAIL) {
                  return Observable.just(baseResponse);
                }
                return Observable.error(throwable);
              }
            });
      }
    }).map(new Func1<BaseResponse, RegisterEntity>() {
      @Override public RegisterEntity call(BaseResponse baseResponse) {
        return new RegisterEntity(baseResponse.resultCode, baseResponse.message);
      }
    }).compose(SchedulersCompat.<RegisterEntity>applyExecutorSchedulers());
  }

  /**
   * 找回密码
   */
  public Observable<RetrieveEntity> requestRetrieve(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call() {
        return loginService.requestRetrieve(params)
            .timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<BaseResponse, Observable<BaseResponse>>() {
      @Override public Observable<BaseResponse> call(final BaseResponse baseResponse) {
        return baseResponse.filterWebServiceErrors()
            .onErrorResumeNext(new Func1<Throwable, Observable>() {
              @Override public Observable call(Throwable throwable) {
                if (throwable instanceof WebServiceException
                    && baseResponse.resultCode == Constants.RESULT_FAIL) {
                  return Observable.just(baseResponse);
                }
                return Observable.error(throwable);
              }
            });
      }
    }).map(new Func1<BaseResponse, RetrieveEntity>() {
      @Override public RetrieveEntity call(BaseResponse baseResponse) {
        return new RetrieveEntity(baseResponse.resultCode, baseResponse.message);
      }
    }).compose(SchedulersCompat.<RetrieveEntity>applyExecutorSchedulers());
  }

  /**
   * 登陆
   */
  public Observable<LoginEntity> requestLogin(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<LoginResponse>>() {
      @Override public Observable<LoginResponse> call() {
        return loginService.requestLogin(params).timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<LoginResponse, Observable<LoginResponse>>() {
      @Override public Observable<LoginResponse> call(LoginResponse loginResponse) {
        return loginResponse.filterWebServiceErrors();
      }
    }).map(new Func1<LoginResponse, LoginEntity>() {
      @Override public LoginEntity call(LoginResponse loginResponse) {
        return new LoginEntity(loginResponse.getLoginInfo().userId,
            loginResponse.getLoginInfo().addressId);
      }
    }).compose(SchedulersCompat.<LoginEntity>applyExecutorSchedulers());
  }

  /**
   * 获取帮助中心列表
   */
  public Observable<List<HelpEntity>> getHelpList(final Map<String, String> params) {

    return Observable.defer(new Func0<Observable<HelpResponse>>() {
      @Override public Observable<HelpResponse> call() {
        /*获取帮助列表，超时8秒*/
        return loginService.getHelpList(params).timeout(Constants.TIME_OUT, TimeUnit.MILLISECONDS);
      }
    }).retry(new Func2<Integer, Throwable, Boolean>() {
      @Override public Boolean call(Integer integer, Throwable throwable) {
        return throwable instanceof TimeoutException && integer < 1;
      }
    }).concatMap(new Func1<HelpResponse, Observable<HelpResponse>>() {
      @Override public Observable<HelpResponse> call(HelpResponse helpResponse) {
        return helpResponse.filterWebServiceErrors();
      }
    }).flatMap(new Func1<HelpResponse, Observable<HelpResponse.Help>>() {
      @Override public Observable<HelpResponse.Help> call(HelpResponse helpResponse) {
        return Observable.from(helpResponse.getHelpList());
      }
    }).map(new Func1<HelpResponse.Help, HelpEntity>() {
      @Override public HelpEntity call(HelpResponse.Help help) {

        HelpEntity helpEntity = helpInstance.newInstance();
        helpEntity.setTitle(help.title);
        helpEntity.setContent(help.content);

        return helpEntity;
      }
    }).toList().compose(SchedulersCompat.<List<HelpEntity>>applyExecutorSchedulers());
  }
}
