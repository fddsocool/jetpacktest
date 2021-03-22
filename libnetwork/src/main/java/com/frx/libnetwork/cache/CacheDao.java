package com.frx.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CacheDao {

    //查询数据
    //cache指数据库名称，'key'指列名称，:key指传入参数
    @Query("select * from cache_table where 'key'=:key")
    Cache getCache(String key);

    @Delete
    void delete(Cache cache);

    @Delete
    void delete(Cache... caches);

    @Delete
    void delete(List<Cache> caches);

    //插入数据
    //onConflict方案
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long saveCache(Cache cache);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> saveCache(Cache... caches);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> saveCache(List<Cache> caches);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(Cache cache);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Cache... caches);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(List<Cache> caches);
}
