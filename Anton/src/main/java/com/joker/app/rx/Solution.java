package com.joker.app.rx;

import android.text.TextUtils;
import android.util.Log;
import com.joker.app.rx.error.PassWordError;
import com.joker.app.rx.error.UserNameError;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

public class Solution {

  public static final long SEQUENCE_TIMEOUT = 4000;
  //private static final String TAG = Solution.class.getSimpleName();
  private static final String TAG = "LaunchActivity";

  public static Observable<UserInfoEntity> checkLoginStream(
      Observable<UserInfoEntity> loginStream) {

    final Observable<UserInfoEntity> uncheckedObservable =
        loginStream.flatMap(new Func1<UserInfoEntity, Observable<UserInfoEntity>>() {
          @Override public Observable<UserInfoEntity> call(UserInfoEntity userInfoEntity) {

            Log.e(TAG, "输入：userName：" + userInfoEntity.userName);
            Log.e(TAG, "输入：passWord：" + userInfoEntity.passWord);

            userInfoEntity.userName = userInfoEntity.userName.trim();
            userInfoEntity.passWord = userInfoEntity.passWord.trim();

            if (TextUtils.isEmpty(userInfoEntity.userName)) {
              return Observable.error(new UserNameError("Username cannot be empty"));
            } else if (TextUtils.isEmpty(userInfoEntity.passWord)) {
              return Observable.error(new PassWordError("Password cannot be empty"));
            }
            return Observable.just(userInfoEntity);
          }
        })/*.onErrorResumeNext(new Func1<Throwable, Observable<? extends UserInfoEntity>>() {
          @Override public Observable<? extends UserInfoEntity> call(Throwable throwable) {

            Log.e(TAG, "onErrorResumeNext:" + throwable.getMessage());

           *//* final UserInfoEntity userInfoEntity = new UserInfoEntity();
            final String userName = "ErrorUsername";
            final String passWord = "ErrorPassword";

            return userInfoEntity.valueObservable(userName, passWord);*//*

            return Observable.error(throwable);
          }
        })*//*.onErrorReturn(new Func1<Throwable, UserInfoEntity>() {
          @Override public UserInfoEntity call(Throwable throwable) {

            Log.e(TAG, "onErrorReturn:" + throwable.getMessage());

            final UserInfoEntity userInfoEntity = new UserInfoEntity();
            userInfoEntity.userName = "joker";
            userInfoEntity.passWord = "joker";

            return userInfoEntity;
          }
        })*/
        /*.retry(new Func2<Integer, Throwable, Boolean>() {
          @Override public Boolean call(Integer integer, Throwable throwable) {
            if (throwable instanceof UserNameError && integer <= 2) {
              Log.e(TAG, "userName格式错误" + integer);
              return true;
            }
            Log.e(TAG, "userName格式错误" + integer);
            return false;
          }
        })*/;

    return Encryption.EncryptionStream(uncheckedObservable);
  }
}
