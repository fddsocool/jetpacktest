package com.frx.libnetwork.cache;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 注意：
 * 1.冒号后面必须紧跟参数名，中间不能有空格。大于小于号和冒号中间是有空格的。
 * 2.参数必须用 `` 符号包裹而不是 '' 符号
 * <p>
 * select * from cache
 * where `表中列名`=:参数名                         -----> 等于参数名
 * where `表中列名`<:参数名                         -----> 小于参数名
 * where `表中列名` between :参数1 and :参数2       -----> 查询 [参数名1,参数名2]
 * where `表中列名` like :参数名                    ----->模糊查询
 * where `表中列名` in (:【参数名集合】)---->查询符合集合内指定字段值的记录
 */

@Dao
public interface CacheDao {

    //查询数据
    //cache指数据库名称，`key`指列名称，:key指传入参数
    @Query("select * from cache_table where `key`=:key")
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
