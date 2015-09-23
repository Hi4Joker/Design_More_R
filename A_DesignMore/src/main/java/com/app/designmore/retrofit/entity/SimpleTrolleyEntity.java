package com.app.designmore.retrofit.entity;

import com.google.gson.annotations.Expose;

/**
 * Created by Joker on 2015/9/23.
 */
public class SimpleTrolleyEntity {

  /*{count:1,attr_id:1,gid:产品ID,is_del:1}*/
  @Expose private String count;
  @Expose private String attr_id;
  @Expose private String gid;
  @Expose private String is_del;

  public SimpleTrolleyEntity(String attr_id, String count, String gid, String is_del) {
    this.attr_id = attr_id;
    this.count = count;
    this.gid = gid;
    this.is_del = is_del;
  }
}
