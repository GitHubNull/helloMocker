package org.oxff.hellomocker.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON工具类
 * 提供JSON序列化和反序列化功能
 *
 * @author oxff
 * @version 1.0
 */
public class JsonUtils {

    private static final ObjectMapper OBJECT_MAPPER;

    static {
        OBJECT_MAPPER = new ObjectMapper()
                // 注册Java 8日期时间模块
                .registerModule(new JavaTimeModule())
                // 禁用日期时间戳格式，使用ISO格式
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 美化输出
                .enable(SerializationFeature.INDENT_OUTPUT)
                // 忽略未知属性
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                // 允许空字符串转null
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    private JsonUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 获取ObjectMapper实例
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    /**
     * 将对象序列化为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     * @throws JsonProcessingException 序列化异常
     */
    public static String toJson(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    /**
     * 将对象序列化为JSON字符串（忽略异常）
     *
     * @param obj 对象
     * @return JSON字符串，失败返回null
     */
    public static String toJsonSafe(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 将对象序列化为JSON字节数组
     *
     * @param obj 对象
     * @return JSON字节数组
     * @throws JsonProcessingException 序列化异常
     */
    public static byte[] toJsonBytes(Object obj) throws JsonProcessingException {
        return OBJECT_MAPPER.writeValueAsBytes(obj);
    }

    /**
     * 将对象写入文件
     *
     * @param obj 对象
     * @param path 文件路径
     * @throws IOException IO异常
     */
    public static void writeToFile(Object obj, Path path) throws IOException {
        try (OutputStream os = Files.newOutputStream(path)) {
            OBJECT_MAPPER.writeValue(os, obj);
        }
    }

    /**
     * 从JSON字符串反序列化对象
     *
     * @param json JSON字符串
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 对象实例
     * @throws JsonProcessingException 反序列化异常
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    /**
     * 从JSON字符串反序列化对象（忽略异常）
     *
     * @param json JSON字符串
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 对象实例，失败返回null
     */
    public static <T> T fromJsonSafe(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 从输入流反序列化对象
     *
     * @param is 输入流
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 对象实例
     * @throws IOException IO异常
     */
    public static <T> T fromJson(InputStream is, Class<T> clazz) throws IOException {
        return OBJECT_MAPPER.readValue(is, clazz);
    }

    /**
     * 从文件反序列化对象
     *
     * @param path 文件路径
     * @param clazz 目标类
     * @param <T> 类型参数
     * @return 对象实例
     * @throws IOException IO异常
     */
    public static <T> T fromFile(Path path, Class<T> clazz) throws IOException {
        try (InputStream is = Files.newInputStream(path)) {
            return OBJECT_MAPPER.readValue(is, clazz);
        }
    }

    /**
     * 验证JSON字符串是否有效
     *
     * @param json JSON字符串
     * @return 是否有效
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 格式化JSON字符串
     *
     * @param json JSON字符串
     * @return 格式化后的JSON字符串
     */
    public static String formatJson(String json) {
        try {
            Object obj = OBJECT_MAPPER.readValue(json, Object.class);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return json;
        }
    }
}
