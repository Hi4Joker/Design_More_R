package com.joker.app.rx;

import android.util.Log;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.functions.Func1;

public class Encryption {

  private static final String TAG = "LaunchActivity";

  public static Observable<UserInfoEntity> EncryptionStream(
      Observable<UserInfoEntity> loginStream) {

    final Observable<UserInfoEntity> EncryptionObservable =
        loginStream.flatMap(new Func1<UserInfoEntity, Observable<UserInfoEntity>>() {
          @Override public Observable<UserInfoEntity> call(UserInfoEntity userInfoEntity) {

            /*Do Encryption*/
            String userName = userInfoEntity.userName + "Joker";
            String passWord = userInfoEntity.passWord + "Joker";

            return userInfoEntity.valueObservable(userName, passWord);
          }
        }).doOnNext(new Action1<UserInfoEntity>() {
          @Override public void call(UserInfoEntity userInfoEntity) {
            Log.e(TAG, "加密后：userName：" + userInfoEntity.userName);
            Log.e(TAG, "加密后：passWord：" + userInfoEntity.passWord);
          }
        });

    return EncryptionObservable;
  }
}
