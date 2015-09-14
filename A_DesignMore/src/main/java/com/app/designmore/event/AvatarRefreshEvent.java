package com.app.designmore.event;

import java.io.File;

/**
 * Created by Joker on 2015/9/15.
 */
public class AvatarRefreshEvent {

  private File file;

  public AvatarRefreshEvent(File file) {
    this.file = file;
  }

  public File getFile() {
    return file;
  }
}
