package com.frx.libnetwork.cache;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "cache_table")
public class Cache implements Serializable {

    //PrimaryKey 必须要有,且不为空,autoGenerate:主键的值是否由Room自动生成,默认false
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String key;

    //由于每个接口返回字段不一样，把数据转化为二进制方便传输
    @ColumnInfo(name = "data")//指定该字段在表中的列的名字
    public byte[] data;

    //@Embedded 对象嵌套,ForeignTable对象中所有字段 也都会被映射到cache表中,
    //同时也支持ForeignTable内部还有嵌套对象
    //public ForeignTable foreignTable;
}

//public class ForeignTable implements Serializable {
//    @PrimaryKey
//    @NonNull
//    public String foreign_key;
//
//    //@ColumnInfo(name = "_data")
//    public byte[] foreign_data;
//}