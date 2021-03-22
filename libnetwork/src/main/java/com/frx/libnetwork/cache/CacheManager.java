package com.frx.libnetwork.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class CacheManager {

    public static <T> void saveCache(String key, T body) {
        Cache cache = new Cache();
        cache.key = key;
        cache.data = toByteArray(body);

        //写入
        CacheDatabase.get().getDao().saveCache(cache);
    }

    public static Object getCache(String Key) {
        Cache cache = CacheDatabase.get().getDao().getCache(Key);
        return toObject(cache);
    }

    private static Object toObject(Cache cache) {
        if (cache != null && cache.data != null) {
            ByteArrayInputStream bais = null;
            ObjectInputStream ois = null;

            try {
                bais = new ByteArrayInputStream(cache.data);
                ois = new ObjectInputStream(bais);
                return ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (bais != null) {
                        bais.close();
                    }

                    if (ois != null) {
                        ois.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static <T> byte[] toByteArray(T body) {

        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(body);
            oos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }

                if (oos != null) {
                    oos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[0];
    }
}
