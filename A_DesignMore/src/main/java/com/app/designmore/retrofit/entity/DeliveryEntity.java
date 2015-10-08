package com.app.designmore.retrofit.entity;

import java.io.Serializable;

/**
 * Created by Joker on 2015/10/8.
 */
public class DeliveryEntity implements Serializable, Cloneable {

  private String deliveryId;
  private String deliveryName;
  private String deliveryBaseFee;

  @Override public boolean equals(Object obj) {

    if (obj == null || this.getClass() != obj.getClass() || !this.getDeliveryId()
        .equals(((DeliveryEntity) obj).getDeliveryId())) {
      return false;
    }

    return true;
  }

  public String getDeliveryId() {
    return deliveryId;
  }

  public void setDeliveryId(String deliveryId) {
    this.deliveryId = deliveryId;
  }

  public String getDeliveryName() {
    return deliveryName;
  }

  public void setDeliveryName(String deliveryName) {
    this.deliveryName = deliveryName;
  }

  public String getDeliveryBaseFee() {
    return deliveryBaseFee;
  }

  public void setDeliveryBaseFee(String deliveryBaseFee) {
    this.deliveryBaseFee = deliveryBaseFee;
  }

  public DeliveryEntity newInstance() {

    try {
      return (DeliveryEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    DeliveryEntity deliveryEntity = (DeliveryEntity) super.clone();
    return deliveryEntity;
  }

  @Override public String toString() {
    return "DeliveryEntity{" +
        "deliveryId='" + deliveryId + '\'' +
        ", deliveryName='" + deliveryName + '\'' +
        ", deliveryBaseFee='" + deliveryBaseFee + '\'' +
        '}';
  }
}
