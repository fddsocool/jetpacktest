package com.frx.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

// Response数据结构体:
// json
//   {
//       "status":200,
//       "message":"成功",
//       "data":{
//           "data":{}
//          // 或者是 "data":[],
//       }
//   }

/**
 * 转换器
 */
public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object dataObj = data.get("data");
            if (dataObj != null) {
                return JSON.parseObject(dataObj.toString(), type);
            }
        }
        return null;
    }

    @Override
    public Object convert(String response, Class claz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            Object dataObj = data.get("data");
            if (dataObj != null) {
                return JSON.parseObject(dataObj.toString(), claz);
            }
        }
        return null;
    }
}
