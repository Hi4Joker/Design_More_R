package com.app.designmore.utils;


import android.util.Log;

public class MLog {
    /**
     * 测试阶段:show:true   [ debug将会开启.用于控制MLog的打印和其他log ]
     * 发布阶段:show:false  [ 所有MLog,将不打印 发布版本需要关闭 ]
     */
    private static boolean show = false;

    private MLog() {
    }


    public static void d(String tag, String msg) {
        if (show)
            Log.d(tag, msg);
    }


    public static void i(String tag, String msg) {
        if (show)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg) {
        if (show)
            Log.e(tag, msg);
    }

    public static void w(String tag, String msg) {
        if (show)
            Log.w(tag, msg);
    }

    public static void e(String tAG, String localizedMessage, Exception e) {
        if (show)
            Log.e(tAG, localizedMessage, e);
    }

    public static void v(String tag, String msg) {
        if (show)
            Log.v(tag, msg);
    }
}
