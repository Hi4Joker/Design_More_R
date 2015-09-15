package com.app.designmore.retrofit.entity;

import java.io.Serializable;

/**
 * Created by Joker on 2015/9/15.
 */
public class JournalEntity implements Cloneable ,Serializable{

  private String journalId;
  private String thumbUrl;
  private String journalTitle;
  private String journalContent;
  private String journalUrl;

  public String getJournalContent() {
    return journalContent;
  }

  public void setJournalContent(String journalContent) {
    this.journalContent = journalContent;
  }

  public String getJournalId() {
    return journalId;
  }

  public void setJournalId(String journalId) {
    this.journalId = journalId;
  }

  public String getThumbUrl() {
    return thumbUrl;
  }

  public void setThumbUrl(String thumbUrl) {
    this.thumbUrl = thumbUrl;
  }

  public String getJournalTitle() {
    return journalTitle;
  }

  public void setJournalTitle(String journalTitle) {
    this.journalTitle = journalTitle;
  }

  public String getJournalUrl() {
    return journalUrl;
  }

  public void setJournalUrl(String journalUrl) {
    this.journalUrl = journalUrl;
  }

  public JournalEntity newInstance() {

    try {
      return (JournalEntity) super.clone();
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override protected Object clone() throws CloneNotSupportedException {
    JournalEntity journalEntity = (JournalEntity) super.clone();
    return journalEntity;
  }

  @Override public String toString() {
    return "JournalEntity{" +
        "journalId='" + journalId + '\'' +
        ", thumbUrl='" + thumbUrl + '\'' +
        ", journalTitle='" + journalTitle + '\'' +
        ", journalContent='" + journalContent + '\'' +
        ", journalUrl='" + journalUrl + '\'' +
        '}';
  }
}
