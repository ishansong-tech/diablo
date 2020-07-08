package com.ishansong.diablo.core.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.ishansong.diablo.core.constant.Constants;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLDecoder;
import java.util.*;

public class GsonUtils {

    private static final GsonUtils INSTANCE = new GsonUtils();

    private static final TypeAdapter<String> STRING = new TypeAdapter<String>() {

        @Override
        public void write(final JsonWriter out, final String value) {
            try {
                if (StringUtils.isBlank(value)) {
                    out.nullValue();
                    return;
                }
                out.value(value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String read(final JsonReader reader) {
            try {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                    // 原先是返回null，这里改为返回空字符串
                    return "";
                }
                return reader.nextString();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }

    };

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(String.class, STRING)
            .create();


    private static final String DOT = ".";

    private static final String E = "e";

    public static GsonUtils getInstance() {
        return INSTANCE;
    }

    public String toJson(final Object object) {
        return GSON.toJson(object);
    }

    public <T> T fromJson(final String json, final Class<T> tClass) {
        return GSON.fromJson(json, tClass);
    }

    @SuppressWarnings("unchecked")
    public <T> T fromJson(final String json, final Type typeOfT) {
        return (T) GSON.fromJson(json, typeOfT);
    }

    public <T> List<T> fromList(final String json, final Class<T> clazz) {
        return GSON.fromJson(json, TypeToken.getParameterized(List.class, clazz).getType());
    }

    public String toGetParam(final String json) {
        if (StringUtils.isBlank(json)) {
            return "";
        }
        final Map<String, String> map = toStringMap(json);
        StringBuilder stringBuilder = new StringBuilder();
        map.forEach((k, v) -> {
            try {
                stringBuilder.append(k)
                             .append("=")
                             .append(URLDecoder.decode(v, Constants.DECODE))
                             .append("&");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
        final String r = stringBuilder.toString();
        return r.substring(0, r.lastIndexOf("&"));

    }

    private Map<String, String> toStringMap(final String json) {
        return GSON.fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }

    public List<Map> toListMap(final String json) {
        return GSON.fromJson(json, new TypeToken<List<Map>>() {
        }.getType());
    }


    private static final Gson GSON_MAP = new GsonBuilder().registerTypeHierarchyAdapter(new TypeToken<Map<String, Object>>() {
    }.getRawType(), new MapDeserializer<String, Object>()).create();

    private static final Gson GSON_LIST = new GsonBuilder().registerTypeHierarchyAdapter(new TypeToken<List<Object>>() {
    }.getRawType(), (JsonDeserializer<List<Object>>) (JsonElement json, Type typeOfT, JsonDeserializationContext context) -> {
        if (json.isJsonArray()) {
            JsonArray jsonArray = json.getAsJsonArray();

            // Type itemType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
            List<Object> list = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {

                JsonElement jsonElement = jsonArray.get(i);
                Object item = context.deserialize(jsonElement, Object.class);
                list.add(item);
            }

            return list;
        }

        return Collections.emptyList();
    }).create();

    private final Type listType = new TypeToken<List<Object>>() {
    }.getType();

    private final Type mapType = new TypeToken<Map<String, Object>>() {
    }.getType();


    public List<Object> fromList(final String json) {
        return GSON_LIST.fromJson(json, listType);
    }

    public String toListJson(List<Object> list) {
        return GSON_LIST.toJson(list);
    }

    public Map<String, Object> toObjectMap(final String json) {
        return GSON_MAP.fromJson(json, mapType);
    }

    public String toMapJson(Map map) {
        return GSON_MAP.toJson(map);
    }

    private static class MapDeserializer<T, U> implements JsonDeserializer<Map<T, U>> {

        @Override
        public Map<T, U> deserialize(final JsonElement json, final Type type, final JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                return null;
            }

            JsonObject jsonObject = json.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> jsonEntrySet = jsonObject.entrySet();
            Map<T, U> resultMap = new LinkedHashMap<>();

            for (Map.Entry<String, JsonElement> entry : jsonEntrySet) {
                if (entry.getValue().getClass().isAssignableFrom(JsonNull.class)) {
                    continue;
                }

                JsonElement element = entry.getValue();

                if (element.isJsonArray()) {
                    JsonArray jsonArray = element.getAsJsonArray();

                    List<Object> list = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {

                        JsonElement jsonElement = jsonArray.get(i);

                        Object item = context.deserialize(jsonElement, Object.class);
                        list.add(item);
                    }

                    resultMap.put((T) entry.getKey(), (U) list);
                } else {
                    U value = context.deserialize(entry.getValue(), this.getType(element));
                    resultMap.put((T) entry.getKey(), value);
                }

            }

            return resultMap;
        }

        public Class getType(final JsonElement element) {
            if (element.isJsonPrimitive()) {
                final JsonPrimitive primitive = element.getAsJsonPrimitive();
                if (primitive.isString()) {
                    return String.class;
                } else if (primitive.isNumber()) {
                    String numStr = primitive.getAsString();
                    if (numStr.contains(DOT) || numStr.contains(E)
                            || numStr.contains("E")) {
                        return Double.class;
                    }
                    return Long.class;
                } else if (primitive.isBoolean()) {
                    return Boolean.class;
                }
            }
            return element.getClass();
        }
    }

}
