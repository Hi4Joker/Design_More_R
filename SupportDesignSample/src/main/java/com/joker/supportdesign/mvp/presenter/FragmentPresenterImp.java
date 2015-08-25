package com.joker.supportdesign.mvp.presenter;

import com.joker.supportdesign.mvp.domain.event.ItemEvent;
import com.joker.supportdesign.mvp.model.FragmentInteractor;
import com.joker.supportdesign.mvp.model.FragmentInteractorImp;
import com.joker.supportdesign.mvp.viewInterface.FragmentView;
import com.joker.supportdesign.util.EventBusInstance;

/**
 * Created by Administrator on 2015/6/29.
 */
public class FragmentPresenterImp implements FragmentPresenter<FragmentView> {

  private FragmentView fragmentView;
  private FragmentInteractor fragmentInteractor;

  public FragmentPresenterImp() {
  }

  @Override public void attachView(FragmentView view) {
    this.fragmentView = view;
    fragmentInteractor = new FragmentInteractorImp();
   EventBusInstance.getDefault().register(FragmentPresenterImp.this);
  }

  @Override public void detachView(boolean retainInstance) {

    EventBusInstance.getDefault().unregister(FragmentPresenterImp.this);
  }

  @Override public void inflateData() {
    fragmentInteractor.getData();
  }

  public void onEventMainThread(ItemEvent event) {

    fragmentView.setItems(event.getDataList());
  }
}
