package com.hithway.loginsdkhelper.helper;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * @author Lai
 * @time 2019/11/30 11:32
 * @describe describe
 */
public class OkHttpUtil {
    private static OkHttpClient singleton;
    private OkHttpUtil(){

    }
    public static OkHttpClient getInstance() {
        if (singleton == null)
        {
            synchronized (OkHttpUtil.class)
            {
                if (singleton == null)
                {
                    singleton = new OkHttpClient();
                }
            }
        }
        return singleton;
    }

    /** 取消所有请求请求 */
    public static void cancelAll() {
        for (Call call : getInstance().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getInstance().dispatcher().runningCalls()) {
            call.cancel();
        }
    }
}
