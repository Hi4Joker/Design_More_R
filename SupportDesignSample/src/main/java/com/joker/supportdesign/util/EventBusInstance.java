package com.joker.supportdesign.util;

import android.os.Build;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.io.FileFilter;

/**
 * Created by Joker on 2015/6/29.
 */
public class EventBusInstance {

  private static EventBus defaultInstance;

  public static final int DEVICEINFO_UNKNOWN = -1;

  public static EventBus getDefault() {
    if (defaultInstance == null) {
      synchronized (EventBusInstance.class) {
        if (defaultInstance == null) {

          defaultInstance = EventBus.builder()
              .logNoSubscriberMessages(false)
              .sendNoSubscriberEvent(false)
              .logSubscriberExceptions(false)
              .sendSubscriberExceptionEvent(false)
              .eventInheritance(false)
              .executorService(ExecutorManager.eventExecutor)
              .installDefaultEventBus();
        }
      }
    }
    return defaultInstance;
  }

  /**
   * Linux中的设备都是以文件的形式存在，CPU也不例外，因此CPU的文件个数就等价与核数。
   * Android的CPU 设备文件位于/sys/devices/system/cpu/目录，文件名的的格式为cpu\d+。
   */
  public static int getCountOfCPU() {

    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
      return 1;
    }
    int count;
    try {
      count = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
    } catch (SecurityException | NullPointerException e) {
      count = DEVICEINFO_UNKNOWN;
    }
    return count;
  }

  private static final FileFilter CPU_FILTER = new FileFilter() {
    @Override public boolean accept(File pathname) {

      String path = pathname.getName();
      if (path.startsWith("cpu")) {
        for (int i = 3; i < path.length(); i++) {
          if (path.charAt(i) < '0' || path.charAt(i) > '9') {
            return false;
          }
        }
        return true;
      }
      return false;
    }
  };
}
