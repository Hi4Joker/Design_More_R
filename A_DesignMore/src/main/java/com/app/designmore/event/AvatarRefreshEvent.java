package com.app.designmore.event;

import android.graphics.Bitmap;
import java.io.File;

/**
 * Created by Joker on 2015/9/15.
 */
public class AvatarRefreshEvent {

  private File file;
  private Bitmap bitmap;

  public AvatarRefreshEvent(File file, Bitmap bitmap) {
    this.file = file;
    this.bitmap = bitmap;
  }

  public Bitmap getBitmap() {
    return bitmap;
  }

  public File getFile() {
    return file;
  }
}
