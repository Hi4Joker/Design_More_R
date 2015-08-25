package com.futurice.project;

import android.util.Log;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Func1;

public class Solution<E> {

  public static final String SECRET_SEQUENCE = "ABBABA"; // this is the password the user must input
  public static final long SEQUENCE_TIMEOUT = 4000; // this is the timeframe in milliseconds
  private static final String TAG = Solution.class.getSimpleName();

  /**
   * Given two buttons (A & B) we want to print a "CORRECT!" message if a certain "secret sequence"
   * is clicked within a given timeframe. The "secret sequence" is the combination "ABBABA" that
   * has to be inputted within 4 seconds. In other words, the user must input the password, but
   * must do it quickly.
   *
   * @param inputStream an observable event stream of String, either "A" String or "B" String.
   * @param scheduler if you need a scheduler, use this one provided.
   * @return an observable event stream of String, that returns the secret sequence AT THE RIGHT
   * time according to the problem specification.
   */
  public static Observable<String> defineSuccessStream(Observable<String> inputStream,
      Scheduler scheduler) {

   /* return inputStream.map(new Func1<String, String>() {
      @Override public String call(String s) {

        Log.e(TAG, s + "2");

        return s + "2";
      }
    }).observeOn(scheduler);*/


    //return Observable.error(new Throwable(""));

    return Observable.empty();


    // TODO Your code goes here. Return an Observable<String> to solve the problem.
    // You can use the statics SECRET_SEQUENCE and SEQUENCE_TIMEOUT here.
    //return Observable.empty(); // TODO delete me and replace with your solution
  }

  public <T> T getTAG() {

    return null;
  }

  public interface Func3<T extends R, R> {
    <O> O call(O o);
  }

  public <T> void get(T[] ts, int index) {

  }

  public <K> K[] toArray(K[] array) {

    return null;
  }

  public <A, B extends A, C> B testGenericMethodDefine(A a, B b, C c) {

    return null;
  }
}
