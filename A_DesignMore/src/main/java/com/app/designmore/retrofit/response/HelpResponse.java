package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joker on 2015/9/13.
 */
public class HelpResponse extends BaseResponse {

  /*{
      "title": "如何注册", //问题
      "content": "应该这样中粗恶", //答案
      "article_id": "7" //问题ID
    }
   */

  @Expose @SerializedName("result") private List<Help> helpList = new ArrayList<>();

  public List<Help> getHelpList() {
    return helpList;
  }

  public class Help {

    @Expose @SerializedName("title") public String title;
    @Expose @SerializedName("content") public String content;

    @Override public String toString() {
      return "Help{" +
          "content='" + content + '\'' +
          ", title='" + title + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "HelpResponse{" +
        "helpList=" + helpList +
        '}';
  }
}
