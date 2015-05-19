package com.nice.common.util;

public class JMSyncHelper {

    public static void syncObj(Object obj, long timeout) {
        synchronized (obj) {
            try {
                obj.wait(timeout);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }
        }
    }

    public static void notice(Object obj) {
        synchronized (obj) {
            obj.notifyAll();
        }
    }

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.interrupted();
        }
    }

}
