package org.oxff.hellomocker.handler;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;
import org.oxff.hellomocker.model.ResponseConfig;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JAR 扩展处理器
 * 负责加载用户提供的 JAR 文件并调用其中的处理器
 *
 * @author oxff
 * @version 1.0
 */
public class JarExtensionHandler {

    private static final Logger LOGGER = Logger.getLogger(JarExtensionHandler.class.getName());

    private final MontoyaApi api;
    private URLClassLoader classLoader;
    private IMockHandler handler;
    private boolean loaded = false;
    private String jarPath;
    private String handlerClassName;

    public JarExtensionHandler(MontoyaApi api) {
        this.api = api;
    }

    /**
     * 加载 JAR 文件
     *
     * @param jarPath           JAR 文件路径
     * @param handlerClassName  处理器类全限定名
     * @return 是否加载成功
     */
    public boolean loadJar(String jarPath, String handlerClassName) {
        // 先卸载之前的
        unload();

        this.jarPath = jarPath;
        this.handlerClassName = handlerClassName;

        try {
            File jarFile = new File(jarPath);
            if (!jarFile.exists()) {
                LOGGER.severe("JAR file not found: " + jarPath);
                return false;
            }

            // 创建 URLClassLoader
            URL[] urls = {jarFile.toURI().toURL()};
            classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());

            // 加载类
            Class<?> handlerClass = classLoader.loadClass(handlerClassName);

            // 检查是否实现了 IMockHandler 接口
            if (!IMockHandler.class.isAssignableFrom(handlerClass)) {
                LOGGER.severe("Class " + handlerClassName + " does not implement IMockHandler");
                return false;
            }

            // 创建实例
            handler = (IMockHandler) handlerClass.getDeclaredConstructor().newInstance();

            // 初始化处理器
            handler.init();

            loaded = true;
            LOGGER.info("Successfully loaded JAR handler: " + handler.getName());
            return true;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load JAR: " + jarPath, e);
            unload();
            return false;
        }
    }

    /**
     * 卸载 JAR
     */
    public void unload() {
        if (handler != null) {
            try {
                handler.destroy();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error destroying handler", e);
            }
            handler = null;
        }

        if (classLoader != null) {
            try {
                classLoader.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing class loader", e);
            }
            classLoader = null;
        }

        loaded = false;
        jarPath = null;
        handlerClassName = null;
    }

    /**
     * 处理请求
     *
     * @param request HTTP 请求
     * @return HTTP 响应
     */
    public HttpResponse handleRequest(HttpRequest request) {
        if (!loaded || handler == null) {
            return createErrorResponse("JAR handler not loaded");
        }

        try {
            return handler.handleRequest(request);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling request", e);
            return createErrorResponse("Handler error: " + e.getMessage());
        }
    }

    /**
     * 创建错误响应
     */
    private HttpResponse createErrorResponse(String errorMessage) {
        String responseBody = "{\"error\": \"" + errorMessage + "\"}";
        String response = "HTTP/1.1 500 Internal Server Error\r\n" +
                "Content-Type: application/json; charset=UTF-8\r\n" +
                "Content-Length: " + responseBody.getBytes().length + "\r\n\r\n" +
                responseBody;
        return HttpResponse.httpResponse(response);
    }

    /**
     * 是否已加载
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * 获取处理器名称
     */
    public String getHandlerName() {
        if (handler != null) {
            return handler.getName();
        }
        return "Unknown";
    }

    /**
     * 获取处理器描述
     */
    public String getHandlerDescription() {
        if (handler != null) {
            return handler.getDescription();
        }
        return "Not loaded";
    }

    /**
     * 获取 JAR 路径
     */
    public String getJarPath() {
        return jarPath;
    }

    /**
     * 获取处理器类名
     */
    public String getHandlerClassName() {
        return handlerClassName;
    }
}