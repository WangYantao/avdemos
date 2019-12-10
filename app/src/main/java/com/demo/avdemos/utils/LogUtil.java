package com.demo.avdemos.utils;

import android.util.Log;

/**
 * Created by wangyt on 2019/12/9
 */
public class LogUtil {
    private static final String TAG = "AVDemos";

    private static final String LOG_PREFIX = "______________";

    public static void e(String error){
        Log.e(TAG, "____________" + error);
    }
}
