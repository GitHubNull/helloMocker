package org.oxff.hellomocker.api;

import java.util.Map;

/**
 * Mock处理器接口
 * 供用户扩展JAR包时实现
 *
 * @author oxff
 * @version 1.0
 */
public interface IMockHandler {

    /**
     * 处理请求并返回响应
     *
     * @param request 包含请求信息的对象
     * @return 响应字节数组
     */
    byte[] handle(MockRequest request);

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
        return "Mock Handler";
    }
}
