package com.joker.supportdesign.util;

import android.support.annotation.NonNull;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Joker on 2015/8/24.
 */
public class ExecutorManager {

  public static ExecutorService eventExecutor;
  //private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
  private static final int CPU_COUNT = EventBusInstance.getCountOfCPU();
  private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
  private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
  private static final int KEEP_ALIVE = 1;
  private static final BlockingQueue<Runnable> eventPoolWaitQueue = new LinkedBlockingQueue<>(128);
  private static final ThreadFactory eventThreadFactory = new ThreadFactory() {
    private final AtomicInteger mCount = new AtomicInteger(1);

    public Thread newThread(@NonNull Runnable r) {
      return new Thread(r, "eventAsyncAndBackground #" + mCount.getAndIncrement());
    }
  };

  private static final RejectedExecutionHandler eventHandler =
      new ThreadPoolExecutor.CallerRunsPolicy();

  static {
    eventExecutor =
        new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS,
            eventPoolWaitQueue, eventThreadFactory, eventHandler);
  }
}
