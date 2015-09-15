package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/15.
 */
public class JournalResponse extends BaseResponse {

  /* "ad_id": "1",//广告ID
      "position_id": "0",//位置ID
      "media_type": "0",//广告类型
      "ad_name": "杂志001",//广告name
      "ad_desc": "ajdsf lk了",// 广告详情
      "ad_link": "http://api.dmore.com.cn/m/1/index.html",// 广告地址（跳转）
      "ad_code": "1440653354294293740.jpg",//广告图片
      "start_time": "1440316800",//开始时间（显示）
      "end_time": "1443168000",//结束时间（显示）
      "link_man": "",//暂时没用
      "link_email": "",//暂时没用
      "link_phone": "",//暂时没用
      "click_count": "0",//暂时没用
      "enabled": "1"//暂时没用
    */

  @Expose @SerializedName("result") private List<Journal> journals;

  public List<Journal> getJournals() {
    return journals;
  }

  public class Journal {

    @Expose @SerializedName("ad_id") public String journalId;
    @Expose @SerializedName("ad_code") public String journalThumb;
    @Expose @SerializedName("ad_name") public String journalTitle;
    @Expose @SerializedName("ad_desc") public String journalContent;
    @Expose @SerializedName("ad_link") public String journalUrl;
  }
}
