package com.joker.supportdesign.mvp.viewInterface;

import com.joker.supportdesign.mvp.domain.Animal;
import java.util.List;

/**
 * Created by Administrator on 2015/6/29.
 */
public interface FragmentView extends MvpView {

  void setItems(List<Animal> dataItems);
}
