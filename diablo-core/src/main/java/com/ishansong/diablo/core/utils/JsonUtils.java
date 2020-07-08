package com.ishansong.diablo.core.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
public final class JsonUtils {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        javaTimeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern("HH:mm:ss")));
        FilterProvider filterProvider = new SimpleFilterProvider()
                .addFilter("classFilter", SimpleBeanPropertyFilter.serializeAllExcept("class"));
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
              .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
              .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
              .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
              .configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true)
              .setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
              .registerModule(javaTimeModule)
              .setFilterProvider(filterProvider);


    }

    public static String toJson(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("toJson write to json string obj:{}, cause:{}", object, Throwables.getStackTraceAsString(e));

            return null;
        }
    }

    public static String dubboResultJson(Object object) {
        try {
            if (object instanceof Map) {
                Map map = (Map) object;
                map.remove("class");
                return mapper.writeValueAsString(map);
            }
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            log.error("dubboResultJson write to json string obj:{}, cause:{}", object, Throwables.getStackTraceAsString(e));

            return null;
        }
    }


}
