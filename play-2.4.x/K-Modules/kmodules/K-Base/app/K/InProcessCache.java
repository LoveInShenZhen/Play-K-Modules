package K;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Created by kk on 14-7-12.
 */
public class InProcessCache {
    private static LoadingCache<Integer, Cache<String, Object>> expireCaches;
    private static CacheLoader<Integer, Cache<String, Object>> expireCacheLoader;
    private static HashMap<String, Integer> keysMap;

    static {
        expireCacheLoader = new CacheLoader<Integer, Cache<String, Object>>() {
            @Override
            public Cache<String, Object> load(Integer key) throws Exception {
                Cache<String, Object> cache = CacheBuilder.newBuilder()
                        .expireAfterWrite(key.longValue(), TimeUnit.SECONDS)
                        .build();
                return cache;
            }
        };

        expireCaches = CacheBuilder.newBuilder()
                .build(expireCacheLoader);

        keysMap = new HashMap<>();
    }

    public static void Put(String Key, Object obj, int TimeInSec) {
        keysMap.put(Key, TimeInSec);
        Cache<String, Object> cache = null;
        try {
            cache = expireCaches.get(TimeInSec);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        cache.put(Key, obj);
    }

    public static Object Get(String key) {
        Integer byTimeInSec = keysMap.get(key);
        if (byTimeInSec == null) {
            return null;
        }
        Cache<String, Object> cache;
        try {
            cache = expireCaches.get(byTimeInSec);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return null;
        }
        return cache.getIfPresent(key);
    }
}
