package org.oxff.hellomocker.api;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

/**
 * HTTP Mock 处理器接口
 * 用户开发的 JAR 包需要实现此接口来处理请求
 *
 * @author oxff
 * @version 1.0
 */
public interface IMockHandler {

    /**
     * 处理 HTTP 请求并返回响应
     *
     * @param request HTTP 请求对象
     * @return HTTP 响应对象
     */
    HttpResponse handleRequest(HttpRequest request);

    /**
     * 获取处理器名称
     *
     * @return 处理器名称
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * 获取处理器描述
     *
     * @return 处理器描述
     */
    default String getDescription() {
        return "Custom JAR handler";
    }

    /**
     * 初始化处理器
     * 在加载 JAR 并创建实例后调用
     */
    default void init() {
        // 默认空实现，子类可重写
    }

    /**
     * 销毁处理器
     * 在卸载 JAR 或插件关闭时调用
     */
    default void destroy() {
        // 默认空实现，子类可重写
    }
}
