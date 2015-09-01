package com.app.designmore.retrofit;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Protocol;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.appengine.UrlFetchClient;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.observables.GroupedObservable;

/**
 * Created by Joker on 2015/9/1.
 */
public class RetrofitSample {

  class User {
    String name;
  }

  class Repo implements Comparable {
    String full_name;

    @Override public int compareTo(Object another) {
      return 0;
    }
  }

  class Contributor {
    String login;
    long contributions;
  }

  class Hook {
    String name;
    Map<String, Object> config;
    List<String> events;
    boolean active;
  }

  interface GitHub {

    @GET("/repos/{owner}/{repo}/contributors") List<Contributor> contributors(
        @Path("owner") String owner, @Path("repo") String repo,
        @Query("anon") boolean includeAnonymous);

    @POST("/repos/{owner}/{repo}/hooks") Response createHook(@Path("owner") String owner,
        @Path("repo") String repo, @Body Hook hook);

    @GET("/repos/{owner}/{repo}/contributors") Observable<List<Contributor>> contributors(
        @Path("owner") String owner, @Path("repo") String repo);

    @GET("/users/{user}") Observable<User> user(@Path("user") String user);

    @GET("/users/{user}/starred") Observable<List<Repo>> starred(@Path("user") String user);
  }

  public void getService() {

    RequestInterceptor requestInterceptor = new RequestInterceptor() {
      @Override public void intercept(RequestInterceptor.RequestFacade request) {
        request.addHeader("Accept", "application/json");
      }
    };

    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation() //不导出实体中没有用@Expose注解的属性
        .enableComplexMapKeySerialization() //支持Map的key为复杂对象的形式
        .serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss:SSS")//时间转化为特定格式
        .setFieldNamingPolicy(
            FieldNamingPolicy.UPPER_CAMEL_CASE)////会把字段首字母大写,注:对于实体上使用了@SerializedName注解的不会生效.
        .setPrettyPrinting() //对json结果格式化.
        .setVersion(1.0)    //有的字段不是一开始就有的,会随着版本的升级添加进来,那么在进行序列化和返序列化的时候就会根据版本号来选择是否要序列化.
            //@Since(版本号)能完美地实现这个功能.还的字段可能,随着版本的升级而删除,那么
            //@Until(版本号)也能实现这个功能,GsonBuilder.setVersion(double)方法需要调用.
        .create();

    RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("https://api.github.com/")
        .setRequestInterceptor(requestInterceptor)
        .setConverter(new GsonConverter(gson))
        .build();
    GitHub gitHub = restAdapter.create(GitHub.class);

    /**
     * **************GET
     */

    List<Contributor> contributors = gitHub.contributors("netflix", "rxjava", true);

    for (Contributor c : contributors) {
      System.load(c.login + '\t' + c.contributions);
    }

    /**
     * 1483 benjchristensen
     225 zsxwing
     167 samuelgruetter
     146 jmhofer
     137 akarnokd
     105 DavidMGross
     102 AppliedDuality
     ...
     */

    /**
     * **************POST
     */
    OkHttpClient client = new OkHttpClient();
    client.setProtocols(Arrays.asList(Protocol.HTTP_2));

    RestAdapter restAdapter2 =
        new RestAdapter.Builder().setEndpoint("https://drive.google.com/api/").setClient(
            new OkClient(client))
            //.setConverter(new ProtoConverter())
            .build();

    GitHub gitHub2 = restAdapter2.create(GitHub.class);
    Response response = gitHub2.createHook("", "", new Hook());
    response.getBody();

    /**
     * ********** RxJava
     */

    /*gitHub.contributors("netflix", "rxjava")
        .lift(flattenList())
        .flatMap(c -> gitHub.starred(c.login))
        .lift(flattenList())
        .groupBy(r -> r.full_name)
        .flatMap(g -> g.count().map(c -> c + "\t" + g.getKey()))
        .toSortedList((a, b) -> b.compareTo(a))
        .lift(flattenList())
        .take(8)
        .forEach(Main::println);*/

    Observable.just(new Repo()).groupBy(new Func1<Repo, String>() {
      @Override public String call(Repo repo) {
        return repo.full_name;
      }
    }).flatMap(new Func1<GroupedObservable<String, Repo>, Observable<Repo>>() {
      @Override public Observable<Repo> call(
          final GroupedObservable<String, Repo> stringRepoGroupedObservable) {

        stringRepoGroupedObservable.count().map(new Func1<Integer, Integer>() {
          @Override public Integer call(Integer integer) {

            stringRepoGroupedObservable.getKey();

            return null;
          }
        });

        return null;
      }
    }).flatMap(new Func1<Repo, Observable<Repo>>() {
      @Override public Observable<Repo> call(Repo repo) {

        return null;
      }
    }).toSortedList(new Func2<Repo, Repo, Integer>() {
      @Override public Integer call(Repo o, Repo o2) {

        return null;
      }
    });

   /* gitHub.contributors("netflix", "rxjava")
        .lift(flattenList())
        .flatMap(c -> gitHub.user(c.login))
        .filter(user -> user.name != null)
        .forEach(user -> println(user.name));*/

    /**
     * 7 Netflix/RxJava
     2 twitter/finagle
     2 scala/scala
     2 mbostock/d3
     2 kpelykh/docker-java
     2 Netflix/zuul
     2 Netflix/feign
     2 Netflix/archaius
     ...
     */

    /**
     *
     */

    /*gitHub.contributors("square", "retrofit")
        .lift(flattenList())
        .flatMap(c -> gitHub.starred(c.login))
        .lift(flattenList())
        .filter(r -> !r.full_name.startsWith("square/"))
        .groupBy(r -> r.full_name)
        .flatMap(g -> g.count().map(c -> c + "\t" + g.getKey()))
        .toSortedList((a, b) -> b.compareTo(a))
        .lift(flattenList())
        .take(8)
        .forEach(Main::println);*/

    /**
     * 4 frankiesardo/auto-parcel
     4 Comcast/FreeFlow
     3 xxv/android-lifecycle
     3 robolectric/robolectric
     3 inmite/android-butterknife-zelezny
     3 google/auto
     3 facebook/rebound
     3 etsy/AndroidStaggeredGrid
     */
  }
}
