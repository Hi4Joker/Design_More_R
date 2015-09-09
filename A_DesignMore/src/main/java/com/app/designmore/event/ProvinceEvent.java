package com.app.designmore.event;

import com.app.designmore.retrofit.entity.Province;
import java.util.ArrayList;

/**
 * Created by Joker on 2015/9/10.
 */
public class ProvinceEvent {

  private ArrayList<Province> provinces;

  public ProvinceEvent(ArrayList<Province> provinces) {
    this.provinces = provinces;
  }

  public ArrayList<Province> getProvinces() {
    return provinces;
  }
}
