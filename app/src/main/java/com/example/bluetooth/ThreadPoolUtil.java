package com.example.bluetooth;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by WangJie on 2017/10/13.
 */
public class ThreadPoolUtil {
    private static final ExecutorService executorService =  Executors.newFixedThreadPool(30);
    public static ExecutorService getExecutorService(){
        return  executorService;
    }
}
