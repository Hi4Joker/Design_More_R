package com.app.designmore.manager;

import android.os.Build;
import android.support.annotation.NonNull;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joker on 2015/6/29.
 */
public class EventBusInstance {

  private volatile static EventBus defaultInstance;

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
}
