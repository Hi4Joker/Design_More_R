package com.joker.app.rx;

import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Joker on 2015/8/8.
 */
public class UserInfoEntity {

  public String userName;
  public String passWord;

  public Observable<UserInfoEntity> valueObservable(String userName, String passWord) {
    this.userName = userName;
    this.passWord = passWord;

    return Observable.defer(new Func0<Observable<UserInfoEntity>>() {
      @Override public Observable<UserInfoEntity> call() {
        return Observable.just(UserInfoEntity.this);
      }
    });
  }
}
