package cn.evole.plugins.tp;

import java.util.HashMap;

/**
 * Name: civs-plugin / Session
 * Author: cnlimiter
 * CreateTime: 2023/10/20 14:53
 * Description:
 */

public class Session {
    private static final HashMap<String, Long> cacheMap = new HashMap<>();
    private static final HashMap<String, Long> cacheMap2 = new HashMap<>();

    public static void Set(String value, long outtime) {
        cacheMap.put(value, System.currentTimeMillis() + outtime);
    }

    public static int Get(String value) {
        try {
            long outtime = cacheMap.get(value);
            long nowtime = System.currentTimeMillis();
            if (nowtime >= outtime) {
                cacheMap.remove(value);
                return 1;
            } else {
                return 2;
            }
        } catch (Exception var5) {
            return 0;
        }
    }

    public static void del(String key) {
        cacheMap.remove(key);
    }

    public static void put(String key, long value) {
        cacheMap2.put(key, value);
    }

    public static Object out(String key) {
        return cacheMap2.get(key);
    }

}
