>整理自：[Retrofit and RxJava](https://speakerdeck.com/jakewharton/2014-1)


![Retrofit](http://upload-images.jianshu.io/upload_images/268450-b40af2a92b0192a0.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

原文以幻灯片的形式，展示了一个Retrofit结合RxJava使用的简单案例。喜欢收藏的同学可以点击原网页下载PDF。

作为译文集中的一篇，此处小鄧子直接戳重点，不废话。

##Sample##

调用GitHub API（[GitHub API v3](https://developer.github.com/v3/)）。
获取贡献者列表。

GET请求：/repos/:owner/:repo/contributors

首先定义一个服务接口：

    interface GitHub {
    @GET("/repos/{owner}/{repo}/contributors") List<Contributor> contributors(
        @Path("owner") String owner, @Path("repo") String repo);
    }

    class Contributor {
     String login;
     long contributions;
    }


创建Retrofit实例：

    RestAdapter restAdapter =
        new RestAdapter.Builder().setEndpoint("https://api.github.com/").build();
    GitHub gitHub = restAdapter.create(GitHub.class);

    List<Contributor> contributors = gitHub.contributors("netflix", "rxjava");

    for (Contributor c : contributors) {
      println(c.login + '\t' + c.contributions);
    }

输出：

     1483 benjchristensen
     225 zsxwing
     167 samuelgruetter
     146 jmhofer
     137 akarnokd
     105 DavidMGross
     102 AppliedDuality
     ...

Retrofit支持两种形式的查询操作：

1.固定参数：


    interface GitHub {
    @GET("/repos/{owner}/{repo}/contributors?anon=true") List<Contributor> contributors(
        @Path("owner") String owner, @Path("repo") String repo);
    }

2.动态参数：


    interface GitHub {
    @GET("/repos/{owner}/{repo}/contributors") List<Contributor> contributors(
        @Path("owner") String owner, @Path("repo") String repo,
        @Query("anon") boolean includeAnonymous);
    }
    List<Contributor> contributors = gitHub.contributors("netflix", "rxjava", true);


Retrofit POST请求写成这样，可以将参数放在请求body中：


    interface GitHub {
    @POST("/repos/{owner}/{repo}/hooks") Response createHook(@Path("owner") String owner,
        @Path("repo") String repo, @Body Hook hook);
    }

    class Hook {
      String name;
      Map<String, Object> config;
      List<String> events;
      boolean active;
    }

     OkHttpClient client = new OkHttpClient();
     client.setProtocols(Arrays.asList(Protocol.HTTP_2));

     RestAdapter restAdapter2 = new RestAdapter.Builder().setEndpoint("https://drive.google.com/api/")
        .setClient(new OkClient(client))
        .setConverter(new ProtoConverter())
        .build();

     GitHub gitHub2 = restAdapter2.create(GitHub.class);


![Retrofit 1](http://upload-images.jianshu.io/upload_images/268450-bfcfd5fef1596522.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

上图可见，Retrofit 1已经足够优秀了，而且还支持RxJava。

##RxJava + Retrofit##

    interface GitHub {
    @GET("/repos/{owner}/{repo}/contributors") Observable<List<Contributor>> contributors(
        @Path("owner") String owner, @Path("repo") String repo);

    @GET("/users/{user}") Observable<User> user(@Path("user") String user);

    @GET("/users/{user}/starred") Observable<List<Repo>> starred(@Path("user") String user);
    }

    class User {
     String name;
    }

    class Contributor {
      String login;
      long contributions;
    }

     class Repo implements Comparable {
     String full_name;
     @Override public int compareTo(Object another) {
       return 0;
      }
     }

查询名字不为空的贡献者：

    gitHub.contributors("netflix", "rxjava")
        .lift(flattenList())
        .flatMap(c -> gitHub.user(c.login))
        .filter(user -> user.name != null)
        .forEach(user -> println(user.name));

输出结果：

    Ben Christensen
    Shixiong Zhu
    Joachim Hofer
    David Gross
    Matthias Käppler
    Justin Ryan
    Mairbek Khadikov
    ...


根据FullName排序：


    gitHub.contributors("netflix", "rxjava")
        .lift(flattenList())
        .flatMap(c -> gitHub.starred(c.login))
        .lift(flattenList())
        .groupBy(r -> r.full_name)
        .flatMap(g -> g.count().map(c -> c + "\t" + g.getKey()))
        .toSortedList((a, b) -> b.compareTo(a))
        .lift(flattenList())
        .take(8)
        .forEach(Main::println);


输出结果：

     7 Netflix/RxJava
     2 twitter/finagle
     2 scala/scala
     2 mbostock/d3
     2 kpelykh/docker-java
     2 Netflix/zuul
     2 Netflix/feign
     2 Netflix/archaius

筛选来自Square的贡献者

    gitHub.contributors("square", "retrofit")
        .lift(flattenList())
        .flatMap(c -> gitHub.starred(c.login))
        .lift(flattenList())
        .filter(r -> !r.full_name.startsWith("square/"))
        .groupBy(r -> r.full_name)
        .flatMap(g -> g.count().map(c -> c + "\t" + g.getKey()))
        .toSortedList((a, b) -> b.compareTo(a))
        .lift(flattenList())
        .take(8)
        .forEach(Main::println);

输出结果：

    4 frankiesardo/auto-parcel
    4 Comcast/FreeFlow
    3 xxv/android-lifecycle
    3 robolectric/robolectric
    3 inmite/android-butterknife-zelezny
    3 google/auto
    3 facebook/rebound
    3 etsy/AndroidStaggeredGriddddddddddddddddd


