package com.app.designmore.mvp;

import com.app.designmore.retrofit.entity.Province;
import java.util.List;
import rx.Observable;

/**
 * Created by Joker on 2015/9/23.
 */
public interface ExecutorCallback {

  void onDataFinish(Observable<List<Province>> observable);
}
