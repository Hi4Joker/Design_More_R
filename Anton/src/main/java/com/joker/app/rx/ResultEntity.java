package com.joker.app.rx;

import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Joker on 2015/8/8.
 */
public class ResultEntity {

  public int code;
  public String message;

  public ResultEntity() {
  }

  public ResultEntity(int code, String message) {
    this.code = code;
    this.message = message;
  }

  public Observable<ResultEntity> valueObservable(int userName, String passWord) {
    this.code = userName;
    this.message = passWord;

    /*return Observable.defer(new Func0<Observable<ResultEntity>>() {
      @Override public Observable<ResultEntity> call() {
        return Observable.just(ResultEntity.this);
      }
    });*/
    return Observable.just(ResultEntity.this);
  }
}
