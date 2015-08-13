/**
 * 
 */

package com.cmcc.hyapps.andyou.helper;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;

/**
 * @author Kuloud
 */
public class UIStartUpHelper {
    private static final String TAG = "UIStartUpHelper";

    private static boolean sSplashScreenTasksExecuted = false;

    private static UIStartUpHelper mInstance = null;

    public static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private UIStartUpHelper() {
    }

    public static UIStartUpHelper getInstance() {
        if (mInstance == null) {
            mInstance = new UIStartUpHelper();
        }
        return mInstance;
    }

    /**
     * 执行主线程和子线程任务
     */
    public static void executeOnSplashScreen() {
        if (sSplashScreenTasksExecuted) {
            return;
        }
        sSplashScreenTasksExecuted = true;

        mInstance = UIStartUpHelper.getInstance();
        mInstance.executeSplashScreenMainThreadTasks();
        mInstance.executeSplashScreenAsyncTasks();
    }

    /**
     * Handle main thread tasks
     * 处理主线任务
     */
    private void executeSplashScreenMainThreadTasks() {
        // TODO
    }

    /**
     * Handle async tasks
     * 处理异步任务
     */
    private void executeSplashScreenAsyncTasks() {

        new Thread() {
            @Override
            public void run() {
                // TODO
            }
        }.start();
    }

    /**
     * 空闲线程IdleHandler：
     * 向消息队列中添加新的MessageQueue.IdleHandler。
     * 当调用IdleHandler.queueIdle()返回false时，自动的从消息队列中移除。
     * 或者调用removeIdleHandler(MessageQueue.IdleHandler)
     * @param r
     */
    public final static void executeWhenIdle(final Runnable r) {
        MessageQueue mq = Looper.myQueue();
        mq.addIdleHandler(new IdleHandler() {

            @Override
            public boolean queueIdle() {
                r.run();
                return false;
            }
        });
    }
}
