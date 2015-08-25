package com.joker.supportdesign.mvp.domain.event;

import com.joker.supportdesign.mvp.domain.Animal;
import java.util.List;

/**
 * Created by Joker on 2015/6/29.
 */
public class ItemEvent {

  private List<Animal> dataList;

  public ItemEvent(List<Animal> dataList) {
    this.dataList = dataList;
  }

  public List<Animal> getDataList() {
    return dataList;
  }

  @Override public String toString() {
    return dataList.toString();
  }
}
