package com.dyleaf.Utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class GsonUtils {
    //    private static Gson gson = new Gson();
    private static Gson gson = new GsonBuilder().
            registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                @Override
                public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src == src.intValue())
                        return new JsonPrimitive(src.intValue());
                    return new JsonPrimitive(src);
                }
            }).create();

    public static Map<Integer, Object> GsonToMap(String data) {
        Map<Integer, Object> map = gson.fromJson(data, new TypeToken<Map<Integer, Object>>() {
        }.getType());
        return map;
    }

    public static Integer Double2Integer(Double number){
        return new Integer(number.intValue());
    }
}
