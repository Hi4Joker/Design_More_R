package com.app.designmore.retrofit.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Created by Joker on 2015/9/24.
 */
public class CategoryResponse extends BaseResponse {


  /*"result": [
        {
            "cat_id": "5",
            "cat_name": "分类1",
            "icon": "http://api.dmore.com.cn/type_image/type1.jpg"
        },
        {
            "cat_id": "6",
            "cat_name": "分类2",
            "icon": "http://api.dmore.com.cn/type_image/type2.jpg"
        },
        {
            "cat_id": "7",
            "cat_name": "分类3",
            "icon": "http://api.dmore.com.cn/type_image/type3.jpg"
        }
    ]*/

  @Expose @SerializedName("result") private List<Category> categories;

  public List<Category> getCategories() {
    return categories;
  }

  public class Category {

    @Expose @SerializedName("cat_id") public String catId;
    @Expose @SerializedName("cat_name") public String catName;
    @Expose @SerializedName("icon") public String catThumbUrl;

    @Override public String toString() {
      return "Category{" +
          "catId='" + catId + '\'' +
          ", catName='" + catName + '\'' +
          ", catThumbUrl='" + catThumbUrl + '\'' +
          '}';
    }
  }

  @Override public String toString() {
    return "CategoryResponse{" +
        "categories=" + categories +
        '}';
  }
}
