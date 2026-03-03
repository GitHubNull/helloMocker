package org.oxff.hellomocker.handler;

import org.oxff.hellomocker.exception.MockProcessingException;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.model.ResponseConfig;

/**
 * 响应处理器接口
 * 所有响应生成器必须实现此接口
 *
 * @author oxff
 * @version 1.0
 */
public interface ResponseHandler {

    /**
     * 检查是否支持该响应配置
     *
     * @param config 响应配置
     * @return 是否支持
     */
    boolean supports(ResponseConfig config);

    /**
     * 生成响应
     *
     * @param context 请求上下文
     * @param config 响应配置
     * @return Mock响应对象
     * @throws MockProcessingException 处理异常
     */
    MockResponse handle(MockContext context, ResponseConfig config) throws MockProcessingException;

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
        return "Response Handler";
    }

    /**
     * 获取处理器优先级（数字越小优先级越高）
     *
     * @return 优先级
     */
    default int getPriority() {
        return 100;
    }
}
